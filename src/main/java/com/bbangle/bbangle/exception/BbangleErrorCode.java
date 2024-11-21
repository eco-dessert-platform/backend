package com.bbangle.bbangle.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.stream.Stream;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@Getter
@RequiredArgsConstructor
public enum BbangleErrorCode {

    UNKNOWN_CATEGORY(-1, "올바르지 않은 Category 입니다. 다시 입력해주세요", BAD_REQUEST),
    DUPLICATE_NICKNAME(-2, "중복된 닉네임이에요!", CONFLICT),
    EXCEED_NICKNAME_LENGTH(-3, "닉네임은 20자 제한이에요!", BAD_REQUEST),

    NOTFOUND_MEMBER(-4, "해당 user가 존재하지 않습니다.", NOT_FOUND),
    NOTFOUND_WISH_INFO(-5, "일치하는 스토어 찜을 찾지못했습니다.", NOT_FOUND),

    AWS_ACL_BLOCK(-6, "S3에 버킷의 ACL권한을 설정해주세요", HttpStatus.INTERNAL_SERVER_ERROR),
    AWS_ENVIRONMENT(-7, "AWS 환경에서 진행해주세요", HttpStatus.INTERNAL_SERVER_ERROR),

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
    GOOGLE_AUTHENTICATION_ERROR(-995, "구글 인증 토큰 발행 중 에러가 발생했습니다.",
        HttpStatus.INTERNAL_SERVER_ERROR),
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
