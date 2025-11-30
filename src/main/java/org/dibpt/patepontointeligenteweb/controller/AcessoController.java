package org.dibpt.patepontointeligenteweb.controller;

import org.dibpt.patepontointeligenteweb.DTO.AcessoResponseDTO;
import org.dibpt.patepontointeligenteweb.service.AcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;
import java.time.LocalDate;
import org.springframework.data.domain.Page;

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

    @GetMapping("/logs")
    public ResponseEntity<Page<org.dibpt.patepontointeligenteweb.DTO.AcessoLogResponseDTO>> getLogsComFiltros(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) LocalDate data,
            @RequestParam(required = false) Long pessoaId,
            @RequestParam(required = false) Long localId,
            @RequestParam(required = false) String tipoAcesso) {
        return ResponseEntity.ok(acessoService.listarLogsComFiltros(page, size, data, pessoaId, localId, tipoAcesso));
    }

    @GetMapping("/relatorio")
    public ResponseEntity<Map<String, Object>> gerarRelatorio(
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "ultimo_mes") String periodo) {
        return ResponseEntity.ok(acessoService.gerarRelatorioAcessos(dataInicio, dataFim, periodo));
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