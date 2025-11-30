package org.dibpt.patepontointeligenteweb.Entity;

import jakarta.persistence.*;
import org.dibpt.patepontointeligenteweb.Entity.ENUM.FuncaoPessoa;
import org.dibpt.patepontointeligenteweb.Entity.ENUM.NivelRestricao;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "locais")
public class Local {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(length = 255, nullable = true)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_restricao", nullable = false)
    private NivelRestricao nivelRestricao = NivelRestricao.BAIXO;

    @Column(nullable = false)
    private Boolean ativo = true;


    @Column(name = "capacidade_maxima")
    private Integer capacidadeMaxima;

    @Column(nullable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "pending_auth_state", nullable = false)
    private String pendingAuthenticationState = "IDLE";

    @Column(name = "pending_auth_timestamp")
    private LocalDateTime pendingAuthTimestamp;

    @Column(name = "pending_user_id")
    private Long pendingUserId;

    @ElementCollection(targetClass = FuncaoPessoa.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "local_funcoes_autorizadas", joinColumns = @JoinColumn(name = "local_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "funcao")
    private Set<FuncaoPessoa> funcoesAutorizadas = new HashSet<>();



    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "local_permissoes",
            joinColumns = @JoinColumn(name = "local_id"),
            inverseJoinColumns = @JoinColumn(name = "pessoa_id")
    )
    private Set<Pessoa> pessoasAutorizadas = new HashSet<>();

    public Local() {
        this.dataCadastro = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public NivelRestricao getNivelRestricao() { return nivelRestricao; }
    public void setNivelRestricao(NivelRestricao nivelRestricao) { this.nivelRestricao = nivelRestricao; }
    public Integer getCapacidadeMaxima() { return capacidadeMaxima; }
    public void setCapacidadeMaxima(Integer capacidadeMaxima) { this.capacidadeMaxima = capacidadeMaxima; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
    public Set<Pessoa> getPessoasAutorizadas() { return pessoasAutorizadas; }
    public void setPessoasAutorizadas(Set<Pessoa> pessoasAutorizadas) { this.pessoasAutorizadas = pessoasAutorizadas; }
    public String getPendingAuthenticationState() { return pendingAuthenticationState; }
    public void setPendingAuthenticationState(String pendingAuthenticationState) { this.pendingAuthenticationState = pendingAuthenticationState; }
    public Long getPendingUserId() { return pendingUserId; }
    public void setPendingUserId(Long pendingUserId) { this.pendingUserId = pendingUserId; }
    public LocalDateTime getPendingAuthTimestamp() { return pendingAuthTimestamp; }
    public void setPendingAuthTimestamp(LocalDateTime pendingAuthTimestamp) { this.pendingAuthTimestamp = pendingAuthTimestamp; }
    public Set<FuncaoPessoa> getFuncoesAutorizadas() {return funcoesAutorizadas;}

    public void setFuncoesAutorizadas(Set<FuncaoPessoa> funcoesAutorizadas) {this.funcoesAutorizadas = funcoesAutorizadas;}
}