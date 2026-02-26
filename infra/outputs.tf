output "instance_id" {
  description = "EC2 인스턴스 ID"
  value       = aws_instance.dev_server.id
}

output "elastic_ip" {
  description = "탄력적 IP 주소"
  value       = aws_eip.dev_eip.public_ip
}

output "public_dns" {
  description = "인스턴스 퍼블릭 DNS"
  value       = aws_eip.dev_eip.public_dns
}

output "ssh_command" {
  description = "SSH 접속 명령어"
  value       = "ssh -i ~/.ssh/${var.key_name}.pem ubuntu@${aws_eip.dev_eip.public_ip}"
}

output "security_group_id" {
  description = "보안 그룹 ID"
  value       = aws_security_group.dev_sg.id
}

output "ami_id" {
  description = "사용된 Ubuntu AMI ID"
  value       = data.aws_ami.ubuntu.id
}
