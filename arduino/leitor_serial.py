#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import serial
import time
import re

PORTA = "COM5"  
BAUDRATE = 115200                
TIMEOUT = 1                      


uid_pattern = re.compile(r"UID \(Identificador Unico\):\s*([0-9A-Fa-f]+)")

def main():
    try:
        ser = serial.Serial(PORTA, BAUDRATE, timeout=TIMEOUT)
        print(f"Conectado em {PORTA} @ {BAUDRATE}bps. Aguardando dados...\n")
        time.sleep(2)  
    except Exception as e:
        print(f"Erro ao abrir porta {PORTA}: {e}")
        return

    try:
        while True:
            line = ser.readline().decode(errors='ignore').strip()
            if not line:
                continue

            print(line)

            m = uid_pattern.search(line)
            if m:
                uid_hex = m.group(1).upper()
                bytes_list = [uid_hex[i:i+2] for i in range(0, len(uid_hex), 2)]
                print(f"-> UID detectado: {uid_hex}  (bytes: {' : '.join(bytes_list)})")
                print("-" * 40)

    except KeyboardInterrupt:
        print("\nEncerrando leitura serial...")
    finally:
        ser.close()

if __name__ == "__main__":
    main()
