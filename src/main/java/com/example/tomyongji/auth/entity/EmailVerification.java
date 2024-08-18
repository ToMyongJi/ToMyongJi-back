package com.example.tomyongji.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "email_verification")
public class EmailVerification {
    @Id
    private String email;
    private String verificationCode;
    private LocalDateTime createdAt;
}
