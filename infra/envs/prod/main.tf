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
  root_volume_size = 20
}
