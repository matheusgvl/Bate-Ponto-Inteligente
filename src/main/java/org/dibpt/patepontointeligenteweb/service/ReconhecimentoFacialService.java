package org.dibpt.patepontointeligenteweb.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.FaceDetectorYN;
import org.opencv.objdetect.FaceRecognizerSF;
import org.opencv.videoio.VideoCapture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


@Service
@ConditionalOnProperty(name = "ponto.hardware.enabled", havingValue = "true", matchIfMissing = true)
public class ReconhecimentoFacialService {

    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public record RostoConhecido(String nome, float[] encoding) {}

    private final List<RostoConhecido> rostosConhecidos = new CopyOnWriteArrayList<>();
    private final Map<String, Long> ultimoReconhecimentoMs = new ConcurrentHashMap<>();

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicReference<Integer> registrationId = new AtomicReference<>(null);
    private final AtomicReference<Map<String, Object>> captureState = new AtomicReference<>(
            Map.of("status", "idle", "message", "", "encoding", Collections.emptyList())
    );

    @Value("${PONTO_LOCAL_ID:1}")
    private int localId;

    // Aponte para o IP/Domínio da sua VPS (ex: https://sua-vps.com/api)
    @Value("${ponto.api.base-url:http://localhost:8080/api}")
    private String springBootApiUrl;

    @Value("${ponto.camera.index:0}")
    private int cameraIndex;

    @Value("${ponto.camera.exibir-janela:true}")
    private boolean exibirJanela;

    @Value("${ponto.camera.yunet-model:src/main/resources/models/face_detection_yunet_2023mar.onnx}")
    private String yunetModelPath;

    @Value("${ponto.camera.sface-model:src/main/resources/models/face_recognition_sface_2021dec.onnx}")
    private String sfaceModelPath;

    private static final double LIMIAR_DISTANCIA_L2 = 1.128;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Thread cameraThread;

    @PostConstruct
    public void init() {
        System.out.println("[INIT] Iniciando serviço de Reconhecimento Facial em Java...");
        syncFacesFromSpring();

        cameraThread = new Thread(this::videoProcessingLoop, "opencv-camera-thread");
        cameraThread.setDaemon(true);
        cameraThread.start();
    }

    public void syncFacesFromSpring() {
        System.out.println("[INFO] Sincronizando rostos com o backend na VPS...");
        try {
            List<Map<String, Object>> data = restClient.get()
                    .uri(springBootApiUrl + "/person/faces")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (data != null) {
                List<RostoConhecido> temp = new ArrayList<>();
                for (Map<String, Object> item : data) {
                    String nome = String.valueOf(item.get("nome"));
                    String encodingJson = String.valueOf(item.get("encoding"));
                    try {
                        float[] vetor = objectMapper.readValue(encodingJson, new TypeReference<float[]>() {});
                        temp.add(new RostoConhecido(nome, vetor));
                    } catch (Exception ignored) {
                    }
                }
                rostosConhecidos.clear();
                rostosConhecidos.addAll(temp);
                System.out.printf("[INFO] Sincronização concluída. %d rostos carregados.%n", rostosConhecidos.size());
            }
        } catch (Exception e) {
            System.err.println("[ERRO] Não foi possível sincronizar rostos com a VPS: " + e.getMessage());
        }
    }

    private void videoProcessingLoop() {
        VideoCapture videoCapture = new VideoCapture(cameraIndex);
        if (!videoCapture.isOpened()) {
            System.err.println("[ERRO] Não foi possível abrir a câmera no índice " + cameraIndex);
            return;
        }

        FaceDetectorYN faceDetector = FaceDetectorYN.create(
                yunetModelPath, "", new Size(320, 240), 0.9f, 0.3f, 5000
        );
        FaceRecognizerSF faceRecognizer = FaceRecognizerSF.create(sfaceModelPath, "");

        Mat frame = new Mat();
        Mat smallFrame = new Mat();
        Mat faces = new Mat();

        while (running.get()) {
            if (!videoCapture.read(frame) || frame.empty()) {
                System.err.println("[ERRO] Falha na câmera.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }

            Imgproc.resize(frame, smallFrame, new Size(), 0.25, 0.25, Imgproc.INTER_LINEAR);
            faceDetector.setInputSize(smallFrame.size());
            faceDetector.detect(smallFrame, faces);

            for (int i = 0; i < faces.rows(); i++) {
                Mat alignedFace = new Mat();
                Mat featureMat = new Mat();
                faceRecognizer.alignCrop(smallFrame, faces.row(i), alignedFace);
                faceRecognizer.feature(alignedFace, featureMat);

                float[] faceEncoding = new float[(int) featureMat.total()];
                featureMat.get(0, 0, faceEncoding);

                String name = identificarRosto(faceEncoding);

                if (!"Desconhecido".equals(name)) {
                    long agora = System.currentTimeMillis();
                    Long ultimo = ultimoReconhecimentoMs.get(name);
                    if (ultimo == null || (agora - ultimo) > 60_000L) {
                        System.out.printf("[RECONHECIMENTO] %s detectado! Enviando para a VPS.%n", name);
                        registrarAcessoRosto(name);
                        ultimoReconhecimentoMs.put(name, agora);
                    }
                } else if (registrationId.get() != null) {
                    System.out.println("[CADASTRO] Rosto capturado! Guardando na memória...");
                    List<Float> encodingList = new ArrayList<>(faceEncoding.length);
                    for (float v : faceEncoding) {
                        encodingList.add(v);
                    }
                    Map<String, Object> novoEstado = new HashMap<>();
                    novoEstado.put("status", "success");
                    novoEstado.put("message", "Rosto capturado com sucesso!");
                    novoEstado.put("encoding", encodingList);
                    captureState.set(novoEstado);
                    registrationId.set(null);
                }

                float[] box = new float[4];
                faces.get(i, 0, box);
                int left = Math.round(box[0] * 4);
                int top = Math.round(box[1] * 4);
                int right = Math.round((box[0] + box[2]) * 4);
                int bottom = Math.round((box[1] + box[3]) * 4);

                Scalar color = !"Desconhecido".equals(name)
                        ? new Scalar(0, 255, 0)
                        : new Scalar(0, 0, 255);

                Imgproc.rectangle(frame, new Point(left, top), new Point(right, bottom), color, 2);
                Imgproc.rectangle(frame, new Point(left, bottom - 35), new Point(right, bottom), color, Imgproc.FILLED);
                Imgproc.putText(frame, name, new Point(left + 6, bottom - 6),
                        Imgproc.FONT_HERSHEY_DUPLEX, 1.0, new Scalar(255, 255, 255), 1);
            }

            if (exibirJanela && !java.awt.GraphicsEnvironment.isHeadless()) {
                HighGui.imshow("Bate-Ponto Inteligente (Java Service)", frame);
                if ((HighGui.waitKey(1) & 0xFF) == 'q') {
                    running.set(false);
                    break;
                }
            }
        }

        videoCapture.release();
        if (exibirJanela && !java.awt.GraphicsEnvironment.isHeadless()) {
            HighGui.destroyAllWindows();
        }
        System.out.println("[INFO] Câmera finalizada.");
    }

    private String identificarRosto(float[] candidato) {
        if (rostosConhecidos.isEmpty()) {
            return "Desconhecido";
        }
        String melhorNome = "Desconhecido";
        double menorDistancia = Double.MAX_VALUE;

        for (RostoConhecido conhecido : rostosConhecidos) {
            double dist = calcularDistanciaEuclidiana(conhecido.encoding(), candidato);
            if (dist < menorDistancia) {
                menorDistancia = dist;
                melhorNome = conhecido.nome();
            }
        }
        return menorDistancia <= LIMIAR_DISTANCIA_L2 ? melhorNome : "Desconhecido";
    }

    private double calcularDistanciaEuclidiana(float[] a, float[] b) {
        int len = Math.min(a.length, b.length);
        double soma = 0.0;
        for (int i = 0; i < len; i++) {
            double diff = a[i] - b[i];
            soma += diff * diff;
        }
        return Math.sqrt(soma);
    }

    private void registrarAcessoRosto(String nome) {
        try {
            Map<String, Object> payload = Map.of("nome", nome, "localId", localId);
            restClient.post()
                    .uri(springBootApiUrl + "/acesso/registrar/rosto")
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ignored) {
        }
    }

    @PreDestroy
    public void stopService() {
        running.set(false);
    }

    public void ativarModoCadastro(Integer id) {
        registrationId.set(id != null ? id : 999999);
    }

    public void iniciarCapturaFacial(String nome) {
        Map<String, Object> estado = new HashMap<>();
        estado.put("status", "capturing");
        estado.put("message", "Aguardando rosto...");
        estado.put("encoding", null);
        captureState.set(estado);
        registrationId.set(123456);
        System.out.println("[API] Iniciando captura para: " + nome);
    }

    public Map<String, Object> obterStatusCaptura() {
        return captureState.get();
    }

    public void pararCapturaFacial() {
        registrationId.set(null);
        Map<String, Object> estado = new HashMap<>();
        estado.put("status", "idle");
        estado.put("message", "Cancelado");
        estado.put("encoding", null);
        captureState.set(estado);
    }

    @RestController
    @ConditionalOnProperty(name = "ponto.hardware.enabled", havingValue = "true", matchIfMissing = true)
    public static class ReconhecimentoFacialController {

        private final ReconhecimentoFacialService facialService;

        public ReconhecimentoFacialController(ReconhecimentoFacialService facialService) {
            this.facialService = facialService;
        }

        @PostMapping("/register")
        public ResponseEntity<Map<String, Object>> startRegistration(@RequestBody Map<String, Object> data) {
            Integer id = data.get("id") instanceof Number n ? n.intValue() : 999999;
            facialService.ativarModoCadastro(id);
            return ResponseEntity.ok(Map.of("mensagem", "Modo cadastro ativado"));
        }

        @PostMapping("/face/capture")
        public ResponseEntity<Map<String, Object>> startFaceCapture(@RequestBody Map<String, Object> data) {
            facialService.iniciarCapturaFacial(String.valueOf(data.get("nome")));
            return ResponseEntity.ok(Map.of("mensagem", "Captura iniciada", "status", "capturing"));
        }

        @GetMapping("/face/status")
        public ResponseEntity<Map<String, Object>> getCaptureStatus() {
            return ResponseEntity.ok(facialService.obterStatusCaptura());
        }

        @PostMapping("/face/stop")
        public ResponseEntity<Map<String, Object>> stopFaceCapture() {
            facialService.pararCapturaFacial();
            return ResponseEntity.ok(Map.of("mensagem", "Parado"));
        }

        @PostMapping("/sync")
        public ResponseEntity<Map<String, Object>> forceSync() {
            CompletableFuture.runAsync(facialService::syncFacesFromSpring);
            return ResponseEntity.accepted().body(Map.of("mensagem", "Sync iniciado"));
        }

        @GetMapping("/health")
        public ResponseEntity<Map<String, Object>> healthCheck() {
            return ResponseEntity.ok(Map.of("status", "online"));
        }
    }
}