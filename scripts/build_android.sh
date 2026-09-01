#!/usr/bin/env bash
set -e

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "╔════════════════════════════════════════╗"
echo "║     NexoAI Android Builder              ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Validações
echo "[1/5] Validando ambiente..."
if [ ! -f "$ROOT/models/nexoai-model.gguf" ]; then
    echo "✗ Erro: Modelo GGUF não encontrado em $ROOT/models/nexoai-model.gguf"
    exit 1
fi
echo "✓ Modelo GGUF encontrado"

if [ ! -d "$ROOT/engine/llama.cpp" ]; then
    echo "✗ Erro: llama.cpp não encontrado em $ROOT/engine/llama.cpp"
    exit 1
fi
echo "✓ llama.cpp encontrado"

# Verifica Gradle
echo ""
echo "[2/5] Preparando ambiente Android..."
cd "$ROOT/android/NexoAIApp"

if [ ! -f "gradlew" ]; then
    echo "✗ Erro: Gradle Wrapper não encontrado"
    exit 1
fi

chmod +x gradlew
echo "✓ Gradle Wrapper pronto"

# Compilação Python
echo ""
echo "[3/5] Validando módulos Python..."
cd "$ROOT"
python3 -m py_compile src/local_ai_service.py || echo "⚠ Aviso: Alguns módulos Python podem ter problemas"
python3 -m py_compile database/db.py || echo "⚠ Aviso: Database pode ter problemas"
python3 -m py_compile src/ai_server.py || echo "⚠ Aviso: AI Server pode ter problemas"
echo "✓ Módulos Python verificados"

# Build APK
echo ""
echo "[4/5] Compilando aplicativo Android..."
cd "$ROOT/android/NexoAIApp"

echo "  Limpando build anterior..."
./gradlew clean --quiet || true

echo "  Compilando Debug APK..."
./gradlew :app:assembleDebug

echo "✓ Compilação bem-sucedida"

# Validação
echo ""
echo "[5/5] Validando artefatos..."
APK_DEBUG="$ROOT/android/NexoAIApp/app/build/outputs/apk/debug/app-debug.apk"

if [ -f "$APK_DEBUG" ]; then
    echo "✓ APK Debug: $APK_DEBUG"
    ls -lh "$APK_DEBUG"
else
    echo "✗ APK não foi gerado"
    exit 1
fi

echo ""
echo "╔════════════════════════════════════════╗"
echo "║     Build Completo! ✓                   ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "Para instalar no dispositivo:"
echo "  adb install $APK_DEBUG"
echo ""
echo "Para iniciar o servidor Python:"
echo "  python3 $ROOT/src/ai_server.py"
echo ""
