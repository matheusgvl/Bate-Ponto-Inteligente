package org.dibpt.patepontointeligenteweb.repository;

import org.dibpt.patepontointeligenteweb.Entity.Acesso;
import org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AcessoRepository extends JpaRepository<Acesso, Long> {

    long countByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    long countByTipoAcessoInAndDataHoraBetween(List<TipoAcesso> tipos, LocalDateTime inicio, LocalDateTime fim);
}