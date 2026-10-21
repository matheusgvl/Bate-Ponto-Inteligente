#!/usr/bin/env python3
import serial
import time
import sys
import json

PORTA = "COM3"  # Altere para COM5 se for a porta correta do Arduino
BAUDRATE = 9600 # Deve ser 9600 para coincidir com a configuração do Arduino
TIMEOUT = 1

def main():
    try:
        ser = serial.Serial(PORTA, BAUDRATE, timeout=TIMEOUT)
        time.sleep(2)
        ser.reset_input_buffer()
    except Exception as e:
        print(json.dumps({"success": False, "error": f"Falha ao abrir {PORTA}. Verifique se está livre."}), flush=True)
        sys.exit(1)

    try:
        inicio = time.time()
        tempo_maximo = 30

        while (time.time() - inicio) < tempo_maximo:
            if ser.in_waiting > 0:
                try:
                    line = ser.readline().decode('utf-8', errors='ignore').strip()

                    if not line:
                        continue

                    # Extrai apenas os caracteres hexadecimais (limpa textos indesejados que o Arduino possa enviar)
                    uid_limpo = ''.join(filter(lambda x: x in '0123456789ABCDEFabcdef', line)).upper()

                    if len(uid_limpo) >= 8:
                        print(json.dumps({"success": True, "uid": uid_limpo}), flush=True)
                        time.sleep(1)
                        return
                except Exception:
                    continue

            time.sleep(0.1)

        print(json.dumps({"success": False, "error": "Tempo esgotado. Nenhum cartão lido."}), flush=True)

    except Exception as e:
        print(json.dumps({"success": False, "error": str(e)}), flush=True)
    finally:
        if 'ser' in locals() and ser.is_open:
            ser.close()

if __name__ == "__main__":
    main()