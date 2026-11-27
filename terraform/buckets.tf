# ---------------------------------------------------------------------------
# Criação dos buckets S3 (contorno para o AWS Academy Learner Lab)
#
# O resource aws_s3_bucket não funciona no Learner Lab: depois de criar o
# bucket, o provider sempre lê a configuração de Object Lock, e uma SCP do Lab
# nega s3:GetBucketObjectLockConfiguration (nenhuma versão do provider ignora
# esse erro). Por isso:
#   1. o bucket é criado pelo AWS CLI (só se ainda não existir);
#   2. o Terraform o referencia via data "aws_s3_bucket", que não lê Object Lock;
#   3. todas as configurações (policy, criptografia, versionamento, logs...)
#      continuam como resources normais do Terraform.
#
# Cada bucket tem seu próprio terraform_data, e cada data source depende só do
# seu. Assim, adicionar um bucket novo não adia a leitura dos outros para o
# apply (o que forçaria recriar todas as configurações deles).
#
# Requer o AWS CLI instalado onde o Terraform roda.
# Atenção: o terraform destroy NÃO apaga os buckets; remova-os pelo console
# ou com: aws s3 rb s3://<bucket> --force
# ---------------------------------------------------------------------------

locals {
  s3_buckets = {
    vpc_flow_logs    = "${var.project_name}-${var.environment}-vpc-flow-logs-${data.aws_caller_identity.current.account_id}"
    profile_pictures = "${var.project_name}-${var.environment}-profile-pictures-${data.aws_caller_identity.current.account_id}"
    artifacts        = "${var.project_name}-${var.environment}-artifacts-${data.aws_caller_identity.current.account_id}"
  }

  # Script idempotente: cria o bucket se não existir e aplica as tags.
  s3_bucket_bootstrap = <<-EOT
    set -e
    if aws s3api head-bucket --bucket "$BUCKET" --region "$REGION" >/dev/null 2>&1; then
      echo "Bucket $BUCKET ja existe, mantendo."
    elif [ "$REGION" = "us-east-1" ]; then
      aws s3api create-bucket --bucket "$BUCKET" --region "$REGION"
    else
      aws s3api create-bucket --bucket "$BUCKET" --region "$REGION" \
        --create-bucket-configuration LocationConstraint="$REGION"
    fi
    aws s3api put-bucket-tagging --bucket "$BUCKET" --region "$REGION" \
      --tagging "TagSet=[{Key=Name,Value=$NAME_TAG},{Key=Project,Value=$PROJECT},{Key=Environment,Value=$ENVIRONMENT},{Key=ManagedBy,Value=Terraform}]"
  EOT

  s3_bucket_env = {
    REGION      = var.aws_region
    PROJECT     = var.project_name
    ENVIRONMENT = var.environment
  }
}

# --- VPC Flow Logs -----------------------------------------------------------

resource "terraform_data" "bucket_vpc_flow_logs" {
  triggers_replace = [local.s3_buckets.vpc_flow_logs, var.aws_region]

  provisioner "local-exec" {
    command = local.s3_bucket_bootstrap
    environment = merge(local.s3_bucket_env, {
      BUCKET   = local.s3_buckets.vpc_flow_logs
      NAME_TAG = "${var.project_name}-${var.environment}-vpc-flow-logs"
    })
  }
}

data "aws_s3_bucket" "vpc_flow_logs" {
  bucket     = local.s3_buckets.vpc_flow_logs
  depends_on = [terraform_data.bucket_vpc_flow_logs]
}

# --- Fotos de perfil -----------------------------------------------------------

resource "terraform_data" "bucket_profile_pictures" {
  triggers_replace = [local.s3_buckets.profile_pictures, var.aws_region]

  provisioner "local-exec" {
    command = local.s3_bucket_bootstrap
    environment = merge(local.s3_bucket_env, {
      BUCKET   = local.s3_buckets.profile_pictures
      NAME_TAG = "${var.project_name}-${var.environment}-profile-pictures"
    })
  }
}

data "aws_s3_bucket" "profile_pictures" {
  bucket     = local.s3_buckets.profile_pictures
  depends_on = [terraform_data.bucket_profile_pictures]
}

# --- Artefatos (seed do banco) ---------------------------------------------------

resource "terraform_data" "bucket_artifacts" {
  triggers_replace = [local.s3_buckets.artifacts, var.aws_region]

  provisioner "local-exec" {
    command = local.s3_bucket_bootstrap
    environment = merge(local.s3_bucket_env, {
      BUCKET   = local.s3_buckets.artifacts
      NAME_TAG = "${var.project_name}-${var.environment}-artifacts"
    })
  }
}

data "aws_s3_bucket" "artifacts" {
  bucket     = local.s3_buckets.artifacts
  depends_on = [terraform_data.bucket_artifacts]
}

# --- Migração de estado (antes era um único terraform_data com for_each) ---------

moved {
  from = terraform_data.s3_bucket["vpc_flow_logs"]
  to   = terraform_data.bucket_vpc_flow_logs
}

moved {
  from = terraform_data.s3_bucket["profile_pictures"]
  to   = terraform_data.bucket_profile_pictures
}
