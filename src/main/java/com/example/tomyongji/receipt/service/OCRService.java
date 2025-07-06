package com.example.tomyongji.receipt.service;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.dto.OCRResultDto;
import com.example.tomyongji.receipt.dto.ReceiptCreateDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.mapper.ReceiptMapper;
import com.example.tomyongji.validation.CustomException;
import com.example.tomyongji.validation.ErrorMsg;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class OCRService {

    private final ReceiptService receiptService;
    private final ReceiptMapper receiptMapper;
    private final UserRepository userRepository;

    @Value("${ocr.apiUrl}")
    private String apiURL;
    @Value("${ocr.secretKey}")
    private String secretKey;

    public OCRService(ReceiptService receiptService, ReceiptMapper receiptMapper,
        UserRepository userRepository) {
        this.receiptService = receiptService;
        this.receiptMapper = receiptMapper;
        this.userRepository = userRepository;
    }

    /**
     * OCR 이미지만 처리하고 결과 DTO 반환
     */
    public OCRResultDto processImage(MultipartFile file) {
        try {
            return sendOCRRequest(file);
        } catch (IOException e) {
            log.error("OCR I/O 에러", e);
            throw new CustomException("OCR 요청 중 I/O 오류가 발생했습니다.", 500);
        } catch (ParseException e) {
            log.error("날짜 파싱 오류", e);
            throw new CustomException("OCR 응답 날짜 파싱에 실패했습니다.", 500);
        } catch (Exception e) {
            log.error("OCR 처리 오류", e);
            throw new CustomException("OCR 처리 중 예외가 발생했습니다.", 500);
        }
    }

    /**
     * OCR 처리 후 영수증을 DB에 저장
     */
    @Transactional
    public void uploadOcrReceipt(OCRResultDto ocrResultDto, String userId, UserDetails currentUser) {
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(ErrorMsg.NOT_FOUND_USER, 400));

        ReceiptDto receiptDto = receiptMapper.toReceiptDto(ocrResultDto);
        ReceiptCreateDto createDto = receiptMapper.toReceiptCreateDto(receiptDto);
        createDto.setUserId(userId);
        receiptService.createReceipt(createDto, currentUser);
    }

    /**
     * CLOVA OCR API 호출 및 결과 파싱
     */
    private OCRResultDto sendOCRRequest(MultipartFile file) throws IOException, ParseException {
        ObjectMapper objectMapper = new ObjectMapper();

        // 메시지 JSON 구성
        ObjectNode messageNode = objectMapper.createObjectNode();
        messageNode.put("version", "V2");
        messageNode.put("requestId", UUID.randomUUID().toString());
        messageNode.put("timestamp", System.currentTimeMillis());

        ObjectNode imageNode = objectMapper.createObjectNode();
        String ext = getFileExt(file);
        imageNode.put("format", ext);
        imageNode.put("name", file.getOriginalFilename());
        ArrayNode images = objectMapper.createArrayNode();
        images.add(imageNode);
        messageNode.set("images", images);

        String jsonMessage = messageNode.toString();

        // multipart body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> jsonPart = new HttpEntity<>(jsonMessage, jsonHeaders);
        body.add("message", jsonPart);

        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override public String getFilename() { return file.getOriginalFilename(); }
        };
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        HttpEntity<Resource> filePart = new HttpEntity<>(fileResource, fileHeaders);
        body.add("file", filePart);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-OCR-SECRET", secretKey);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = new org.springframework.web.client.RestTemplate()
            .postForEntity(apiURL, requestEntity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomException("OCR API 호출 실패: " + response.getStatusCode(), 502);
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        return parseOCRResult(root);
    }

    /**
     * JSON 응답에서 날짜, 상호명, 금액 추출
     */
    private OCRResultDto parseOCRResult(JsonNode root) throws ParseException {
        JsonNode result = root.path("images").get(0).path("receipt").path("result");

        JsonNode dateF = result.path("paymentInfo").path("date").path("formatted");
        String dateString = String.format("%s-%s-%s",
            dateF.path("year").asText(),
            dateF.path("month").asText(),
            dateF.path("day").asText());
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);

        String content = result.path("storeInfo").path("name").path("text").asText();

        String priceVal = result.path("totalPrice").path("price").path("formatted").path("value").asText();
        int withdrawal = Integer.parseInt(priceVal.replaceAll(",", ""));
        if (withdrawal == 0) {
            throw new CustomException("OCR 처리 결과 금액이 0입니다.", 500);
        }

        return new OCRResultDto(date, content, withdrawal);
    }

    /**
     * 지원 포맷 확인 및 확장자 반환
     */
    private String getFileExt(MultipartFile file) {
        String name = file.getOriginalFilename();
        String ext = (name != null && name.contains(".")) ?
            name.substring(name.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!List.of("jpg","jpeg","png","pdf").contains(ext)) {
            throw new CustomException("지원하지 않는 파일 형식: " + ext, 400);
        }
        return ext.equals("jpeg") ? "jpg" : ext;
    }
}