#!/usr/bin/env python3
"""Serviço local para orquestrar execução do modelo GGUF via llama.cpp."""

import json
import os
import subprocess
import sys
from dataclasses import dataclass, asdict
from pathlib import Path
from typing import List, Optional

MODEL_PATH = Path(__file__).resolve().parents[1] / "models" / "nexoai-model.gguf"
LLAMA_BIN = Path(__file__).resolve().parents[1] / "engine" / "llama.cpp" / "build" / "bin" / "llama-cli"


@dataclass
class ChatMessage:
    role: str
    content: str


@dataclass
class Project:
    name: str
    agents: List[str]
    status: str = "draft"


class LocalAIService:
    def __init__(self, model_path: str = str(MODEL_PATH), llama_bin: str = str(LLAMA_BIN)):
        self.model_path = Path(model_path)
        self.llama_bin = Path(llama_bin)
        self.history: List[ChatMessage] = []
        self.memory: List[str] = []
        self.projects: List[Project] = []

    def ensure_model_exists(self) -> None:
        if not self.model_path.exists():
            raise FileNotFoundError(f"Modelo GGUF não encontrado em: {self.model_path}")

    def generate(self, prompt: str, system_prompt: str = "Você é NexoAI, assistente local de criação, design, desenvolvimento e marketing.") -> str:
        self.ensure_model_exists()
        if not self.llama_bin.exists():
            raise FileNotFoundError(f"Binário llama.cpp não encontrado em: {self.llama_bin}. Execute a build do motor local.")

        command = [
            str(self.llama_bin),
            "-m", str(self.model_path),
            "-p", f"{system_prompt}\n\nUsuário: {prompt}\nAssistente:",
            "-n", "256",
            "-c", "4096",
            "--temp", "0.7",
            "--top-k", "40",
            "--top-p", "0.95",
            "--repeat-penalty", "1.1",
        ]

        result = subprocess.run(command, capture_output=True, text=True, timeout=120)
        if result.returncode != 0:
            raise RuntimeError(f"Falha na inferência local: {result.stderr.strip() or result.stdout.strip()}")

        output = result.stdout.strip()
        self.history.append(ChatMessage("user", prompt))
        self.history.append(ChatMessage("assistant", output))
        self.memory.append(prompt)
        return output

    def load_projects(self, path: str) -> None:
        p = Path(path)
        if p.exists():
            self.projects = [Project(**item) for item in json.loads(p.read_text())]

    def save_projects(self, path: str) -> None:
        Path(path).write_text(json.dumps([asdict(project) for project in self.projects], indent=2))

    def get_history(self) -> List[dict]:
        """Retorna histórico de conversa formatado."""
        return [{"role": msg.role, "content": msg.content} for msg in self.history]

    def get_memory(self) -> List[str]:
        """Retorna memória de contexto."""
        return self.memory

    def add_to_memory(self, context: str) -> None:
        """Adiciona contexto à memória."""
        self.memory.append(context)

    def clear_history(self) -> None:
        """Limpa histórico."""
        self.history = []

    def create_project(self, name: str, agents: List[str]) -> Project:
        """Cria novo projeto."""
        project = Project(name=name, agents=agents)
        self.projects.append(project)
        return project

    def get_agents(self) -> dict:
        """Retorna instâncias de todos os agentes."""
        from agents.prompt_engineer import PromptEngineerAgent
        from agents.web_creator import WebCreatorAgent
        from agents.design_director import DesignDirectorAgent
        from agents.developer import DeveloperAgent
        from agents.marketing_strategist import MarketingStrategistAgent
        
        return {
            "prompt_engineer": PromptEngineerAgent(self),
            "web_creator": WebCreatorAgent(self),
            "design_director": DesignDirectorAgent(self),
            "developer": DeveloperAgent(self),
            "marketing_strategist": MarketingStrategistAgent(self),
        }


if __name__ == "__main__":
    service = LocalAIService()
    service.ensure_model_exists()
    print("✓ NexoAI Local Engine ativo")
    print(f"✓ Modelo: {service.model_path}")
    print(f"✓ Motor: {service.llama_bin}")
    
    agents = service.get_agents()
    print(f"✓ Agentes disponíveis: {', '.join(agents.keys())}")
    
    while True:
        try:
            print("\nOpções:")
            print("  /agent [prompt_engineer|developer|web_creator|design_director|marketing_strategist] <texto>")
            print("  /project <nome> <agentes_separados_por_,>")
            print("  /history")
            print("  /exit")
            
            cmd = input("\nNexoAI> ").strip()
            
            if cmd == "/exit":
                break
            elif cmd.startswith("/agent "):
                parts = cmd[7:].split(" ", 1)
                if len(parts) == 2:
                    agent_name, text = parts
                    if agent_name in agents:
                        print(f"\n{agent_name}:", agents[agent_name].generate(text))
                    else:
                        print("Agente não encontrado")
                else:
                    print("Formato: /agent <agente> <texto>")
            elif cmd.startswith("/project "):
                parts = cmd[9:].split(" ", 1)
                if len(parts) == 2:
                    name, agents_str = parts
                    agents_list = [a.strip() for a in agents_str.split(",")]
                    project = service.create_project(name, agents_list)
                    print(f"Projeto '{project.name}' criado com agentes: {', '.join(agents_list)}")
                else:
                    print("Formato: /project <nome> <agente1,agente2,...>")
            elif cmd == "/history":
                for msg in service.get_history():
                    print(f"{msg['role'].upper()}: {msg['content'][:100]}...")
            elif cmd and not cmd.startswith("/"):
                print("\nNexoAI:", service.generate(cmd))
            
        except KeyboardInterrupt:
            break
        except Exception as e:
            print(f"Erro: {e}")
