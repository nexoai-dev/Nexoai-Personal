class MarketingStrategistAgent:
    name = "Marketing Strategist"

    def __init__(self, service):
        self.service = service

    def generate(self, market: str) -> str:
        prompt = (
            "Atue como Marketing Strategist. "
            f"Mercado: {market}. "
            "Crie estratégia, posicionamento, personas, canais, funil, campanhas, mensagens e indicadores chave para crescimento." 
        )
        return self.service.generate(prompt, system_prompt="Você é o Marketing Strategist da NexoAI.")
