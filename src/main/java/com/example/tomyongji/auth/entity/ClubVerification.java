package com.example.tomyongji.auth.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    private LocalDateTime verificatedAt;
    private String studentNum;
    @OneToOne
    @JsonBackReference
    private User user;
}
