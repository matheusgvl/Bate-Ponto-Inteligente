"""Ponte HTTP para o Spring no Docker acessar o leitor serial do Windows."""

import json
import os
from pathlib import Path
import subprocess
import sys
import threading

from flask import Flask, jsonify

app = Flask(__name__)
process_lock = threading.Lock()
current_process = None
READER_SCRIPT = Path(__file__).with_name("leitor_serial_web.py")


def stop_process(process):
    if process is not None and process.poll() is None:
        process.terminate()
        try:
            process.wait(timeout=2)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait(timeout=2)


@app.get("/health")
def health():
    return jsonify(status="online")


@app.post("/card/read")
def read_card():
    global current_process
    with process_lock:
        if current_process is not None:
            return jsonify(success=False, error="Ja existe uma leitura NFC em andamento.")
        try:
            process = subprocess.Popen(
                [sys.executable, "-u", str(READER_SCRIPT)],
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True,
                encoding="utf-8",
                errors="replace",
            )
        except OSError:
            app.logger.exception("Falha ao iniciar leitor NFC")
            return jsonify(success=False, error="Nao foi possivel iniciar o leitor NFC.")
        current_process = process

    try:
        stdout, _ = process.communicate(timeout=35)
        for line in stdout.splitlines():
            try:
                result = json.loads(line)
            except json.JSONDecodeError:
                continue
            if isinstance(result, dict) and isinstance(result.get("success"), bool):
                return jsonify(result)
        return jsonify(success=False, error="Leitura interrompida ou leitor desconectado.")
    except subprocess.TimeoutExpired:
        stop_process(process)
        return jsonify(success=False, error="Tempo esgotado. Nenhum cartao lido.")
    finally:
        stop_process(process)
        with process_lock:
            if current_process is process:
                current_process = None


@app.post("/card/stop")
def stop_card():
    with process_lock:
        stop_process(current_process)
    return jsonify(success=True, message="Leitura interrompida.")


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.getenv("NFC_API_PORT", "5001")),
            debug=False, use_reloader=False, threaded=True)
