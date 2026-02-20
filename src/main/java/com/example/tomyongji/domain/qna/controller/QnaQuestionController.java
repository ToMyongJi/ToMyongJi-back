package com.example.tomyongji.domain.qna.controller;

import com.example.tomyongji.global.common.response.ApiResponse;
import com.example.tomyongji.domain.qna.dto.request.QuestionSaveDto;
import com.example.tomyongji.domain.qna.dto.response.PageResponseDto;
import com.example.tomyongji.domain.qna.dto.response.QuestionDto;
import com.example.tomyongji.domain.qna.service.QnaQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name="QnA 게시판 질문글 조회 api", description = "게시판 질문글과 관련된 API들입니다.")
@RestController
@RequestMapping("/api/qna/question")
@RequiredArgsConstructor
public class QnaQuestionController {
    private final QnaQuestionService questionService;

    @Operation(summary = "질문글 작성 api", description = "유저 아이디를 통해 특정 학생회의 질문글을 작성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<QuestionDto>> createQuestion(@Valid @RequestBody QuestionSaveDto questionDto, @AuthenticationPrincipal UserDetails currentUser) {
        QuestionDto createdQuestion = questionService.createQuestion(questionDto, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.onCreated(createdQuestion));
    }

    @Operation(summary = "질문글 페이지별 조회 api", description = "페이지 정보, 질문글 수를 통해 질문글을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<QuestionDto>>> getQuestionPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponseDto<QuestionDto> questionPage = questionService.findAllQuestionsPaging(page, size);
        return ResponseEntity.ok(ApiResponse.onSuccess(questionPage));
    }

    @Operation(summary = "질문글 1개 조회 api", description = "질문글 아이디를 통해 특정 질문글을 조회합니다.")
    @GetMapping("/{questionId}")
    public ResponseEntity<ApiResponse<QuestionDto>> getQuestionById(@PathVariable("questionId") Long questionId) {
        QuestionDto question = questionService.findQuestionById(questionId);
        return ResponseEntity.ok(ApiResponse.onSuccess(question));
    }

    @Operation(summary = "질문글 수정 api", description = "질문글 아이디를 통해 특정 질문글을 수정합니다.")
    @PutMapping("/{questionId}")
    public ResponseEntity<ApiResponse<QuestionDto>> updateQuestion(
            @PathVariable("questionId") Long questionId,
            @Valid @RequestBody QuestionSaveDto questionDto,
            @AuthenticationPrincipal UserDetails currentUser) {
        QuestionDto updatedQuestion = questionService.updateQuestion(questionId, questionDto, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.onSuccess(updatedQuestion));
    }

    @Operation(summary = "질문글 삭제 api", description = "질문글 아이디를 통해 특정 질문글을 삭제합니다.")
    @DeleteMapping("/{questionId}")
    public ResponseEntity<ApiResponse<QuestionDto>> deleteQuestion(@PathVariable("questionId") Long questionId, @AuthenticationPrincipal UserDetails currentUser) {
        QuestionDto question = questionService.deleteQuestion(questionId, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.onSuccess(question));
    }
}
