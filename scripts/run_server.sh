#!/usr/bin/env bash
# Script para iniciar servidor Python NexoAI

set -e

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "╔════════════════════════════════════════╗"
echo "║     NexoAI Local Server                ║"
echo "║  Comunicação: Android ↔ llama.cpp     ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Validações
echo "[1/3] Validando ambiente..."

if [ ! -f "$ROOT/models/nexoai-model.gguf" ]; then
    echo "✗ Erro: Modelo GGUF não encontrado"
    echo "  Esperado: $ROOT/models/nexoai-model.gguf"
    exit 1
fi
echo "✓ Modelo GGUF: $ROOT/models/nexoai-model.gguf"

if ! command -v python3 &> /dev/null; then
    echo "✗ Erro: Python 3 não encontrado"
    exit 1
fi
echo "✓ Python 3: $(python3 --version)"

# Validação de dependências Python
echo ""
echo "[2/3] Validando módulos Python..."

cd "$ROOT"

# Tenta importar módulos
python3 << EOF
import sys
try:
    import sqlite3
    print("✓ sqlite3 disponível")
except ImportError:
    print("✗ Erro: sqlite3 não disponível")
    sys.exit(1)

try:
    import json
    print("✓ json disponível")
except ImportError:
    print("✗ Erro: json não disponível")
    sys.exit(1)

try:
    import socket
    print("✓ socket disponível")
except ImportError:
    print("✗ Erro: socket não disponível")
    sys.exit(1)

print("\n✓ Todas as dependências Python satisfeitas")
EOF

# Validação dos arquivos
echo ""
echo "[3/3] Validando estrutura do projeto..."

required_files=(
    "src/local_ai_service.py"
    "src/ai_server.py"
    "database/db.py"
    "src/agents/__init__.py"
    "src/agents/prompt_engineer.py"
    "src/agents/developer.py"
    "src/agents/design_director.py"
    "src/agents/web_creator.py"
    "src/agents/marketing_strategist.py"
)

for file in "${required_files[@]}"; do
    if [ ! -f "$ROOT/$file" ]; then
        echo "✗ Erro: Arquivo faltando: $file"
        exit 1
    fi
done

echo "✓ Todos os arquivos necessários encontrados"

echo ""
echo "╔════════════════════════════════════════╗"
echo "║   Iniciando Servidor NexoAI...         ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "Aguardando conexões em:"
echo "  HOST: 0.0.0.0"
echo "  PORT: 9999"
echo ""
echo "Pressione Ctrl+C para parar o servidor"
echo ""

# Executa servidor
cd "$ROOT"
python3 src/ai_server.py
