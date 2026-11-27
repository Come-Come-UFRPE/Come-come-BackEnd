resource "aws_security_group" "application" {
  name = "${var.project_name}-${var.environment}-application-sg"
  # Descrições do SG e das regras: só ASCII, sem acentos e sem "<>" (a AWS recusa).
  # Mudar a descrição recria o Security Group, por isso ela foi mantida.
  description = "Permite acesso HTTP ao API Gateway; os demais servicos permanecem internos."
  vpc_id      = aws_vpc.main.id

  # Entrada só pelo Caddy (proxy reverso com HTTPS automático). Todos os demais
  # serviços (gateway, bancos, RabbitMQ, Grafana...) não publicam portas no host.
  # As portas 80/443 precisam ficar abertas para a internet: é uma API pública e
  # o Let's Encrypt valida o certificado a partir de IPs variados.
  ingress {
    description = "HTTP (redireciona para HTTPS e desafio ACME)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    #trivy:ignore:AWS-0107
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS (Caddy para o API Gateway)"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    #trivy:ignore:AWS-0107
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "Permite comunicacao dentro da VPC"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [var.vpc_cidr]
  }

  egress {
    description     = "HTTPS para o S3 (fotos de perfil) via VPC Endpoint"
    from_port       = 443
    to_port         = 443
    protocol        = "tcp"
    prefix_list_ids = [aws_vpc_endpoint.s3.prefix_list_id]
  }

  # Saída HTTPS: GHCR/Docker Hub (imagens), Infisical (segredos), SSM (deploy),
  # Let's Encrypt, repositórios do sistema e a API do OpenFoodFacts. Esses
  # destinos não têm IPs fixos, então não dá para restringir por CIDR.
  egress {
    description = "HTTPS para servicos externos"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    #trivy:ignore:AWS-0104
    cidr_blocks = ["0.0.0.0/0"]
  }

  # SMTP com STARTTLS do serviço de e-mail (Brevo).
  egress {
    description = "SMTP submission para o provedor de e-mail"
    from_port   = 587
    to_port     = 587
    protocol    = "tcp"
    #trivy:ignore:AWS-0104
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-application-sg"
  }
}
