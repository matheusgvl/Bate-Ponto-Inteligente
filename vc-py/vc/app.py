import face_recognition
import numpy as np
import cv2
import requests
import threading
import time
import json
import os
from flask import Flask, request, jsonify

app = Flask(__name__)
LOCAL_ID = int(os.getenv("PONTO_LOCAL_ID", 1))
known_face_names = []
known_face_encodings = []

video_capture = cv2.VideoCapture(0)
running = True
registration_id = None
data_lock = threading.Lock()

SPRING_BOOT_API_URL = "http://localhost:8080/api"


def sync_faces_from_spring():

    global known_face_names, known_face_encodings

    print("[INFO] Sincronizando rostos com o backend Spring Boot...")
    try:
        response = requests.get(f"{SPRING_BOOT_API_URL}/person/faces")

        if response.status_code == 200:
            data = response.json()

            temp_names = []
            temp_encodings = []

            for item in data:
                temp_names.append(item['nome'])
                encoding_list = json.loads(item['encoding'])
                temp_encodings.append(np.array(encoding_list))

            with data_lock:
                known_face_names = temp_names
                known_face_encodings = temp_encodings

            print(f"[INFO] Sincronização concluída. {len(known_face_names)} rostos carregados.")
        else:
            print(f"[ERRO] Falha ao buscar rostos. Status: {response.status_code}")

    except requests.exceptions.ConnectionError:
        print("[ERRO] Não foi possível conectar ao backend Spring Boot. Verifique se ele está rodando.")


def video_processing_thread():

    global registration_id, running

    last_recognition_time = {}

    while running:
        ret, frame = video_capture.read()
        if not ret:
            print("[ERRO] Não foi possível capturar frame da câmera.")
            time.sleep(1)
            continue

        small_frame = cv2.resize(frame, (0, 0), fx=0.25, fy=0.25)
        rgb = cv2.cvtColor(small_frame, cv2.COLOR_BGR2RGB)

        faces = face_recognition.face_locations(rgb)
        encodings = face_recognition.face_encodings(rgb, faces)

        with data_lock:
            for (top, right, bottom, left), face_encoding in zip(faces, encodings):
                name = "Desconhecido"

                if len(known_face_encodings) > 0:
                    matches = face_recognition.compare_faces(known_face_encodings, face_encoding)
                    face_distances = face_recognition.face_distance(known_face_encodings, face_encoding)
                    best_match_index = np.argmin(face_distances)
                    if matches[best_match_index]:
                        name = known_face_names[best_match_index]


                if name != "Desconhecido":
                    now = time.time()
                    if name not in last_recognition_time or (now - last_recognition_time[name] > 60):
                        print(f"[RECONHECIMENTO] {name} detectado! Enviando para o Spring.")
                        try:
                            payload = {
                                "nome": name,
                                "localId": LOCAL_ID
                            }
                            requests.post(f"{SPRING_BOOT_API_URL}/acesso/registrar/rosto", json=payload)
                            last_recognition_time[name] = now
                        except requests.exceptions.ConnectionError:
                            print(f"[ERRO] Falha ao enviar reconhecimento de {name} para o Spring.")

                elif name == "Desconhecido" and registration_id is not None:
                    print(f"[CADASTRO] Rosto desconhecido capturado para ID: {registration_id}")
                    try:
                        encoding_str = json.dumps(face_encoding.tolist())

                        payload = {
                            "id": registration_id,
                            "encoding": encoding_str
                        }

                        response = requests.post(f"{SPRING_BOOT_API_URL}/person/register-face", json=payload)

                        if response.status_code == 200:
                            print(f"[CADASTRO] Rosto para ID {registration_id} enviado ao Spring com sucesso.")
                            sync_faces_from_spring()
                        else:
                            print(f"[ERRO] Spring recusou o cadastro. Status: {response.status_code}")

                    except requests.exceptions.ConnectionError:
                        print(f"[ERRO] Falha ao enviar novo cadastro para o Spring.")

                    registration_id = None

                top *= 4
                right *= 4
                bottom *= 4
                left *= 4
                cv2.rectangle(frame, (left, top), (right, bottom), (0, 255, 0), 2)
                cv2.rectangle(frame, (left, bottom - 35), (right, bottom), (0, 255, 0), cv2.FILLED)
                cv2.putText(frame, name, (left + 6, bottom - 6), cv2.FONT_HERSHEY_DUPLEX, 1.0, (255, 255, 255), 1)

        cv2.imshow("Bate-Ponto Inteligente (Python Service)", frame)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            running = False
            break

    video_capture.release()
    cv2.destroyAllWindows()
    print("[INFO] Thread da câmera finalizada.")


@app.route("/register", methods=["POST"])
def start_registration():

    global registration_id
    data = request.get_json()

    if "id" not in data or "nome" not in data:
        return jsonify({"erro": "Os campos 'id' e 'nome' são obrigatórios."}), 400

    id_to_register = data["id"]
    name_to_register = data["nome"]
    registration_id = id_to_register

    print(f"\n[API] MODO DE CADASTRO ATIVADO PARA: ID={id_to_register}, Nome={name_to_register}")
    print("[API] Aproxime o rosto da câmera...")

    return jsonify({"mensagem": f"Modo de cadastro ativado para {name_to_register}."}), 200

@app.route("/sync", methods=["POST"])
def force_sync():

    print("\n[API] Sincronização forçada solicitada pelo Spring.")
    threading.Thread(target=sync_faces_from_spring).start()
    return jsonify({"mensagem": "Sincronização iniciada."}), 202

@app.route("/health", methods=["GET"])
def health_check():
    return jsonify({"status": "online"}), 200


if __name__ == "__main__":
    print(f"[INIT] Iniciando serviço de visão computacional para o LOCAL_ID: {LOCAL_ID}")
    sync_faces_from_spring()

    print("[INIT] Iniciando thread da câmera...")
    cam_thread = threading.Thread(target=video_processing_thread)
    cam_thread.daemon = True
    cam_thread.start()

    print("[INIT] Iniciando servidor Flask na porta 5000...")
    app.run(host="0.0.0.0", port=5000, debug=False)

    print("[INFO] Parando o servidor...")
    running = False
    cam_thread.join()
    print("[INFO] Serviço finalizado.")