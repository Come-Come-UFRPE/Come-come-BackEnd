# ---------------------------------------------------------------------------
# EC2 da aplicação
#
# Uma instância roda a stack inteira com Docker Compose (docker-compose.yaml +
# docker-compose.prod.yml). Os dados do Docker ficam num volume EBS separado,
# que sobrevive a uma eventual troca da instância. O acesso administrativo e o
# deploy são feitos pelo AWS Systems Manager (SSM): não há SSH nem porta 22.
# ---------------------------------------------------------------------------

data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }

  filter {
    name   = "architecture"
    values = ["x86_64"]
  }
}

resource "aws_ebs_volume" "app_data" {
  availability_zone = var.availability_zone
  size              = var.data_volume_size
  type              = "gp3"
  encrypted         = true

  tags = {
    Name = "${var.project_name}-${var.environment}-app-data"
  }
}

resource "aws_instance" "app" {
  ami                    = data.aws_ami.al2023.id
  instance_type          = var.instance_type
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.application.id]
  iam_instance_profile   = data.aws_iam_instance_profile.lab.name

  # IMDSv2 obrigatório; hop limit 2 para os containers alcançarem as
  # credenciais da instância (S3 das fotos de perfil, Infisical AWS Auth).
  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 2
  }

  root_block_device {
    volume_type = "gp3"
    volume_size = var.root_volume_size
    encrypted   = true
  }

  user_data = templatefile("${path.module}/templates/user_data.sh.tftpl", {
    data_volume_id = aws_ebs_volume.app_data.id
    repository_url = var.repository_url
  })

  tags = {
    Name = "${var.project_name}-${var.environment}-app"
  }

  lifecycle {
    # Uma AMI nova ou um ajuste no bootstrap não devem recriar o servidor em
    # produção. Para aplicá-los de propósito: terraform apply -replace=aws_instance.app
    ignore_changes = [ami, user_data]
  }
}

resource "aws_volume_attachment" "app_data" {
  device_name = "/dev/sdf"
  volume_id   = aws_ebs_volume.app_data.id
  instance_id = aws_instance.app.id
}

# IP fixo: sobrevive ao stop/start da instância a cada sessão do Learner Lab,
# então o domínio sslip.io (e o certificado HTTPS) não muda.
resource "aws_eip" "app" {
  domain   = "vpc"
  instance = aws_instance.app.id

  tags = {
    Name = "${var.project_name}-${var.environment}-app-eip"
  }

  depends_on = [aws_internet_gateway.main]
}
