package org.dibpt.patepontointeligenteweb.controller;

import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import org.dibpt.patepontointeligenteweb.service.PessoaService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.dibpt.patepontointeligenteweb.DTO.AtualizarPessoaDTO;
import org.dibpt.patepontointeligenteweb.DTO.PessoaResponseDTO;
import org.dibpt.patepontointeligenteweb.DTO.SalvarPessoaDTO;
import org.dibpt.patepontointeligenteweb.DTO.StatsResponseDTO;
import org.dibpt.patepontointeligenteweb.DTO.SalvarFaceDTO;
import org.springframework.web.client.RestTemplate;
import org.dibpt.patepontointeligenteweb.service.CardReaderService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/person")
@CrossOrigin(origins = "*")
public class PessoaController {

    private final PessoaService pessoaService;
    private final RestTemplate restTemplate;
    private final CardReaderService cardReaderService;

    @Autowired
    public PessoaController(PessoaService pessoaService, RestTemplate restTemplate, CardReaderService cardReaderService) {
        this.pessoaService = pessoaService;
        this.restTemplate = restTemplate;
        this.cardReaderService = cardReaderService;
    }

    @PostMapping("/save")
    public ResponseEntity<PessoaResponseDTO> salvarPessoa(@RequestBody SalvarPessoaDTO dto) throws Exception {
        Pessoa pessoaSalva = pessoaService.salvarPessoa(dto);
        PessoaResponseDTO responseDto = new PessoaResponseDTO(pessoaSalva);
        URI location = URI.create("/api/person/" + responseDto.id());
        return ResponseEntity.created(location).body(responseDto);
    }

    @GetMapping("/list")
    public ResponseEntity<List<PessoaResponseDTO>> listarPessoas() {
        List<PessoaResponseDTO> pessoasDto = pessoaService.listarPessoasDTOs();
        return ResponseEntity.ok(pessoasDto);
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponseDTO> obterEstatisticas() {
        StatsResponseDTO stats = pessoaService.obterEstatisticas();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/search/card/{uid}")
    public ResponseEntity<PessoaResponseDTO> buscarPorCartao(@PathVariable String uid) throws Exception {
        return ResponseEntity.ok(pessoaService.buscarDTOPorCartao(uid));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<PessoaResponseDTO> atualizarPessoa(@PathVariable Long id, @RequestBody AtualizarPessoaDTO dto) throws Exception {
        return ResponseEntity.ok(pessoaService.atualizarPessoa(id, dto));
    }

    @PostMapping("/{id}/initiate-face-register")
    public ResponseEntity<String> iniciarCadastroRosto(@PathVariable Long id) {
        try {
            String response = pessoaService.iniciarCadastroRosto(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/register-face")
    public ResponseEntity<?> salvarFace(@RequestBody SalvarFaceDTO dto) {
        try {
            pessoaService.salvarFace(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/faces")
    public ResponseEntity<List<Map<String, Object>>> listarFaces() {
        List<Map<String, Object>> faces = pessoaService.listarFacesParaPython();
        return ResponseEntity.ok(faces);
    }
    @PostMapping("/card/read")
    public ResponseEntity<Map<String, Object>> lerCartaoParaCadastro() {
        return ResponseEntity.ok(cardReaderService.iniciarLeitura());
    }

    @PostMapping("/card/stop")
    public ResponseEntity<Map<String, Object>> cancelarLeituraCartao() {
        return ResponseEntity.ok(cardReaderService.pararLeitura());
    }

    @PostMapping("/face/capture")
    public ResponseEntity<?> capturarRosto(@RequestBody Map<String, String> requestBody) {
        try {
            String nome = requestBody.get("nome");
            if (nome == null || nome.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Nome é obrigatório"));
            }

            // Chama o serviço Python para captura de rosto
            Map<String, Object> resultado = pessoaService.capturarRostoPython(nome);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erro ao capturar rosto: " + e.getMessage()));
        }
    }

    @PostMapping("/face/stop")
    public ResponseEntity<Map<String, Object>> pararCapturaRosto() {
        try {
            Map<String, Object> resultado = pessoaService.pararCapturaRosto();
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erro ao parar captura: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirPessoa(@PathVariable Long id) throws Exception {
        pessoaService.excluirPessoa(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/face/status")
    public ResponseEntity<Map<String, Object>> verificarStatusCaptura() {
        return ResponseEntity.ok(pessoaService.verificarStatusCaptura());
    }
}