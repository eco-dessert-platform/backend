package com.bbangle.bbangle.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum BbangleErrorCode {

    UNKNOWN_CATEGORY(-1, "올바르지 않은 Category 입니다. 다시 입력해주세요", BAD_REQUEST),
    DUPLICATE_NICKNAME(-2, "중복된 닉네임이에요!", CONFLICT),
    EXCEED_NICKNAME_LENGTH(-3, "닉네임은 20자 제한이에요!", BAD_REQUEST),

    NOTFOUND_MEMBER(-4, "해당 user가 존재하지 않습니다.", NOT_FOUND),
    NOTFOUND_WISH_INFO(-5, "일치하는 스토어 찜을 찾지못했습니다.", NOT_FOUND),
    PRICE_NOT_OVER_ZERO(-8, "0원 이상의 가격을 입력해주세요", BAD_REQUEST),
    INVALID_CATEGORY(-9, "존재하지 않는 카테고리입니다.", BAD_REQUEST),
    BOARD_NOT_FOUND(-10, "존재하지 않는 게시글입니다.", BAD_REQUEST),
    RANKING_NOT_FOUND(-11, "해당 게시글의 랭킹이 존재하지 않습니다.", BAD_REQUEST),
    INVALID_CURSOR_ID(-12, "유효하지 않은 cursorId 입니다.", BAD_REQUEST),
    NOTIFICATION_NOT_FOUND(-13, "존재하지 않는 공지사항입니다.", BAD_REQUEST),
    INVALID_FOLDER_TITLE(-14, "유효하지 않은 폴더 제목입니다.", BAD_REQUEST),
    OVER_MAX_FOLDER(-15, "10개를 초과한 폴더를 생성하실 수 없습니다.", BAD_REQUEST),
    FOLDER_NAME_ALREADY_EXIST(-16, "이미 존재하는 폴더 이름은 다시 사용하실 수 없습니다.", BAD_REQUEST),
    INVALID_FOLDER_MEMBER(-17, "폴더 생성 시 멤버 정보는 필수입니다.", BAD_REQUEST),
    FOLDER_NOT_FOUND(-18, "해당 폴더를 찾을 수 없습니다.", BAD_REQUEST),
    DEFAULT_FOLDER_NAME_CANNOT_CHNAGE(-19, "기본 폴더는 이름을 변경할 수 없습니다.", BAD_REQUEST),
    ALREADY_ON_WISHLIST(-20, "이미 위시리스트에 존재하는 게시글입니다.", BAD_REQUEST),
    WISHLIST_BOARD_NOT_FOUND(-21, "해당 게시글 찜 내역을 찾을 수 없습니다.", BAD_REQUEST),
    WISHLIST_BOARD_ALREADY_CANCELED(-22, "이미 찜 게시글에서 삭제하였습니다.", BAD_REQUEST),
    CANNOT_DELETE_DEFAULT_FOLDER(-23, "기본 폴더는 삭제할 수 없습니다.", BAD_REQUEST),
    FOLDER_ALREADY_DELETED(-24, "이미 삭제된 폴더는 다시 삭제할 수 없습니다.", BAD_REQUEST),
    CANNOT_UPDATE_ALREADY_DELETED_FOLDER(-25, "이미 삭제된 폴더는 변경할 수 없습니다.", BAD_REQUEST),
    FOLDER_ID_MUST_NOT_NULL(-26, "폴더 아이디는 반드시 포함되어야 합니다.", BAD_REQUEST),
    STORE_NOT_FOUND(-27, "존재하지 않는 스토어입니다", BAD_REQUEST),
    PREFERENCE_NOT_FOUND(-28, "존재하지 않는 선호타입입니다.", BAD_REQUEST),
    PREFERENCE_ALREADY_ASSIGNED(-29, "이미 선호타입을 등록하였습니다.", BAD_REQUEST),
    MEMBER_PREFERENCE_NOT_FOUND(-30, "유저의 선호타입 내역을 확인할 수 없습니다.", BAD_REQUEST),
    IMAGE_NOT_FOUND(-31, "해당하는 이미지를 찾을 수 없습니다.", NOT_FOUND),
    REVIEW_NOT_FOUND(-32, "존재하지 않는 리뷰입니다", BAD_REQUEST),
    PUSH_NOT_FOUND(-33, "존재하지 않는 푸시 알림 신청입니다.", BAD_REQUEST),
    EMPTY_KEYWORD(-34, "검색어가 비어있습니다.", BAD_REQUEST),
    REVIEW_MEMBER_NOT_PROPER(-35, "해당 리뷰를 작성한 사용자가 아닙니다.", BAD_REQUEST),
    EMPTY_PRODUCT_ITEM(-36, "게시판 아이디에 해당하는 상품이 없습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    SURVEY_NOT_FOUND(-37, "수정하고자 하는 설문 정보가 존재하지 않습니다.", NOT_FOUND),
    IMAGE_URL_NULL(-38, "Image URL이 Null입니다.", NOT_FOUND),
    DEFAULT_FOLDER_NAME_USED(-39, "폴더 이름을 기본 폴더로 수정할 수 없습니다.", BAD_REQUEST),
    REVIEW_ALREADY_LIKED(-40, "이미 도움돼요를 누른 게시글입니다.", BAD_REQUEST),
    INVALID_TOKEN_TYPE(-41, "유효하지 않은 형태소 타입입니다.", NOT_FOUND),
    AlREADY_ON_REVIEWLIKE(-42, "이미 좋아요를 누른 리뷰 댓글입니다.", NOT_FOUND),
    NOTFOUND_DELIVERYCOMPANY(-43, "존재하지 않는 택배회사입니다.", NOT_FOUND),
    INVALID_BOARD_PRICE(-44, "가격은 음수가 될 수 없습니다.", BAD_REQUEST),
    INVALID_BOARD_DISCOUNT(-45, "할인율은 0~100 사이여야 합니다.", BAD_REQUEST),
    INVALID_DELIVERY_FEE(-46, "배송비는 음수가 될 수 없습니다.", BAD_REQUEST),
    INVALID_PRODUCT_DELIVERY_DAY(-47, "가능한 발송 요일이 하나라도 있어야 합니다. ", BAD_REQUEST),
    INVALID_PRODUCT_NAME(-48, "상품 이름은 3글자 이상 50글자 이하여야 합니다.", BAD_REQUEST),
    INVALID_PRODUCT_INFO_NOTICE_NAME(-49, "상품 정보 제공 이름은 3글자 이상 50글자 이하여야 합니다.", BAD_REQUEST),
    BOARD_WITH_IMAGE_NOTFOUND(-50, "상품 이미지가 없는 게시글이 존재합니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    UPLOAD_STREAM_CLOSE_ERROR(-51, "파일 업로드 중 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    NULL_INPUT_STREAM(-52, "파일이 유효하지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_VALID_CONTENT_TYPE(-53, "컨텐츠 타입이 유효하지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_VALID_FILE_SIZE(-54, "파일의 크기를 다시 설정해주세요.", HttpStatus.INTERNAL_SERVER_ERROR),
    NULL_FILE_URL(-55, "파일 URL 주소를 확인해주세요.", BAD_REQUEST),
    CSV_NOT_READ_ERROR(-56, "CSV 파일을 읽는 중 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    CSV_NOT_CONVERT_ERROR(-57, "CSV 파일을 리스트로 변환 도중 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_VALID_INDEX(-58, "유효하지 않은 CSV 컬럼값 입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INPUT_STREAM_NOT_CLOSE(-59, "InputStream이 정상적으로 종료되지 않았습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  
    //AWS Error (600)
    AWS_ERROR(-600, "AWS S3 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    AWS_CLIENT_ERROR(-601, "AWS SDK 클라이언트 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    AWS_ACL_BLOCK(-602, "S3에 버킷의 ACL권한을 설정해주세요.", HttpStatus.INTERNAL_SERVER_ERROR),
    AWS_ENVIRONMENT(-603, "AWS 환경에서 진행해주세요.", HttpStatus.INTERNAL_SERVER_ERROR),
    AWS_S3_FILE_NOT_FOUND(-604, "URL에 파일이 존재하지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    STREAM_CLOSING_ERROR(-605, "Stream 파일 닫기에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    GOOGLE_AUTHENTICATION_ERROR(-995, "구글 인증 토큰 발행 중 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    JSON_SERIALIZATION_ERROR(-996, "json 변환 중 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FCM_INITIALIZATION_ERROR(-997, "Firebase 초기화 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FCM_CONNECTION_ERROR(-998, "FCM 서버 요청 중 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_SERVER_ERROR(-999, "서버 내부 에러입니다", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    public static BbangleErrorCode of(int code) {
        return Stream.of(BbangleErrorCode.values())
            .filter(message -> message.getCode() == code)
            .findFirst()
            .orElseThrow(BbangleException::new);
    }

    public static BbangleErrorCode of(String message) {
        return Stream.of(BbangleErrorCode.values())
            .filter(error -> error.getMessage()
                .equals(message))
            .findFirst()
            .orElseThrow(BbangleException::new);
    }


}
