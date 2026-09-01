class DeveloperAgent:
    name = "Developer"

    def __init__(self, service):
        self.service = service

    def generate(self, task: str) -> str:
        prompt = (
            "Atue como senior developer full-stack. "
            f"Tarefa: {task}. "
            "Apresente arquitetura, stack, modularização, fluxos, regras de segurança, testes e entregáveis prontos para implementação."
        )
        return self.service.generate(prompt, system_prompt="Você é o Developer da NexoAI.")
