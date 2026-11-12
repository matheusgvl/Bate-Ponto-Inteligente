# Leitor NFC no Windows

Com o Spring Boot no Docker e o Arduino conectado ao Windows, execute na raiz
do projeto:

```powershell
python vc-and-nfc/nfc/servidor_nfc.py
```

Mantenha esse processo aberto enquanto cadastra cartoes. O servidor escuta na
porta 5001 e inicia `leitor_serial_web.py` com o mesmo Python usado para iniciar
o servidor. As dependencias estao em `vc-and-nfc/requirements.txt`.

O Docker Compose configura `NFC_API_URL=http://host.docker.internal:5001` no
Spring. Para executar o Spring diretamente no Windows, o endereco padrao e
`http://127.0.0.1:5001`.

O leitor serial usa COM3 a 9600 baud. Feche o monitor serial da IDE Arduino e
outros programas que estejam usando essa porta antes de iniciar uma leitura.
O antigo conteiner `bate-ponto-nfc` nao deve ser iniciado nesse modo.

Rotas da ponte: `GET /health`, `POST /card/read` (aguarda ate 35 segundos) e
`POST /card/stop` (cancela e libera o leitor). A leitura apenas retorna o UID;
o cadastro da pessoa continua sendo feito pela aplicacao Spring Boot.
