package org.dibpt.patepontointeligenteweb.service;

import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import org.dibpt.patepontointeligenteweb.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.dibpt.patepontointeligenteweb.DTO.SalvarPessoaDTO;
import org.dibpt.patepontointeligenteweb.DTO.AtualizarPessoaDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class PessoaService {
    
    @Autowired
    private PessoaRepository pessoaRepository;

    public Pessoa salvarPessoa(SalvarPessoaDTO dto) throws Exception {
        if (dto.nome() == null || dto.nome().trim().isEmpty()) {
            throw new Exception("Nome é obrigatório");
        }

        if (dto.cpf() == null || dto.cpf().trim().isEmpty()) {
            throw new Exception("CPF é obrigatório");
        }

        String cpfLimpo = dto.cpf().replaceAll("[^\\d]", "");
        if (!validarCpf(cpfLimpo)) {
            throw new Exception("CPF inválido");
        }
        LocalDate dataNascimento = null;
        if (dto.dataNascimento() != null && !dto.dataNascimento().trim().isEmpty()) {
            try {
                dataNascimento = LocalDate.parse(dto.dataNascimento(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                throw new Exception("Data de nascimento inválida");
            }
        }
        if (pessoaRepository.existsByCpf(cpfLimpo)) {
            throw new Exception("CPF já cadastrado no sistema");
        }

        String cardUIDLimpo = (dto.cardUID() != null) ? dto.cardUID().trim() : null;
        if (cardUIDLimpo != null && !cardUIDLimpo.isEmpty()) {
            if (pessoaRepository.existsByCardUID(cardUIDLimpo)) {
                throw new Exception("Cartão já cadastrado para outra pessoa");
            }
        }
        Pessoa pessoa = new Pessoa();
        pessoa.setNome(dto.nome().trim());
        pessoa.setCpf(cpfLimpo);
        pessoa.setRg(dto.rg() != null ? dto.rg().trim() : "");
        pessoa.setDataNascimento(dataNascimento);
        pessoa.setFuncao(dto.funcao() != null ? dto.funcao().trim() : "funcionario");
        pessoa.setCardUID(cardUIDLimpo);

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

    public Pessoa atualizarPessoa(Long id, AtualizarPessoaDTO dto) throws Exception {
        Optional<Pessoa> pessoaExistente = pessoaRepository.findById(id);

        if (pessoaExistente.isEmpty()) {
            throw new Exception("Pessoa não encontrada");
        }

        Pessoa pessoa = pessoaExistente.get();

        String cpfLimpo = dto.cpf().replaceAll("[^\\d]", "");
        if (!pessoa.getCpf().equals(cpfLimpo)) {
            if (pessoaRepository.existsByCpf(cpfLimpo)) {
                throw new Exception("CPF já cadastrado no sistema");
            }
        }

        String cardUIDLimpo = (dto.cardUID() != null) ? dto.cardUID().trim() : null;
        if (cardUIDLimpo != null && !cardUIDLimpo.isEmpty() &&
                !cardUIDLimpo.equals(pessoa.getCardUID())) {
            if (pessoaRepository.existsByCardUID(cardUIDLimpo)) {
                throw new Exception("Cartão já cadastrado para outra pessoa");
            }
        }

        LocalDate dataNascimento = null;
        if (dto.dataNascimento() != null && !dto.dataNascimento().trim().isEmpty()) {
            dataNascimento = LocalDate.parse(dto.dataNascimento(), DateTimeFormatter.ISO_LOCAL_DATE);
        }

        pessoa.setNome(dto.nome().trim());
        pessoa.setCpf(cpfLimpo);
        pessoa.setRg(dto.rg() != null ? dto.rg().trim() : "");
        pessoa.setDataNascimento(dataNascimento);
        pessoa.setFuncao(dto.funcao() != null ? dto.funcao().trim() : "funcionario");
        pessoa.setCardUID(cardUIDLimpo);

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
