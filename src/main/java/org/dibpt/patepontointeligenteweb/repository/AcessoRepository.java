package org.dibpt.patepontointeligenteweb.repository;

import org.dibpt.patepontointeligenteweb.Entity.Acesso;
import org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface AcessoRepository extends JpaRepository<Acesso, Long> {

    long countByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    long countByTipoAcessoInAndDataHoraBetween(List<TipoAcesso> tipos, LocalDateTime inicio, LocalDateTime fim);

    List<Acesso> findByDataHoraAfterOrderByDataHoraDesc(LocalDateTime data);

    List<Acesso> findByDataHoraBetweenOrderByDataHoraDesc(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT a FROM Acesso a WHERE " +
           "(:dataInicio IS NULL OR a.dataHora >= :dataInicio) AND " +
           "(:dataFim IS NULL OR a.dataHora <= :dataFim) AND " +
           "(:pessoaId IS NULL OR a.pessoa.id = :pessoaId) AND " +
           "(:localId IS NULL OR a.local.id = :localId) AND " +
           "(:tipoAcesso IS NULL OR " +
           "  (:tipoAcesso = 'Permitido' AND a.tipoAcesso = org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso.PERMITIDO_2FA) OR " +
           "  (:tipoAcesso = 'Negado' AND a.tipoAcesso IN (org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso.NEGADO_CARTAO_NAO_ENCONTRADO, " +
           "    org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso.NEGADO_ROSTO_NAO_RECONHECIDO, " +
           "    org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso.NEGADO_FALHA_DETECCAO_ROSTO, " +
           "    org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso.NEGADO_PESSOA_INATIVA, " +
           "    org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso.NEGADO_FALHA_LEITURA_CARTAO, " +
           "    org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso.NEGADO_SEM_PERMISSAO_LOCAL, " +
           "    org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso.NEGADO_SEQUENCIA_INVALIDA, " +
           "    org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso.NEGADO_ROSTO_NAO_CORRESPONDE, " +
           "    org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso.NEGADO_TIMEOUT)))")
    Page<Acesso> findByFiltros(LocalDateTime dataInicio, LocalDateTime dataFim, 
                               Long pessoaId, Long localId, String tipoAcesso, Pageable pageable);
}