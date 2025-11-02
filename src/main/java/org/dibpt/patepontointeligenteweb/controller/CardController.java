package org.dibpt.patepontointeligenteweb.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/card")
@CrossOrigin(origins = "*")
public class CardController {
    
    private static final String PYTHON_SCRIPT_PATH = "arduino/leitor_serial_web.py";
    private Process currentProcess;
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @PostMapping("/read")
    public ResponseEntity<Map<String, Object>> readCard() {
        Map<String, Object> response = new HashMap<>();
        
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
            
            System.out.println("Aguardando leitura do cartão NFC...");
            
            while ((line = reader.readLine()) != null) {
                System.out.println("Python: " + line);
                
                if (System.currentTimeMillis() - startTime > timeout) {
                    currentProcess.destroy();
                    response.put("success", false);
                    response.put("error", "Timeout - nenhum cartão detectado");
                    return ResponseEntity.ok(response);
                }
                
                if (line.trim().startsWith("{") && line.trim().endsWith("}")) {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(line);
                        
                        if (jsonNode.has("success") && jsonNode.get("success").asBoolean()) {
                            if (jsonNode.has("uid")) {
                                String uid = jsonNode.get("uid").asText().toUpperCase();
                                System.out.println("UID detectado via JSON: " + uid);
                                
                                if (uid.startsWith("63DF") || uid.startsWith("A979")) {
                                    currentProcess.destroy();
                                    
                                    response.put("success", true);
                                    response.put("uid", uid);
                                    response.put("message", "Cartão detectado com sucesso");
                                    
                                    return ResponseEntity.ok(response);
                                } else {
                                    System.out.println("UID inválido (não começa com 63DF ou A979): " + uid);
                                }
                            }
                        }
                        
                        if (jsonNode.has("error")) {
                            currentProcess.destroy();
                            response.put("success", false);
                            response.put("error", jsonNode.get("error").asText());
                            return ResponseEntity.ok(response);
                        }
                        
                    } catch (Exception jsonEx) {
                        System.out.println("JSON inválido ignorado: " + line);
                    }
                }
            }
            
            currentProcess.destroy();
            response.put("success", false);
            response.put("error", "Nenhum cartão válido detectado");
            
        } catch (IOException e) {
            System.err.println("Erro ao executar leitor serial: " + e.getMessage());
            response.put("success", false);
            response.put("error", "Erro ao acessar leitor: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopReading() {
        Map<String, Object> response = new HashMap<>();
        
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroy();
            try {
                currentProcess.waitFor(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                currentProcess.destroyForcibly();
            }
            response.put("success", true);
            response.put("message", "Leitura interrompida");
        } else {
            response.put("success", false);
            response.put("message", "Nenhuma leitura ativa");
        }
        
        return ResponseEntity.ok(response);
    }
}
