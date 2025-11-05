package org.dibpt.patepontointeligenteweb.service;

import org.dibpt.patepontointeligenteweb.DTO.AcessoLogResponseDTO;
import org.dibpt.patepontointeligenteweb.Entity.Acesso;
import org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso;
import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import org.dibpt.patepontointeligenteweb.repository.AcessoRepository;
import org.dibpt.patepontointeligenteweb.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.dibpt.patepontointeligenteweb.DTO.AcessoResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.dibpt.patepontointeligenteweb.Entity.Local;
import org.dibpt.patepontointeligenteweb.repository.LocalRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AcessoService {

    private final AcessoRepository acessoRepository;
    private final PessoaRepository pessoaRepository;
    private final LocalRepository localRepository;

    @Autowired
    public AcessoService(AcessoRepository acessoRepository, PessoaRepository pessoaRepository, LocalRepository localRepository) {
        this.acessoRepository = acessoRepository;
        this.pessoaRepository = pessoaRepository;
        this.localRepository = localRepository;
    }

    public ResponseEntity<AcessoResponseDTO> registrarAcessoPorCartao(String cardUID, Long localId) {
        if (cardUID == null || cardUID.trim().isEmpty()) {
            AcessoResponseDTO response = new AcessoResponseDTO("NEGADO", "UID do cartão não fornecido.", null);
            return ResponseEntity.badRequest().body(response);
        }

        // 1. Busca o Local
        Optional<Local> localOpt = localRepository.findById(localId);
        if (localOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AcessoResponseDTO("NEGADO", "Local desconhecido.", null));
        }
        Local local = localOpt.get();

        Optional<Pessoa> pessoaOpt = pessoaRepository.findByCardUID(cardUID.trim());

        if (pessoaOpt.isEmpty()) {
            Acesso falha = new Acesso(TipoAcesso.NEGADO_CARTAO_NAO_ENCONTRADO, null, local);
            acessoRepository.save(falha);
            AcessoResponseDTO response = new AcessoResponseDTO("NEGADO", "Cartão não cadastrado.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        Pessoa pessoa = pessoaOpt.get();

        Optional<ResponseEntity<AcessoResponseDTO>> inativoResponse = checkPessoaAtiva(pessoa, local);
        if (inativoResponse.isPresent()) {
            return inativoResponse.get();
        }

        if (!pessoa.getLocaisAutorizados().contains(local)) {
            Acesso falha = new Acesso(TipoAcesso.NEGADO_SEM_PERMISSAO_LOCAL, pessoa, local);
            acessoRepository.save(falha);
            AcessoResponseDTO response = new AcessoResponseDTO("NEGADO", "Acesso não autorizado para este local.", pessoa.getNome());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        Acesso sucesso = new Acesso(TipoAcesso.PERMITIDO, pessoa, local);
        acessoRepository.save(sucesso);
        AcessoResponseDTO response = new AcessoResponseDTO("PERMITIDO", null, pessoa.getNome());
        return ResponseEntity.ok(response);
    }

    public long contarAcessosHoje() {
        LocalDateTime inicioDoDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDoDia = LocalDate.now().atTime(23, 59, 59);
        return acessoRepository.countByDataHoraBetween(inicioDoDia, fimDoDia);
    }

    public long contarAcessosNegadosHoje() {
        LocalDateTime inicioDoDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDoDia = LocalDate.now().atTime(23, 59, 59);
        List<TipoAcesso> tiposNegados = List.of(
                TipoAcesso.NEGADO_CARTAO_NAO_ENCONTRADO,
                TipoAcesso.NEGADO_ROSTO_NAO_RECONHECIDO,
                TipoAcesso.NEGADO_PESSOA_INATIVA,
                TipoAcesso.NEGADO_FALHA_LEITURA_CARTAO,
                TipoAcesso.NEGADO_FALHA_DETECCAO_ROSTO,
                TipoAcesso.NEGADO_SEM_PERMISSAO_LOCAL
        );
        return acessoRepository.countByTipoAcessoInAndDataHoraBetween(tiposNegados, inicioDoDia, fimDoDia);
    }

    public ResponseEntity<AcessoResponseDTO> registrarAcessoPorRosto(String nome, Long localId) {
        if (nome == null || nome.trim().isEmpty()) {
            AcessoResponseDTO response = new AcessoResponseDTO("NEGADO", "Nome não fornecido.", null);
            return ResponseEntity.badRequest().body(response);
        }

        Optional<Local> localOpt = localRepository.findById(localId);
        if (localOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AcessoResponseDTO("NEGADO", "Local desconhecido.", null));
        }
        Local local = localOpt.get();

        Optional<Pessoa> pessoaOpt = pessoaRepository.findByNome(nome.trim());

        if (pessoaOpt.isEmpty()) {
            Acesso falha = new Acesso(TipoAcesso.NEGADO_ROSTO_NAO_RECONHECIDO, null, local); //
            acessoRepository.save(falha);
            AcessoResponseDTO response = new AcessoResponseDTO("NEGADO", "Rosto não cadastrado.", null); //
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); //
        }

        Pessoa pessoa = pessoaOpt.get();

        Optional<ResponseEntity<AcessoResponseDTO>> inativoResponse = checkPessoaAtiva(pessoa, local);
        if (inativoResponse.isPresent()) {
            return inativoResponse.get();
        }

        if (!pessoa.getLocaisAutorizados().contains(local)) {
            Acesso falha = new Acesso(TipoAcesso.NEGADO_SEM_PERMISSAO_LOCAL, pessoa, local);
            acessoRepository.save(falha);
            AcessoResponseDTO response = new AcessoResponseDTO("NEGADO", "Acesso não autorizado para este local.", pessoa.getNome());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        Acesso sucesso = new Acesso(TipoAcesso.PERMITIDO_ROSTO, pessoa, local);
        acessoRepository.save(sucesso);
        AcessoResponseDTO response = new AcessoResponseDTO("PERMITIDO", null, pessoa.getNome());
        return ResponseEntity.ok(response);
    }

    private Optional<ResponseEntity<AcessoResponseDTO>> checkPessoaAtiva(Pessoa pessoa, Local local) {
        if (!pessoa.getAtivo()) {
            Acesso falha = new Acesso(TipoAcesso.NEGADO_PESSOA_INATIVA, pessoa, local);
            acessoRepository.save(falha);
            AcessoResponseDTO response = new AcessoResponseDTO("NEGADO", "Usuário inativo.", pessoa.getNome());
            return Optional.of(ResponseEntity.status(HttpStatus.FORBIDDEN).body(response));
        }
        return Optional.empty();
    }
    public List<AcessoLogResponseDTO> listarHistoricoDeAcessos() {
        List<Acesso> ultimosAcessos = acessoRepository.findTop50ByOrderByDataHoraDesc();

        return ultimosAcessos.stream()
                .map(AcessoLogResponseDTO::new)
                .collect(Collectors.toList());
    }
}