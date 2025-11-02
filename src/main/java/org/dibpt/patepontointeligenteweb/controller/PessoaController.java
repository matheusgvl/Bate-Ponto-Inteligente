package org.dibpt.patepontointeligenteweb.controller;

import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import org.dibpt.patepontointeligenteweb.service.PessoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Map<String, Object>> salvarPessoa(@RequestBody Map<String, Object> dados) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String nome = (String) dados.get("nome");
            String cpf = ((String) dados.get("cpf")).replaceAll("[^\\d]", "");
            String rg = (String) dados.get("rg");
            String dataNascimentoStr = (String) dados.get("dataNascimento");
            String funcao = (String) dados.get("funcao");
            String cardUID = (String) dados.get("cardUID");
            
            // Validar dados obrigatórios
            if (nome == null || nome.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Nome é obrigatório");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (cpf == null || cpf.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "CPF é obrigatório");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!pessoaService.validarCpf(cpf)) {
                response.put("success", false);
                response.put("message", "CPF inválido");
                return ResponseEntity.badRequest().body(response);
            }
            
            LocalDate dataNascimento = null;
            if (dataNascimentoStr != null && !dataNascimentoStr.trim().isEmpty()) {
                try {
                    dataNascimento = LocalDate.parse(dataNascimentoStr, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception e) {
                    response.put("success", false);
                    response.put("message", "Data de nascimento inválida");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            Pessoa pessoa = new Pessoa();
            pessoa.setNome(nome.trim());
            pessoa.setCpf(cpf);
            pessoa.setRg(rg != null ? rg.trim() : "");
            pessoa.setDataNascimento(dataNascimento);
            pessoa.setFuncao(funcao != null ? funcao.trim() : "funcionario");
            pessoa.setCardUID(cardUID != null ? cardUID.trim() : null);
            
            Pessoa pessoaSalva = pessoaService.salvarPessoa(pessoa);
            
            response.put("success", true);
            response.put("message", "Pessoa cadastrada com sucesso");
            response.put("pessoa", pessoaSalva);
            
            return ResponseEntity.ok(response);
            
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
            
            response.put("success", true);
            response.put("pessoas", pessoas);
            response.put("total", pessoas.size());
            
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
                response.put("success", true);
                response.put("pessoa", pessoa.get());
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
