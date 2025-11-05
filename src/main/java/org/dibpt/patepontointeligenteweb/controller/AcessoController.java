package org.dibpt.patepontointeligenteweb.controller;

import org.dibpt.patepontointeligenteweb.DTO.AcessoResponseDTO;
import org.dibpt.patepontointeligenteweb.service.AcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.dibpt.patepontointeligenteweb.DTO.AcessoLogResponseDTO;
import java.util.List;

@RestController
@RequestMapping("/api/acesso")
@CrossOrigin(origins = "*")
public class AcessoController {

    private final AcessoService acessoService;

    @Autowired
    public AcessoController(AcessoService acessoService) {
        this.acessoService = acessoService;
    }

    @PostMapping("/registrar/cartao")
    public ResponseEntity<AcessoResponseDTO> registrarAcesso(@RequestBody Map<String, Object> requestBody) {
        String uid = (String) requestBody.get("uid");
        Long localId = ((Number) requestBody.get("localId")).longValue();
        return acessoService.registrarAcessoPorCartao(uid, localId);
    }

    @PostMapping("/registrar/rosto")
    public ResponseEntity<AcessoResponseDTO> registrarAcessoRosto(@RequestBody Map<String, Object> requestBody) {
        String nome = (String) requestBody.get("nome");
        Long localId = ((Number) requestBody.get("localId")).longValue();
        return acessoService.registrarAcessoPorRosto(nome, localId);
    }
    @GetMapping("/log")
    public ResponseEntity<List<AcessoLogResponseDTO>> getAccessLog() {
        return ResponseEntity.ok(acessoService.listarHistoricoDeAcessos());
    }
}