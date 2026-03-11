output "instance_id" {
  description = "EC2 인스턴스 ID"
  value       = module.server.instance_id
}

output "elastic_ip" {
  description = "탄력적 IP 주소"
  value       = module.server.elastic_ip
}

output "public_dns" {
  description = "인스턴스 퍼블릭 DNS"
  value       = module.server.public_dns
}

output "ssh_command" {
  description = "SSH 접속 명령어"
  value       = module.server.ssh_command
}

output "security_group_id" {
  description = "보안 그룹 ID"
  value       = module.server.security_group_id
}

output "ami_id" {
  description = "사용된 Ubuntu AMI ID"
  value       = module.server.ami_id
}
