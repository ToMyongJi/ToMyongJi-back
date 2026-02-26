variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.micro"
}

variable "key_name" {
  description = "EC2 key pair name"
  type        = string
  default     = "tomyongji-dev-key"
}

variable "app_name" {
  description = "Application name"
  type        = string
  default     = "tomyongji"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
}

variable "root_volume_size" {
  description = "Root EBS volume size in GB"
  type        = number
  default     = 20
}
