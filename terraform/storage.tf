# Armazenamento das fotos de perfil (serviço de Cadastro).
#
# O bucket é privado por padrão; só o prefixo users/ tem leitura pública, porque
# a API devolve e salva a URL direta da foto para o front exibir. Escrita e
# remoção são feitas pela EC2 via LabInstanceProfile, sem Access Keys.
# O bucket em si é criado em buckets.tf.

# Desativa ACLs: o acesso é controlado só por políticas.
resource "aws_s3_bucket_ownership_controls" "profile_pictures" {
  bucket = data.aws_s3_bucket.profile_pictures.id

  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}

# Bloqueia ACLs públicas, mas permite a bucket policy que libera users/*.
# Atenção: se o "Block Public Access" estiver ligado no nível da CONTA AWS,
# ele prevalece e a leitura pública não funcionará.
resource "aws_s3_bucket_public_access_block" "profile_pictures" {
  bucket = data.aws_s3_bucket.profile_pictures.id

  block_public_acls  = true
  ignore_public_acls = true

  # Risco aceito: a leitura pública de users/* é intencional (o front exibe a
  # URL direta da foto). Só GetObject é público; escrita/remoção exigem a role
  # da EC2, e o upload aceita apenas JPEG/PNG/WebP.
  #trivy:ignore:AWS-0087
  block_public_policy = false
  #trivy:ignore:AWS-0093
  restrict_public_buckets = false
}

# SSE-S3 (AES256) de propósito: objetos cifrados com chave KMS própria não podem
# ser lidos anonimamente (exigem kms:Decrypt), o que quebraria a leitura pública.
#trivy:ignore:AWS-0132
resource "aws_s3_bucket_server_side_encryption_configuration" "profile_pictures" {
  bucket = data.aws_s3_bucket.profile_pictures.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# Log de acesso do S3 (quem leu/gravou cada foto), gravado no bucket de logs
# da VPC sob um prefixo próprio. Segue a retenção de 7 dias daquele bucket.
resource "aws_s3_bucket_logging" "profile_pictures" {
  bucket = data.aws_s3_bucket.profile_pictures.id

  target_bucket = data.aws_s3_bucket.vpc_flow_logs.id
  target_prefix = "s3-access/profile-pictures/"

  depends_on = [aws_s3_bucket_policy.vpc_flow_logs]
}

# Versionamento protege contra sobrescrita/remoção acidental; versões antigas
# expiram em 30 dias para não acumular custo (cada upload sobrescreve profile.jpg).
resource "aws_s3_bucket_versioning" "profile_pictures" {
  bucket = data.aws_s3_bucket.profile_pictures.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "profile_pictures" {
  bucket     = data.aws_s3_bucket.profile_pictures.id
  depends_on = [aws_s3_bucket_versioning.profile_pictures]

  rule {
    id     = "expira-versoes-antigas"
    status = "Enabled"

    filter {}

    noncurrent_version_expiration {
      noncurrent_days = 30
    }

    abort_incomplete_multipart_upload {
      days_after_initiation = 1
    }
  }
}

resource "aws_s3_bucket_policy" "profile_pictures" {
  bucket     = data.aws_s3_bucket.profile_pictures.id
  depends_on = [aws_s3_bucket_public_access_block.profile_pictures]

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "LeituraPublicaFotosDePerfil"
        Effect    = "Allow"
        Principal = "*"
        Action    = "s3:GetObject"
        Resource  = "${data.aws_s3_bucket.profile_pictures.arn}/users/*"
      },
      {
        Sid       = "NegaTrafegoSemTLS"
        Effect    = "Deny"
        Principal = "*"
        Action    = "s3:*"
        Resource = [
          data.aws_s3_bucket.profile_pictures.arn,
          "${data.aws_s3_bucket.profile_pictures.arn}/*"
        ]
        Condition = {
          Bool = { "aws:SecureTransport" = "false" }
        }
      }
    ]
  })
}

# ---------------------------------------------------------------------------
# Acesso da aplicação: o AWS Academy Learner Lab não permite criar roles IAM,
# então a EC2 (compute.tf) usa o LabInstanceProfile que o próprio Lab fornece (o
# LabRole já tem acesso ao S3). O AWS SDK do serviço de Cadastro pega essas
# credenciais automaticamente, sem Access Keys.
# A EC2 (compute.tf) usa IMDSv2 com hop limit 2 para que os containers Docker
# alcancem as credenciais da instância.
# ---------------------------------------------------------------------------

data "aws_iam_instance_profile" "lab" {
  name = "LabInstanceProfile"
}

# Gateway Endpoint do S3 (sem custo): o tráfego da VPC até o S3 não passa pela
# internet, e o Security Group libera saída só para os IPs do S3 na região.
resource "aws_vpc_endpoint" "s3" {
  vpc_id            = aws_vpc.main.id
  service_name      = "com.amazonaws.${var.aws_region}.s3"
  vpc_endpoint_type = "Gateway"
  route_table_ids   = [aws_route_table.public.id]

  tags = {
    Name = "${var.project_name}-${var.environment}-s3-endpoint"
  }
}

# ---------------------------------------------------------------------------
# Bucket privado de artefatos: arquivos que não vão para o Git mas a EC2
# precisa, como o seed do banco do OpenFoodFacts (seed/OFF.sql). Nada público.
# ---------------------------------------------------------------------------

resource "aws_s3_bucket_ownership_controls" "artifacts" {
  bucket = data.aws_s3_bucket.artifacts.id

  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}

resource "aws_s3_bucket_public_access_block" "artifacts" {
  bucket = data.aws_s3_bucket.artifacts.id

  block_public_acls       = true
  ignore_public_acls      = true
  block_public_policy     = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "artifacts" {
  bucket = data.aws_s3_bucket.artifacts.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_versioning" "artifacts" {
  bucket = data.aws_s3_bucket.artifacts.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_policy" "artifacts" {
  bucket     = data.aws_s3_bucket.artifacts.id
  depends_on = [aws_s3_bucket_public_access_block.artifacts]

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Sid       = "NegaTrafegoSemTLS"
      Effect    = "Deny"
      Principal = "*"
      Action    = "s3:*"
      Resource = [
        data.aws_s3_bucket.artifacts.arn,
        "${data.aws_s3_bucket.artifacts.arn}/*"
      ]
      Condition = {
        Bool = { "aws:SecureTransport" = "false" }
      }
    }]
  })
}
