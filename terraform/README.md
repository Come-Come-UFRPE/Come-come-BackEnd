# Terraform — Come-come (fundação de rede)

Esta primeira etapa cria somente recursos básicos de rede na AWS:

- VPC com DNS habilitado;
- uma sub-rede pública em `us-east-1a`;
- Internet Gateway;
- tabela de rotas para saída/entrada via Internet Gateway;
- Security Group que libera somente a porta configurada do API Gateway na entrada.

Ainda **não** cria EC2, NAT Gateway, ECR, banco gerenciado ou armazenamento. VPC, sub-rede, tabela de rotas e Security Group normalmente não têm cobrança horária por si só; o Internet Gateway também não tem tarifa horária, mas tráfego de dados pode ser cobrado. Consulte a tabela de preços AWS antes de evoluir a infraestrutura.

## Pré-requisitos locais

- Terraform >= 1.5;
- AWS CLI configurada com credenciais de uma identidade IAM;
- permissões mínimas necessárias para criar/consultar VPC, sub-redes, rotas, Internet Gateway e Security Groups.

Não coloque Access Keys no código, em `terraform.tfvars` ou no Git. Use `aws configure`/SSO ou variáveis de ambiente de sessão no terminal.

## Preparação

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
terraform init
terraform fmt -check
terraform validate
terraform plan
```

Revise o plano antes de qualquer criação. Para criar os recursos, somente depois da revisão:

```bash
terraform apply
```

Para remover os recursos desta etapa, após confirmar que não estão sendo usados:

```bash
terraform destroy
```

`terraform.tfvars` e arquivos de estado (`*.tfstate*`) não devem ser versionados. O estado pode conter informações sensíveis.

## Segurança

- Não há regra de entrada SSH (porta 22).
- A única entrada configurada é a porta do API Gateway (padrão 8083).
- Para testes, prefira definir `gateway_allowed_cidrs` com seu IP público em formato `/32`, em vez de `0.0.0.0/0`.
- A regra de saída está ampla nesta etapa para permitir futuras operações da instância; será revisada quando definirmos os fluxos necessários.
- A sub-rede é pública para simplificar a primeira implantação. Na etapa de compute, vamos avaliar acesso administrativo via AWS Systems Manager e manter bancos, Redis e RabbitMQ sem exposição pública.
