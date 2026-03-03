terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  required_version = ">= 1.3.0"
}

provider "aws" {
  region = "ap-northeast-2"
}

module "server" {
  source = "../../modules/server"

  app_name         = "tomyongji"
  environment      = "prod"
  instance_type    = "t3.micro"
  key_name         = "tomyongji-key"
  root_volume_size = 30
  volume_encrypted = false

  # 기존 AMI 고정 (변경 시 EC2 재생성 → 서비스 중단 위험)
  ami_id = "ami-00e73adb2e2c80366"

  # 기존 SG 이름/설명 유지 (변경 시 SG 재생성 → 서비스 중단 위험)
  sg_name        = "tomyongji-sg"
  sg_description = "launch-wizard-1 created 2025-10-03T11:28:25.754Z"

  # prod 서버는 8080, 12345 미사용 (기존 설정 유지)
  enable_app_ports = false

  # 팀원 MySQL 접근 허용 룰
  extra_ingress_rules = [
    {
      description = "woojoo"
      from_port   = 3306
      to_port     = 3306
      protocol    = "tcp"
      cidr_blocks = ["119.199.154.198/32"]
      self        = false
    },
    {
      description = ""
      from_port   = 3306
      to_port     = 3306
      protocol    = "tcp"
      cidr_blocks = ["211.209.22.109/32"]
      self        = true
    },
    {
      description = "jinhyeong"
      from_port   = 3306
      to_port     = 3306
      protocol    = "tcp"
      cidr_blocks = ["223.38.87.207/32"]
      self        = false
    },
    {
      description = "tomyongji-dev"
      from_port   = 3306
      to_port     = 3306
      protocol    = "tcp"
      cidr_blocks = ["3.37.120.178/32"]
      self        = false
    }
  ]
}
