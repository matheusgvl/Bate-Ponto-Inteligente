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

    private static final String PYTHON_SCRIPT_PATH = "arduino/leitor_serial_web.py";
    private Process currentProcess;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> iniciarLeitura() {
        Map<String, Object> response = new HashMap<>();

        this.pararLeitura();

        try {
            ProcessBuilder pb = new ProcessBuilder("python", PYTHON_SCRIPT_PATH);
            pb.redirectErrorStream(true);
            currentProcess = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(currentProcess.getInputStream())
            );

            String line;
            long startTime = System.currentTimeMillis();
            long timeout = 35000;

            System.out.println("Serviço: Aguardando leitura do cartão NFC...");

            while ((line = reader.readLine()) != null) {

                if (System.currentTimeMillis() - startTime > timeout) {
                    pararLeitura();
                    response.put("success", false);
                    response.put("error", "Tempo esgotado - nenhum cartão detectado");
                    return response;
                }

                if (line.trim().startsWith("{") && line.trim().endsWith("}")) {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(line);

                        if (jsonNode.has("success") && jsonNode.get("success").asBoolean()) {
                            if (jsonNode.has("uid")) {
                                String uid = jsonNode.get("uid").asText().toUpperCase();
                                pararLeitura();
                                response.put("success", true);
                                response.put("uid", uid);
                                return response;
                            }
                        }

                        if (jsonNode.has("error")) {
                            pararLeitura();
                            response.put("success", false);
                            response.put("error", jsonNode.get("error").asText());
                            return response;
                        }

                    } catch (Exception ignored) {
                    }
                }
            }

            pararLeitura();
            response.put("success", false);
            response.put("error", "Leitor desconectado ou nenhum cartão válido detectado");

        } catch (IOException e) {
            pararLeitura();
            response.put("success", false);
            response.put("error", "Erro interno ao executar script leitor: " + e.getMessage());
        }

        return response;
    }

    public Map<String, Object> pararLeitura() {
        Map<String, Object> response = new HashMap<>();
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroy();
            try {
                if (!currentProcess.waitFor(1, TimeUnit.SECONDS)) {
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
            response.put("message", "Nenhuma leitura ativa para interromper.");
        }
        return response;
    }
}