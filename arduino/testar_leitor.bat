@echo off
echo Testando conectividade do leitor NFC...
echo.

REM Verificar se Python está instalado
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERRO: Python não está instalado ou não está no PATH
    echo Por favor, instale o Python 3.x
    pause
    exit /b 1
)

echo Python detectado: 
python --version

REM Verificar se o módulo serial está instalado
python -c "import serial; print('pyserial version:', serial.VERSION)" 2>nul
if %errorlevel% neq 0 (
    echo.
    echo AVISO: Módulo pyserial não está instalado
    echo Instalando pyserial...
    pip install pyserial
    if %errorlevel% neq 0 (
        echo ERRO: Falha ao instalar pyserial
        pause
        exit /b 1
    )
)

echo.
echo pyserial OK
echo.

REM Verificar se a porta COM5 está disponível
echo Verificando porta COM5...
python -c "import serial; s=serial.Serial('COM5', 115200, timeout=1); s.close(); print('Porta COM5 OK')" 2>nul
if %errorlevel% neq 0 (
    echo.
    echo AVISO: Porta COM5 não está disponível
    echo Verifique se:
    echo - O Arduino está conectado
    echo - O driver está instalado
    echo - A porta está correta
    echo.
    echo Listando portas disponíveis:
    python -c "import serial.tools.list_ports; [print('  -', p.device, p.description) for p in serial.tools.list_ports.comports()]"
    echo.
    pause
) else (
    echo Porta COM5 disponível!
)

echo.
echo Configuração completa!
echo O sistema está pronto para ler cartões NFC.
pause
