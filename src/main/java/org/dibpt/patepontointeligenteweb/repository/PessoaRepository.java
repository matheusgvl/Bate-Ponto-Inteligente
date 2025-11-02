package org.dibpt.patepontointeligenteweb.repository;

import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
    
    Optional<Pessoa> findByCpf(String cpf);
    
    Optional<Pessoa> findByCardUID(String cardUID);
    
    boolean existsByCpf(String cpf);
    
    boolean existsByCardUID(String cardUID);
    
    List<Pessoa> findByAtivoTrue();
    
    List<Pessoa> findByFuncaoAndAtivoTrue(String funcao);
    
    @Query("SELECT p FROM Pessoa p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%')) AND p.ativo = true")
    List<Pessoa> findByNomeContainingIgnoreCaseAndAtivoTrue(@Param("nome") String nome);
    
    @Query("SELECT COUNT(p) FROM Pessoa p WHERE p.ativo = true")
    long countPessoasAtivas();
    
    @Query("SELECT COUNT(p) FROM Pessoa p WHERE p.funcao = :funcao AND p.ativo = true")
    long countByFuncaoAndAtivoTrue(@Param("funcao") String funcao);
}
