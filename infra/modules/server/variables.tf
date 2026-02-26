variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
}

variable "key_name" {
  description = "EC2 key pair name"
  type        = string
}

variable "app_name" {
  description = "Application name"
  type        = string
}

variable "environment" {
  description = "Environment name (dev | prod)"
  type        = string
}

variable "root_volume_size" {
  description = "Root EBS volume size in GB"
  type        = number
}

variable "volume_encrypted" {
  description = "Whether to encrypt the root EBS volume"
  type        = bool
  default     = true
}

variable "sg_name" {
  description = "Security group name override. 기본값: {app_name}-{environment}-sg"
  type        = string
  default     = ""
}

variable "sg_description" {
  description = "Security group description override"
  type        = string
  default     = ""
}

variable "enable_app_ports" {
  description = "8080(Spring Boot), 12345(Grafana Alloy) 포트 오픈 여부"
  type        = bool
  default     = true
}

variable "ami_id" {
  description = "AMI ID 고정값. 기본값 빈 문자열이면 최신 Ubuntu 22.04 자동 선택"
  type        = string
  default     = ""
}

variable "extra_ingress_rules" {
  description = "추가 ingress 룰 (예: 특정 IP에 3306 허용)"
  type = list(object({
    description = string
    from_port   = number
    to_port     = number
    protocol    = string
    cidr_blocks = list(string)
    self        = optional(bool, false)
  }))
  default = []
}
