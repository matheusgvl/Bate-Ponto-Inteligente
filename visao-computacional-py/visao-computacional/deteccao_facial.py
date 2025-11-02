import sys
import json
import os
from ultralytics import YOLO

MODEL_PATH = 'yolov8n.pt'
TARGET_CLASS_ID = 0
CONFIDENCE_THRESHOLD = 0.65

def create_error_response(message, path=""):
    return {
        "status": "error",
        "detected": False,
        "message": message,
        "image_path": path
    }

def run_detection(image_path):
    """
    Carrega o modelo YOLOv8, detecta objetos na imagem e retorna as coordenadas da primeira 'pessoa' encontrada.

    Args:
        image_path (str): Caminho completo para o arquivo de imagem a ser analisado.

    Returns:
        dict: Um dicionário de resultado no formato JSON.
    """

    def create_error_response(message, path=""):
        return {
            "status": "error",
            "detected": False,
            "message": message,
            "image_path": path
        }

    try:
        if not os.path.exists(image_path):
            return create_error_response(f"Arquivo de imagem não encontrado: {image_path}", image_path)

        model = YOLO(MODEL_PATH)

        results = model(image_path, conf=CONFIDENCE_THRESHOLD, verbose=False)

        for r in results:
            for box in r.boxes:
                class_id = int(box.cls[0])
                confidence = float(box.conf[0])

                if class_id == TARGET_CLASS_ID and model.names[class_id] == 'person':

                    coords = box.xyxy[0].tolist()
                    x1, y1, x2, y2 = map(int, coords)

                    return {
                        "status": "success",
                        "detected": True,
                        "class": model.names[class_id],
                        "confidence": confidence,
                        "box": {
                            "x_start": x1,
                            "y_start": y1,
                            "x_end": x2,
                            "y_end": y2
                        }
                    }

        return {
            "status": "success",
            "detected": False,
            "message": "Nenhuma pessoa detectada com a confiança mínima de {:.2f}.".format(CONFIDENCE_THRESHOLD)
        }

    except Exception as e:
        return create_error_response(f"Erro inesperado no YOLO: {str(e)}", image_path)


if __name__ == "__main__":

    if len(sys.argv) < 2:
        error_result = create_error_response("Faltando argumento: O caminho da imagem deve ser fornecido.")
        print(json.dumps(error_result))
        sys.exit(1)

    image_file_path = sys.argv[1]

    result = run_detection(image_file_path)

    print(json.dumps(result))