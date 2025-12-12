# ⚙️ Configuração Rápida

## 📦 O que o Docker faz automaticamente:

✅ Instala e configura MySQL 8.0  
✅ Compila e roda Spring Boot (Java 21 + Maven)  
✅ Instala Python + OpenCV + Face Recognition  
✅ Configura Arduino/NFC com PySerial  
✅ Conecta tudo automaticamente  

## 🚀 Para iniciar:

```bash
# 1. Se necessário, configure a porta USB do Arduino
cp .env.example .env
# Edite .env e ajuste ARDUINO_PORT se necessário

# 2. Inicie tudo
docker-compose up --build -d

# 3. Acesse
http://localhost:8080
```

## 🔌 Portas USB por sistema:

- **Mac**: `/dev/tty.usbserial-XXXX` ou `/dev/cu.usbserial-XXXX`
- **Windows**: `COM3`, `COM4`, `COM5`, etc
- **Linux**: `/dev/ttyUSB0` ou `/dev/ttyACM0`

Para descobrir no Mac/Linux:
```bash
ls /dev/tty.* | grep usb  # Mac
ls /dev/ttyUSB*           # Linux
```

## 📋 Comandos essenciais:

```bash
docker-compose logs -f              # Ver logs
docker-compose logs -f spring-app   # Ver logs do Spring
docker-compose ps                   # Ver status
docker-compose restart              # Reiniciar
docker-compose down                 # Parar tudo
```

## ⚠️ Problemas comuns:

**Erro "port already in use"**: Algo usa a porta 8080
```bash
docker-compose down
# Ou mude a porta no docker-compose.yml
```

**Arduino não conecta**: Ajuste ARDUINO_PORT no .env

**Webcam não funciona no Mac/Linux**: Ajuste permissões do Docker para acessar `/dev/video0`

---

Pronto! Tudo configurado automaticamente! 🎉
