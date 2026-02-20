package com.example.tomyongji.domain.qna.controller;

import com.example.tomyongji.global.common.response.ApiResponse;
import com.example.tomyongji.domain.qna.dto.request.AnswerSaveDto;
import com.example.tomyongji.domain.qna.dto.response.AnswerDto;
import com.example.tomyongji.domain.qna.dto.response.PageResponseDto;
import com.example.tomyongji.domain.qna.service.QnaAnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name="QnA 게시판 답변글 조회 api", description = "게시판 답변글과 관련된 API들입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qna/answer")
public class QnaAnswerController {
    private final QnaAnswerService qnaAnswerService;

    @Operation(summary = "답변글 작성 api", description = "질문에 대한 답변글을 작성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<AnswerDto>> createAnswer(
            @RequestParam Long questionId, @Valid @RequestBody AnswerSaveDto answerDto, @AuthenticationPrincipal UserDetails currentUser) {
        AnswerDto createdAnswer = qnaAnswerService.createAnswer(questionId, answerDto, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.onCreated(createdAnswer));
    }

    @Operation(summary = "질문에 대한 답변글 페이지별 조회 api", description = "페이지 정보, 답변글 수를 통해 특정 질문에 대한 답변글을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<AnswerDto>>> getAnswerByQuestionIdPaging(
            @RequestParam Long questionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponseDto<AnswerDto> answerPage = qnaAnswerService.findAnswersByQuestionIdPaging(questionId, page, size);
        return ResponseEntity.ok(ApiResponse.onSuccess(answerPage));
    }

    @Operation(summary = "답변글 수정 api", description = "답변글 아이디를 통해 특정 답변글을 수정합니다.")
    @PutMapping("/{answerId}")
    public ResponseEntity<ApiResponse<AnswerDto>> updateAnswer(
            @PathVariable("answerId") Long answerId,
            @Valid @RequestBody AnswerSaveDto answerSaveDto,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        AnswerDto updatedAnswer = qnaAnswerService.updateAnswer(answerId, answerSaveDto, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.onSuccess(updatedAnswer));
    }

    @Operation(summary = "답변글 삭제 api", description = "답변글 아이디를 통해 특정 답변글을 삭제합니다.")
    @DeleteMapping("/{answerId}")
    public ResponseEntity<ApiResponse<AnswerDto>> deleteAnswer(@PathVariable("answerId") Long answerId, @AuthenticationPrincipal UserDetails currentUser) {
        AnswerDto deletedAnswer = qnaAnswerService.deleteAnswer(answerId, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.onSuccess(deletedAnswer));
    }
}
