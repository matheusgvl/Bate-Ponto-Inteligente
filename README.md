graph TD
    User((Frontend / Usuário))
    PyScriptNFC[[Python: Leitor NFC]]
    PyScriptFace[[Python: Câmera]]

    subgraph Controller_Layer [Controllers - API REST]
        PC[PessoaController]
        LC[LocalController]
        AC[AcessoController]
    end

    subgraph DTO_Layer [DTOs - O que entra e sai]
        InputDTOs[SalvarPessoaDTO<br>LocalRequestDTO<br>Map JSON]
        OutputDTOs[PessoaResponseDTO<br>LocalResponseDTO<br>AcessoResponseDTO]
    end

    subgraph Service_Layer [Services - Lógica]
        PS[PessoaService]
        LS[LocalService]
        AS[AcessoService]
    end

    subgraph Entity_Layer [Entities - Tabelas]
        E_Pessoa[(Pessoa)]
        E_Local[(Local)]
        E_Acesso[(Acesso)]
    end

    User -->|Envia JSON| PC & LC & AC
    PyScriptNFC -->|POST /registrar/cartao| AC
    PyScriptFace -->|POST /registrar/rosto| AC

    PC -->|Chama| PS
    LC -->|Chama| LS
    AC -->|Chama| AS

    PC & LC & AC -.->|Recebe| InputDTOs
    PC & LC & AC -.->|Retorna| OutputDTOs

    PS <-->|Lê/Grava| E_Pessoa
    LS <-->|Lê/Grava| E_Local
    AS -->|Busca| E_Pessoa
    AS -->|Busca| E_Local
    AS -->|Cria| E_Acesso

    E_Local <-->|ManyToMany<br>PessoasAutorizadas| E_Pessoa
    E_Acesso -->|ManyToOne| E_Pessoa
    E_Acesso -->|ManyToOne| E_Local

    style E_Pessoa fill:#f9f,stroke:#333,stroke-width:2px
    style E_Local fill:#bbf,stroke:#333,stroke-width:2px
    style E_Acesso fill:#bfb,stroke:#333,stroke-width:2px
