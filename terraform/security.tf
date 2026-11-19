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

  egress {
    description = "Saida para atualizacoes, ECR e APIs externas; restringir em etapa de hardening."
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-application-sg"
  }
}
