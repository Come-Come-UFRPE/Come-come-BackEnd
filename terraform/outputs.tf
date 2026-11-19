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
