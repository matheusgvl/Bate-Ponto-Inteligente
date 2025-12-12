FROM python:3.11-slim

WORKDIR /app

RUN apt-get update && apt-get install -y \
    udev \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .

RUN pip install --no-cache-dir pyserial requests

COPY . .

ENV PYTHONUNBUFFERED=1

CMD ["python", "leitor_serial_web.py"]
