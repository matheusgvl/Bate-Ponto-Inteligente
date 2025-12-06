import face_recognition
import numpy as np
import cv2
import requests
import threading
import time
import json
from flask import Flask, request, jsonify
import os

app = Flask(__name__)

known_face_names = []
known_face_encodings = []

capture_state = {
    "status": "idle",
    "message": "",
    "encoding": None
}

video_capture = cv2.VideoCapture(0)
running = True
registration_id = None
data_lock = threading.Lock()

SPRING_BOOT_API_URL = "http://localhost:8080/api"
LOCAL_ID = int(os.getenv("PONTO_LOCAL_ID", 1))

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
                try:
                    encoding_list = json.loads(item['encoding'])
                    temp_encodings.append(np.array(encoding_list))
                except:
                    pass

            with data_lock:
                known_face_names = temp_names
                known_face_encodings = temp_encodings

            print(f"[INFO] Sincronização concluída. {len(known_face_names)} rostos carregados.")
        else:
            print(f"[ERRO] Falha ao buscar rostos. Status: {response.status_code}")

    except requests.exceptions.ConnectionError:
        print("[ERRO] Não foi possível conectar ao backend Spring Boot.")

def video_processing_thread():
    global registration_id, running, capture_state, known_face_names, known_face_encodings
    last_recognition_time = {}

    while running:
        ret, frame = video_capture.read()
        if not ret:
            print("[ERRO] Falha na câmera.")
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
                            payload = {"nome": name, "localId": LOCAL_ID}
                            requests.post(f"{SPRING_BOOT_API_URL}/acesso/registrar/rosto", json=payload)
                            last_recognition_time[name] = now
                        except:
                            pass

                elif name == "Desconhecido" and registration_id is not None:
                    print(f"[CADASTRO] Rosto capturado! Guardando na memória...")

                    encoding_list = face_encoding.tolist()

                    capture_state = {
                        "status": "success",
                        "message": "Rosto capturado com sucesso!",
                        "encoding": encoding_list
                    }

                    registration_id = None

                top *= 4
                right *= 4
                bottom *= 4
                left *= 4
                color = (0, 255, 0) if name != "Desconhecido" else (0, 0, 255)
                cv2.rectangle(frame, (left, top), (right, bottom), color, 2)
                cv2.rectangle(frame, (left, bottom - 35), (right, bottom), color, cv2.FILLED)
                cv2.putText(frame, name, (left + 6, bottom - 6), cv2.FONT_HERSHEY_DUPLEX, 1.0, (255, 255, 255), 1)

        cv2.imshow("Bate-Ponto Inteligente (Python Service)", frame)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            running = False
            break

    video_capture.release()
    cv2.destroyAllWindows()
    print("[INFO] Câmera finalizada.")

@app.route("/register", methods=["POST"])
def start_registration():
    global registration_id
    data = request.get_json()
    registration_id = data.get("id", 999999)
    return jsonify({"mensagem": "Modo cadastro ativado"}), 200

@app.route("/face/capture", methods=["POST"])
def start_face_capture():
    global registration_id, capture_state
    data = request.get_json()

    capture_state = {"status": "capturing", "message": "Aguardando rosto...", "encoding": None}

    registration_id = 123456

    print(f"[API] Iniciando captura para: {data.get('nome')}")
    return jsonify({"mensagem": "Captura iniciada", "status": "capturing"}), 200

@app.route("/face/status", methods=["GET"])
def get_capture_status():
    global capture_state
    return jsonify(capture_state), 200

@app.route("/face/stop", methods=["POST"])
def stop_face_capture():
    global registration_id, capture_state
    registration_id = None
    capture_state = {"status": "idle", "message": "Cancelado", "encoding": None}
    return jsonify({"mensagem": "Parado"}), 200

@app.route("/sync", methods=["POST"])
def force_sync():
    threading.Thread(target=sync_faces_from_spring).start()
    return jsonify({"mensagem": "Sync iniciado"}), 202

@app.route("/health", methods=["GET"])
def health_check():
    return jsonify({"status": "online"}), 200

if __name__ == "__main__":
    print("[INIT] Iniciando serviço...")
    sync_faces_from_spring()

    cam_thread = threading.Thread(target=video_processing_thread)
    cam_thread.daemon = True
    cam_thread.start()

    app.run(host="0.0.0.0", port=5000, debug=False)