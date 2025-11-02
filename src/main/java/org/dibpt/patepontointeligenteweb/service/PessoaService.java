package org.dibpt.patepontointeligenteweb.service;

import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import org.dibpt.patepontointeligenteweb.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PessoaService {
    
    @Autowired
    private PessoaRepository pessoaRepository;
    
    public Pessoa salvarPessoa(Pessoa pessoa) throws Exception {
        if (pessoaRepository.existsByCpf(pessoa.getCpf())) {
            throw new Exception("CPF já cadastrado no sistema");
        }
        
        if (pessoa.getCardUID() != null && !pessoa.getCardUID().isEmpty()) {
            if (pessoaRepository.existsByCardUID(pessoa.getCardUID())) {
                throw new Exception("Cartão já cadastrado para outra pessoa");
            }
        }
        
        return pessoaRepository.save(pessoa);
    }
    
    public Optional<Pessoa> buscarPorId(Long id) {
        return pessoaRepository.findById(id);
    }
    
    public Optional<Pessoa> buscarPorCpf(String cpf) {
        return pessoaRepository.findByCpf(cpf);
    }
    
    public Optional<Pessoa> buscarPorCartao(String cardUID) {
        return pessoaRepository.findByCardUID(cardUID);
    }
    
    public List<Pessoa> listarPessoasAtivas() {
        return pessoaRepository.findByAtivoTrue();
    }
    
    public List<Pessoa> buscarPorNome(String nome) {
        return pessoaRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(nome);
    }
    
    public List<Pessoa> buscarPorFuncao(String funcao) {
        return pessoaRepository.findByFuncaoAndAtivoTrue(funcao);
    }
    
    public long contarPessoasAtivas() {
        return pessoaRepository.countPessoasAtivas();
    }
    
    public long contarPorFuncao(String funcao) {
        return pessoaRepository.countByFuncaoAndAtivoTrue(funcao);
    }
    
    public Pessoa atualizarPessoa(Long id, Pessoa dadosAtualizados) throws Exception {
        Optional<Pessoa> pessoaExistente = pessoaRepository.findById(id);
        
        if (pessoaExistente.isEmpty()) {
            throw new Exception("Pessoa não encontrada");
        }
        
        Pessoa pessoa = pessoaExistente.get();
        
        if (!pessoa.getCpf().equals(dadosAtualizados.getCpf())) {
            if (pessoaRepository.existsByCpf(dadosAtualizados.getCpf())) {
                throw new Exception("CPF já cadastrado no sistema");
            }
        }
        
        if (dadosAtualizados.getCardUID() != null && 
            !dadosAtualizados.getCardUID().equals(pessoa.getCardUID())) {
            if (pessoaRepository.existsByCardUID(dadosAtualizados.getCardUID())) {
                throw new Exception("Cartão já cadastrado para outra pessoa");
            }
        }
        
        pessoa.setNome(dadosAtualizados.getNome());
        pessoa.setCpf(dadosAtualizados.getCpf());
        pessoa.setRg(dadosAtualizados.getRg());
        pessoa.setDataNascimento(dadosAtualizados.getDataNascimento());
        pessoa.setFuncao(dadosAtualizados.getFuncao());
        pessoa.setCardUID(dadosAtualizados.getCardUID());
        
        return pessoaRepository.save(pessoa);
    }
    
    public void desativarPessoa(Long id) throws Exception {
        Optional<Pessoa> pessoa = pessoaRepository.findById(id);
        if (pessoa.isEmpty()) {
            throw new Exception("Pessoa não encontrada");
        }
        
        Pessoa p = pessoa.get();
        p.setAtivo(false);
        pessoaRepository.save(p);
    }
    
    public boolean validarCpf(String cpf) {
        cpf = cpf.replaceAll("[^\\d]", "");
        
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        
        // Calcular primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int digito1 = 11 - (soma % 11);
        if (digito1 >= 10) digito1 = 0;
        
        if (Character.getNumericValue(cpf.charAt(9)) != digito1) {
            return false;
        }
        
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int digito2 = 11 - (soma % 11);
        if (digito2 >= 10) digito2 = 0;
        
        return Character.getNumericValue(cpf.charAt(10)) == digito2;
    }
}
