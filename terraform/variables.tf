variable "aws_region" {
  description = "Região AWS onde a infraestrutura será criada."
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Nome usado para identificar os recursos do projeto."
  type        = string
  default     = "comecome"
}

variable "environment" {
  description = "Ambiente lógico (dev, staging ou prod)."
  type        = string
  default     = "dev"
}

variable "vpc_cidr" {
  description = "Bloco IPv4 privado da VPC."
  type        = string
  default     = "10.20.0.0/16"
}

variable "public_subnet_cidr" {
  description = "Bloco IPv4 da sub-rede pública inicial."
  type        = string
  default     = "10.20.1.0/24"
}

variable "availability_zone" {
  description = "Zona de disponibilidade da sub-rede pública."
  type        = string
  default     = "us-east-1a"
}

variable "gateway_port" {
  description = "Porta HTTP publicada pelo API Gateway no Docker Compose."
  type        = number
  default     = 8083
}

variable "gateway_allowed_cidrs" {
  description = "Origens autorizadas a acessar o API Gateway. Restrinja para seu IP/CIDR sempre que possível."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "grafana_port" {
  description = "Porta HTTP publicada pelo Grafana no Docker Compose."
  type        = number
  default     = 3000

  validation {
    condition     = var.grafana_port > 0 && var.grafana_port < 65536
    error_message = "grafana_port deve ser uma porta TCP válida."
  }
}

variable "grafana_allowed_cidrs" {
  description = "Origens autorizadas a acessar o Grafana. Lista vazia mantém o Grafana sem acesso externo."
  type        = list(string)
  default     = []

  validation {
    condition     = alltrue([for cidr in var.grafana_allowed_cidrs : can(cidrnetmask(cidr))])
    error_message = "grafana_allowed_cidrs deve conter apenas blocos CIDR IPv4 válidos, como 203.0.113.10/32."
  }
}
