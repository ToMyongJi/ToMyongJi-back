package com.example.tomyongji.qna.entity;

import com.example.tomyongji.domain.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class QnaQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;
    private String content;

    @Column(updatable = false)
    private LocalDateTime createdTime;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnaAnswer> answers = new ArrayList<>();

    public void addAnswer(QnaAnswer answer) {
        // 기존에 이미 다른 질문에 연결되어 있었다면 그 관계를 끊어줌 (방어 코드)
        if (answer.getQuestion() != null) {
            answer.getQuestion().getAnswers().remove(answer);
        }

        answer.setQuestion(this);
        this.answers.add(answer);
    }

    @PrePersist
    public void prePersist() {
        this.createdTime = LocalDateTime.now();
    }
}
