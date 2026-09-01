#!/usr/bin/env python3
"""Database local para histórico, projetos e configurações do NexoAI."""

import sqlite3
import json
from datetime import datetime
from pathlib import Path
from typing import List, Dict, Any, Optional

DB_PATH = Path(__file__).resolve().parent / "nexoai.db"


class NexoAIDatabase:
    """Gerenciador de banco de dados local SQLite."""

    def __init__(self, db_path: str = str(DB_PATH)):
        self.db_path = Path(db_path)
        self.db_path.parent.mkdir(parents=True, exist_ok=True)
        self.init_db()

    def init_db(self) -> None:
        """Inicializa tabelas do banco de dados."""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()

            # Tabela de histórico de conversa
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS chat_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    session_id TEXT NOT NULL,
                    role TEXT NOT NULL,
                    content TEXT NOT NULL,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                    agent TEXT
                )
            """)

            # Tabela de projetos
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS projects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE NOT NULL,
                    description TEXT,
                    agents TEXT NOT NULL,
                    status TEXT DEFAULT 'draft',
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """)

            # Tabela de configurações
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS settings (
                    key TEXT PRIMARY KEY,
                    value TEXT NOT NULL,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """)

            # Tabela de memória/contexto
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS memory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    context TEXT NOT NULL,
                    category TEXT,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """)

            # Tabela de sessões
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS sessions (
                    id TEXT PRIMARY KEY,
                    title TEXT,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """)

            conn.commit()

    def add_chat_message(self, session_id: str, role: str, content: str, agent: Optional[str] = None) -> None:
        """Adiciona mensagem ao histórico."""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT INTO chat_history (session_id, role, content, agent)
                VALUES (?, ?, ?, ?)
            """, (session_id, role, content, agent))
            conn.commit()

    def get_chat_history(self, session_id: str, limit: int = 100) -> List[Dict[str, Any]]:
        """Obtém histórico de conversa de uma sessão."""
        with sqlite3.connect(self.db_path) as conn:
            conn.row_factory = sqlite3.Row
            cursor = conn.cursor()
            cursor.execute("""
                SELECT role, content, agent, timestamp FROM chat_history
                WHERE session_id = ?
                ORDER BY timestamp DESC
                LIMIT ?
            """, (session_id, limit))
            rows = cursor.fetchall()
            return [dict(row) for row in reversed(rows)]

    def create_project(self, name: str, agents: List[str], description: str = "") -> Dict[str, Any]:
        """Cria novo projeto."""
        agents_json = json.dumps(agents)
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT INTO projects (name, description, agents)
                VALUES (?, ?, ?)
            """, (name, description, agents_json))
            conn.commit()
            project_id = cursor.lastrowid

        return self.get_project(project_id)

    def get_project(self, project_id: int) -> Optional[Dict[str, Any]]:
        """Obtém projeto por ID."""
        with sqlite3.connect(self.db_path) as conn:
            conn.row_factory = sqlite3.Row
            cursor = conn.cursor()
            cursor.execute("""
                SELECT id, name, description, agents, status, created_at, updated_at
                FROM projects WHERE id = ?
            """, (project_id,))
            row = cursor.fetchone()
            if row:
                data = dict(row)
                data["agents"] = json.loads(data["agents"])
                return data
            return None

    def list_projects(self) -> List[Dict[str, Any]]:
        """Lista todos os projetos."""
        with sqlite3.connect(self.db_path) as conn:
            conn.row_factory = sqlite3.Row
            cursor = conn.cursor()
            cursor.execute("""
                SELECT id, name, description, agents, status, created_at, updated_at
                FROM projects ORDER BY updated_at DESC
            """)
            rows = cursor.fetchall()
            projects = []
            for row in rows:
                data = dict(row)
                data["agents"] = json.loads(data["agents"])
                projects.append(data)
            return projects

    def update_project_status(self, project_id: int, status: str) -> None:
        """Atualiza status do projeto."""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("""
                UPDATE projects SET status = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
            """, (status, project_id))
            conn.commit()

    def set_setting(self, key: str, value: str) -> None:
        """Define configuração."""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT OR REPLACE INTO settings (key, value)
                VALUES (?, ?)
            """, (key, value))
            conn.commit()

    def get_setting(self, key: str, default: Optional[str] = None) -> Optional[str]:
        """Obtém configuração."""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT value FROM settings WHERE key = ?", (key,))
            row = cursor.fetchone()
            return row[0] if row else default

    def add_memory(self, context: str, category: Optional[str] = None) -> None:
        """Adiciona contexto à memória."""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT INTO memory (context, category)
                VALUES (?, ?)
            """, (context, category))
            conn.commit()

    def get_memory(self, category: Optional[str] = None, limit: int = 50) -> List[Dict[str, Any]]:
        """Obtém memória/contexto."""
        with sqlite3.connect(self.db_path) as conn:
            conn.row_factory = sqlite3.Row
            cursor = conn.cursor()
            if category:
                cursor.execute("""
                    SELECT context, category, timestamp FROM memory
                    WHERE category = ?
                    ORDER BY timestamp DESC LIMIT ?
                """, (category, limit))
            else:
                cursor.execute("""
                    SELECT context, category, timestamp FROM memory
                    ORDER BY timestamp DESC LIMIT ?
                """, (limit,))
            rows = cursor.fetchall()
            return [dict(row) for row in rows]

    def create_session(self, session_id: str, title: str = "") -> None:
        """Cria nova sessão."""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT OR IGNORE INTO sessions (id, title)
                VALUES (?, ?)
            """, (session_id, title))
            conn.commit()

    def list_sessions(self, limit: int = 20) -> List[Dict[str, Any]]:
        """Lista sessões recentes."""
        with sqlite3.connect(self.db_path) as conn:
            conn.row_factory = sqlite3.Row
            cursor = conn.cursor()
            cursor.execute("""
                SELECT id, title, created_at, updated_at FROM sessions
                ORDER BY updated_at DESC LIMIT ?
            """, (limit,))
            rows = cursor.fetchall()
            return [dict(row) for row in rows]

    def clear_old_data(self, days: int = 30) -> None:
        """Remove dados antigos (mais antigos que N dias)."""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("""
                DELETE FROM chat_history
                WHERE timestamp < datetime('now', '-' || ? || ' days')
            """, (days,))
            cursor.execute("""
                DELETE FROM memory
                WHERE timestamp < datetime('now', '-' || ? || ' days')
            """, (days,))
            conn.commit()

    def export_session(self, session_id: str) -> Dict[str, Any]:
        """Exporta sessão completa (histórico + contexto)."""
        history = self.get_chat_history(session_id)
        memory = self.get_memory()
        return {
            "session_id": session_id,
            "timestamp": datetime.now().isoformat(),
            "chat_history": history,
            "memory": memory,
        }


if __name__ == "__main__":
    db = NexoAIDatabase()
    print("✓ Database NexoAI inicializado")

    # Exemplo de uso
    session_id = "session_001"
    db.create_session(session_id, "Primeira Sessão")
    db.add_chat_message(session_id, "user", "Crie um design para meu app")
    db.add_chat_message(session_id, "assistant", "Vou analisar e propor um design...", agent="design_director")
    
    print("\nHistórico:")
    for msg in db.get_chat_history(session_id):
        print(f"  {msg['role']}: {msg['content'][:50]}...")
    
    db.create_project("Meu App", ["design_director", "developer"], "App de produtividade")
    print("\nProjetos:")
    for proj in db.list_projects():
        print(f"  {proj['name']} ({proj['status']}): {', '.join(proj['agents'])}")
