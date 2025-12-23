package com.example.tomyongji.validation;

public class ErrorMsg {

    // **400 Bad Request**
    public static final String INCORRECT_ROLE_VALUE = "Role의 값이 올바르지 않습니다.";
    public static final String EMPTY_BODY = "Body가 비어있습니다.";
    public static final String EMPTY_FILE = "PDF 파일이 존재하지 않습니다.";
    public static final String NOT_FOUND_INCODING = "지원하지 않는 인코딩 방식입니다.";
    public static final String EMPTY_CONTENT = "학생회비 사용 내용을 작성해주세요.";
    public static final String EMPTY_MONEY = "입출금 내역을 작성해주세요.";
    public static final String DUPLICATED_FLOW = "입금과 출금 둘 중 하나만 적어주세요.";
    public static final String INVALID_KEYWORD = "검색어는 두 글자 이상으로 작성해주세요.";
    public static final String NO_RECEIPTS_TO_TRANSFER = "이월할 영수증이 없습니다.";
    public static final String INVALID_DATE_SEARCH = "연도(Year) 정보 없이 월(Month)만 검색할 수 없습니다.";

    // **401 Unauthorized**
    public static final String NOT_VERIFY_EMAIL = "이메일 인증이 되지 않은 유저입니다.";
    public static final String NOT_HAVE_STUDENT_CLUB = "소속 인증이 되지 않은 유저입니다.";
    public static final String NOT_VERIFY_CLUB = "소속 인증이 완료되지 않은 유저입니다.";

    // **403 Forbidden**
    public static final String NO_AUTHORIZATION_USER = "접근 권한이 없습니다"; // 가입된 유저가 아닌 경우
    public static final String NO_AUTHORIZATION_ROLE = "접근 권한이 없습니다"; // 접근 가능한 ROLE이 아닌 경우
    public static final String NO_AUTHORIZATION_BELONGING = "접근 권한이 없습니다"; // 접근 가능한 소속이 아닌 경우

    // **404 Not Found**
    public static final String NOT_FOUND_COLLEGE = "단과대를 찾을 수 없습니다.";
    public static final String NOT_FOUND_STUDENT_CLUB = "학생회를 찾을 수 없습니다.";
    public static final String NOT_FOUND_PRESIDENT = "학생회장을 찾을 수 없습니다.";
    public static final String NOT_FOUND_MEMBER = "부원을 찾을 수 없습니다.";
    public static final String NOT_FOUND_USER_EMAIL = "해당 이메일의 ID를 찾을 수 없습니다";
    public static final String NOT_FOUND_USER = "유저를 찾을 수 없습니다.";
    public static final String NOT_FOUND_RECEIPT = "영수증을 찾을 수 없습니다.";
    public static final String NOT_FOUND_BREAKDOWN = "거래내역서를 찾을 수 없습니다.";

    // **409 Conflict**
    public static final String EXISTING_USER = "이미 등록된 유저 정보입니다.";
    public static final String MISMATCHED_USER = "유저 정보가 일치하지 않습니다.";

    // **422 Unprocessable Entity**
    public static final String ERROR_SEND_EMAIL = "이메일 전송 중 오류가 발생했습니다.";
    public static final String PARSING_ERROR = "PDF 파싱 중 오류가 발생했습니다.";
    public static final String PARSING_TRANSACTION_ERROR = "PDF 거래내역 파싱 중 오류가 발생했습니다.";
    public static final String AUTENTICITY_FAILURE = "진위확인에 실패했습니다.";

    // **500 Internal Server Error**
    public static final String EXTERNAL_SERVER_ERROR = "외부 서버의 오류가 발생했습니다.";
}
