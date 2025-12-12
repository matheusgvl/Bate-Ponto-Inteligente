package org.dibpt.patepontointeligenteweb.service;

import org.dibpt.patepontointeligenteweb.DTO.LocalRequestDTO;
import org.dibpt.patepontointeligenteweb.DTO.LocalResponseDTO;
import org.dibpt.patepontointeligenteweb.Entity.ENUM.FuncaoPessoa;
import org.dibpt.patepontointeligenteweb.Entity.ENUM.NivelRestricao;
import org.dibpt.patepontointeligenteweb.Entity.Local;
import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import org.dibpt.patepontointeligenteweb.repository.LocalRepository;
import org.dibpt.patepontointeligenteweb.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("null")
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
        
        if (dto.nivelRestricao() != null) {
            try {
                local.setNivelRestricao(NivelRestricao.valueOf(dto.nivelRestricao().toUpperCase()));
            } catch (IllegalArgumentException e) {
                local.setNivelRestricao(NivelRestricao.BAIXO);
            }
        }
        
        if (dto.funcoesAutorizadas() != null) {
            Set<FuncaoPessoa> funcoes = new HashSet<>();
            for (String f : dto.funcoesAutorizadas()) {
                try {
                    funcoes.add(FuncaoPessoa.valueOf(f.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
            local.setFuncoesAutorizadas(funcoes);
        }
        Local localSalvo = localRepository.save(local);
        return new LocalResponseDTO(localSalvo);
    }

    public LocalResponseDTO atualizarLocal(Long id, LocalRequestDTO dto) throws Exception {
        Local local = localRepository.findById(id)
                .orElseThrow(() -> new Exception("Local não encontrado com ID: " + id));

        local.setNome(dto.nome());
        local.setDescricao(dto.descricao());
        local.setCapacidadeMaxima(dto.capacidadeMaxima());
        
        if (dto.nivelRestricao() != null) {
            try {
                local.setNivelRestricao(NivelRestricao.valueOf(dto.nivelRestricao().toUpperCase()));
            } catch (IllegalArgumentException e) {
                local.setNivelRestricao(NivelRestricao.BAIXO);
            }
        }
        
        if (dto.funcoesAutorizadas() != null) {
            local.getFuncoesAutorizadas().clear();
            for (String f : dto.funcoesAutorizadas()) {
                try {
                    local.getFuncoesAutorizadas().add(FuncaoPessoa.valueOf(f.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        Local localSalvo = localRepository.save(local);
        return new LocalResponseDTO(localSalvo);
    }

    public LocalResponseDTO buscarLocalPorId(Long id) throws Exception {
        Local local = localRepository.findById(id)
                .orElseThrow(() -> new Exception("Local não encontrado com ID: " + id));
        return new LocalResponseDTO(local);
    }

    public List<LocalResponseDTO> listarLocaisAtivos() {
        return localRepository.findByAtivoTrue()
                .stream()
                .map(LocalResponseDTO::new)
                .collect(Collectors.toList());
    }

    public void desativarLocal(Long id) throws Exception {
        Local local = localRepository.findById(id)
                .orElseThrow(() -> new Exception("Local não encontrado com ID: " + id));

        local.setAtivo(false);
        localRepository.save(local);
    }


    public List<org.dibpt.patepontointeligenteweb.DTO.PessoaResponseDTO> listarPessoasAutorizadas(Long localId) throws Exception {
        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new Exception("Local não encontrado com ID: " + localId));
        
        return local.getPessoasAutorizadas()
                .stream()
                .map(org.dibpt.patepontointeligenteweb.DTO.PessoaResponseDTO::new)
                .collect(Collectors.toList());
    }

    public void adicionarPermissao(Long localId, Long pessoaId) throws Exception {
        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new Exception("Local não encontrado com ID: " + localId));
        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new Exception("Pessoa não encontrada com ID: " + pessoaId));

        local.getPessoasAutorizadas().add(pessoa);
        localRepository.save(local);
    }

    public void removerPermissao(Long localId, Long pessoaId) throws Exception {
        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new Exception("Local não encontrado com ID: " + localId));
        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new Exception("Pessoa não encontrada com ID: " + pessoaId));

        local.getPessoasAutorizadas().remove(pessoa);
        localRepository.save(local);
    }
}