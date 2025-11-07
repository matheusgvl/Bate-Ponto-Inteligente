package org.dibpt.patepontointeligenteweb.repository;

import org.dibpt.patepontointeligenteweb.Entity.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LocalRepository extends JpaRepository<Local, Long> {

    List<Local> findByAtivoTrue();

}