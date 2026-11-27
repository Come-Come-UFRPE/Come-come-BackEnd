# Terraform — Come-come

Cria a infraestrutura da aplicação na AWS (pensada para o AWS Academy Learner Lab):

- VPC com DNS habilitado;
- uma sub-rede pública em `us-east-1a`;
- Internet Gateway;
- tabela de rotas para saída/entrada via Internet Gateway;
- Security Group que libera na entrada só 80/443 (Caddy, HTTPS); saída para a VPC, HTTPS e SMTP (587);
- VPC Flow Logs (só tráfego rejeitado) gravados num bucket S3 privado, com retenção de 7 dias;
- bucket S3 das fotos de perfil (privado, com leitura pública só em `users/*`, criptografado e versionado);
- a EC2 da aplicação grava no bucket pelo `LabInstanceProfile` do Lab, sem Access Keys;
- VPC Gateway Endpoint do S3 (sem custo), para o tráfego até o bucket não sair pela internet;
- bucket S3 privado de artefatos (seed do banco do OpenFoodFacts em `seed/OFF.sql`);
- **EC2 da aplicação** (`compute.tf`): Amazon Linux 2023, `t3.large`, IMDSv2, Docker + Compose + Infisical CLI instalados no primeiro boot, volume EBS de dados separado e Elastic IP.

O deploy da aplicação na EC2 está em [`deploy/README.md`](../deploy/README.md).

**AWS Academy Learner Lab:** o Lab não permite criar roles IAM, por isso nenhum recurso aqui cria IAM. Os flow logs usam destino S3, que dispensa role.

Outras restrições do Lab tratadas aqui:

- **Buckets S3:** uma SCP do Lab nega `s3:GetBucketObjectLockConfiguration`, que o resource `aws_s3_bucket` sempre lê. Por isso os buckets são criados pelo AWS CLI dentro do Terraform (`buckets.tf`, só se ainda não existirem) e referenciados via `data "aws_s3_bucket"`. É preciso ter o **AWS CLI instalado**. O `terraform destroy` **não apaga os buckets**; remova-os com `aws s3 rb s3://<bucket> --force`.
- **Descrição de Security Group:** a AWS só aceita ASCII (sem acentos).

Não cria NAT Gateway, ECR nem banco gerenciado. A EC2 `t3.large`, os volumes EBS e o Elastic IP consomem créditos do Lab (o Elastic IP também cobra com a instância parada). O S3 cobra por armazenamento e requisições (valores baixos para fotos de perfil). VPC, sub-rede, tabela de rotas e Security Group normalmente não têm cobrança horária por si só; o Internet Gateway também não tem tarifa horária, mas tráfego de dados pode ser cobrado. Consulte a tabela de preços AWS antes de evoluir a infraestrutura.

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

- Não há regra de entrada SSH (porta 22): acesso administrativo e deploy são pelo AWS Systems Manager (SSM).
- A entrada é só 80/443, atendida pelo Caddy, que faz proxy para o API Gateway com HTTPS. Elas ficam abertas para a internet porque a API é pública e o Let's Encrypt precisa validar o domínio.
- Bancos, Redis, RabbitMQ, Eureka, Grafana e os microsserviços não publicam portas no host (`docker-compose.prod.yml`).
- Saída para a internet só em 443 (imagens, Infisical, SSM, Let's Encrypt, API do OpenFoodFacts) e 587 (SMTP). Esses destinos não têm IP fixo.
- Mudanças de AMI ou do `user_data` são ignoradas para não recriar a instância sem querer. Para aplicar de propósito: `terraform apply -replace=aws_instance.app`.
