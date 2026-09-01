#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LLAMA_ROOT="$ROOT/engine/llama.cpp"
BUILD_DIR="$LLAMA_ROOT/build"
LLAMA_BIN="$BUILD_DIR/bin/llama-cli"

if [ ! -d "$LLAMA_ROOT" ]; then
  echo "Erro: engine/llama.cpp não encontrado em $LLAMA_ROOT"
  exit 1
fi

if [ ! -f "$ROOT/models/nexoai-model.gguf" ]; then
  echo "Erro: Modelo GGUF não encontrado em $ROOT/models/nexoai-model.gguf"
  exit 1
fi

cmake -S "$LLAMA_ROOT" -B "$BUILD_DIR" \
  -DCMAKE_BUILD_TYPE=Release \
  -DLLAMA_BUILD_COMMON=ON \
  -DLLAMA_BUILD_TESTS=OFF \
  -DLLAMA_BUILD_EXAMPLES=OFF \
  -DLLAMA_BUILD_SERVER=OFF \
  -DLLAMA_BUILD_APP=OFF \
  -DLLAMA_BUILD_TOOLS=ON \
  -DLLAMA_BUILD_UI=OFF
cmake --build "$BUILD_DIR" --config Release --parallel

if [ ! -x "$LLAMA_BIN" ]; then
  echo "Erro: Binário não foi gerado em $LLAMA_BIN"
  exit 1
fi

echo "Motor llama.cpp pronto: $LLAMA_BIN"
