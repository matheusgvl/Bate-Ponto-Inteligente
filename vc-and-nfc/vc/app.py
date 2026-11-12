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

def iniciar_camera_nativa():
    print("[INFO] Procurando câmera nativa ativa...")
    for i in range(5):
        cap = cv2.VideoCapture(i, cv2.CAP_DSHOW)
        if cap.isOpened():
            cap.set(cv2.CAP_PROP_FRAME_WIDTH, 1280)
            cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 720)
            cap.set(cv2.CAP_PROP_BUFFERSIZE, 1)
            ret, frame = cap.read()
            if ret and frame is not None and frame.size > 0:
                print(f"[INFO] Câmera nativa conectada com sucesso no índice {i}.")
                return cap
        cap.release()
    raise Exception("[ERRO CRÍTICO] Nenhuma câmera física detectada no notebook.")

video_capture = iniciar_camera_nativa()
running = True
registration_id = None
data_lock = threading.Lock()

SPRING_BOOT_API_URL = os.getenv("SPRING_BOOT_API_URL", "http://127.0.0.1:8082/api").rstrip("/")
LOCAL_ID = int(os.getenv("PONTO_LOCAL_ID", 3))

def sync_faces_from_spring():
    global known_face_names, known_face_encodings
    print("[INFO] Sincronizando rostos com o backend Spring Boot...")
    try:
        response = requests.get(f"{SPRING_BOOT_API_URL}/person/faces", timeout=5)

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

    except requests.exceptions.RequestException:
        print("[ERRO] Não foi possível conectar ao backend Spring Boot.")

def video_processing_thread():
    global registration_id, running, capture_state, known_face_names, known_face_encodings
    last_recognition_time = {}

    while running:
        ret, frame = video_capture.read()

        if not ret or frame is None or frame.size == 0:
            time.sleep(0.1)
            continue

        # 1. Reduz o frame para 1/4 (25%) - Tamanho ideal para a IA detetar rápido e sem lag
        detection_scale = 0.5
        small_frame = cv2.resize(frame, (0, 0), fx=detection_scale, fy=detection_scale,
                                 interpolation=cv2.INTER_AREA)

        # 2. O dlib usado pelo face_recognition exige RGB/gray em uint8.
        # Algumas webcams/drivers entregam outro dtype, então normalizamos aqui.
        if small_frame.dtype != np.uint8:
            if np.issubdtype(small_frame.dtype, np.floating):
                small_frame = np.clip(small_frame, 0, 255).astype(np.uint8)
            else:
                small_frame = cv2.convertScaleAbs(small_frame)
        rgb_small_frame = cv2.cvtColor(small_frame, cv2.COLOR_BGR2RGB)
        rgb_small_frame = np.ascontiguousarray(rgb_small_frame, dtype=np.uint8)

        try:
            faces = face_recognition.face_locations(rgb_small_frame)
            encodings = face_recognition.face_encodings(rgb_small_frame, faces)
            print(
    f"[DEBUG] Faces detectadas: {len(faces)} | "
    f"Encodings gerados: {len(encodings)} | "
    f"Cadastro ativo: {registration_id is not None}",
    flush=True
)

        except Exception as e:
            print(f"[ERRO IA] Falha no processamento: {e}")
            with data_lock:
                if capture_state["status"] == "capturing":
                    capture_state = {
                        "status": "error",
                        "message": f"Falha no processamento facial: {e}",
                        "encoding": None
                    }
                    registration_id = None
            faces = []
            encodings = []

        with data_lock:
            print(
                f"[DIAGNOSTICO] "
                f"faces={len(faces)} | "
                f"encodings={len(encodings)} | "
                f"registration_id={registration_id} | "
                f"status={capture_state['status']}",
                flush=True
            )
            # Finaliza o cadastro antes de qualquer chamada de reconhecimento ao backend.
            if registration_id is not None and capture_state["status"] == "capturing":
                if len(faces) == 1 and len(encodings) == 1:
                    capture_state = {
                        "status": "success",
                        "message": "Rosto capturado com sucesso!",
                        "encoding": encodings[0].tolist()
                    }
                    registration_id = None
                    print("[SUCESSO] Biometria facial capturada!", flush=True)
                elif len(faces) > 1:
                    capture_state["message"] = "Posicione apenas uma pessoa em frente à câmera."
                else:
                    capture_state["message"] = "Aguardando rosto. Olhe para a câmera em um local iluminado."

            for (top, right, bottom, left), face_encoding in zip(faces, encodings):
                name = "Desconhecido"

                if len(known_face_encodings) > 0:
                    matches = face_recognition.compare_faces(known_face_encodings, face_encoding)
                    face_distances = face_recognition.face_distance(known_face_encodings, face_encoding)
                    if len(face_distances) > 0:
                        best_match_index = np.argmin(face_distances)
                        if matches[best_match_index]:
                            name = known_face_names[best_match_index]

                if name != "Desconhecido":
                    now = time.time()
                    if name not in last_recognition_time or (now - last_recognition_time[name] > 60):
                        print(f"[RECONHECIMENTO] {name} detectado! Enviando para o Spring.")
                        try:
                            payload = {"nome": name, "localId": LOCAL_ID}
                            requests.post(f"{SPRING_BOOT_API_URL}/acesso/registrar/rosto", json=payload, timeout=5)
                            last_recognition_time[name] = now
                        except:
                            pass

                # 3. Multiplica as coordenadas por 4 para o quadrado voltar ao tamanho real do vídeo
                scale_x = frame.shape[1] / small_frame.shape[1]
                scale_y = frame.shape[0] / small_frame.shape[0]
                top = int(round(top * scale_y))
                right = int(round(right * scale_x))
                bottom = int(round(bottom * scale_y))
                left = int(round(left * scale_x))
                left = max(0, min(left, frame.shape[1] - 1))
                right = max(left + 1, min(right, frame.shape[1]))
                top = max(0, min(top, frame.shape[0] - 1))
                bottom = max(top + 1, min(bottom, frame.shape[0]))

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

    with data_lock:
        capture_state = {"status": "capturing", "message": "Aguardando rosto...", "encoding": None}
        registration_id = 123456

    print(f"[API] Iniciando captura para: {data.get('nome')}")
    return jsonify({"mensagem": "Captura iniciada", "status": "capturing"}), 200

@app.route("/face/status", methods=["GET"])
def get_capture_status():
    global capture_state
    with data_lock:
        state = dict(capture_state)
    return jsonify(state), 200

@app.route("/face/stop", methods=["POST"])
def stop_face_capture():
    global registration_id, capture_state
    with data_lock:
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

    flask_thread = threading.Thread(target=lambda: app.run(host="0.0.0.0", port=5000, debug=False, use_reloader=False))
    flask_thread.daemon = True
    flask_thread.start()

    video_processing_thread()
