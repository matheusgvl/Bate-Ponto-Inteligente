package org.dibpt.patepontointeligenteweb.service;

import org.dibpt.patepontointeligenteweb.DTO.PessoaResponseDTO;
import org.dibpt.patepontointeligenteweb.DTO.StatsResponseDTO;
import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import org.dibpt.patepontointeligenteweb.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.dibpt.patepontointeligenteweb.DTO.SalvarPessoaDTO;
import org.dibpt.patepontointeligenteweb.DTO.AtualizarPessoaDTO;
import org.dibpt.patepontointeligenteweb.Entity.ENUM.FuncaoPessoa;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        mapearDadosParaEntidade(pessoa, dto.nome(), cpfLimpo, dto.rg(), dto.dataNascimento(), dto.funcao(), dto.cardUID());

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

    public PessoaResponseDTO buscarDTOPorCartao(String uid) throws Exception {
        Pessoa pessoa = this.buscarPorCartao(uid)
                .orElseThrow(() -> new Exception("Pessoa não encontrada para este cartão"));
        return new PessoaResponseDTO(pessoa);
    }
    
    public List<Pessoa> listarPessoasAtivas() {
        return pessoaRepository.findByAtivoTrue();
    }
    
    public List<Pessoa> buscarPorNome(String nome) {
        return pessoaRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(nome);
    }

    public List<Pessoa> buscarPorFuncao(FuncaoPessoa funcao) {
        return pessoaRepository.findByFuncaoAndAtivoTrue(funcao);
    }
    
    public long contarPessoasAtivas() {
        return pessoaRepository.countPessoasAtivas();
    }
    
    public long contarPorFuncao(FuncaoPessoa funcao) {
        return pessoaRepository.countByFuncaoAndAtivoTrue(funcao);
    }

    public PessoaResponseDTO atualizarPessoa(Long id, AtualizarPessoaDTO dto) throws Exception {
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

        mapearDadosParaEntidade(pessoa, dto.nome(), cpfLimpo, dto.rg(), dto.dataNascimento(), dto.funcao(), dto.cardUID());
        Pessoa pessoaAtualizada = pessoaRepository.save(pessoa);
        return new PessoaResponseDTO(pessoaAtualizada);
    }
    private void mapearDadosParaEntidade(Pessoa pessoa, String nome, String cpf, String rg,
                                         String dataNascimentoStr, String funcao, String cardUID) throws Exception {

        LocalDate dataNascimento = null;
        if (dataNascimentoStr != null && !dataNascimentoStr.trim().isEmpty()) {
            try {
                dataNascimento = LocalDate.parse(dataNascimentoStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                throw new Exception("Data de nascimento inválida");
            }
        }

        String cardUIDLimpo = (cardUID != null) ? cardUID.trim() : null;

        pessoa.setNome(nome.trim());
        pessoa.setCpf(cpf);
        pessoa.setRg(rg != null ? rg.trim() : "");
        pessoa.setDataNascimento(dataNascimento);
        String funcaoString = (funcao != null && !funcao.trim().isEmpty())
                ? funcao.trim()
                : "funcionario";
        FuncaoPessoa funcaoEnum;
        try {
            funcaoEnum = FuncaoPessoa.valueOf(funcaoString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Função inválida fornecida: " + funcaoString);
        }
        pessoa.setFuncao(funcaoEnum);
        pessoa.setCardUID(cardUIDLimpo);
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

    public StatsResponseDTO obterEstatisticas() {
        long totalPessoas = this.contarPessoasAtivas();
        long funcionarios = this.contarPorFuncao(FuncaoPessoa.FUNCIONARIO);
        long supervisores = this.contarPorFuncao(FuncaoPessoa.SUPERVISOR);
        long gerentes = this.contarPorFuncao(FuncaoPessoa.GERENTE);
        long terceirizados = this.contarPorFuncao(FuncaoPessoa.TERCEIRIZADO);

        return new StatsResponseDTO(
                totalPessoas,
                funcionarios,
                supervisores,
                gerentes,
                terceirizados
        );
    }

    public List<PessoaResponseDTO> listarPessoasDTOs() {
        List<Pessoa> pessoas = this.listarPessoasAtivas(); //

        return pessoas.stream()
                .map(PessoaResponseDTO::new)
                .collect(Collectors.toList());
    }
    
    public boolean validarCpf(String cpf) {
        cpf = cpf.replaceAll("[^\\d]", "");
        
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        
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
