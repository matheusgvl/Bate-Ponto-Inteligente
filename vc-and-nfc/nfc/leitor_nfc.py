import serial
import requests
import time
import os

# --- CONFIGURAÇÃO ---
# IMPORTANTE: Mude para a porta que apareceu no seu Gerenciador de Dispositivos
PORTA_SERIAL = "COM3"
BAUD_RATE = 9600

# ID do Local que este leitor físico representa (ex: 1 = Portaria)
LOCAL_ID = int(os.getenv("PONTO_LOCAL_ID", 2))

SPRING_API_URL = "http://localhost:8080/api/acesso/registrar/cartao"
# --- FIM CONFIGURAÇÃO ---

def iniciar():
    print(f"[NFC] Iniciando ponte na porta {PORTA_SERIAL} para Local ID {LOCAL_ID}...")

    try:
        arduino = serial.Serial(PORTA_SERIAL, BAUD_RATE, timeout=1)
        time.sleep(2) # Espera o Arduino reiniciar após conexão
        print(f"[NFC] Conectado! Aguardando cartões...")

        while True:
            if arduino.in_waiting > 0:
                # Lê a linha enviada pelo Arduino (remove espaços e quebras de linha)
                uid = arduino.readline().decode('utf-8').strip()

                if uid:
                    print(f"\n[NFC] Cartão detectado: {uid}")
                    enviar_para_spring(uid)

    except serial.SerialException:
        print(f"[ERRO] Não foi possível conectar na {PORTA_SERIAL}. Verifique se o Arduino está plugado ou se a porta está correta.")
    except KeyboardInterrupt:
        print("\n[NFC] Encerrando...")
    finally:
        if 'arduino' in locals() and arduino.is_open:
            arduino.close()

def enviar_para_spring(uid):
    try:
        payload = {"uid": uid, "localId": LOCAL_ID}
        print(f"[API] Enviando para Spring: {payload}")

        response = requests.post(SPRING_API_URL, json=payload)

        if response.status_code == 200:
            dados = response.json()
            print(f"[API] Resposta SUCESSO: {dados.get('status')} - {dados.get('motivo')}")
        else:
            # Se for 403 Forbidden or 404 Not Found, ainda é uma resposta válida da nossa API
            try:
                dados = response.json()
                print(f"[API] Resposta NEGADO: {dados.get('status')} - {dados.get('motivo')}")
            except:
                print(f"[API] Erro desconhecido: {response.status_code}")

    except requests.exceptions.ConnectionError:
        print("[ERRO] Não foi possível conectar ao Spring Boot. Ele está rodando?")
    except Exception as e:
        print(f"[ERRO] {e}")

if __name__ == "__main__":
    iniciar()