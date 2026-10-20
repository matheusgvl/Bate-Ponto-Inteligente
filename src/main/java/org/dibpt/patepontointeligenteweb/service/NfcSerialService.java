package org.dibpt.patepontointeligenteweb.service;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serviço unificado que substitui:
 * - leitor_nfc.py (leitura contínua de ponto via Arduino/NFC)
 * - leitor_serial.py e leitor_serial_web.py (captura de UID com timeout de 30s para cadastro web)
 *
 * Como o servidor principal vai rodar em uma VPS, este serviço só é ativado na máquina física
 * onde o leitor NFC estiver conectado (quando ponto.hardware.enabled=true no application.properties).
 */
@Service
@ConditionalOnProperty(name = "ponto.hardware.enabled", havingValue = "true", matchIfMissing = true)
public class NfcSerialService {

    private static final Pattern UID_PATTERN = Pattern.compile(
            "UID \\(Identificador Unico\\):\\s*([0-9A-Fa-f]+)"
    );

    @Value("${ponto.nfc.porta:COM3}")
    private String portaSerial;

    @Value("${ponto.nfc.baudrate:9600}")
    private int baudRate;

    @Value("${PONTO_LOCAL_ID:1}")
    private int localId;

    // Aponte para o IP/Domínio da sua VPS (ex: https://sua-vps.com/api)
    @Value("${ponto.api.base-url:http://localhost:8080/api}")
    private String springApiBaseUrl;

    private SerialPort serialPort;
    private final RestClient restClient = RestClient.create();
    private final StringBuilder bufferLinha = new StringBuilder();

    // Quando preenchido, desvia a próxima leitura do cartão para o fluxo de cadastro web (30s timeout)
    private final AtomicReference<CompletableFuture<String>> capturaCadastroFuture = new AtomicReference<>(null);

    @PostConstruct
    public void iniciar() {
        System.out.printf("[NFC] Iniciando ponte na porta %s @ %dbps para Local ID %d...%n",
                portaSerial, baudRate, localId);

        serialPort = SerialPort.getCommPort(portaSerial);
        serialPort.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);

        if (!serialPort.openPort()) {
            System.err.printf("[ERRO] Não foi possível conectar na %s. Verifique se o Arduino está plugado.%n", portaSerial);
            return;
        }

        System.out.println("[NFC] Conectado! Aguardando cartões...");

        // Listener orientado a eventos (substitui o while True do Python sem bloquear a thread principal)
        serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    return;
                }
                byte[] newData = new byte[serialPort.bytesAvailable()];
                int numRead = serialPort.readBytes(newData, newData.length);
                if (numRead > 0) {
                    String chunk = new String(newData, 0, numRead, StandardCharsets.UTF_8);
                    processarChunkSerial(chunk);
                }
            }
        });
    }

    private synchronized void processarChunkSerial(String chunk) {
        bufferLinha.append(chunk);
        int indexQuebra;
        while ((indexQuebra = bufferLinha.indexOf("\n")) != -1) {
            String linha = bufferLinha.substring(0, indexQuebra).trim();
            bufferLinha.delete(0, indexQuebra + 1);
            if (!linha.isEmpty()) {
                processarLinha(linha);
            }
        }
    }

    private void processarLinha(String linha) {
        String uid = extrairUid(linha);
        if (uid == null || uid.isEmpty()) {
            return;
        }

        System.out.printf("%n[NFC] Cartão detectado: %s%n", uid);

        // Verifica se há uma requisição web aguardando leitura para cadastro (leitor_serial_web.py)
        CompletableFuture<String> futureCadastro = capturaCadastroFuture.getAndSet(null);
        if (futureCadastro != null && !futureCadastro.isDone()) {
            futureCadastro.complete(uid);
            return;
        }

        // Fluxo normal de batida de ponto (leitor_nfc.py)
        enviarParaSpring(uid);
    }

    private String extrairUid(String linha) {
        Matcher matcher = UID_PATTERN.matcher(linha);
        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }
        if (linha.matches("^[0-9A-Fa-f]{4,20}$")) {
            return linha.toUpperCase();
        }
        return null;
    }

    /**
     * Envia o UID via HTTP para a API rodando na VPS.
     */
    public void enviarParaSpring(String uid) {
        try {
            Map<String, Object> payload = Map.of(
                    "uid", uid,
                    "localId", localId
            );
            System.out.println("[API] Enviando para VPS Spring: " + payload);

            @SuppressWarnings("unchecked")
            Map<String, Object> dados = restClient.post()
                    .uri(springApiBaseUrl + "/acesso/registrar/cartao")
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            if (dados != null) {
                System.out.printf("[API] Resposta SUCESSO: %s - %s%n",
                        dados.get("status"), dados.get("motivo"));
            }
        } catch (Exception e) {
            System.err.println("[API] Resposta NEGADO ou erro ao conectar na VPS: " + e.getMessage());
        }
    }

    /**
     * Equivalente ao leitor_serial_web.py: aguarda até 30 segundos pela aproximação de um cartão NFC.
     */
    public Map<String, Object> aguardarCartaoParaCadastro(long timeoutSegundos) {
        if (serialPort == null || !serialPort.isOpen()) {
            return Map.of("error", "Porta serial " + portaSerial + " não está aberta.");
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        capturaCadastroFuture.set(future);

        try {
            String uidHex = future.get(timeoutSegundos, TimeUnit.SECONDS);
            if (uidHex.startsWith("63DF") || uidHex.startsWith("A979")) {
                return Map.of(
                        "success", true,
                        "uid", uidHex,
                        "message", "Cartão NFC detectado com sucesso"
                );
            } else {
                return Map.of(
                        "warning", "Cartão detectado mas UID fora do prefixo padrão: " + uidHex,
                        "uid", uidHex
                );
            }
        } catch (TimeoutException e) {
            capturaCadastroFuture.compareAndSet(future, null);
            return Map.of("error", "Timeout - nenhum cartão detectado em " + timeoutSegundos + " segundos");
        } catch (Exception e) {
            capturaCadastroFuture.compareAndSet(future, null);
            return Map.of("error", "Erro durante leitura: " + e.getMessage());
        }
    }

    @PreDestroy
    public void encerrar() {
        if (serialPort != null && serialPort.isOpen()) {
            System.out.println("[NFC] Encerrando porta serial...");
            serialPort.removeDataListener();
            serialPort.closePort();
        }
    }

    @RestController
    @RequestMapping("/api/nfc")
    @ConditionalOnProperty(name = "ponto.hardware.enabled", havingValue = "true", matchIfMissing = true)
    public static class NfcCaptureController {

        private final NfcSerialService nfcSerialService;

        public NfcCaptureController(NfcSerialService nfcSerialService) {
            this.nfcSerialService = nfcSerialService;
        }

        @GetMapping("/ler-cartao")
        public ResponseEntity<Map<String, Object>> lerCartaoComTimeout(
                @RequestParam(defaultValue = "30") long timeoutSegundos) {
            Map<String, Object> resultado = nfcSerialService.aguardarCartaoParaCadastro(timeoutSegundos);
            if (resultado.containsKey("error")) {
                return ResponseEntity.status(408).body(resultado);
            }
            return ResponseEntity.ok(resultado);
        }
    }
}