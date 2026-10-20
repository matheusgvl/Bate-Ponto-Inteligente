# 🔐 Smart Control

Sistema de controle de acesso com autenticação 2FA usando cartão NFC + reconhecimento facial.

## 🚀 Iniciar o Projeto

### Pré-requisitos
- Docker e Docker Compose instalados
- Arduino com leitor NFC conectado via USB
- Webcam

### Configuração Inicial

1. **Clone o repositório**
```bash
git clone https://github.com/iOzoki/Bate-Ponto-Inteligente.git
cd Bate-Ponto-Inteligente
```

2. **Configure a porta do Arduino** (opcional)
```bash
# Criar arquivo .env (se necessário ajustar a porta USB)
cp .env.example .env

# Mac: ARDUINO_PORT=/dev/tty.usbserial-XXXX
# Windows: ARDUINO_PORT=COM5
# Linux: ARDUINO_PORT=/dev/ttyUSB0
```

3. **Inicie todos os serviços**
```bash
docker-compose up --build -d
```

4. **Acesse o sistema**
```
http://localhost:8081
```

**Login padrão:**
- Usuário: `admin`
- Senha: `admin`

## �️ Comandos Úteis

```bash
# Ver logs
docker-compose logs -f

# Parar tudo
docker-compose down

# Reiniciar após mudanças
docker-compose restart

# Reconstruir tudo
docker-compose up --build -d
```

## 🏗️ Arquitetura

- **Spring Boot** (8081) - Backend + Frontend
- **MySQL** (3306) - Banco de dados
- **Face Recognition** (5000) - IA reconhecimento facial
- **Arduino NFC** - Leitor de cartões

## � Tecnologias

- Java 25 (LTS) + Spring Boot 3.5.6
- Python + Face Recognition + OpenCV
- MySQL 8.0
- Docker + Docker Compose
- Arduino/ESP32 + RFID PN532
