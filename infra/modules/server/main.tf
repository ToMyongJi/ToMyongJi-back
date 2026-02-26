# ---------------------------------------------------------------------------
# AMI: Ubuntu 22.04 LTS (Jammy) - 서울 리전 최신 버전 자동 선택
# ---------------------------------------------------------------------------
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

locals {
  sg_name        = var.sg_name != "" ? var.sg_name : "${var.app_name}-${var.environment}-sg"
  sg_description = var.sg_description != "" ? var.sg_description : "Security group for ${var.app_name} ${var.environment}"
  ami_id         = var.ami_id != "" ? var.ami_id : data.aws_ami.ubuntu.id
}

# ---------------------------------------------------------------------------
# Security Group
# 기본 포트: 22(SSH), 80(HTTP), 443(HTTPS)
# 선택 포트: 8080(Spring Boot), 12345(Alloy) → enable_app_ports = true 시 오픈
# 추가 포트: extra_ingress_rules 로 환경별 커스텀 룰 추가 가능
# ---------------------------------------------------------------------------
resource "aws_security_group" "this" {
  name        = local.sg_name
  description = local.sg_description

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTP (Nginx)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS (Nginx + Certbot)"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  dynamic "ingress" {
    for_each = var.enable_app_ports ? [1] : []
    content {
      description = "Spring Boot App"
      from_port   = 8080
      to_port     = 8080
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
    }
  }

  dynamic "ingress" {
    for_each = var.enable_app_ports ? [1] : []
    content {
      description = "Grafana Alloy UI"
      from_port   = 12345
      to_port     = 12345
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
    }
  }

  dynamic "ingress" {
    for_each = var.extra_ingress_rules
    content {
      description = ingress.value.description
      from_port   = ingress.value.from_port
      to_port     = ingress.value.to_port
      protocol    = ingress.value.protocol
      cidr_blocks = ingress.value.cidr_blocks
      self        = ingress.value.self
    }
  }

  egress {
    description = "All outbound"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = local.sg_name
    Environment = var.environment
    Project     = var.app_name
  }
}

# ---------------------------------------------------------------------------
# EC2 Instance
# ---------------------------------------------------------------------------
resource "aws_instance" "this" {
  ami                    = local.ami_id
  instance_type          = var.instance_type
  key_name               = var.key_name
  vpc_security_group_ids = [aws_security_group.this.id]

  root_block_device {
    volume_size = var.root_volume_size
    volume_type = "gp3"
    encrypted   = var.volume_encrypted
  }

  # 서버 초기 세팅: Docker 설치 + 스왑 메모리 3GB 구성
  user_data = <<-EOF
    #!/bin/bash
    set -e

    apt-get update -y
    apt-get upgrade -y

    # Docker 공식 저장소 추가 및 설치
    apt-get install -y ca-certificates curl gnupg
    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
      | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    chmod a+r /etc/apt/keyrings/docker.gpg
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
      https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
      | tee /etc/apt/sources.list.d/docker.list > /dev/null
    apt-get update -y
    apt-get install -y docker-ce docker-ce-cli containerd.io \
      docker-buildx-plugin docker-compose-plugin

    # ubuntu 유저에게 docker 권한 부여
    usermod -aG docker ubuntu
    systemctl enable docker
    systemctl start docker

    # 스왑 메모리 3GB 설정 (t2.micro 1GB RAM 보완)
    fallocate -l 3G /swapfile
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    echo '/swapfile none swap sw 0 0' >> /etc/fstab

    # 앱 배포 디렉토리 생성
    mkdir -p /home/ubuntu/app
    chown ubuntu:ubuntu /home/ubuntu/app
  EOF

  tags = {
    Name        = "${var.app_name}-${var.environment}-server"
    Environment = var.environment
    Project     = var.app_name
  }
}

# ---------------------------------------------------------------------------
# Elastic IP
# ---------------------------------------------------------------------------
resource "aws_eip" "this" {
  instance = aws_instance.this.id
  domain   = "vpc"

  tags = {
    Name        = "${var.app_name}-${var.environment}-eip"
    Environment = var.environment
    Project     = var.app_name
  }
}
