package org.dibpt.patepontointeligenteweb.Entity;

import jakarta.persistence.*;
import org.dibpt.patepontointeligenteweb.Entity.ENUM.FuncaoPessoa;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FuncaoPessoa funcao;

    @Column(unique = true, length = 20)
    private String cardUID;
    
    @Column(nullable = false)
    private LocalDateTime dataCadastro;
    
    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "face_encoding", columnDefinition = "TEXT")
    private String faceEncoding;

    @ManyToMany(mappedBy = "pessoasAutorizadas", fetch = FetchType.LAZY)
    private Set<Local> locaisAutorizados = new HashSet<>();

    public Pessoa() {
        this.dataCadastro = LocalDateTime.now();
    }
    
    public Pessoa(String nome, String cpf, String rg, LocalDate dataNascimento, FuncaoPessoa funcao, String cardUID) {
        this.nome = nome;
        this.cpf = cpf;
        this.rg = rg;
        this.dataNascimento = dataNascimento;
        this.funcao = funcao;
        this.cardUID = cardUID;
        this.dataCadastro = LocalDateTime.now();
        this.ativo = true;
    }
    public FuncaoPessoa getFuncao() {return funcao;}

    public void setFuncao(FuncaoPessoa funcao) { this.funcao = funcao; }
    
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

    public String getFaceEncoding() {
        return faceEncoding;
    }

    public void setFaceEncoding(String faceEncoding) {
        this.faceEncoding = faceEncoding;
    }

    public Set<Local> getLocaisAutorizados() {
        return locaisAutorizados;
    }

    public void setLocaisAutorizados(Set<Local> locaisAutorizados) {
        this.locaisAutorizados = locaisAutorizados;
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
