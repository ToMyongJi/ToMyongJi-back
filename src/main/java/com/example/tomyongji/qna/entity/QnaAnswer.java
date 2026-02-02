package com.example.tomyongji.qna.entity;

import com.example.tomyongji.domain.auth.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class QnaAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    private String content;

    @Column(updatable = false)
    private LocalDateTime createdTime;

    @ManyToOne
    private QnaQuestion question;

    @PrePersist
    public void prePersist() {
        this.createdTime = LocalDateTime.now();
    }
}
