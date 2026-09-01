class WebCreatorAgent:
    name = "Web Creator"

    def __init__(self, service):
        self.service = service

    def generate(self, brief: str) -> str:
        prompt = (
            "Atue como um criador de web experiences e páginas de conversão. "
            f"Brief: {brief}. "
            "Descreva arquitetura, copy, blocos de conteúdo, UX, seção por seção e proposta de valor."
        )
        return self.service.generate(prompt, system_prompt="Você é o Web Creator da NexoAI.")
