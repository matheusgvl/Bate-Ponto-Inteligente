package org.dibpt.patepontointeligenteweb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class CardReaderService {

    // Certifique-se que estes caminhos estão corretos no seu computador
    private static final String PYTHON_EXECUTABLE = "G:\\Trabalhos\\bate-ponto-inteligente\\Bate-Ponto-Inteligente\\.venv\\Scripts\\python.exe";
    private static final String PYTHON_SCRIPT_PATH = "G:\\Trabalhos\\bate-ponto-inteligente\\Bate-Ponto-Inteligente\\vc-and-nfc\\nfc\\leitor_serial_web.py";

    private Process currentProcess;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public synchronized Map<String, Object> iniciarLeitura() {
        Map<String, Object> response = new HashMap<>();

        this.pararLeituraInterno();

        try {
            ProcessBuilder pb = new ProcessBuilder(PYTHON_EXECUTABLE, "-u", PYTHON_SCRIPT_PATH);
            pb.redirectErrorStream(true);
            currentProcess = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(currentProcess.getInputStream())
            );

            String line;
            long startTime = System.currentTimeMillis();
            long timeout = 35000;

            System.out.println("Serviço: Iniciando leitura sincronizada...");

            while ((line = reader.readLine()) != null) {

                if (System.currentTimeMillis() - startTime > timeout) {
                    System.out.println("Serviço: Timeout Java atingido.");
                    pararLeituraInterno();
                    response.put("success", false);
                    response.put("error", "Tempo esgotado - nenhum cartão detectado");
                    return response;
                }

                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("{") && trimmedLine.endsWith("}")) {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(trimmedLine);

                        if (jsonNode.has("success") && jsonNode.get("success").asBoolean()) {
                            if (jsonNode.has("uid")) {
                                String uid = jsonNode.get("uid").asText().toUpperCase();
                                pararLeituraInterno();
                                response.put("success", true);
                                response.put("uid", uid);
                                return response;
                            }
                        }

                        if (jsonNode.has("error")) {
                            pararLeituraInterno();
                            response.put("success", false);
                            response.put("error", jsonNode.get("error").asText());
                            return response;
                        }
                    } catch (Exception ignored) { }
                }
            }

            pararLeituraInterno();
            response.put("success", false);
            response.put("error", "Leitor desconectado ou processo finalizado.");

        } catch (IOException e) {
            pararLeituraInterno();
            response.put("success", false);
            response.put("error", "Erro interno: " + e.getMessage());
        }

        return response;
    }

    public synchronized Map<String, Object> pararLeitura() {
        return pararLeituraInterno();
    }

    private Map<String, Object> pararLeituraInterno() {
        Map<String, Object> response = new HashMap<>();
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroy();
            try {
                if (!currentProcess.waitFor(2, TimeUnit.SECONDS)) {
                    currentProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                currentProcess.destroyForcibly();
                Thread.currentThread().interrupt();
            }
            response.put("success", true);
            response.put("message", "Leitura interrompida.");
        } else {
            response.put("success", false);
            response.put("message", "Nenhuma leitura ativa.");
        }
        return response;
    }
}