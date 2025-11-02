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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    public ResponseEntity<Map<String, Object>> salvarPessoa(@RequestBody SalvarPessoaDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            Pessoa pessoaSalva = pessoaService.salvarPessoa(dto);
            PessoaResponseDTO responseDto = new PessoaResponseDTO(pessoaSalva);
            response.put("success", true);
            response.put("message", "Pessoa cadastrada com sucesso");
            response.put("pessoa", responseDto);
            URI location = URI.create("/api/person/" + responseDto.id());
            return ResponseEntity.created(location).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listarPessoas() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Pessoa> pessoas = pessoaService.listarPessoasAtivas();

            List<PessoaResponseDTO> pessoasDto = pessoas.stream()
                    .map(PessoaResponseDTO::new)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("pessoas", pessoasDto);
            response.put("total", pessoasDto.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        Map<String, Object> response = new HashMap<>();
        
        try {
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
            
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/search/card/{uid}")
    public ResponseEntity<Map<String, Object>> buscarPorCartao(@PathVariable String uid) {
        Map<String, Object> response = new HashMap<>();

        try {
            var pessoa = pessoaService.buscarPorCartao(uid);

            if (pessoa.isPresent()) {
                PessoaResponseDTO pessoaDto = new PessoaResponseDTO(pessoa.get());
                response.put("success", true);
                response.put("pessoa", pessoaDto);
            } else {
                response.put("success", false);
                response.put("message", "Pessoa não encontrada para este cartão");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
