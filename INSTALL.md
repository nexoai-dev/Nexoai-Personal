# 📖 Guia de Instalação - NexoAI

## 🚀 Instalação Completa do NexoAI

### Pré-requisitos do Sistema

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install -y \
    python3 python3-pip python3-dev \
    default-jdk-headless gradle cmake \
    build-essential git curl

# Verificar instalações
python3 --version  # Python 3.10+
java -version      # Java 17+
gradle --version   # Gradle 8+
```

### 1. Preparar Modelo GGUF

```bash
# O modelo já deve estar em:
ls -lh /workspaces/Nexoai-Personal/models/nexoai-model.gguf

# Se não existir, copiar:
cp seu_modelo.gguf /workspaces/Nexoai-Personal/models/nexoai-model.gguf

# Validar
file /workspaces/Nexoai-Personal/models/nexoai-model.gguf
# Deve retoriar algo como: data (GGUF binary format)
```

### 2. Configurar Android SDK/NDK

#### Opção A: Android Studio (Recomendado)

1. Baixar Android Studio: https://developer.android.com/studio
2. Instalar e executar
3. Tools → SDK Manager
4. Instalar:
   - Android SDK 34
   - Android NDK r25c (ou mais recente)
   - CMake 3.31.6
5. Anotar caminhos e adicionar ao ~/.bashrc:

```bash
export ANDROID_SDK_ROOT=$HOME/Android/Sdk
export ANDROID_NDK_ROOT=$HOME/Android/Sdk/ndk/25.2.9519653
export ANDROID_HOME=$ANDROID_SDK_ROOT
```

#### Opção B: CLI (Linux)

```bash
# Criar diretório
mkdir -p /opt/android-sdk
cd /opt/android-sdk

# Baixar command-line tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-11076708_latest.zip
rm *.zip

# Instalar SDKs
mkdir -p cmdline-tools/latest
mv cmdline-tools/* cmdline-tools/latest/ || true

export PATH=/opt/android-sdk/cmdline-tools/latest/bin:$PATH

sdkmanager --install "platforms;android-34" "ndk;25.2.9519653" "cmake;3.31.6"

# Adicionar ao ~/.bashrc
echo 'export ANDROID_SDK_ROOT=/opt/android-sdk' >> ~/.bashrc
echo 'export ANDROID_NDK_ROOT=/opt/android-sdk/ndk/25.2.9519653' >> ~/.bashrc
source ~/.bashrc
```

### 3. Validar Estrutura do Projeto

```bash
cd /workspaces/Nexoai-Personal

# Verificar modelo
test -f models/nexoai-model.gguf && echo "✓ Modelo GGUF OK" || echo "✗ Modelo faltando"

# Verificar llama.cpp
test -d engine/llama.cpp && echo "✓ llama.cpp OK" || echo "✗ llama.cpp faltando"

# Verificar Android
test -d android/NexoAIApp && echo "✓ Android OK" || echo "✗ Android faltando"

# Verificar Python
test -f src/local_ai_service.py && echo "✓ LocalAIService OK" || echo "✗ LocalAIService faltando"
test -f src/ai_server.py && echo "✓ AI Server OK" || echo "✗ AI Server faltando"
test -f database/db.py && echo "✓ Database OK" || echo "✗ Database faltando"

# Verificar agentes
for agent in prompt_engineer design_director developer web_creator marketing_strategist; do
    test -f "src/agents/${agent}.py" && echo "✓ $agent OK" || echo "✗ $agent faltando"
done
```

### 4. Compilar Aplicativo Android

```bash
cd /workspaces/Nexoai-Personal

# Método 1: Script automático (RECOMENDADO)
bash scripts/build_android.sh

# Método 2: Manual
cd android/NexoAIApp
./gradlew clean
./gradlew :app:assembleDebug
cd ../..
```

**Resultado esperado:**
```
BUILD SUCCESSFUL

✓ APK Debug: android/NexoAIApp/app/build/outputs/apk/debug/app-debug.apk
```

### 5. Instalar no Dispositivo Android

#### Via USB

```bash
# Conectar dispositivo Android com "Depuração USB" ativada

# Verificar conexão
adb devices
# Deve listar o dispositivo

# Instalar APK
adb install android/NexoAIApp/app/build/outputs/apk/debug/app-debug.apk

# Confirmar instalação
adb shell pm list packages | grep nexoai
```

#### Via Emulador

```bash
# Criar emulador (se não existir)
emulator -list-avds
emulator -avd <nome_avd> &

# Aguardar boot
sleep 30

# Instalar APK
adb install android/NexoAIApp/app/build/outputs/apk/debug/app-debug.apk
```

### 6. Validar Python & Dependências

```bash
cd /workspaces/Nexoai-Personal

# Testar imports
python3 << 'EOF'
import sys
import json
import sqlite3
import socket
import subprocess
from pathlib import Path

print("✓ json OK")
print("✓ sqlite3 OK")
print("✓ socket OK")
print("✓ subprocess OK")
print("✓ pathlib OK")

# Verificar arquivos
files = [
    "src/local_ai_service.py",
    "src/ai_server.py",
    "database/db.py",
    "models/nexoai-model.gguf"
]

for f in files:
    p = Path(f)
    status = "✓" if p.exists() else "✗"
    print(f"{status} {f}")
EOF
```

### 7. Iniciar Servidor Python

```bash
cd /workspaces/Nexoai-Personal

# Método 1: Script automático (RECOMENDADO)
bash scripts/run_server.sh

# Método 2: Manual
python3 src/ai_server.py

# Resultado esperado:
# ╔════════════════════════════════════════╗
# ║        NexoAI Local AI Server          ║
# ║  Comunicação: Android ↔ llama.cpp     ║
# ╚════════════════════════════════════════╝
# 
# ✓ Servidor NexoAI iniciado em 0.0.0.0:9999
# ✓ Modelo: /workspaces/Nexoai-Personal/models/nexoai-model.gguf
# ✓ Aguardando conexões...
```

### 8. Testar Conexão

#### Terminal 1: Servidor
```bash
cd /workspaces/Nexoai-Personal
python3 src/ai_server.py
```

#### Terminal 2: Teste de conexão
```bash
python3 << 'EOF'
import socket
import json

# Conectar ao servidor
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(("localhost", 9999))

# Enviar requisição
request = {
    "prompt": "Olá, como você funciona?",
    "agent": "general",
    "system_prompt": "Você é NexoAI"
}

s.sendall(json.dumps(request).encode("utf-8") + b"\n")

# Receber resposta
response = b""
while True:
    chunk = s.recv(4096)
    if not chunk:
        break
    response += chunk
    if b"::END::" in response:
        break

s.close()

# Mostrar resultado
text = response.decode("utf-8").strip()
print("✓ Servidor respondeu!")
print(text[:200] + "...")
EOF
```

### 9. Usar o App

1. **Abrir NexoAI** no dispositivo Android
2. **Aguardar conexão** ao servidor (primeira vez pode demorar)
3. **Selecionar agente** (📝 Prompt, 🎨 Design, 💻 Dev, 🌐 Web, 📊 Marketing)
4. **Digitar mensagem** e enviar
5. **Aguardar resposta** do motor de IA

## ✅ Checklist de Validação

- [ ] Python 3.10+ instalado
- [ ] Java 17+ instalado
- [ ] Gradle 8+ instalado
- [ ] Android SDK 34+ configurado
- [ ] Android NDK r25+ configurado
- [ ] CMake 3.31+ configurado
- [ ] Modelo GGUF em `/models/nexoai-model.gguf`
- [ ] llama.cpp em `/engine/llama.cpp`
- [ ] Arquivo `AndroidManifest.xml` presente
- [ ] `build.gradle.kts` com dependências
- [ ] `src/ai_server.py` executável
- [ ] `database/db.py` válido
- [ ] Agentes Python (5 arquivos) presentes
- [ ] APK compilada com sucesso
- [ ] APK instalada no dispositivo
- [ ] Servidor Python iniciando sem erros
- [ ] App se conecta ao servidor
- [ ] Resposta do modelo recebida no app

## 🆘 Troubleshooting

### Erro: "Android SDK não encontrado"
```bash
export ANDROID_SDK_ROOT=/seu/caminho/android-sdk
export ANDROID_NDK_ROOT=/seu/caminho/android-sdk/ndk/25.2.9519653
./gradlew sync
```

### Erro: "CMake version 3.31.6 or higher is required"
```bash
sdkmanager "cmake;3.31.6"
```

### Erro: "Gradle Wrapper: Permission denied"
```bash
chmod +x android/NexoAIApp/gradlew
```

### Erro: "Connection refused" (9999)
1. Servidor não está rodando
2. Firewall bloqueando porta 9999
3. App e servidor não na mesma rede

### Erro: "Modelo GGUF não encontrado"
```bash
ls -lh /workspaces/Nexoai-Personal/models/
# Deve conter nexoai-model.gguf
```

### APK não instala
```bash
adb uninstall com.nexoai.app
adb install android/NexoAIApp/app/build/outputs/apk/debug/app-debug.apk
```

## 📊 Verificar Logs

### Logs do Android
```bash
adb logcat | grep NexoAI
adb logcat -c  # Limpar
```

### Logs do Servidor Python
```bash
# Terminal onde o servidor está rodando
# Ctrl+C para parar

# Ou criar arquivo de log:
python3 src/ai_server.py 2>&1 | tee server.log
```

## 🎯 Próximos Passos

1. ✅ Instalação completa
2. ✅ Servidor Python rodando
3. ✅ App Android instalado
4. ✅ Primeira conversa com IA

Agora você pode:
- Usar os 5 agentes especializados
- Salvar histórico de conversas
- Gerenciar projetos
- Adicionar memória/contexto

---

**Suporte**: Verifique logs e troubleshooting acima

v1.0 | 2025
