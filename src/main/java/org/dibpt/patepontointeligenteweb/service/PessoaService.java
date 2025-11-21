package org.dibpt.patepontointeligenteweb.service;

import org.dibpt.patepontointeligenteweb.DTO.*;
import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import org.dibpt.patepontointeligenteweb.Entity.ENUM.FuncaoPessoa;
import org.dibpt.patepontointeligenteweb.exception.RegraNegocioException;
import org.dibpt.patepontointeligenteweb.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final AcessoService acessoService;
    private final RestTemplate restTemplate;

    @Autowired
    public PessoaService(PessoaRepository pessoaRepository, AcessoService acessoService, RestTemplate restTemplate) {
        this.pessoaRepository = pessoaRepository;
        this.acessoService = acessoService;
        this.restTemplate = restTemplate;
    }

    public Pessoa salvarPessoa(SalvarPessoaDTO dto) {
        validarDadosObrigatorios(dto);

        String cpfLimpo = dto.cpf().replaceAll("\\D", "");
        if (!validarCpf(cpfLimpo)) {
            throw new RegraNegocioException("CPF inválido");
        }

        if (pessoaRepository.existsByCpf(cpfLimpo)) {
            throw new RegraNegocioException("CPF já cadastrado no sistema");
        }

        String cardUIDLimpo = (dto.cardUID() != null) ? dto.cardUID().trim() : null;
        if (cardUIDLimpo != null && !cardUIDLimpo.isEmpty()) {
            if (pessoaRepository.existsByCardUID(cardUIDLimpo)) {
                throw new RegraNegocioException("Cartão já cadastrado para outra pessoa");
            }
        }

        Pessoa pessoa = new Pessoa();
        mapearDadosParaEntidade(pessoa, dto.nome(), cpfLimpo, dto.rg(), dto.dataNascimento(), dto.funcao(), dto.cardUID());

        return pessoaRepository.save(pessoa);
    }

    public PessoaResponseDTO atualizarPessoa(Long id, AtualizarPessoaDTO dto) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Pessoa não encontrada"));

        String cpfLimpo = dto.cpf().replaceAll("\\D", "");
        if (!pessoa.getCpf().equals(cpfLimpo)) {
            if (pessoaRepository.existsByCpf(cpfLimpo)) {
                throw new RegraNegocioException("CPF já cadastrado no sistema");
            }
        }

        String cardUIDLimpo = (dto.cardUID() != null) ? dto.cardUID().trim() : null;
        if (cardUIDLimpo != null && !cardUIDLimpo.isEmpty() && !cardUIDLimpo.equals(pessoa.getCardUID())) {
            if (pessoaRepository.existsByCardUID(cardUIDLimpo)) {
                throw new RegraNegocioException("Cartão já cadastrado para outra pessoa");
            }
        }

        mapearDadosParaEntidade(pessoa, dto.nome(), cpfLimpo, dto.rg(), dto.dataNascimento(), dto.funcao(), dto.cardUID());
        Pessoa pessoaAtualizada = pessoaRepository.save(pessoa);
        return new PessoaResponseDTO(pessoaAtualizada);
    }

    public String iniciarCadastroRosto(Long id) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Pessoa não encontrada com ID: " + id));

        Map<String, Object> request = new HashMap<>();
        request.put("id", pessoa.getId());
        request.put("nome", pessoa.getNome());

        String pythonApiUrl = "http://localhost:5000/register";

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(pythonApiUrl, request, String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RegraNegocioException("Erro ao comunicar com o serviço de visão: " + e.getMessage());
        }
    }

    public void salvarFace(SalvarFaceDTO dto) {
        Pessoa pessoa = pessoaRepository.findById(dto.id())
                .orElseThrow(() -> new RegraNegocioException("Pessoa não encontrada para o ID: " + dto.id()));

        pessoa.setFaceEncoding(dto.encoding());
        pessoaRepository.save(pessoa);
    }

    public void excluirPessoa(Long id) {
        if (!pessoaRepository.existsById(id)) {
            throw new RegraNegocioException("Pessoa não encontrada");
        }
        pessoaRepository.deleteById(id);
    }

    public PessoaResponseDTO buscarDTOPorCartao(String uid) {
        Pessoa pessoa = pessoaRepository.findByCardUID(uid)
                .orElseThrow(() -> new RegraNegocioException("Pessoa não encontrada para este cartão"));
        return new PessoaResponseDTO(pessoa);
    }

    public List<PessoaResponseDTO> listarPessoasDTOs() {
        return pessoaRepository.findByAtivoTrue().stream()
                .map(PessoaResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> listarFacesParaPython() {
        return pessoaRepository.findAll().stream()
                .filter(pessoa -> pessoa.getFaceEncoding() != null && !pessoa.getFaceEncoding().isEmpty())
                .map(pessoa -> {
                    Map<String, Object> faceData = new HashMap<>();
                    faceData.put("nome", pessoa.getNome());
                    faceData.put("encoding", pessoa.getFaceEncoding());
                    return faceData;
                })
                .collect(Collectors.toList());
    }

    public StatsResponseDTO obterEstatisticas() {
        long totalPessoas = pessoaRepository.countPessoasAtivas();
        long funcionarios = pessoaRepository.countByFuncaoAndAtivoTrue(FuncaoPessoa.FUNCIONARIO);
        long supervisores = pessoaRepository.countByFuncaoAndAtivoTrue(FuncaoPessoa.SUPERVISOR);
        long gerentes = pessoaRepository.countByFuncaoAndAtivoTrue(FuncaoPessoa.GERENTE);
        long terceirizados = pessoaRepository.countByFuncaoAndAtivoTrue(FuncaoPessoa.TERCEIRIZADO);
        long acessosHoje = acessoService.contarAcessosHoje();
        long acessosNegados = acessoService.contarAcessosNegadosHoje();

        return new StatsResponseDTO(totalPessoas, funcionarios, supervisores, gerentes, terceirizados, acessosHoje, acessosNegados);
    }

    private void validarDadosObrigatorios(SalvarPessoaDTO dto) {
        if (dto.nome() == null || dto.nome().trim().isEmpty()) {
            throw new RegraNegocioException("Nome é obrigatório");
        }
        if (dto.cpf() == null || dto.cpf().trim().isEmpty()) {
            throw new RegraNegocioException("CPF é obrigatório");
        }
    }

    private void mapearDadosParaEntidade(Pessoa pessoa, String nome, String cpf, String rg, String dataNascimentoStr, String funcao, String cardUID) {
        LocalDate dataNascimento = null;
        if (dataNascimentoStr != null && !dataNascimentoStr.trim().isEmpty()) {
            try {
                dataNascimento = LocalDate.parse(dataNascimentoStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                throw new RegraNegocioException("Data de nascimento inválida");
            }
        }

        String cardUIDLimpo = (cardUID != null) ? cardUID.trim() : null;

        pessoa.setNome(nome.trim());
        pessoa.setCpf(cpf);
        pessoa.setRg(rg != null ? rg.trim() : "");
        pessoa.setDataNascimento(dataNascimento);

        String funcaoString = (funcao != null && !funcao.trim().isEmpty()) ? funcao.trim() : "funcionario";
        try {
            pessoa.setFuncao(FuncaoPessoa.valueOf(funcaoString.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException("Função inválida fornecida: " + funcaoString);
        }

        pessoa.setCardUID(cardUIDLimpo);
    }

    private boolean validarCpf(String cpf) {
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false;
        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            int digito1 = 11 - (soma % 11);
            if (digito1 >= 10) digito1 = 0;
            if (Character.getNumericValue(cpf.charAt(9)) != digito1) return false;

            soma = 0;
            for (int i = 0; i < 10; i++) soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            int digito2 = 11 - (soma % 11);
            if (digito2 >= 10) digito2 = 0;

            return Character.getNumericValue(cpf.charAt(10)) == digito2;
        } catch (Exception e) {
            return false;
        }
    }
}