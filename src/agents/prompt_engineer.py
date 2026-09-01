class PromptEngineerAgent:
    name = "Prompt Engineer"

    def __init__(self, service):
        self.service = service

    def generate(self, objective: str) -> str:
        prompt = (
            "Atue como um Prompt Engineer especialista. "
            f"Objetivo: {objective}. "
            "Crie um prompt refinado, estrutura de contexto, instruções, metas e critérios de avaliação."
        )
        return self.service.generate(prompt, system_prompt="Você é o Prompt Engineer da NexoAI.")
