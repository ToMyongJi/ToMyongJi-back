package com.example.tomyongji.receipt.service;

import com.example.tomyongji.receipt.dto.OCRResultDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.validation.CustomException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${ocr.apiUrl]")
    private String apiURL ;
    @Value("${ocr.secretKey]")
    private String secretKey;

    private final ReceiptService receiptService;

    public OCRService(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }


    @Transactional
    public OCRResultDto processImage(MultipartFile file, Long id) {
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
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray imagesArray = jsonObject.getJSONArray("images");
        JSONObject firstImage = imagesArray.getJSONObject(0);
        JSONObject receipt = firstImage.getJSONObject("receipt").getJSONObject("result");

        // 날짜 추출
        JSONObject paymentInfo = receipt.getJSONObject("paymentInfo");
        JSONObject dateObject = paymentInfo.getJSONObject("date").getJSONObject("formatted");
        String year = dateObject.getString("year");
        String month = dateObject.getString("month");
        String day = dateObject.getString("day");
        String dateString = year + "-" + month + "-" + day;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse(dateString);

        // 상호명 추출
        String content = receipt.getJSONObject("storeInfo").getJSONObject("name").getString("text");

        // 출금 금액 추출
        int withdrawal = 0;
        if (receipt.has("totalPrice") && receipt.getJSONObject("totalPrice").has("price")) {
            JSONObject totalPrice = receipt.getJSONObject("totalPrice").getJSONObject("price").getJSONObject("formatted");
            withdrawal = Integer.parseInt(totalPrice.getString("value").replaceAll(",", ""));
        } else {
            throw new CustomException("OCR 처리 중 오류가 발생했습니다. 유효한 금액 필드를 찾을 수 없습니다.", 500);
        }

        if (withdrawal == 0) {
            throw new CustomException("OCR 처리 중 오류가 발생했습니다. 추출된 금액이 0입니다.", 500);
        }

        return new OCRResultDto(date, content, withdrawal);
    }

    private ReceiptDto OcrToRecipt(OCRResultDto ocrResultDto) {
        ReceiptDto receiptDto = new ReceiptDto();
        receiptDto.setDate(ocrResultDto.getDate());  // Date 타입으로 설정
        receiptDto.setContent(ocrResultDto.getContent());
        receiptDto.setWithdrawal(ocrResultDto.getWithdrawal());
        return receiptDto;
    }



    public void uploadOcrReceipt(OCRResultDto receiptDto, Long id) {
        // ReceiptDto를 사용하여 데이터베이스에 저장하는 로직을 여기에 구현
        // receiptService의 createReceipt 메서드를 사용해 영수증 저장

        receiptService.createReceipt(OcrToRecipt(receiptDto), id);
    }
}






