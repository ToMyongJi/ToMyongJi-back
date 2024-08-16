package com.example.tomyongji.receipt.service;

import com.example.tomyongji.receipt.dto.OCRResultDto;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OCRService {

    private final String apiURL = "YOUR_API_URL";
    private final String secretKey = "YOUR_SECRET_KEY";

    public OCRResultDto processImage(MultipartFile file)
        throws IOException, ParseException, JSONException {
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
        image.put("format", "jpg");
        image.put("name", "demo");

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

        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = br.readLine()) != null) {
            response.append(inputLine);
        }
        br.close();

        return parseOCRResult(response.toString());
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
            fileString.append("Content-Disposition:form-data; name=\"file\"; filename=\"").append(file.getOriginalFilename()).append("\"\r\n");
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

    private OCRResultDto parseOCRResult(String jsonResponse) throws ParseException, JSONException {
        // JSON 파싱 로직
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray imagesArray = jsonObject.getJSONArray("images");
        JSONObject firstImage = imagesArray.getJSONObject(0);
        JSONArray fields = firstImage.getJSONArray("fields");

        Date date = null;
        String content = null;
        int withdrawal = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.getJSONObject(i);
            String inferText = field.getString("inferText");

            // 날짜 처리
            if (inferText.matches("\\d{4}-\\d{2}-\\d{2}")) {
                date = sdf.parse(inferText);
            }

            // 이용처 처리
            if (content == null) {
                content = inferText;
            }

            // 금액 처리
            if (inferText.matches("\\d+")) {
                withdrawal = Integer.parseInt(inferText);
            }
        }

        return new OCRResultDto(date, content, withdrawal);
    }
}



