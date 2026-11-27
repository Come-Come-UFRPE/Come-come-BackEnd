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
}

resource "terraform_data" "s3_bucket" {
  for_each = local.s3_buckets

  triggers_replace = [each.value, var.aws_region]

  provisioner "local-exec" {
    environment = {
      BUCKET      = each.value
      REGION      = var.aws_region
      NAME_TAG    = "${var.project_name}-${var.environment}-${replace(each.key, "_", "-")}"
      PROJECT     = var.project_name
      ENVIRONMENT = var.environment
    }

    command = <<-EOT
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
  }
}

data "aws_s3_bucket" "vpc_flow_logs" {
  bucket     = local.s3_buckets.vpc_flow_logs
  depends_on = [terraform_data.s3_bucket]
}

data "aws_s3_bucket" "profile_pictures" {
  bucket     = local.s3_buckets.profile_pictures
  depends_on = [terraform_data.s3_bucket]
}

data "aws_s3_bucket" "artifacts" {
  bucket     = local.s3_buckets.artifacts
  depends_on = [terraform_data.s3_bucket]
}
