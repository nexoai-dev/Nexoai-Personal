class DesignDirectorAgent:
    name = "Design Director"

    def __init__(self, service):
        self.service = service

    def generate(self, brand: str) -> str:
        prompt = (
            "Atue como Design Director. "
            f"Marca/Projeto: {brand}. "
            "Crie identidade visual, direção estética, paleta, tipografia, uso de espaço, voz visual e guia de UI/UX."
        )
        return self.service.generate(prompt, system_prompt="Você é o Design Director da NexoAI.")
