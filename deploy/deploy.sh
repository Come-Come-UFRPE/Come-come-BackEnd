#!/usr/bin/env bash
# Sobe (ou atualiza) a stack de produção na EC2.
#
# Uso (como root, dentro de /opt/comecome/repo já no commit desejado):
#   INFISICAL_IDENTITY_ID=... INFISICAL_PROJECT_ID=... bash deploy/deploy.sh <sha-do-commit>
#
# Chamado pelo workflow .github/workflows/deploy.yml via AWS SSM. Os segredos
# vêm do Infisical (ambiente prod, todas as pastas) com autenticação AWS IAM:
# a própria instância se identifica pelo LabRole, sem nenhum segredo em disco.
set -euo pipefail

IMAGE_TAG="${1:?informe a tag das imagens (SHA do commit)}"
: "${INFISICAL_IDENTITY_ID:?defina INFISICAL_IDENTITY_ID}"
: "${INFISICAL_PROJECT_ID:?defina INFISICAL_PROJECT_ID}"
PROJECT_NAME="${PROJECT_NAME:-comecome}"
ENVIRONMENT="${ENVIRONMENT:-dev}"
INFISICAL_ENV="${INFISICAL_ENV:-prod}"
# Servidor fixo: sem isso, se o login falhar, o CLI abre um menu interativo
# ("Select your hosting option") e o deploy trava em vez de falhar.
export INFISICAL_DOMAIN="${INFISICAL_DOMAIN:-https://app.infisical.com}"
SEED_FILE=/opt/comecome/data/OFF.sql

export HOME="${HOME:-/root}"
cd "$(dirname "$0")/.."

log() { echo "[deploy $(date -u +%H:%M:%S)] $*"; }

imds() {
  local token
  token="$(curl -fsS -X PUT http://169.254.169.254/latest/api/token \
    -H 'X-aws-ec2-metadata-token-ttl-seconds: 60')"
  curl -fsS -H "X-aws-ec2-metadata-token: $token" "http://169.254.169.254/latest/$1"
}

# --- Domínio público (Elastic IP -> sslip.io) ---------------------------------
PUBLIC_IP="$(imds meta-data/public-ipv4)"
REGION="$(imds meta-data/placement/region)"
ACCOUNT_ID="$(imds dynamic/instance-identity/document | python3 -c 'import json,sys; print(json.load(sys.stdin)["accountId"])')"
export APP_DOMAIN="${PUBLIC_IP//./-}.sslip.io"
export IMAGE_TAG
log "commit $IMAGE_TAG -> https://$APP_DOMAIN"

# --- Seed do banco do OpenFoodFacts ---------------------------------------
# Só é usado na primeira inicialização do Postgres (volume vazio), mas o
# arquivo precisa existir sempre, senão o Docker cria um diretório no lugar.
if [ ! -s "$SEED_FILE" ]; then
  ARTIFACTS_BUCKET="${PROJECT_NAME}-${ENVIRONMENT}-artifacts-${ACCOUNT_ID}"
  log "baixando seed s3://$ARTIFACTS_BUCKET/seed/OFF.sql"
  mkdir -p "$(dirname "$SEED_FILE")"
  aws s3 cp "s3://$ARTIFACTS_BUCKET/seed/OFF.sql" "$SEED_FILE" --region "$REGION" --only-show-errors
fi

# --- Segredos (Infisical via AWS IAM) --------------------------------------
log "autenticando no Infisical"
if ! INFISICAL_TOKEN="$(infisical login --method=aws-iam \
  --machine-identity-id="$INFISICAL_IDENTITY_ID" --plain --silent)" || [ -z "$INFISICAL_TOKEN" ]; then
  log "ERRO: falha no login do Infisical. Confira se a identidade $INFISICAL_IDENTITY_ID tem AWS Auth"
  log "      (principal arn:aws:sts::$ACCOUNT_ID:assumed-role/LabRole/*) e papel no projeto."
  exit 1
fi
export INFISICAL_TOKEN

compose() {
  infisical run --projectId "$INFISICAL_PROJECT_ID" --env "$INFISICAL_ENV" \
    --path / --recursive --silent -- \
    docker compose --project-name comecome -f docker-compose.yaml -f docker-compose.prod.yml "$@"
}

# --- Deploy -----------------------------------------------------------------
log "baixando imagens"
compose pull --quiet

log "subindo a stack"
compose up -d --remove-orphans

log "removendo imagens antigas (mais de 7 dias)"
docker image prune -af --filter "until=168h" >/dev/null

compose ps --format 'table {{.Service}}\t{{.Status}}'
log "deploy concluído: https://$APP_DOMAIN"
