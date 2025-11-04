package org.dibpt.patepontointeligenteweb.controller;

import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import org.dibpt.patepontointeligenteweb.service.PessoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.dibpt.patepontointeligenteweb.DTO.AtualizarPessoaDTO;
import org.dibpt.patepontointeligenteweb.DTO.PessoaResponseDTO;
import org.dibpt.patepontointeligenteweb.DTO.SalvarPessoaDTO;
import org.dibpt.patepontointeligenteweb.DTO.StatsResponseDTO;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/person")
@CrossOrigin(origins = "*")
public class PessoaController {

    @Autowired
    private PessoaService pessoaService;

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirPessoa(@PathVariable Long id) throws Exception {
        pessoaService.excluirPessoa(id);
        return ResponseEntity.noContent().build();
    }
}