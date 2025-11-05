package org.dibpt.patepontointeligenteweb.service;

import org.dibpt.patepontointeligenteweb.Entity.Acesso;
import org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso;
import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import org.dibpt.patepontointeligenteweb.Entity.Local;
import org.dibpt.patepontointeligenteweb.repository.AcessoRepository;
import org.dibpt.patepontointeligenteweb.repository.PessoaRepository;
import org.dibpt.patepontointeligenteweb.repository.LocalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.dibpt.patepontointeligenteweb.DTO.AcessoResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class AcessoService {

    private final AcessoRepository acessoRepository;
    private final PessoaRepository pessoaRepository;
    private final LocalRepository localRepository;

    private static final int AUTH_TIMEOUT_SECONDS = 15;

    @Autowired
    public AcessoService(AcessoRepository acessoRepository, PessoaRepository pessoaRepository, LocalRepository localRepository) {
        this.acessoRepository = acessoRepository;
        this.pessoaRepository = pessoaRepository;
        this.localRepository = localRepository;
    }

    public ResponseEntity<AcessoResponseDTO> registrarAcessoPorCartao(String cardUID, Long localId) {

        Optional<Local> localOpt = localRepository.findById(localId);
        if (localOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AcessoResponseDTO("NEGADO", "Local desconhecido.", null));
        }
        Local local = localOpt.get();

        Optional<Pessoa> pessoaOpt = pessoaRepository.findByCardUID(cardUID.trim());

        if (pessoaOpt.isEmpty()) {
            Acesso falha = new Acesso(TipoAcesso.NEGADO_CARTAO_NAO_ENCONTRADO, null, local);
            acessoRepository.save(falha);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AcessoResponseDTO("NEGADO", "Cartão não cadastrado.", null));
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

        local.setPendingAuthenticationState("AWAITING_FACE");
        local.setPendingUserId(pessoa.getId());
        local.setPendingAuthTimestamp(LocalDateTime.now());
        localRepository.save(local);

        AcessoResponseDTO response = new AcessoResponseDTO("PENDENTE", "Cartão OK. Aproxime o rosto.", pessoa.getNome());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<AcessoResponseDTO> registrarAcessoPorRosto(String nome, Long localId) {

        Optional<Local> localOpt = localRepository.findById(localId);
        if (localOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AcessoResponseDTO("NEGADO", "Local desconhecido.", null));
        }
        Local local = localOpt.get();

        Optional<Pessoa> pessoaOpt = pessoaRepository.findByNome(nome.trim());

        if (pessoaOpt.isEmpty()) {
            Acesso falha = new Acesso(TipoAcesso.NEGADO_ROSTO_NAO_RECONHECIDO, null, local);
            acessoRepository.save(falha);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AcessoResponseDTO("NEGADO", "Rosto não cadastrado.", null));
        }

        Pessoa pessoa = pessoaOpt.get();

        if (local.getPendingAuthenticationState().equals("IDLE")) {
            Acesso falha = new Acesso(TipoAcesso.NEGADO_SEQUENCIA_INVALIDA, pessoa, local);
            acessoRepository.save(falha);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AcessoResponseDTO("NEGADO", "Por favor, aproxime o cartão primeiro.", pessoa.getNome()));
        }

        if (!pessoa.getId().equals(local.getPendingUserId())) {
            Pessoa pessoaDoCartao = pessoaRepository.findById(local.getPendingUserId()).orElse(null);

            Acesso falha = new Acesso(TipoAcesso.NEGADO_ROSTO_NAO_CORRESPONDE, pessoa, local);
            acessoRepository.save(falha);

            resetarEstadoLocal(local);

            String motivo = "Rosto não corresponde ao cartão.";
            if (pessoaDoCartao != null) {
                motivo = "Rosto (" + pessoa.getNome() + ") não corresponde ao cartão de (" + pessoaDoCartao.getNome() + ").";
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AcessoResponseDTO("NEGADO", motivo, pessoa.getNome()));
        }

        long segundosPassados = ChronoUnit.SECONDS.between(local.getPendingAuthTimestamp(), LocalDateTime.now());
        if (segundosPassados > AUTH_TIMEOUT_SECONDS) {
            Acesso falha = new Acesso(TipoAcesso.NEGADO_TIMEOUT, pessoa, local);
            acessoRepository.save(falha);

            resetarEstadoLocal(local);

            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(new AcessoResponseDTO("NEGADO", "Tempo esgotado. Aproxime o cartão novamente.", pessoa.getNome()));
        }

        Acesso sucesso = new Acesso(TipoAcesso.PERMITIDO_2FA, pessoa, local);
        acessoRepository.save(sucesso);

        resetarEstadoLocal(local);

        AcessoResponseDTO response = new AcessoResponseDTO("PERMITIDO", "Acesso 2FA liberado.", pessoa.getNome());
        return ResponseEntity.ok(response);
    }

    private void resetarEstadoLocal(Local local) {
        local.setPendingAuthenticationState("IDLE");
        local.setPendingUserId(null);
        local.setPendingAuthTimestamp(null);
        localRepository.save(local);
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
                TipoAcesso.NEGADO_SEM_PERMISSAO_LOCAL,
                TipoAcesso.NEGADO_SEQUENCIA_INVALIDA,
                TipoAcesso.NEGADO_ROSTO_NAO_CORRESPONDE,
                TipoAcesso.NEGADO_TIMEOUT
        );

        return acessoRepository.countByTipoAcessoInAndDataHoraBetween(tiposNegados, inicioDoDia, fimDoDia);
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
}