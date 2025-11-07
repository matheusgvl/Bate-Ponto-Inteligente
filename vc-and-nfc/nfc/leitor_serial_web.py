#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import serial
import time
import sys
import json

# --- CONFIGURAÇÃO CORRIGIDA PARA O SEU ARDUINO ---
PORTA = "COM3"       # Corrigido de COM5 para COM3
BAUDRATE = 9600      # Corrigido de 115200 para 9600 (padrão do seu hardware)
TIMEOUT = 1
# -------------------------------------------------

def main():
    try:
        # Tenta abrir a porta serial
        ser = serial.Serial(PORTA, BAUDRATE, timeout=TIMEOUT)
        # Dá um tempo para o Arduino reiniciar após a conexão
        time.sleep(2)

        # Limpa qualquer lixo que possa estar no buffer
        ser.reset_input_buffer()

        # Avisa (opcional, o Java vai ignorar se não for JSON, mas ajuda no debug manual)
        # print(f"DEBUG: Conectado na {PORTA} aguardando cartão...", file=sys.stderr)

    except Exception as e:
        # Se falhar ao abrir, retorna erro JSON para o Java
        print(json.dumps({"success": False, "error": f"Falha ao abrir {PORTA}. Verifique se está livre."}), flush=True)
        sys.exit(1)

    try:
        inicio = time.time()
        tempo_maximo = 30 # 30 segundos esperando

        while (time.time() - inicio) < tempo_maximo:
            if ser.in_waiting > 0:
                try:
                    line = ser.readline().decode('utf-8', errors='ignore').strip()

                    # Se a linha for vazia, ignora
                    if not line:
                        continue

                    # Tenta limpar a linha para achar só o UID (caso venha com texto extra)
                    # Remove caracteres comuns que não são HEX
                    uid_limpo = ''.join(filter(lambda x: x in '0123456789ABCDEFabcdef', line)).upper()

                    # Validação básica: UID geralmente tem 8 caracteres (ex: 63DFB001)
                    if len(uid_limpo) >= 8:
                        # SUCESSO! Imprime o JSON e sai.
                        print(json.dumps({"success": True, "uid": uid_limpo}), flush=True)
                        time.sleep(1)
                        return

                except Exception:
                    continue

            # Pequena pausa para não fritar a CPU
            time.sleep(0.1)

        # Se saiu do loop, foi timeout
        print(json.dumps({"success": False, "error": "Tempo esgotado. Nenhum cartão lido."}), flush=True)

    except Exception as e:
        print(json.dumps({"success": False, "error": str(e)}), flush=True)
    finally:
        if 'ser' in locals() and ser.is_open:
            ser.close()

if __name__ == "__main__":
    main()