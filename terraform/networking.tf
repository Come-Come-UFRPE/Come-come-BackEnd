
# VPC principal
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "${var.project_name}-${var.environment}-vpc"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "${var.project_name}-${var.environment}-igw"
  }
}

# Subnet pública
resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidr
  availability_zone       = var.availability_zone
  map_public_ip_on_launch = false

  tags = {
    Name = "${var.project_name}-${var.environment}-public-subnet"
  }
}

# Tabela de rotas pública
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-public-rt"
  }
}

# Associação da subnet com a tabela de rotas
resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

# ---------------------------------------------------------------------------
# VPC Flow Logs -> bucket S3
#
# O destino S3 não precisa de IAM Role (o serviço de entrega de logs grava
# direto no bucket, autorizado pela bucket policy). Isso é necessário no AWS
# Academy Learner Lab, que não permite criar roles IAM.
# ---------------------------------------------------------------------------

resource "aws_s3_bucket" "vpc_flow_logs" {
  # Nomes de bucket são globais na AWS; o ID da conta evita colisão.
  bucket = "${var.project_name}-${var.environment}-vpc-flow-logs-${data.aws_caller_identity.current.account_id}"

  # Permite que o terraform destroy remova o bucket mesmo com logs dentro.
  force_destroy = true

  tags = {
    Name = "${var.project_name}-${var.environment}-vpc-flow-logs"
  }
}

resource "aws_s3_bucket_ownership_controls" "vpc_flow_logs" {
  bucket = aws_s3_bucket.vpc_flow_logs.id

  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}

resource "aws_s3_bucket_public_access_block" "vpc_flow_logs" {
  bucket = aws_s3_bucket.vpc_flow_logs.id

  block_public_acls       = true
  ignore_public_acls      = true
  block_public_policy     = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "vpc_flow_logs" {
  bucket = aws_s3_bucket.vpc_flow_logs.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# Mesma retenção que o log group do CloudWatch tinha antes (7 dias).
resource "aws_s3_bucket_lifecycle_configuration" "vpc_flow_logs" {
  bucket = aws_s3_bucket.vpc_flow_logs.id

  rule {
    id     = "expira-flow-logs"
    status = "Enabled"

    filter {}

    expiration {
      days = 7
    }
  }
}

# Autoriza só o serviço de entrega de logs desta conta a gravar no bucket.
resource "aws_s3_bucket_policy" "vpc_flow_logs" {
  bucket     = aws_s3_bucket.vpc_flow_logs.id
  depends_on = [aws_s3_bucket_public_access_block.vpc_flow_logs]

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AWSLogDeliveryWrite"
        Effect    = "Allow"
        Principal = { Service = "delivery.logs.amazonaws.com" }
        Action    = "s3:PutObject"
        Resource  = "${aws_s3_bucket.vpc_flow_logs.arn}/AWSLogs/${data.aws_caller_identity.current.account_id}/*"
        Condition = {
          StringEquals = { "aws:SourceAccount" = data.aws_caller_identity.current.account_id }
          ArnLike      = { "aws:SourceArn" = "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:*" }
        }
      },
      {
        Sid       = "AWSLogDeliveryAclCheck"
        Effect    = "Allow"
        Principal = { Service = "delivery.logs.amazonaws.com" }
        Action    = "s3:GetBucketAcl"
        Resource  = aws_s3_bucket.vpc_flow_logs.arn
        Condition = {
          StringEquals = { "aws:SourceAccount" = data.aws_caller_identity.current.account_id }
          ArnLike      = { "aws:SourceArn" = "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:*" }
        }
      },
      {
        # Log de acesso do bucket de fotos de perfil (storage.tf).
        Sid       = "S3ServerAccessLogsFotosDePerfil"
        Effect    = "Allow"
        Principal = { Service = "logging.s3.amazonaws.com" }
        Action    = "s3:PutObject"
        Resource  = "${aws_s3_bucket.vpc_flow_logs.arn}/s3-access/profile-pictures/*"
        Condition = {
          StringEquals = { "aws:SourceAccount" = data.aws_caller_identity.current.account_id }
          ArnLike      = { "aws:SourceArn" = aws_s3_bucket.profile_pictures.arn }
        }
      },
      {
        Sid       = "NegaTrafegoSemTLS"
        Effect    = "Deny"
        Principal = "*"
        Action    = "s3:*"
        Resource = [
          aws_s3_bucket.vpc_flow_logs.arn,
          "${aws_s3_bucket.vpc_flow_logs.arn}/*"
        ]
        Condition = {
          Bool = { "aws:SecureTransport" = "false" }
        }
      }
    ]
  })
}

# VPC Flow Logs: registra apenas tráfego rejeitado
resource "aws_flow_log" "main" {
  vpc_id               = aws_vpc.main.id
  traffic_type         = "REJECT"
  log_destination_type = "s3"
  log_destination      = aws_s3_bucket.vpc_flow_logs.arn

  # A policy precisa existir antes, senão a AWS tenta criar uma própria.
  depends_on = [aws_s3_bucket_policy.vpc_flow_logs]

  tags = {
    Name = "${var.project_name}-${var.environment}-vpc-flow-logs"
  }
}
