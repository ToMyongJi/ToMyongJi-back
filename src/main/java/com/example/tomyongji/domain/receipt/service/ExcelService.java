package com.example.tomyongji.domain.receipt.service;
import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.dto.*;
import com.example.tomyongji.domain.receipt.dto.ExcelParseResultResponse;
import com.example.tomyongji.domain.receipt.entity.ExcelColumn;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.domain.receipt.repository.ReceiptRepository;
import com.example.tomyongji.global.error.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

import static com.example.tomyongji.global.error.ErrorMsg.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelService {

    private final ExcelColumnService excelColumnService;
    private final ExcelAIService excelAIService;
    private final ExcelParsingService excelParser;
    private final S3FileService s3FileService;
    private final UserRepository userRepository;
    private final ReceiptRepository receiptRepository;

    public ExcelAnalyzeResponse analyzeExcel(MultipartFile file, boolean forceAnalyze, String currentUserId) throws Exception {
        User user = userRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));

        StudentClub studentClub = user.getStudentClub();
        if (studentClub == null) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }

        // 1. S3 파일 업로드 및 fileID 획득
        String fileId = s3FileService.upload(file);

        // 2. EasyExcel로 파일 전체 데이터를 메모리에 1회 로드
        List<Map<Integer, String>> allRows = excelParser.readExcelInMemory(file.getInputStream());

        // 3. 기존 분석 정보 (ExcelColumn) 조회
        Optional<ExcelColumn> existingColumn = excelColumnService.findByStudentClub(studentClub);

        // DB 데이터가 없거나, 재분석(forceAnalyze)을 요청한 경우 AI 사용
        boolean useAI = forceAnalyze || existingColumn.isEmpty();

        String analysisSource = null;
        ExcelColumnMappingDto config = null;
        ExcelParsingService.HeaderMapping mapping = null;

        // [Step 1] DB 설정으로 먼저 시도 (useAI가 false일 때만)
        if (!useAI) {
            try {
                ExcelColumn excelColumn = existingColumn.get();
                config = excelColumnService.toConfig(excelColumn);
                mapping = excelParser.searchHeader(allRows, config);
                analysisSource = "DB";

            } catch (Exception e) {
                // 헤더 찾기 실패 시 시스템을 멈추지 않고 AI 모드로 전환
                log.warn("기존 DB 설정으로 엑셀 헤더 탐색 실패. AI 분석으로 자동 전환합니다.", e.getMessage());
                useAI = true; // 플래그를 true로 바꿔서 아래의 AI 로직을 타게 만듦
            }
        }

        // [Step 2] AI 분석 실행 (처음부터 AI분석이거나, [Step 1]에서 DB 탐색에 실패한 경우)
        if (useAI) {
            analysisSource = "AI";

            // AI 서비스 호출
            config = excelAIService.extractColumnConfig(allRows);

            // AI 분석 결과로 헤더 찾기
            mapping = excelParser.searchHeader(allRows, config);
        }

        ExcelParseResultResponse parseResult = excelParser.parseAll(allRows, mapping, config, studentClub);

        return ExcelAnalyzeResponse.builder()
                .analysisSource(analysisSource)
                .fileId(fileId)
                .analyzeResult(config)
                .parseResult(parseResult)
                .build();
//        // 4. 파싱 예시(Preview) 1행 추출
//        List<Receipt> previewReceipts = excelParser.getPreview(allRows, mapping, config, 5, studentClub);
//
//        return buildResponse(analysisSource, fileId, mapping, config, previewReceipts);
    }

    // 파싱 결과 미리보기
    public ExcelParseResultResponse getPreviewOnly(ExcelMappingRequest request, String currentUserId) throws Exception {

        User user = userRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));
        StudentClub studentClub = user.getStudentClub();
        if (studentClub == null) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }

        // 프론트엔드가 보내준 매핑 정보가 없으면 에러 (미리보기는 반드시 매핑 정보가 필요함)
        if (request.getAnalyzeResult() == null) {
            throw new CustomException("컬럼 매핑 정보가 필요합니다.", 400);
        }

        ExcelColumnMappingDto config = request.getAnalyzeResult();

        // S3에서 파일 다운로드
        InputStream fileInputStream = s3FileService.downloadFile(request.getFileId());

        // 새로운 매핑 정보로 헤더 및 파싱 수행
        List<Map<Integer, String>> allRows = excelParser.readExcelInMemory(fileInputStream);

        ExcelParsingService.HeaderMapping mapping;
        try {
            mapping = excelParser.searchHeader(allRows, config);
        } catch (Exception e) {
            throw new CustomException("선택하신 열 이름을 엑셀 파일에서 찾을 수 없습니다.", 400);
        }

        // 전체 행 파싱 후 결과 반환
        return excelParser.parseAll(allRows, mapping, config, studentClub);
    }

    @Transactional // 영수증 일괄 저장 및 컬럼 설정 저장이 하나의 트랜잭션으로 묶임
    public ExcelReceiptSaveResponse saveExcelData(ExcelMappingRequest request, String currentUserId) throws Exception {

        // 1. 사용자 및 학생회 검증
        User user = userRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));
        StudentClub studentClub = user.getStudentClub();
        if (studentClub == null) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }

        // 2. 파싱 설정(Config) 획득 및 DB 덮어쓰기
        ExcelColumnMappingDto config;
        if (request.getAnalyzeResult() != null) {
            // 사용자가 화면에서 열 매핑을 만졌거나 확정한 경우 DB 갱신
            ExcelColumn savedColumn = excelColumnService.saveOrUpdate(request.getAnalyzeResult(), studentClub);
            config = excelColumnService.toConfig(savedColumn);
        } else {
            // 기존 양식을 그대로 쓰는 경우 DB 조회
            ExcelColumn existingColumn = excelColumnService.findByStudentClub(studentClub)
                    .orElseThrow(() -> new CustomException("저장된 엑셀 양식 설정이 없습니다.", 404));
            config = excelColumnService.toConfig(existingColumn);
        }

        // 3. S3에서 원본 파일 다운로드 및 메모리 로드
        InputStream fileInputStream = s3FileService.downloadFile(request.getFileId());
        List<Map<Integer, String>> allRows = excelParser.readExcelInMemory(fileInputStream);

        // 헤더 탐색
        ExcelParsingService.HeaderMapping mapping = excelParser.searchHeader(allRows, config);

        // 4. 조작이 완료된 깨끗한 데이터로 최종 파싱 및 유효성 검사 수행
        ExcelParseResultResponse parseResult = excelParser.parseAll(allRows, mapping, config, studentClub);

        // 5. 검증 실패가 단 하나라도 존재한다면 DB에 저장하지 않고 에러 리포트 반환
        if (parseResult.getFailCount() > 0) {
            return ExcelReceiptSaveResponse.builder()
                    .status(ExcelReceiptSaveResponse.Status.VALIDATION_FAILED)
                    .parseResult(parseResult)
                    .build();
        }

        // 6. 모든 행이 완벽하게 검증 통과(Success)했다면 DB 일괄 저장
        receiptRepository.saveAll(parseResult.getSuccessfulReceipts());

        // 7. S3 임시 파일 삭제 (더 이상 필요 없음)
        s3FileService.deleteFile(request.getFileId());

        // 8. 성공 응답
        return ExcelReceiptSaveResponse.builder()
                .status(ExcelReceiptSaveResponse.Status.SAVED)
                .parseResult(parseResult)
                .build();
    }
}
