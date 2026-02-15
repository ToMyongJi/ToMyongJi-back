package com.example.tomyongji.qna.repository;

import com.example.tomyongji.qna.entity.QnaAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaAnswerRepository extends JpaRepository<QnaAnswer,Long> {

    @EntityGraph(attributePaths = {"writer"})
    Page<QnaAnswer> findByQuestionId(Long questionId, Pageable pageable);
}
