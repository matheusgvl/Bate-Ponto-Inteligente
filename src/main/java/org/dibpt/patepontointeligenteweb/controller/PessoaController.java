package org.dibpt.patepontointeligenteweb.controller;

import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import org.dibpt.patepontointeligenteweb.service.PessoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.dibpt.patepontointeligenteweb.DTO.AtualizarPessoaDTO;
import org.dibpt.patepontointeligenteweb.DTO.PessoaResponseDTO;
import org.dibpt.patepontointeligenteweb.DTO.SalvarPessoaDTO;

import java.net.URI;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<Pessoa> pessoas = pessoaService.listarPessoasAtivas();

        List<PessoaResponseDTO> pessoasDto = pessoas.stream()
                .map(PessoaResponseDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(pessoasDto);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        long totalPessoas = pessoaService.contarPessoasAtivas();
        long funcionarios = pessoaService.contarPorFuncao("funcionario");
        long supervisores = pessoaService.contarPorFuncao("supervisor");
        long gerentes = pessoaService.contarPorFuncao("gerente");
        long terceirizados = pessoaService.contarPorFuncao("terceirizado");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPessoas", totalPessoas);
        stats.put("funcionarios", funcionarios);
        stats.put("supervisores", supervisores);
        stats.put("gerentes", gerentes);
        stats.put("terceirizados", terceirizados);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/search/card/{uid}")
    public ResponseEntity<PessoaResponseDTO> buscarPorCartao(@PathVariable String uid) throws Exception {
        var pessoa = pessoaService.buscarPorCartao(uid)
                .orElseThrow(() -> new Exception("Pessoa não encontrada para este cartão"));

        PessoaResponseDTO pessoaDto = new PessoaResponseDTO(pessoa);
        return ResponseEntity.ok(pessoaDto);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<PessoaResponseDTO> atualizarPessoa(@PathVariable Long id, @RequestBody AtualizarPessoaDTO dto) throws Exception {
        Pessoa pessoaAtualizada = pessoaService.atualizarPessoa(id, dto);
        PessoaResponseDTO responseDto = new PessoaResponseDTO(pessoaAtualizada);
        return ResponseEntity.ok(responseDto);
    }
}