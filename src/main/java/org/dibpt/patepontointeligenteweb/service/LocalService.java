package org.dibpt.patepontointeligenteweb.service;

import org.dibpt.patepontointeligenteweb.DTO.LocalRequestDTO;
import org.dibpt.patepontointeligenteweb.DTO.LocalResponseDTO;
import org.dibpt.patepontointeligenteweb.DTO.PessoaResponseDTO;
import org.dibpt.patepontointeligenteweb.Entity.ENUM.FuncaoPessoa;
import org.dibpt.patepontointeligenteweb.Entity.Local;
import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import org.dibpt.patepontointeligenteweb.exception.RegraNegocioException;
import org.dibpt.patepontointeligenteweb.repository.LocalRepository;
import org.dibpt.patepontointeligenteweb.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LocalService {

    private final LocalRepository localRepository;
    private final PessoaRepository pessoaRepository;

    @Autowired
    public LocalService(LocalRepository localRepository, PessoaRepository pessoaRepository) {
        this.localRepository = localRepository;
        this.pessoaRepository = pessoaRepository;
    }

    public LocalResponseDTO salvarLocal(LocalRequestDTO dto) {
        Local local = new Local();
        local.setNome(dto.nome());
        local.setDescricao(dto.descricao());
        local.setCapacidadeMaxima(dto.capacidadeMaxima());
        local.setFuncoesAutorizadas(converterFuncoes(dto.funcoesAutorizadas()));

        Local localSalvo = localRepository.save(local);
        return new LocalResponseDTO(localSalvo);
    }

    public LocalResponseDTO atualizarLocal(Long id, LocalRequestDTO dto) {
        Local local = localRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Local não encontrado com ID: " + id));

        local.setNome(dto.nome());
        local.setDescricao(dto.descricao());
        local.setCapacidadeMaxima(dto.capacidadeMaxima());

        if (dto.funcoesAutorizadas() != null) {
            local.setFuncoesAutorizadas(converterFuncoes(dto.funcoesAutorizadas()));
        }

        Local localSalvo = localRepository.save(local);
        return new LocalResponseDTO(localSalvo);
    }

    public LocalResponseDTO buscarLocalPorId(Long id) {
        Local local = localRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Local não encontrado com ID: " + id));
        return new LocalResponseDTO(local);
    }

    public List<LocalResponseDTO> listarLocaisAtivos() {
        return localRepository.findByAtivoTrue()
                .stream()
                .map(LocalResponseDTO::new)
                .collect(Collectors.toList());
    }

    public void desativarLocal(Long id) {
        Local local = localRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Local não encontrado com ID: " + id));

        local.setAtivo(false);
        localRepository.save(local);
    }

    public List<PessoaResponseDTO> listarPessoasAutorizadas(Long localId) {
        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new RegraNegocioException("Local não encontrado com ID: " + localId));

        return local.getPessoasAutorizadas()
                .stream()
                .map(PessoaResponseDTO::new)
                .collect(Collectors.toList());
    }

    public void adicionarPermissao(Long localId, Long pessoaId) {
        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new RegraNegocioException("Local não encontrado com ID: " + localId));
        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new RegraNegocioException("Pessoa não encontrada com ID: " + pessoaId));

        local.getPessoasAutorizadas().add(pessoa);
        localRepository.save(local);
    }

    public void removerPermissao(Long localId, Long pessoaId) {
        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new RegraNegocioException("Local não encontrado com ID: " + localId));
        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new RegraNegocioException("Pessoa não encontrada com ID: " + pessoaId));

        local.getPessoasAutorizadas().remove(pessoa);
        localRepository.save(local);
    }

    private Set<FuncaoPessoa> converterFuncoes(List<String> funcoesNomes) {
        if (funcoesNomes == null) return new HashSet<>();

        Set<FuncaoPessoa> funcoesConvertidas = new HashSet<>();
        for (String nome : funcoesNomes) {
            try {
                funcoesConvertidas.add(FuncaoPessoa.valueOf(nome.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        return funcoesConvertidas;
    }
}