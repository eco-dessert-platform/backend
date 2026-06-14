package com.bbangle.bbangle.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

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
    INVALID_BOARD_STATUS(-10_1, "유효하지 않은 게시글 상태입니다.", BAD_REQUEST),
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
    INVALID_BOARD_TITLE(-68, "게시글 제목은 필수입니다.", BAD_REQUEST),
    INVALID_BOARD_PRICE(-44, "가격은 음수가 될 수 없습니다.", BAD_REQUEST),
    INVALID_BOARD_DISCOUNT(-45, "할인 값이 유효하지 않습니다. (퍼센트: 0~100, 원: 0~상품가격)", BAD_REQUEST),
    INVALID_DELIVERY_FEE(-46, "배송비는 음수가 될 수 없습니다.", BAD_REQUEST),
    INVALID_PRODUCT_DELIVERY_DAY(-47, "가능한 발송 요일이 하나라도 있어야 합니다. ", BAD_REQUEST),
    INVALID_PRODUCT_NAME(-48, "상품 이름은 3글자 이상 50글자 이하여야 합니다.", BAD_REQUEST),
    INVALID_PRODUCT_INFO_NOTICE_NAME(-49, "상품 정보 제공 이름은 3글자 이상 50글자 이하여야 합니다.", BAD_REQUEST),
    INVALID_DISCOUNT_TYPE(-64, "유효하지 않은 할인 타입입니다.", BAD_REQUEST),
    INVALID_PRODUCTION_START_TIME(-65, "유효하지 않은 생산 시작 시간입니다.", BAD_REQUEST),
    INVALID_PRODUCT_CATEGORY(-66, "유효하지 않은 상품 카테고리입니다.", BAD_REQUEST),
    INVALID_PRODUCT_REQUEST(-67, "상품 요청 정보가 올바르지 않습니다.", BAD_REQUEST),
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
    INVALID_REMOVE_PRODUCTS_REQUEST(-60, "옵션 전체삭제가 아닌경우 옵션 ID가 하나라도 있어야합니다.", BAD_REQUEST),
    INVALID_STOCK_AMOUNT(-61, "재고 수량은 0이상이여야 합니다.", BAD_REQUEST),
    NOT_FOUND_OPTION(-62, "존재하지 않는 상품 옵션입니다", NOT_FOUND),
    INVALID_DECREASE_STOCK_AMOUNT(-63, "감소하려는 수보다 현재 재고가 더 작습니다.", BAD_REQUEST),

    //AWS Error (600)
    AWS_ERROR(-600, "AWS S3 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    AWS_CLIENT_ERROR(-601, "AWS SDK 클라이언트 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    AWS_ACL_BLOCK(-602, "S3에 버킷의 ACL권한을 설정해주세요.", HttpStatus.INTERNAL_SERVER_ERROR),
    AWS_ENVIRONMENT(-603, "AWS 환경에서 진행해주세요.", HttpStatus.INTERNAL_SERVER_ERROR),
    AWS_S3_FILE_NOT_FOUND(-604, "URL에 파일이 존재하지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    STREAM_CLOSING_ERROR(-605, "Stream 파일 닫기에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    NOT_FOUND_REQUEST(-989, "해당 요청을 찾을 수 없습니다.", NOT_FOUND),
    REQUEST_IS_REJECTED(-990, "이미 거절된 요청입니다.", BAD_REQUEST),
    REQUEST_IS_APPROVED(-991, "이미 승인된 요청입니다.", BAD_REQUEST),
    REQUEST_IS_PENDING(-992, "승인 대기중인 요청입니다.", BAD_REQUEST),
    _NOT_SUPPORTED_YET(-993, "아직 지원하지 않는 기능입니다.", HttpStatus.NOT_IMPLEMENTED),
    _BAD_REQUEST(-994, "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    GOOGLE_AUTHENTICATION_ERROR(-995, "구글 인증 토큰 발행 중 에러가 발생했습니다.",
        HttpStatus.INTERNAL_SERVER_ERROR),
    JSON_SERIALIZATION_ERROR(-996, "json 변환 중 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FCM_INITIALIZATION_ERROR(-997, "Firebase 초기화 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FCM_CONNECTION_ERROR(-998, "FCM 서버 요청 중 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_SERVER_ERROR(-999, "서버 내부 에러입니다", HttpStatus.INTERNAL_SERVER_ERROR),

    // Seller Error (700~720)
    INVALID_CERTIFICATION_STATUS(-700, "승인 상태가 비어 있습니다.", BAD_REQUEST),
    SELLER_CREATION_FAILED(-701, "Seller 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    SELLER_DOCUMENT_NAME_REQUIRED(-702, "서류 파일명은 필수입니다.", BAD_REQUEST),
    SELLER_DOCUMENT_URL_REQUIRED(-703, "서류 URL은 필수입니다.", BAD_REQUEST),
    SELLER_DOCUMENT_TYPE_REQUIRED(-704, "서류 타입은 필수입니다.", BAD_REQUEST),
    INVALID_DOCUMENT_FILE_EXTENSION(-705, "서류 파일은 jpg, jpeg, png, pdf 형식만 가능합니다.", BAD_REQUEST),
    SELLER_NOT_FOUND(-706, "존재하지 않는 판매자입니다.", NOT_FOUND),
    ACCOUNT_VERIFICATION_NOT_FOUND(-707, "존재하지 않는 인증정보입니다.", NOT_FOUND),
    ACCOUNT_NOT_VERIFIED(-708, "인증되지 않은 계좌입니다.", BAD_REQUEST),
    ORDER_NOT_FOUND(-709, "존재하지 않는 주문입니다.", NOT_FOUND),
    ORDER_ITEM_NOT_FOUND(-710, "존재하지 않는 주문상품입니다.", NOT_FOUND),
    ORDER_ACCESS_DENIED(-711, "해당 주문에 대한 접근 권한이 없습니다.", FORBIDDEN),
    ENCRYPTION_FAILED(-712, "암호화 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DECRYPTION_FAILED(-713, "복호화 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ALREADY_REGISTER_STORE(-714, "이미 스토어를 등록한 판매자 계정입니다.", BAD_REQUEST),
    FORBIDDEN_BOARD_ACCESS(-715, "해당 게시글에 대한 접근 권한이 없습니다.", FORBIDDEN),
    PRODUCT_NOT_FOUND(-716, "존재하지 않는 상품입니다.", NOT_FOUND),
    MISSING_BOARD_THUMBNAIL(-717, "썸네일 이미지는 필수입니다. 새 파일 또는 기존 URL을 제공해주세요.", BAD_REQUEST),
    NOT_REGISTERED_STORE(-718, "스토어를 등록하지 않은 계정입니다.", NOT_FOUND),

    // Store Error (721~740)
    INVALID_STORE(-721, "유효하지 않은 스토어 객체입니다.", BAD_REQUEST),
    INVALID_STORE_NAME(-722, "유효하지 않은 스토어 이름입니다.", BAD_REQUEST),
    INVALID_STORE_ID(-723, "유효하지 않은 스토어 아이디 입니다", BAD_REQUEST),
    INVALID_STORE_INTRODUCE(-724, "유효하지 않은 스토어 한 줄 소개입니다.", BAD_REQUEST),
    INVALID_PROFILE(-725, "프로필 이미지 경로가 비어있습니다.", BAD_REQUEST),
    INVALID_PHONE_NUMBER(-726, "유효하지 않은 핸드폰 번호 입니다.", BAD_REQUEST),
    INVALID_EMAIL(-727, "유효하지 않은 이메일 형식 입니다.", BAD_REQUEST),
    INVALID_ADDRESS(-728, "유효하지 않은 주소 입니다.", BAD_REQUEST),
    INVALID_DETAIL_ADDRESS(-729, "유효하지 않은 상세 주소 입니다.", BAD_REQUEST),
    INVALID_SHORT_DESCRIPTION(-730, "유효하지 않은 한 줄 소개입니다.", BAD_REQUEST),
    ALREADY_RESERVED_STORE(-731, "이미 등록된 스토어입니다.", BAD_REQUEST),
    ALREADY_UPDATE_STORE_NAME(-732, "이미 변경된 스토어명입니다.", BAD_REQUEST),
    STORE_UPDATE_FAILED(-733, "스토어 상세 정보 변경에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_VERIFICATION_ALREADY_EXISTS(-734, "이미 계좌 인증 정보가 존재합니다.", BAD_REQUEST),
    ACCOUNT_VERIFICATION_FAILED(-735, "계좌 인증에 실패했습니다.", BAD_REQUEST),
    STORE_CREATION_FAILED(-736, "Store 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // AUTH (741~ 760)
    ADMIN_NOT_FOUND(-741, "존재하지 않는 관리자입니다.", NOT_FOUND),
    INVALID_ADMIN_ID(-742, "유효하지 않은 관리자 아이디 입니다.", BAD_REQUEST),
    ADMIN_INVALID_PASSWORD(-743, "비밀번호가 일치하지 않습니다.", BAD_REQUEST),
    INVALID_REFRESH_TOKEN(-744, "유효하지 않은 리프레시 토큰입니다.", BAD_REQUEST),
    NOT_SUPPORTED_SERVER(-745, "지원하지 않는 로그인 서버입니다.", BAD_REQUEST),
    MISSING_NAME_NICKNAME(-746, "이름 또는 닉네임이 비공개 상태입니다.", HttpStatus.UNPROCESSABLE_ENTITY),
    _UNAUTHORIZED(-747, "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
    INVALID_OAUTH_PARAMS(-748, "유효하지 않은 파라미터입니다.", BAD_REQUEST),

    // NOTICE Error(761~770)
    TITLE_IS_EMPTY(-761, "제목이 존재하지 않습니다.", BAD_REQUEST),
    CONTENT_IS_EMPTY(-762, "본문이 존재하지 않습니다.", BAD_REQUEST),
    ADMIN_NOTICE_CREATION_FAILED(-763, "공지사항 생성 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_URL_COUNT(-764, "원본 src 개수와 CDN URL 개수가 일치하지 않아 변환을 수행할 수 없습니다.", BAD_REQUEST),
    IMAGE_COUNT_MISMATCH(-765, "이미지 파일 개수와 원본 이미지 src 개수가 일치하지 않습니다.", BAD_REQUEST),
    IMAGE_NOT_MATCHED(-766, "HTML의 이미지 태그 개수와 원본 이미지 src 개수가 일치하지 않습니다.", BAD_REQUEST),
    NOT_FIND_NOTICE(-767, "Notice의 정보를 찾을 수 없습니다.", BAD_REQUEST),
    ADMIN_NOTICE_UPDATE_FAILED(-768, "공지사항 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // Claim Error(771 ~ 780)
    CLAIM_NOT_FOUND(-771, "해당 claim을 찾을 수 없습니다", BAD_REQUEST),
    SELLER_CLAIM_MISMATCH(-772, "Claim과 판매자 ID가 일치하지 않습니다", UNAUTHORIZED),
    CLAIM_INVALID_STATUS(-773, "이미 처리된 Claim 입니다", BAD_REQUEST),

    // Order Error(781 ~ 800)
    ORDER_INVALID_STATUS(-781, "요청하신 order의 상태로 변경할 수 없습니다", BAD_REQUEST),
    RETURN_NOT_ALLOWED(-782, "반품 요청이 불가능한 상태입니다.", BAD_REQUEST),
    EXCHANGE_NOT_ALLOWED(-783, "교환 요청이 불가능한 상태입니다.", BAD_REQUEST),
    DELIVERY_NOT_FOUND(-784, "해당 주문상품의 배송 정보를 찾을 수 없습니다.", NOT_FOUND),
    DELIVERY_MODIFY_NOT_ALLOWED(-785, "현재 배송 상태에서는 운송장을 수정할 수 없습니다.", BAD_REQUEST),
    CUSTOMER_ORDER_UNAUTHORIZED(-786, "주문 조회를 위해 로그인이 필요합니다.", UNAUTHORIZED),
    CUSTOMER_ORDER_MEMBER_NOT_FOUND(-787, "존재하지 않는 회원의 주문 조회 요청입니다.", NOT_FOUND),

    // Settlement Error(801 ~ 820)
    SETTLEMENT_NOT_FOUND(-802, "존재하지 않는 정산 내역입니다.", NOT_FOUND),
    SETTLEMENT_ACCESS_DENIED(-803, "해당 정산에 대한 접근 권한이 없습니다.", FORBIDDEN),
    INVALID_SETTLEMENT_DATE_RANGE(-804, "조회 시작일은 종료일보다 이전이어야 합니다.", BAD_REQUEST),
    SETTLEMENT_DATE_RANGE_EXCEEDED(-805, "조회 기간은 최대 1개월까지 가능합니다.", BAD_REQUEST),
    SETTLEMENT_DATE_REQUIRED(-806, "엑셀 다운로드 시 조회 시작일과 종료일은 필수입니다.", BAD_REQUEST),

    // Charge Error(821 ~ 830)
    CHARGE_BALANCE_NOT_FOUND(-821, "충전금 잔액 정보를 찾을 수 없습니다.", NOT_FOUND),
    CHARGE_WITHDRAWAL_DUPLICATED(-822, "동일한 출금 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.", CONFLICT),
    CHARGE_WITHDRAWAL_INSUFFICIENT_BALANCE(-823, "충전금 잔액이 부족합니다.", BAD_REQUEST),

    // Statistics Error(811~820)
    INVALID_STATISTICS_PERIOD(-811, "유효하지 않은 통계 기간입니다.", BAD_REQUEST),
    INVALID_DATE_RANGE(-812, "유효하지 않은 날짜 범위입니다.", BAD_REQUEST),

    // LinkTracking Error(831 ~ 840)
    TRACKING_LINK_NOT_FOUND(-831, "존재하지 않는 추적 링크입니다.", NOT_FOUND),

    // PaymentHold Error(841~850)
    INVALID_PAYMENT_HOLD_DATE_RANGE(-841, "지급보류 조회 시작일은 종료일보다 이전이어야 합니다.", BAD_REQUEST),
    PAYMENT_HOLD_DATE_RANGE_EXCEEDED(-842, "지급보류 조회 기간은 최대 1개월까지 가능합니다.", BAD_REQUEST),
    PAYMENT_HOLD_DATE_REQUIRED(-843, "지급보류 엑셀 다운로드 시 조회 시작일과 종료일은 필수입니다.", BAD_REQUEST),

    // Vat Error(851 ~ 860)
    INVALID_MONTH_FORMAT(-851, "월 형식이 올바르지 않습니다.", BAD_REQUEST),
    INVALID_VAT_DATE_RANGE(-852, "조회 기간이 올바르지 않습니다.", BAD_REQUEST),
    EXCEEDED_MAX_VAT_DATE_RANGE(-853, "조회 기간은 최대 1년까지 가능합니다.", BAD_REQUEST),
    INVALID_EXCEL_TYPE(-854, "엑셀 다운로드 유형이 올바르지 않습니다.", BAD_REQUEST),
    EXCEL_CREATE_FAILED(-855, "엑셀 파일 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

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
