#!/usr/bin/env python3
import serial
import time
import re
import sys
import json

PORTA = "COM5"  
BAUDRATE = 115200                
TIMEOUT = 1                      

uid_pattern = re.compile(r"UID \(Identificador Unico\):\s*([0-9A-Fa-f]+)")

def main():
    try:
        ser = serial.Serial(PORTA, BAUDRATE, timeout=TIMEOUT)
        print(f"Conectado em {PORTA} @ {BAUDRATE}bps. Aguardando cartão NFC...", flush=True)
        time.sleep(2)  
    except Exception as e:
        error_msg = {"error": f"Erro ao abrir porta {PORTA}: {str(e)}"}
        print(json.dumps(error_msg), flush=True)
        sys.exit(1)

    try:
        start_time = time.time()
        timeout_seconds = 30
        
        while True:
            if time.time() - start_time > timeout_seconds:
                timeout_msg = {"error": "Timeout - nenhum cartão detectado em 30 segundos"}
                print(json.dumps(timeout_msg), flush=True)
                break
            
            line = ser.readline().decode(errors='ignore').strip()
            if not line:
                continue

            print(f"DEBUG: {line}", flush=True)

            m = uid_pattern.search(line)
            if m:
                uid_hex = m.group(1).upper()
                
                if uid_hex.startswith('63DF') or uid_hex.startswith('A979'):
                    success_msg = {
                        "success": True,
                        "uid": uid_hex,
                        "message": "Cartão NFC detectado com sucesso"
                    }
                    print(json.dumps(success_msg), flush=True)
                    break
                else:
                    invalid_msg = {
                        "warning": f"Cartão detectado mas UID inválido: {uid_hex}",
                        "uid": uid_hex
                    }
                    print(json.dumps(invalid_msg), flush=True)

    except KeyboardInterrupt:
        interrupt_msg = {"info": "Leitura interrompida pelo usuário"}
        print(json.dumps(interrupt_msg), flush=True)
    except Exception as e:
        error_msg = {"error": f"Erro durante leitura: {str(e)}"}
        print(json.dumps(error_msg), flush=True)
    finally:
        ser.close()

if __name__ == "__main__":
    main()
