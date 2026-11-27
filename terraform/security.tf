resource "aws_security_group" "application" {
  name        = "${var.project_name}-${var.environment}-application-sg"
  description = "Permite acesso HTTP ao API Gateway; os demais serviços permanecem internos."
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "API Gateway"
    from_port   = var.gateway_port
    to_port     = var.gateway_port
    protocol    = "tcp"
    cidr_blocks = var.gateway_allowed_cidrs
  }

  dynamic "ingress" {
    for_each = length(var.grafana_allowed_cidrs) > 0 ? [var.grafana_allowed_cidrs] : []

    content {
      description = "Grafana"
      from_port   = var.grafana_port
      to_port     = var.grafana_port
      protocol    = "tcp"
      cidr_blocks = ingress.value
    }
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

  tags = {
    Name = "${var.project_name}-${var.environment}-application-sg"
  }
}
