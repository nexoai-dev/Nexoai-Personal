#!/usr/bin/env python3
"""Servidor de comunicação entre app Android e motor llama.cpp local."""

import json
import socket
import threading
import sys
from pathlib import Path
from typing import Optional

# Adiciona paths
sys.path.insert(0, str(Path(__file__).parent))

from local_ai_service import LocalAIService
from database.db import NexoAIDatabase


class AIServer:
    """Servidor TCP que fornece interface para app Android."""
    
    def __init__(self, host: str = "0.0.0.0", port: int = 9999):
        self.host = host
        self.port = port
        self.service = LocalAIService()
        self.db = NexoAIDatabase()
        self.server_socket = None
        self.running = False

    def start(self) -> None:
        """Inicia o servidor."""
        try:
            self.service.ensure_model_exists()
            
            self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.server_socket.bind((self.host, self.port))
            self.server_socket.listen(5)
            self.running = True
            
            print(f"✓ Servidor NexoAI iniciado em {self.host}:{self.port}")
            print(f"✓ Modelo: {self.service.model_path}")
            print(f"✓ Aguardando conexões...")
            
            while self.running:
                try:
                    client_socket, client_addr = self.server_socket.accept()
                    print(f"✓ Conexão recebida de {client_addr}")
                    
                    # Processa cliente em thread separada
                    client_thread = threading.Thread(
                        target=self.handle_client,
                        args=(client_socket, client_addr)
                    )
                    client_thread.daemon = True
                    client_thread.start()
                    
                except Exception as e:
                    if self.running:
                        print(f"Erro ao aceitar conexão: {e}")
                        
        except Exception as e:
            print(f"Erro ao iniciar servidor: {e}")
            sys.exit(1)

    def handle_client(self, client_socket: socket.socket, client_addr: tuple) -> None:
        """Processa requisições de um cliente."""
        try:
            # Lê a requisição JSON
            data = b""
            while True:
                chunk = client_socket.recv(4096)
                if not chunk:
                    break
                data += chunk
                if b"\n" in data:
                    break
            
            if not data:
                return
            
            # Parseia a requisição
            request_str = data.decode("utf-8").strip()
            request = json.loads(request_str)
            
            prompt = request.get("prompt", "")
            agent = request.get("agent", "general")
            system_prompt = request.get("system_prompt", "")
            
            print(f"[{agent}] Processando: {prompt[:50]}...")
            
            # Gera resposta
            if agent in ["prompt_engineer", "design_director", "developer", "web_creator", "marketing_strategist"]:
                agents = self.service.get_agents()
                if agent in agents:
                    response = agents[agent].generate(prompt)
                else:
                    response = self.service.generate(prompt, system_prompt)
            else:
                response = self.service.generate(prompt, system_prompt)
            
            # Salva no histórico
            session_id = f"{client_addr[0]}_{client_addr[1]}"
            self.db.create_session(session_id, f"Sessão de {client_addr[0]}")
            self.db.add_chat_message(session_id, "user", prompt, agent)
            self.db.add_chat_message(session_id, "assistant", response, agent)
            
            # Envia resposta
            response_str = response + "\n::END::\n"
            client_socket.sendall(response_str.encode("utf-8"))
            print(f"[{agent}] Resposta enviada ({len(response)} caracteres)")
            
        except json.JSONDecodeError as e:
            print(f"Erro ao parsear JSON: {e}")
            try:
                error_response = f"Erro ao parsear requisição: {str(e)}\n::END::\n"
                client_socket.sendall(error_response.encode("utf-8"))
            except:
                pass
        except Exception as e:
            print(f"Erro ao processar cliente: {e}")
            try:
                error_response = f"Erro interno: {str(e)}\n::END::\n"
                client_socket.sendall(error_response.encode("utf-8"))
            except:
                pass
        finally:
            try:
                client_socket.close()
            except:
                pass

    def stop(self) -> None:
        """Para o servidor."""
        self.running = False
        if self.server_socket:
            self.server_socket.close()
        print("Servidor parado.")


def main():
    """Função principal."""
    print("╔════════════════════════════════════════╗")
    print("║        NexoAI Local AI Server          ║")
    print("║  Comunicação: Android ↔ llama.cpp     ║")
    print("╚════════════════════════════════════════╝\n")
    
    server = AIServer(host="0.0.0.0", port=9999)
    
    try:
        server.start()
    except KeyboardInterrupt:
        print("\n\nInterrompido pelo usuário")
        server.stop()
    except Exception as e:
        print(f"Erro: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
