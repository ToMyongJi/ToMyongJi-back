package com.example.tomyongji.receipt.service;

import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;
import static com.example.tomyongji.validation.ErrorMsg.NO_AUTHORIZATION_BELONGING;
import static com.example.tomyongji.validation.ErrorMsg.NO_AUTHORIZATION_USER;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.auth.service.CustomUserDetails;
import com.example.tomyongji.receipt.dto.OCRResultDto;
import com.example.tomyongji.receipt.dto.ReceiptCreateDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.mapper.ReceiptMapper;
import com.example.tomyongji.validation.CustomException;
import lombok.AllArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class OCRService {

    @Value("${ocr.apiUrl}")
    private String apiURL ;
    @Value("${ocr.secretKey}")
    private String secretKey;

    private final ReceiptService receiptService;
    private final ReceiptMapper receiptMapper;
    private final UserRepository userRepository;

    public OCRService(ReceiptService receiptService, ReceiptMapper receiptMapper,
        UserRepository userRepository) {
        this.receiptService = receiptService;
        this.receiptMapper = receiptMapper;
        this.userRepository = userRepository;
    }


    @Transactional
    public OCRResultDto processImage(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename).toLowerCase();

        // 확장자 확인
        String imageFormat;
        if (extension.equals("jpg") || extension.equals("jpeg")) {
            imageFormat = "jpg";
        } else if (extension.equals("png")) {
            imageFormat = "png";
        } else if (extension.equals("pdf")) {
            imageFormat = "pdf";
        } else {
            throw new CustomException("지원하지 않는 파일 형식입니다: " + extension + ". 지원하는 형식: jpg, jpeg, png, pdf", 400);
        }

        // OCR API 요청 및 처리
        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setReadTimeout(30000);
            con.setRequestMethod("POST");

            String boundary = "----" + UUID.randomUUID().toString().replaceAll("-", "");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setRequestProperty("X-OCR-SECRET", secretKey);

            JSONObject json = new JSONObject();
            json.put("version", "V2");
            json.put("requestId", UUID.randomUUID().toString());
            json.put("timestamp", System.currentTimeMillis());

            JSONObject image = new JSONObject();
            image.put("format", imageFormat);
            image.put("name", originalFilename);

            JSONArray images = new JSONArray();
            images.put(image);
            json.put("images", images);

            con.connect();

            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                writeMultiPart(wr, json.toString(), file, boundary);
            }

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            // OCR 결과 파싱 후 ReceiptDto로 반환
            return parseOCRResult(response.toString());

        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
            throw new CustomException("OCR 처리 중 오류가 발생했습니다.", 500);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private void writeMultiPart(OutputStream out, String jsonMessage, MultipartFile file, String boundary) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition:form-data; name=\"message\"\r\n\r\n");
        sb.append(jsonMessage);
        sb.append("\r\n");

        out.write(sb.toString().getBytes("UTF-8"));
        out.flush();

        if (!file.isEmpty()) {
            out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
            StringBuilder fileString = new StringBuilder();
            fileString.append("Content-Disposition:form-data; name=\"file\"; filename=\"")
                .append(file.getOriginalFilename()).append("\"\r\n");
            fileString.append("Content-Type: application/octet-stream\r\n\r\n");
            out.write(fileString.toString().getBytes("UTF-8"));
            out.flush();

            try (InputStream fis = file.getInputStream()) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                out.write("\r\n".getBytes());
            }

            out.write(("--" + boundary + "--\r\n").getBytes("UTF-8"));
        }
        out.flush();
    }

    // OCR 결과에서 date, content, withdrawal 파싱 후 ReceiptDto로 반환
    private OCRResultDto parseOCRResult(String jsonResponse) throws ParseException, JSONException {
        // 디버깅을 위한 전체 응답 로그 출력
        System.out.println("OCR API Response: " + jsonResponse);

        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray imagesArray = jsonObject.getJSONArray("images");
        if (imagesArray.length() == 0) {
            throw new CustomException("OCR 응답에 이미지 데이터가 없습니다.", 500);
        }
        JSONObject firstImage = imagesArray.getJSONObject(0);
        JSONObject receiptResult = firstImage.getJSONObject("receipt").getJSONObject("result");

        // 1. 날짜 파싱
        JSONObject paymentInfo = receiptResult.getJSONObject("paymentInfo");
        String dateString = "";
        if (paymentInfo.has("date")) {  // date 키 존재 여부 체크
            Object dateObj = paymentInfo.get("date");
            if (dateObj instanceof String) {
                // 날짜가 문자열로 바로 전달되는 경우 (예: "2020-06-16")
                dateString = (String) dateObj;
            } else if (dateObj instanceof JSONObject) {
                JSONObject dateJson = (JSONObject) dateObj;
                if (dateJson.has("formatted")) {
                    Object formattedObj = dateJson.get("formatted");
                    if (formattedObj instanceof String) {
                        dateString = (String) formattedObj;
                    } else if (formattedObj instanceof JSONObject) {
                        JSONObject formattedJson = (JSONObject) formattedObj;
                        String year = formattedJson.getString("year");
                        String month = formattedJson.getString("month");
                        String day = formattedJson.getString("day");
                        dateString = year + "-" + month + "-" + day;
                    } else {
                        throw new CustomException("유효한 날짜 형식이 아닙니다.", 500);
                    }
                } else {
                    // formatted 필드가 없으면 직접 year, month, day 값 사용
                    String year = dateJson.getString("year");
                    String month = dateJson.getString("month");
                    String day = dateJson.getString("day");
                    dateString = year + "-" + month + "-" + day;
                }
            } else {
                throw new CustomException("유효한 날짜 형식이 아닙니다.", 500);
            }
        } else {
            // date 키가 없는 경우
            throw new CustomException("OCR 응답에 date 데이터가 없습니다.", 400);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse(dateString);

        // 2. 상호명 파싱 (storeInfo.name)
        JSONObject storeInfo = receiptResult.getJSONObject("storeInfo");
        String content = "";
        Object nameObj = storeInfo.get("name");
        if (nameObj instanceof String) {
            content = (String) nameObj;
        } else if (nameObj instanceof JSONObject) {
            JSONObject nameJson = (JSONObject) nameObj;
            if (nameJson.has("text")) {
                content = nameJson.getString("text");
            } else if (nameJson.has("formatted")) {
                Object formattedNameObj = nameJson.get("formatted");
                if (formattedNameObj instanceof String) {
                    content = (String) formattedNameObj;
                } else if (formattedNameObj instanceof JSONObject) {
                    JSONObject formattedNameJson = (JSONObject) formattedNameObj;
                    if (formattedNameJson.has("value")) {
                        content = formattedNameJson.getString("value");
                    } else {
                        throw new CustomException("유효한 상호명 정보를 찾을 수 없습니다.", 500);
                    }
                } else {
                    throw new CustomException("유효한 상호명 정보를 찾을 수 없습니다.", 500);
                }
            } else {
                throw new CustomException("유효한 상호명 정보를 찾을 수 없습니다.", 500);
            }
        } else {
            throw new CustomException("유효한 상호명 정보를 찾을 수 없습니다.", 500);
        }

        // 3. 출금 금액 파싱 (totalPrice.price)
        int withdrawal = 0;
        if (receiptResult.has("totalPrice") && receiptResult.getJSONObject("totalPrice").has("price")) {
            JSONObject priceObj = receiptResult.getJSONObject("totalPrice").getJSONObject("price");
            String priceString = "";
            if (priceObj.has("value")) {
                priceString = priceObj.getString("value");
            } else if (priceObj.has("formatted")) {
                Object formattedPriceObj = priceObj.get("formatted");
                if (formattedPriceObj instanceof String) {
                    priceString = (String) formattedPriceObj;
                } else if (formattedPriceObj instanceof JSONObject) {
                    JSONObject formattedPriceJson = (JSONObject) formattedPriceObj;
                    if (formattedPriceJson.has("value")) {
                        priceString = formattedPriceJson.getString("value");
                    } else {
                        throw new CustomException("유효한 금액 형식이 아닙니다.", 500);
                    }
                } else {
                    throw new CustomException("유효한 금액 형식이 아닙니다.", 500);
                }
            } else {
                throw new CustomException("OCR 응답에 금액 데이터가 없습니다.", 500);
            }
            withdrawal = Integer.parseInt(priceString.replaceAll(",", ""));
        } else {
            throw new CustomException("OCR 응답에 totalPrice.price 데이터가 없습니다.", 500);
        }

        if (withdrawal == 0) {
            throw new CustomException("OCR 처리 중 오류가 발생했습니다. 추출된 금액이 0입니다.", 500);
        }

        return new OCRResultDto(date, content, withdrawal);
    }

    public void uploadOcrReceipt(OCRResultDto ocrResultDto, String userId, UserDetails currentUser) {
        // ReceiptDto를 사용하여 데이터베이스에 저장하는 로직을 여기에 구현
        // receiptService의 createReceipt 메서드를 사용해 영수증 저장
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));

        ReceiptDto receiptDto = receiptMapper.toReceiptDto(ocrResultDto);
        ReceiptCreateDto receiptCreateDto = receiptMapper.toReceiptCreateDto(receiptDto);
        receiptCreateDto.setUserId(userId);

        receiptService.createReceipt(receiptCreateDto, currentUser);
    }
}






