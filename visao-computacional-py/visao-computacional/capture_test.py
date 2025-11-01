import cv2
import time
import os
import sys
import subprocess

CAMERA_SOURCE = 0

OUTPUT_FILE = 'captured_test_frame.jpg'

def capture_frame(source, output_filename):
    """Captura um único frame da fonte de vídeo (IP ou índice) e salva como arquivo."""

    print(f"Tentando conectar à câmera em: {source}")

    try:
        if isinstance(source, str):
            cap = cv2.VideoCapture(source)
        else:
            cap = cv2.VideoCapture(int(source))
    except ValueError:
        print("Erro: A fonte da câmera deve ser um URL ou um índice numérico.")
        return None

    if not cap.isOpened():
        print(f"ERRO: Não foi possível abrir a fonte de vídeo. Verifique se o DroidCam está ativo e o IP/URL está correto.")
        return None

    print("Capturando frames para estabilizar. Aguarde 1 segundo...")
    time.sleep(1)

    ret, frame = cap.read()

    cap.release()

    if ret:
        cv2.imwrite(output_filename, frame)
        print(f"SUCESSO: Imagem salva em: {os.path.abspath(output_filename)}")
        return os.path.abspath(output_filename)
    else:
        print("ERRO: Não foi possível ler o frame da câmera. O DroidCam pode ter desconectado.")
        return None

if __name__ == "__main__":


    image_path = capture_frame(CAMERA_SOURCE, OUTPUT_FILE)

    if image_path:
        print("\n--- TESTANDO DETECÇÃO YOLO (Chamando deteccao_facial.py) ---")

        command = [sys.executable, 'deteccao_facial.py', image_path]

        try:
            process = subprocess.run(
                command,
                capture_output=True,
                text=True,
                check=True
            )

            print("Saída do YOLO (JSON recebido pelo subprocesso):")
            print(process.stdout)

        except subprocess.CalledProcessError as e:
            print("\nERRO ao executar o deteccao_facial.py (O código Python retornou erro):")
            print(f"Código de Saída: {e.returncode}")
            print(f"Mensagem de Erro (stderr): {e.stderr}")
            print(f"Saída Padrão (stdout): {e.stdout}")
        except FileNotFoundError:
            print("\nERRO: O executável 'python' não foi encontrado. Verifique se o VENV está ativado.")