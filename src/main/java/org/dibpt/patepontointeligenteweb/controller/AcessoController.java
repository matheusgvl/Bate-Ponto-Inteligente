package org.dibpt.patepontointeligenteweb.controller;

import org.dibpt.patepontointeligenteweb.DTO.AcessoResponseDTO;
import org.dibpt.patepontointeligenteweb.service.AcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

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
    public ResponseEntity<java.util.List<org.dibpt.patepontointeligenteweb.DTO.AcessoLogResponseDTO>> getAccessLog() {
        return ResponseEntity.ok(acessoService.listarHistoricoDeAcessos());
    }

    @PostMapping("/vincular")
    public ResponseEntity<Map<String, String>> vincularAcesso(@RequestBody Map<String, Object> requestBody) {
        Long localId = ((Number) requestBody.get("localId")).longValue();
        Long pessoaId = ((Number) requestBody.get("pessoaId")).longValue();
        return acessoService.vincularPessoaAoLocal(pessoaId, localId);
    }

    @DeleteMapping("/desvincular")
    public ResponseEntity<Map<String, String>> desvincularAcesso(@RequestBody Map<String, Object> requestBody) {
        Long localId = ((Number) requestBody.get("localId")).longValue();
        Long pessoaId = ((Number) requestBody.get("pessoaId")).longValue();
        return acessoService.desvincularPessoaDoLocal(pessoaId, localId);
    }
}