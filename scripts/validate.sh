#!/usr/bin/env bash
# Script para validar instalação do NexoAI

set -e

# Obtém o diretório raiz - deve ser o pai de 'scripts'
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(dirname "$SCRIPT_DIR")"

# Garantir que estamos no diretório correto
cd "$ROOT"

echo "╔════════════════════════════════════════╗"
echo "║    Validação de Instalação NexoAI      ║"
echo "╚════════════════════════════════════════╝"
echo "Diretório raiz: $ROOT"
echo ""

ERRORS=0
WARNINGS=0

# Função para verificar
check() {
    local name=$1
    local cmd=$2
    local required=${3:-1}
    
    if eval "$cmd" > /dev/null 2>&1; then
        echo "✓ $name"
        return 0
    else
        if [ $required -eq 1 ]; then
            echo "✗ $name (REQUERIDO)"
            ERRORS=$((ERRORS + 1))
        else
            echo "⚠ $name (OPCIONAL)"
            WARNINGS=$((WARNINGS + 1))
        fi
        return 1
    fi
}

# 1. PYTHON
echo "[1/6] Validando Python..."
check "Python 3" "python3 --version" 1
check "Python sqlite3" "python3 -c 'import sqlite3'" 1
check "Python json" "python3 -c 'import json'" 1
check "Python socket" "python3 -c 'import socket'" 1
check "Python subprocess" "python3 -c 'import subprocess'" 1

# 2. JAVA & GRADLE
echo ""
echo "[2/6] Validando Java & Gradle..."
check "Java" "java -version" 1
check "Gradle" "gradle --version" 1

# 3. MODELO GGUF
echo ""
echo "[3/6] Validando Modelo..."
MODEL_PATH="$ROOT/models/nexoai-model.gguf"
if [ -f "$MODEL_PATH" ]; then
    SIZE=$(du -h "$MODEL_PATH" | cut -f1)
    echo "✓ Modelo GGUF encontrado ($SIZE)"
else
    echo "✗ Modelo GGUF não encontrado (REQUERIDO)"
    echo "  Esperado: $MODEL_PATH"
    ERRORS=$((ERRORS + 1))
fi

# 4. ARQUIVOS NECESSÁRIOS
echo ""
echo "[4/6] Validando Arquivos..."

files=(
    "src/local_ai_service.py"
    "src/ai_server.py"
    "database/db.py"
    "android/NexoAIApp/app/src/main/java/com/nexoai/app/MainActivity.kt"
    "android/NexoAIApp/app/src/main/AndroidManifest.xml"
    "android/NexoAIApp/app/build.gradle.kts"
)

for file in "${files[@]}"; do
    if [ -f "$ROOT/$file" ]; then
        echo "✓ $file"
    else
        echo "✗ $file (FALTANDO)"
        ERRORS=$((ERRORS + 1))
    fi
done

# 5. AGENTES
echo ""
echo "[5/6] Validando Agentes..."

agents=(
    "src/agents/prompt_engineer.py"
    "src/agents/design_director.py"
    "src/agents/developer.py"
    "src/agents/web_creator.py"
    "src/agents/marketing_strategist.py"
)

for agent in "${agents[@]}"; do
    if [ -f "$ROOT/$agent" ]; then
        echo "✓ $agent"
    else
        echo "✗ $agent (FALTANDO)"
        ERRORS=$((ERRORS + 1))
    fi
done

# 6. GRADLE WRAPPER
echo ""
echo "[6/6] Validando Android..."

GRADLE_WRAPPER="$ROOT/android/NexoAIApp/gradlew"
if [ -f "$GRADLE_WRAPPER" ]; then
    echo "✓ Gradle Wrapper"
    
    # Tentar sync
    cd "$ROOT/android/NexoAIApp"
    if ./gradlew help --quiet > /dev/null 2>&1; then
        echo "✓ Gradle funcional"
    else
        echo "⚠ Gradle pode ter problemas"
        WARNINGS=$((WARNINGS + 1))
    fi
else
    echo "✗ Gradle Wrapper não encontrado"
    ERRORS=$((ERRORS + 1))
fi

# SDK/NDK (Optional)
echo ""
echo "[OPCIONAL] SDK & NDK..."
check "ANDROID_SDK_ROOT definida" "test -n \"\$ANDROID_SDK_ROOT\"" 0
check "ANDROID_NDK_ROOT definida" "test -n \"\$ANDROID_NDK_ROOT\"" 0

# Resumo
echo ""
echo "╔════════════════════════════════════════╗"

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo "║   ✓ Validação Completa - OK!         ║"
    echo "╚════════════════════════════════════════╝"
    echo ""
    echo "✓ NexoAI está pronto para usar!"
    echo ""
    echo "Próximos passos:"
    echo "  1. Compilar: bash scripts/build_android.sh"
    echo "  2. Servidor: bash scripts/run_server.sh"
    echo "  3. Instalar: adb install android/.../app-debug.apk"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo "║   ⚠ Validação com Avisos              ║"
    echo "╚════════════════════════════════════════╝"
    echo ""
    echo "⚠ $WARNINGS aviso(s)"
    echo "Você pode continuar, mas algumas features podem não funcionar"
    exit 0
else
    echo "║   ✗ Erros Encontrados ($ERRORS)      ║"
    echo "╚════════════════════════════════════════╝"
    echo ""
    echo "✗ $ERRORS erro(s) impedem a continuação"
    echo ""
    echo "Consulte INSTALL.md para instruções"
    exit 1
fi
