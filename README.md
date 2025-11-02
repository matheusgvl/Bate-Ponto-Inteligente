# 🚀 Sistema de Controle de Acesso Inteligente

## ⚙️ **Configuração do Ambiente**

### 1️⃣ **Java 21:**
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = $env:PATH + ";C:\Program Files\Java\jdk-21\bin"
```

### 2️⃣ **Python + PySerial:**
```bash
# Instalar Python 3.x (se não tiver)
# Instalar dependência para leitura do cartão NFC
pip install pyserial

# Testar configuração do leitor
cd arduino
testar_leitor.bat
```

## 🎯 **Executar a Aplicação**

### **Método 1 - Maven Wrapper (Recomendado):**
```powershell
.\mvnw.cmd spring-boot:run
```

### **Método 2 - Maven Instalado:**
```powershell
mvn spring-boot:run
```

## 🌐 **Acessar o Sistema**
- **URL:** http://localhost:8080
- **Usuário:** david alenor
- **Senha:** 123456

## 🎮 **Funcionalidades**

### ✅ **Sistema de Login**
- Painel do supervisor
- Interface Glass Morphism

### ✅ **Dashboard em Tempo Real**
- Estatísticas de acesso
- Monitoramento ao vivo
- Menu lateral responsivo

### ✅ **Cadastro de Pessoas**
- Formulário completo com validações
- Formatação automática de CPF
- Validação matemática de CPF

### ✅ **Leitura de Cartão NFC** 🆕
- Integração com Arduino via Python
- Suporte UIDs: 63DF* e A979*
- Modal interativo "Aproxime o cartão..."
- Vinculação automática pessoa + cartão
- Banco de dados JPA/Hibernate

## 🔧 **Arquitetura**

### **Frontend:**
- HTML5 + CSS3 + JavaScript ES6+
- Font Awesome 6.0.0
- Design responsivo

### **Backend:**
- Spring Boot 3.5.6
- Java 21
- Maven
- JPA/Hibernate
- H2 Database (dev) / MySQL (prod)

### **Hardware:**
- Arduino + Módulo PN532 NFC
- Comunicação serial COM5 @ 115200bps
- Python 3.x + pyserial

## 🐛 **Troubleshooting**

### **Erro: "Python não encontrado"**
```bash
# Verificar instalação
python --version

# Instalar se necessário
pip install pyserial
```

### **Erro: "Porta COM5 não disponível"**
```bash
# Verificar portas disponíveis
python -c "import serial.tools.list_ports; [print(p.device) for p in serial.tools.list_ports.comports()]"

# Alterar porta em:
# arduino/leitor_serial_web.py -> PORTA = "COM_X"
```

### **Erro: "Cartão não detectado"**
- ✅ Verificar conexão Arduino
- ✅ Usar cartão com UID 63DF* ou A979*
- ✅ Aguardar até 35 segundos
- ✅ Aproximar cartão do leitor PN532

## 📊 **Banco de Dados**

### **H2 Console (Desenvolvimento):**
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:testdb
- User: sa
- Password: (vazio)

### **Tabela pessoas:**
```sql
CREATE TABLE pessoas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(200) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    rg VARCHAR(20) NOT NULL,
    data_nascimento DATE NOT NULL,
    funcao VARCHAR(50) NOT NULL,
    card_uid VARCHAR(20) UNIQUE,
    data_cadastro TIMESTAMP NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);
```

## 🚀 **Deploy**

### **Docker Compose (Produção):**
```bash
docker-compose up -d
```

## 📝 **Logs**

### **Sistema Principal:**
```bash
# Logs do Spring Boot
.\mvnw.cmd spring-boot:run

# Saída esperada:
# "Aguardando leitura do cartão NFC..."
# "Python: {"success": true, "uid": "63DFB001"}"
# "UID detectado via JSON: 63DFB001"
```

### **Leitor NFC:**
```bash
# Teste standalone
cd arduino
python leitor_serial_web.py

# Saída esperada:
# DEBUG: >>> TAG DETECTADA COM SUCESSO! <<<
# DEBUG: UID (Identificador Unico): 63DFB001
# {"success": true, "uid": "63DFB001", "message": "Cartão NFC detectado"}
```

---

## 🎉 **Sistema 100% Funcional!**
- ✅ Login e Dashboard
- ✅ Cadastro de pessoas
- ✅ Leitura de cartão NFC
- ✅ Banco de dados integrado
- ✅ Interface moderna e responsiva

**Desenvolvido com ❤️ usando Spring Boot + Arduino + NFC**
