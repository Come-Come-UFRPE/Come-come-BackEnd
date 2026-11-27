# Deploy — EC2 no AWS Academy Learner Lab

```
push na main
  └─ devsecops: testes → scans → build → imagens no GHCR (tag = SHA do commit)
       └─ deploy (environment production, com aprovação)
            1. credenciais da sessão do Lab (secrets AWS_*)
            2. AWS SSM Run Command na EC2: git checkout <sha> + deploy/deploy.sh
                 └─ Infisical (AWS Auth via LabRole) → docker compose pull/up
            3. smoke test: https://<ip-com-hifens>.sslip.io/healthz
```

| Arquivo | Papel |
|---|---|
| `terraform/compute.tf` | EC2, volume EBS de dados, Elastic IP |
| `terraform/templates/user_data.sh.tftpl` | Primeiro boot: Docker, Compose, Infisical CLI, clone do repo |
| `docker-compose.prod.yml` | Override de produção: imagens do GHCR, sem portas expostas, Caddy, limites de memória |
| `deploy/Caddyfile` | Proxy HTTPS (Let's Encrypt automático) para o API Gateway |
| `deploy/deploy.sh` | Roda na EC2: busca segredos no Infisical e sobe a stack |
| `.github/workflows/deploy.yml` | Dispara o deploy depois do devsecops |

---

## Configuração inicial (uma vez)

### 1. Tornar as imagens do GHCR públicas

O repositório é público, então as imagens não revelam nada novo, e a EC2 não precisa de credencial para baixá-las. Para cada um dos 8 pacotes (`comecome-eureka-server`, `-api-gateway`, `-cadastro`, `-anamnese`, `-historico`, `-email`, `-favorite`, `-openfoodfacts`):

GitHub → organização **Come-Come-UFRPE** → **Packages** → pacote → **Package settings** → **Change visibility** → **Public**.

### 2. Machine Identity do Infisical para a EC2 (AWS Auth)

A EC2 se autentica no Infisical com a própria role (`LabRole`), sem segredo em disco.

1. No Infisical: **Organization Settings → Access Control → Identities → Create identity**, com o nome `ec2-app`.
2. **Authentication → Add Auth Method → AWS Auth**:

   | Campo | Valor |
   |---|---|
   | Allowed Principal ARNs | `arn:aws:sts::<ACCOUNT_ID>:assumed-role/LabRole` |
   | Allowed Account IDs | `<ACCOUNT_ID>` |

   `<ACCOUNT_ID>` é o número que aparece em `aws sts get-caller-identity`. Sem `/*` no fim: o Infisical remove o nome da sessão (o ID da instância) do ARN antes de comparar.
3. No **projeto** → **Access Control → Machine Identities → Add**: adicione `ec2-app` com o papel **Viewer**.
4. Anote o **ID da identidade** (página da identidade) e o **Project ID** (Project Settings). É o ID, não o slug.

### 3. Variáveis e secrets no GitHub

**Settings → Secrets and variables → Actions → aba Variables** (nível do repositório):

| Variable | Valor |
|---|---|
| `INFISICAL_EC2_IDENTITY_ID` | ID da identidade `ec2-app` |
| `INFISICAL_PROJECT_ID` | ID do projeto no Infisical |

**Settings → Environments → production → Environment secrets** (renovados a cada sessão do Lab, ver abaixo): `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` e `AWS_SESSION_TOKEN`.

### 4. Criar a EC2 com o Terraform

Com as credenciais do Lab configuradas (ver `terraform/README.md`):

```bash
cd terraform
git pull
terraform init
terraform validate
terraform plan -out=tfplan     # esperado: 10 to add, 1 to change, 0 to destroy
terraform apply tfplan
```

O "1 to change" é o Security Group: as regras mudam no lugar, sem recriar. O primeiro boot leva de 3 a 5 minutos (instala Docker, Compose e Infisical CLI).

### 5. Enviar o seed do banco do OpenFoodFacts

O `OFF.sql` não está no Git. Envie uma vez para o bucket de artefatos:

```bash
aws s3 cp OFF-Come-come/src/main/resources/OFF.sql \
  "s3://$(terraform -chdir=terraform output -raw artifacts_bucket)/seed/OFF.sql"
```

### 6. Conferir se a EC2 está pronta

```bash
aws ssm describe-instance-information \
  --query 'InstanceInformationList[].[InstanceId,PingStatus]' --output text
```

A instância deve aparecer como `Online`. Se não aparecer depois de alguns minutos, o SSM não está disponível para o `LabRole` neste Lab, e o deploy automático não vai funcionar. Avise o time antes de seguir.

Para ver o log do primeiro boot:

```bash
ID=$(terraform -chdir=terraform output -raw app_instance_id)
CMD=$(aws ssm send-command --instance-ids "$ID" --document-name AWS-RunShellScript \
  --parameters 'commands=["tail -n 30 /var/log/comecome-bootstrap.log"]' \
  --query Command.CommandId --output text)
sleep 5; aws ssm get-command-invocation --command-id "$CMD" --instance-id "$ID" \
  --query StandardOutputContent --output text
```

A última linha deve ser `Bootstrap concluído.`

### 7. Primeiro deploy

GitHub → **Actions → deploy → Run workflow** (campo vazio = último commit da main) → aprove o environment `production`.

A primeira subida demora mais: o Postgres importa o seed e o Caddy emite o certificado. A URL aparece no fim do job e em `terraform output app_url`.

---

## A cada sessão do Learner Lab

1. **Start Lab**. A EC2 liga sozinha e os containers voltam (`restart: unless-stopped`). O Elastic IP, e portanto a URL, não muda.
2. Atualize os 3 secrets `AWS_*` do environment `production` com os valores de **AWS Details**:
   ```bash
   gh secret set AWS_ACCESS_KEY_ID     --env production
   gh secret set AWS_SECRET_ACCESS_KEY --env production
   gh secret set AWS_SESSION_TOKEN     --env production
   ```
   (cada comando pede o valor sem mostrá-lo na tela). Sem isso, o deploy falha em "Verificar sessão do Learner Lab".

Se a sessão expirar com um push pendente, rode **Actions → deploy → Run workflow** depois de atualizar os secrets.

---

## Operação

**Status e logs dos containers** (os nomes são `comecome-<serviço>-1` ou o `container_name` do compose):

```bash
aws ssm send-command --instance-ids "$ID" --document-name AWS-RunShellScript \
  --parameters 'commands=["docker ps --format \"table {{.Names}}\\t{{.Status}}\"","docker logs --tail 50 comecome-ms-cadastro-1"]'
```

**Grafana** não é publicado na internet. Com o [Session Manager plugin](https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager-working-with-install-plugin.html) instalado:

```bash
# IP do container do Grafana na rede Docker (via send-command):  docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' grafana
aws ssm start-session --target "$ID" \
  --document-name AWS-StartPortForwardingSessionToRemoteHost \
  --parameters 'host=<IP_DO_CONTAINER>,portNumber=3000,localPortNumber=3000'
```

Depois, abra http://localhost:3000.

**Voltar para uma versão anterior:** **Actions → deploy → Run workflow** com o SHA completo de um commit anterior da main.
