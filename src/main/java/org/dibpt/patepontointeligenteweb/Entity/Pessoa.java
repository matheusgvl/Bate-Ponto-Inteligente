package org.dibpt.patepontointeligenteweb.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pessoas")
public class Pessoa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String nome;
    
    @Column(nullable = false, unique = true, length = 14)
    private String cpf;
    
    @Column(nullable = false, length = 20)
    private String rg;
    
    @Column(nullable = false)
    private LocalDate dataNascimento;
    
    @Column(nullable = false, length = 50)
    private String funcao;
    
    @Column(unique = true, length = 20)
    private String cardUID;
    
    @Column(nullable = false)
    private LocalDateTime dataCadastro;
    
    @Column(nullable = false)
    private Boolean ativo = true;
    
    public Pessoa() {
        this.dataCadastro = LocalDateTime.now();
    }
    
    public Pessoa(String nome, String cpf, String rg, LocalDate dataNascimento, String funcao, String cardUID) {
        this.nome = nome;
        this.cpf = cpf;
        this.rg = rg;
        this.dataNascimento = dataNascimento;
        this.funcao = funcao;
        this.cardUID = cardUID;
        this.dataCadastro = LocalDateTime.now();
        this.ativo = true;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getCpf() {
        return cpf;
    }
    
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    
    public String getRg() {
        return rg;
    }
    
    public void setRg(String rg) {
        this.rg = rg;
    }
    
    public LocalDate getDataNascimento() {
        return dataNascimento;
    }
    
    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }
    
    public String getFuncao() {
        return funcao;
    }
    
    public void setFuncao(String funcao) {
        this.funcao = funcao;
    }
    
    public String getCardUID() {
        return cardUID;
    }
    
    public void setCardUID(String cardUID) {
        this.cardUID = cardUID;
    }
    
    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    public Boolean getAtivo() {
        return ativo;
    }
    
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    
    @Override
    public String toString() {
        return "Pessoa{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", funcao='" + funcao + '\'' +
                ", cardUID='" + cardUID + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
