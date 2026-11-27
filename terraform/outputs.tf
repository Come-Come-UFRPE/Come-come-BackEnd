output "vpc_id" {
  description = "ID da VPC do Come-come."
  value       = aws_vpc.main.id
}

output "public_subnet_id" {
  description = "ID da sub-rede pública inicial."
  value       = aws_subnet.public.id
}

output "application_security_group_id" {
  description = "Security Group da instância de aplicação."
  value       = aws_security_group.application.id
}

output "profile_pictures_bucket" {
  description = "Bucket S3 das fotos de perfil (valor de AWS_S3_BUCKET)."
  value       = data.aws_s3_bucket.profile_pictures.bucket
}

output "application_instance_profile" {
  description = "Instance Profile usado pela EC2 da aplicação."
  value       = data.aws_iam_instance_profile.lab.name
}

output "artifacts_bucket" {
  description = "Bucket privado de artefatos (envie o seed do banco para seed/OFF.sql)."
  value       = data.aws_s3_bucket.artifacts.bucket
}

output "app_instance_id" {
  description = "ID da EC2 da aplicação (usado pelo SSM)."
  value       = aws_instance.app.id
}

output "app_public_ip" {
  description = "Elastic IP da aplicação."
  value       = aws_eip.app.public_ip
}

output "app_url" {
  description = "URL pública da API (HTTPS via Caddy + sslip.io)."
  value       = "https://${replace(aws_eip.app.public_ip, ".", "-")}.sslip.io"
}
