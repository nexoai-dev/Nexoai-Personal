 # NexoAI

NexoAI é uma IA pessoal local para criação, design, desenvolvimento e marketing, executando totalmente no dispositivo com um modelo GGUF local.

## Arquitetura

Interface Android
↓
LocalAIService
↓
Motor de inferência local (llama.cpp)
↓
/models/nexoai-model.gguf

## Estrutura do projeto

- /android/NexoAIApp: aplicação Android com Compose
- /src: orquestração e agentes
- /engine/llama.cpp: motor de inferência local
- /models/nexoai-model.gguf: modelo principal da IA
- /database: armazenamento local
- /scripts: automações e build

## Requisitos

- Android SDK + NDK
- Java 17+
- Gradle 8+
- CMake 3.31+

## Instalação

1. Verifique o modelo GGUF em /models/nexoai-model.gguf
2. Configure SDK Android em /opt/android-sdk
3. Execute:

```bash
./scripts/build_android.sh
```

## Validação

- Modelo GGUF encontrado: validado
- Dependências instaladas: Android SDK + NDK + CMake + Java
- Arquitetura compilável: app em Android com JNI para llama.cpp
- Sem chamadas externas obrigatórias: inferência local e armazenamento local

## Observações

Este projeto usa o motor oficial llama.cpp, compilado localmente para Android via CMake e NDK, sem depender de Claude, OpenAI ou tokens externos.
