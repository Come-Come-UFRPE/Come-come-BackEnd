provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# Conta AWS em uso (usada para gerar nomes únicos de bucket).
data "aws_caller_identity" "current" {}
