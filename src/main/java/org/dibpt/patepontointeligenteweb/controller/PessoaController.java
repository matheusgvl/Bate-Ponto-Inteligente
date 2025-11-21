package org.dibpt.patepontointeligenteweb.controller;

import org.dibpt.patepontointeligenteweb.DTO.*;
import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import org.dibpt.patepontointeligenteweb.service.CardReaderService;
import org.dibpt.patepontointeligenteweb.service.PessoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/person")
@CrossOrigin(origins = "*")
public class PessoaController {

    private final PessoaService pessoaService;
    private final CardReaderService cardReaderService;

    @Autowired
    public PessoaController(PessoaService pessoaService, CardReaderService cardReaderService) {
        this.pessoaService = pessoaService;
        this.cardReaderService = cardReaderService;
    }

    @PostMapping("/save")
    public ResponseEntity<PessoaResponseDTO> salvarPessoa(@RequestBody SalvarPessoaDTO dto) {
        Pessoa pessoaSalva = pessoaService.salvarPessoa(dto);
        PessoaResponseDTO responseDto = new PessoaResponseDTO(pessoaSalva);
        URI location = URI.create("/api/person/" + responseDto.id());
        return ResponseEntity.created(location).body(responseDto);
    }

    @GetMapping("/list")
    public ResponseEntity<List<PessoaResponseDTO>> listarPessoas() {
        return ResponseEntity.ok(pessoaService.listarPessoasDTOs());
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponseDTO> obterEstatisticas() {
        return ResponseEntity.ok(pessoaService.obterEstatisticas());
    }

    @GetMapping("/search/card/{uid}")
    public ResponseEntity<PessoaResponseDTO> buscarPorCartao(@PathVariable String uid) {
        return ResponseEntity.ok(pessoaService.buscarDTOPorCartao(uid));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<PessoaResponseDTO> atualizarPessoa(@PathVariable Long id, @RequestBody AtualizarPessoaDTO dto) {
        return ResponseEntity.ok(pessoaService.atualizarPessoa(id, dto));
    }

    @PostMapping("/{id}/initiate-face-register")
    public ResponseEntity<String> iniciarCadastroRosto(@PathVariable Long id) {
        return ResponseEntity.ok(pessoaService.iniciarCadastroRosto(id));
    }

    @PostMapping("/register-face")
    public ResponseEntity<Void> salvarFace(@RequestBody SalvarFaceDTO dto) {
        pessoaService.salvarFace(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/faces")
    public ResponseEntity<List<Map<String, Object>>> listarFaces() {
        return ResponseEntity.ok(pessoaService.listarFacesParaPython());
    }

    @PostMapping("/card/read")
    public ResponseEntity<Map<String, Object>> lerCartaoParaCadastro() {
        return ResponseEntity.ok(cardReaderService.iniciarLeitura());
    }

    @PostMapping("/card/stop")
    public ResponseEntity<Map<String, Object>> cancelarLeituraCartao() {
        return ResponseEntity.ok(cardReaderService.pararLeitura());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirPessoa(@PathVariable Long id) {
        pessoaService.excluirPessoa(id);
        return ResponseEntity.noContent().build();
    }
}