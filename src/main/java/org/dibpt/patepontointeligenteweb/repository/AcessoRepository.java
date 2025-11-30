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
           "(:tipoAcesso IS NULL OR a.tipoAcesso = :tipoAcesso)")
    Page<Acesso> findByFiltros(LocalDateTime dataInicio, LocalDateTime dataFim, 
                               Long pessoaId, Long localId, String tipoAcesso, Pageable pageable);
}