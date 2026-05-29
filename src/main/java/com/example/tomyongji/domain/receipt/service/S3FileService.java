package com.example.tomyongji.domain.receipt.service;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.example.tomyongji.global.error.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static com.example.tomyongji.global.error.ErrorMsg.INVALID_EXCEL_FILE;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // =========================================================================
    // 1. 업로드 (Upload)
    // =========================================================================
    public String upload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";

        // 확장자 추출 (.xlsx 등)
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // S3에 저장될 고유한 난수 파일명 생성
        String fileId = UUID.randomUUID().toString() + extension;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        log.info("S3 파일 업로드 시작: {}", fileId);
        amazonS3.putObject(bucket, fileId, file.getInputStream(), metadata);
        log.info("S3 파일 업로드 완료: {}", fileId);

        return fileId;
    }

    // =========================================================================
    // 2. 다운로드 (Download)
    // =========================================================================
    public InputStream downloadFile(String fileId) throws IOException {
        try {
            S3Object s3Object = amazonS3.getObject(bucket, fileId);

            try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
                byte[] content = IOUtils.toByteArray(inputStream);
                return new ByteArrayInputStream(content);
            }

        } catch (AmazonS3Exception e) {
            // 업로드한 Excel 파일이 존재하지 않을 때
            log.error("S3 파일 다운로드 중 시스템 오류 발생: {}", e.getMessage());
            throw new CustomException(INVALID_EXCEL_FILE, 400);
        }
    }

    // =========================================================================
    // 3. 삭제 (Delete)
    // =========================================================================
    public void deleteFile(String fileId) {
        try {
            amazonS3.deleteObject(bucket, fileId);
        } catch (Exception e) {
            // 삭제 실패가 전체 로직(DB 저장)을 롤백시키면 안 되므로 예외를 먹고 로그만 남깁니다.
            log.error("S3 파일 삭제 실패 {}", e.getMessage());
        }
    }
}