package com.example.tomyongji.domain.qna.repository;

import com.example.tomyongji.domain.qna.entity.QnaQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaQuestionRepository extends JpaRepository<QnaQuestion,Long> {

    @Override
    @EntityGraph(attributePaths = {"writer"}) // writer만 fetch join
    Page<QnaQuestion> findAll(Pageable pageable);
}
