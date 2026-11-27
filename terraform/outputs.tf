output "vpc_id" {
  description = "ID da VPC do Come-come."
  value       = aws_vpc.main.id
}

output "public_subnet_id" {
  description = "ID da sub-rede pública inicial."
  value       = aws_subnet.public.id
}

output "application_security_group_id" {
  description = "Security Group destinado à futura instância de aplicação."
  value       = aws_security_group.application.id
}

output "profile_pictures_bucket" {
  description = "Bucket S3 das fotos de perfil (valor de AWS_S3_BUCKET)."
  value       = aws_s3_bucket.profile_pictures.bucket
}

output "application_instance_profile" {
  description = "Instance Profile a ser associado à futura EC2 da aplicação."
  value       = data.aws_iam_instance_profile.lab.name
}
