## Iniciar o back-end

Vá até o diretório da aplicação back-end e inicialize o docker
```bash  
cd Come-come-BackEnd
docker compose up --build
```
Após isso tenha paciência(vai demorar um tempinho pra baixar todas as dependências) e divirta-se :D

## Endpoints da Anamnese
(Provavelmente iremos alterar um campo ou outro, mas para vocês já trabalharem é um norte e tanto)

- POST http://localhost:8084/api/anamnese
    - JSON esperado:
    ```bash  
    {
        "peso": Double,
        "altura": Double,
        "idade": Integer,
        "objective": "PERDA_CONTROLE_PESO", "GANHO_MASSA_MUSCULAR", "MELHORA_SAUDE_INTESTINAL", "FORTALECIMENTO_SISTEMA_IMUNOLOGICO", "HABITOS_SAUDAVEIS",
        "sexo": "MASCULINO", "FEMININO",
        "foodAllergy": [ (Pode ser mais de um)
            "LACTOSE",
            "OVO",
            "TRIGO",
            "FRUTOS_DO_MAR",
            "AMENDOIM_E_OLEAGINOSAS"
        ],
        "healthCondition": [ (Pode ser mais de um)
            "HIPERTENSAO",
            "DIABETES",
            "SOBREPESO",
            "DOENCA_CARDIOVASCULAR",
            "ANEMIA"
        ],
        "diet": [ (Pode ser mais de um)
            "BAIXO_CARBOIDRATO",
            "MEDITERRANIA",
            "VEGETARIANA",
            "VEGANA",
            "DASH"
        ]
    }
    ``` 
- GET http://localhost:8084/api/anamnese/{idAnamnese} 
    - No futuro vamos colocar pra pegar pelo ID do usuário pra ficar mais fácil

- PATCH http://localhost:8084/api/anamnese/{idAnamnese} 
    - JSON esperado(Só não pode mudar o sexo/gênero):
    ```bash  
    {
        "peso": Double,
        "altura": Double,
        "idade": Integer,
        "objective": "PERDA_CONTROLE_PESO", "GANHO_MASSA_MUSCULAR", "MELHORA_SAUDE_INTESTINAL", "FORTALECIMENTO_SISTEMA_IMUNOLOGICO", "HABITOS_SAUDAVEIS",
        "foodAllergy": [ (Pode ser mais de um)
            "LACTOSE",
            "OVO",
            "TRIGO",
            "FRUTOS_DO_MAR",
            "AMENDOIM_E_OLEAGINOSAS"
        ],
        "healthCondition": [ (Pode ser mais de um)
            "HIPERTENSAO",
            "DIABETES",
            "SOBREPESO",
            "DOENCA_CARDIOVASCULAR",
            "ANEMIA"
        ],
        "diet": [ (Pode ser mais de um)
            "BAIXO_CARBOIDRATO",
            "MEDITERRANIA",
            "VEGETARIANA",
            "VEGANA",
            "DASH"
        ]
    }
    ``` 

