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

variable "instance_type" {
  description = "Tipo da EC2 da aplicação. O Learner Lab aceita até a família large; a stack inteira (8 JVMs + infra + observabilidade) precisa de ~7 GiB."
  type        = string
  default     = "t3.large"
}

variable "root_volume_size" {
  description = "Tamanho (GiB) do disco do sistema da EC2."
  type        = number
  default     = 20
}

variable "data_volume_size" {
  description = "Tamanho (GiB) do volume EBS de dados (imagens e volumes do Docker: Postgres, RabbitMQ, Prometheus, Loki, Grafana)."
  type        = number
  default     = 30
}

variable "repository_url" {
  description = "Repositório Git clonado na EC2 (compose, Caddyfile e script de deploy). Precisa ser público."
  type        = string
  default     = "https://github.com/Come-Come-UFRPE/Come-come-BackEnd.git"
}
