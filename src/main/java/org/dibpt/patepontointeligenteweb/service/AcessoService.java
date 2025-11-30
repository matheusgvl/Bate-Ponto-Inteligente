package org.dibpt.patepontointeligenteweb.service;

import org.dibpt.patepontointeligenteweb.DTO.AcessoLogResponseDTO;
import org.dibpt.patepontointeligenteweb.DTO.AcessoResponseDTO;
import org.dibpt.patepontointeligenteweb.Entity.Acesso;
import org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso;
import org.dibpt.patepontointeligenteweb.Entity.Local;
import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import org.dibpt.patepontointeligenteweb.repository.AcessoRepository;
import org.dibpt.patepontointeligenteweb.repository.LocalRepository;
import org.dibpt.patepontointeligenteweb.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class AcessoService {

    private final AcessoRepository acessoRepository;
    private final PessoaRepository pessoaRepository;
    private final LocalRepository localRepository;

    private static final long TEMPO_LIMITE_2FA_SEGUNDOS = 30;

    @Autowired
    public AcessoService(AcessoRepository acessoRepository, PessoaRepository pessoaRepository, LocalRepository localRepository) {
        this.acessoRepository = acessoRepository;
        this.pessoaRepository = pessoaRepository;
        this.localRepository = localRepository;
    }

    public ResponseEntity<AcessoResponseDTO> registrarAcessoPorCartao(String cardUID, Long localId) {
        if (cardUID == null || cardUID.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new AcessoResponseDTO("NEGADO", "UID do cartão não fornecido.", null));
        }

        Optional<Local> localOpt = localRepository.findById(localId);
        if (localOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AcessoResponseDTO("NEGADO", "Local desconhecido.", null));
        }
        Local local = localOpt.get();

        Optional<Pessoa> pessoaOpt = pessoaRepository.findByCardUID(cardUID.trim());
        if (pessoaOpt.isEmpty()) {
            registrarFalha(null, local, TipoAcesso.NEGADO_CARTAO_NAO_ENCONTRADO);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AcessoResponseDTO("NEGADO", "Cartão não cadastrado.", null));
        }

        Pessoa pessoa = pessoaOpt.get();

        Optional<ResponseEntity<AcessoResponseDTO>> erro = validarPessoaELocal(pessoa, local);
        if (erro.isPresent()) return erro.get();

        local.setPendingAuthenticationState("WAITING_FACE");
        local.setPendingUserId(pessoa.getId());
        local.setPendingAuthTimestamp(LocalDateTime.now());
        localRepository.save(local);

        return ResponseEntity.ok(new AcessoResponseDTO("AGUARDANDO_ROSTO", "Cartão aceito. Aproxime o rosto.", pessoa.getNome()));
    }

    public ResponseEntity<AcessoResponseDTO> registrarAcessoPorRosto(String nome, Long localId) {
        if (nome == null || nome.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new AcessoResponseDTO("NEGADO", "Nome não fornecido.", null));
        }

        Optional<Local> localOpt = localRepository.findById(localId);
        if (localOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AcessoResponseDTO("NEGADO", "Local desconhecido.", null));
        }
        Local local = localOpt.get();

        Optional<Pessoa> pessoaOpt = pessoaRepository.findByNome(nome.trim());
        if (pessoaOpt.isEmpty()) {
            resetarEstadoLocal(local);
            registrarFalha(null, local, TipoAcesso.NEGADO_ROSTO_NAO_RECONHECIDO);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AcessoResponseDTO("NEGADO", "Rosto não cadastrado.", null));
        }

        Pessoa pessoaRosto = pessoaOpt.get();

        if (!"WAITING_FACE".equals(local.getPendingAuthenticationState())) {
            registrarFalha(pessoaRosto, local, TipoAcesso.NEGADO_FALHA_DETECCAO_ROSTO);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AcessoResponseDTO("NEGADO", "Passe o cartão primeiro.", null));
        }

        if (!pessoaRosto.getId().equals(local.getPendingUserId())) {
            resetarEstadoLocal(local);
            registrarFalha(pessoaRosto, local, TipoAcesso.NEGADO_ROSTO_NAO_RECONHECIDO);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AcessoResponseDTO("NEGADO", "Rosto não corresponde ao cartão.", null));
        }

        long segundosPassados = ChronoUnit.SECONDS.between(local.getPendingAuthTimestamp(), LocalDateTime.now());
        if (segundosPassados > TEMPO_LIMITE_2FA_SEGUNDOS) {
            resetarEstadoLocal(local);
            registrarFalha(pessoaRosto, local, TipoAcesso.NEGADO_FALHA_LEITURA_CARTAO);
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(new AcessoResponseDTO("NEGADO", "Tempo esgotado. Passe o cartão novamente.", null));
        }

        resetarEstadoLocal(local);
        Acesso sucesso = new Acesso(TipoAcesso.PERMITIDO_2FA, pessoaRosto, local);
        acessoRepository.save(sucesso);

        return ResponseEntity.ok(new AcessoResponseDTO("PERMITIDO", null, pessoaRosto.getNome()));
    }

    public ResponseEntity<Map<String, String>> vincularPessoaAoLocal(Long pessoaId, Long localId) {
        Map<String, String> response = new HashMap<>();
        try {
            Local local = localRepository.findById(localId)
                    .orElseThrow(() -> new RuntimeException("Local não encontrado"));
            Pessoa pessoa = pessoaRepository.findById(pessoaId)
                    .orElseThrow(() -> new RuntimeException("Pessoa não encontrada"));

            if (!local.getPessoasAutorizadas().contains(pessoa)) {
                local.getPessoasAutorizadas().add(pessoa);
                localRepository.save(local);
                response.put("message", "Acesso vinculado com sucesso");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Pessoa já possui acesso a este local");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
        } catch (Exception e) {
            response.put("message", "Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public ResponseEntity<Map<String, String>> desvincularPessoaDoLocal(Long pessoaId, Long localId) {
        Map<String, String> response = new HashMap<>();
        try {
            Local local = localRepository.findById(localId)
                    .orElseThrow(() -> new RuntimeException("Local não encontrado"));
            Pessoa pessoa = pessoaRepository.findById(pessoaId)
                    .orElseThrow(() -> new RuntimeException("Pessoa não encontrada"));

            if (local.getPessoasAutorizadas().contains(pessoa)) {
                local.getPessoasAutorizadas().remove(pessoa);
                localRepository.save(local);
                response.put("message", "Acesso removido com sucesso");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Pessoa não possui acesso a este local");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("message", "Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private Optional<ResponseEntity<AcessoResponseDTO>> validarPessoaELocal(Pessoa pessoa, Local local) {
        if (!pessoa.getAtivo()) {
            registrarFalha(pessoa, local, TipoAcesso.NEGADO_PESSOA_INATIVA);
            return Optional.of(ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AcessoResponseDTO("NEGADO", "Usuário inativo.", pessoa.getNome())));
        }

        boolean cargoAutorizado = local.getFuncoesAutorizadas().contains(pessoa.getFuncao());
        boolean pessoaAutorizada = local.getPessoasAutorizadas().contains(pessoa);

        if (!cargoAutorizado && !pessoaAutorizada) {
            registrarFalha(pessoa, local, TipoAcesso.NEGADO_SEM_PERMISSAO_LOCAL);
            return Optional.of(ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AcessoResponseDTO("NEGADO", "Acesso não autorizado para este local.", pessoa.getNome())));
        }
        return Optional.empty();
    }

    private void registrarFalha(Pessoa pessoa, Local local, TipoAcesso tipo) {
        Acesso falha = new Acesso(tipo, pessoa, local);
        acessoRepository.save(falha);
    }

    private void resetarEstadoLocal(Local local) {
        local.setPendingAuthenticationState("IDLE");
        local.setPendingUserId(null);
        local.setPendingAuthTimestamp(null);
        localRepository.save(local);
    }

    public List<AcessoLogResponseDTO> listarHistoricoDeAcessos() {
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(7);
        return acessoRepository.findByDataHoraAfterOrderByDataHoraDesc(dataLimite)
                .stream()
                .map(AcessoLogResponseDTO::new)
                .collect(Collectors.toList());
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

    public Page<AcessoLogResponseDTO> listarLogsComFiltros(int page, int size, LocalDate data, Long pessoaId, Long localId, String tipoAcesso) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataHora"));
        
        LocalDateTime dataInicio = null;
        LocalDateTime dataFim = null;
        
        if (data != null) {
            dataInicio = data.atStartOfDay();
            dataFim = data.atTime(23, 59, 59);
        }
        
        Page<Acesso> acessos = acessoRepository.findByFiltros(dataInicio, dataFim, pessoaId, localId, tipoAcesso, pageable);
        
        return acessos.map(AcessoLogResponseDTO::new);
    }

    public Map<String, Object> gerarRelatorioAcessos(LocalDate dataInicio, LocalDate dataFim, String periodo) {
        if (dataInicio == null || dataFim == null) {
            LocalDate hoje = LocalDate.now();
            switch (periodo) {
                case "hoje" -> {
                    dataInicio = hoje;
                    dataFim = hoje;
                }
                case "ultima_semana" -> {
                    dataInicio = hoje.minusDays(7);
                    dataFim = hoje;
                }
                case "ultimo_mes" -> {
                    dataInicio = hoje.minusMonths(1);
                    dataFim = hoje;
                }
                case "ultimo_trimestre" -> {
                    dataInicio = hoje.minusMonths(3);
                    dataFim = hoje;
                }
                default -> {
                    dataInicio = hoje.minusMonths(1);
                    dataFim = hoje;
                }
            }
        }
        
        LocalDateTime inicioDateTime = dataInicio.atStartOfDay();
        LocalDateTime fimDateTime = dataFim.atTime(23, 59, 59);
        
        List<Acesso> acessos = acessoRepository.findByDataHoraBetweenOrderByDataHoraDesc(inicioDateTime, fimDateTime);
        
        Map<String, Object> relatorio = new HashMap<>();
        
        List<TipoAcesso> tiposPermitidos = List.of(TipoAcesso.PERMITIDO_2FA);
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
        
        long acessosPermitidos = acessos.stream()
            .filter(a -> tiposPermitidos.contains(a.getTipoAcesso()))
            .count();
            
        long acessosNegados = acessos.stream()
            .filter(a -> tiposNegados.contains(a.getTipoAcesso()))
            .count();
        
        long pessoasUnicas = acessos.stream()
            .filter(a -> a.getPessoa() != null)
            .map(a -> a.getPessoa().getId())
            .distinct()
            .count();
        
        long locaisAcessados = acessos.stream()
            .map(a -> a.getLocal().getId())
            .distinct()
            .count();
        
        Map<String, Long> topPessoas = acessos.stream()
            .filter(a -> a.getPessoa() != null)
            .collect(Collectors.groupingBy(
                a -> a.getPessoa().getNome(),
                Collectors.counting()
            ))
            .entrySet()
            .stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                java.util.LinkedHashMap::new
            ));
        
        Map<String, Long> topLocais = acessos.stream()
            .collect(Collectors.groupingBy(
                a -> a.getLocal().getNome(),
                Collectors.counting()
            ))
            .entrySet()
            .stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                java.util.LinkedHashMap::new
            ));
        
        relatorio.put("periodo", Map.of("inicio", dataInicio, "fim", dataFim));
        relatorio.put("totalAcessos", acessos.size());
        relatorio.put("acessosPermitidos", acessosPermitidos);
        relatorio.put("acessosNegados", acessosNegados);
        relatorio.put("pessoasUnicas", pessoasUnicas);
        relatorio.put("locaisAcessados", locaisAcessados);
        relatorio.put("topPessoas", topPessoas);
        relatorio.put("topLocais", topLocais);
        
        return relatorio;
    }
}