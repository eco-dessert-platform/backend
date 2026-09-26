package com.bbangle.bbangle.order.customer.controller.dto.request;

import com.bbangle.bbangle.payment.domain.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 주문 생성 요청.
 *
 * <p>스토어 &gt; 상품(게시글) &gt; 옵션 3단 구조로 받는다. 스토어 1건당 주문 1건이 만들어지고
 * 결제 1건으로 묶인다.
 *
 * <p>클라이언트가 보낸 그룹핑·단가·배송비·결제금액은 모두 <b>대조용</b>이다.
 * 저장되는 값은 전부 서버가 다시 계산한 결과다.
 */
@Schema(description = "주문 생성 요청")
public record CreateOrderRequest(

    @NotEmpty
    @Valid
    @Schema(description = "스토어별 주문 목록. 스토어 1건당 주문 1건이 생성된다.")
    List<@NotNull @Valid StoreOrderRequest> stores,

    @NotNull
    @Valid
    OrdererRequest orderer,

    @NotNull
    @Valid
    ShippingAddressRequest shippingAddress,

    @NotNull
    @Schema(description = "결제 수단", example = "CARD")
    PaymentMethod paymentMethod,

    @AssertTrue(message = "개인정보 수집·이용에 동의해야 주문할 수 있습니다.")
    @Schema(description = "개인정보 수집·이용 동의 여부", example = "true")
    boolean privacyAgreed,

    @NotNull
    @Valid
    PaymentAmountRequest paymentAmount
) {

    @Schema(description = "스토어 단위 주문")
    public record StoreOrderRequest(
        @NotNull
        @Schema(description = "스토어 Id", example = "1")
        Long storeId,

        @NotNull
        @Min(0)
        @Schema(description = "해당 스토어 배송비. 서버 계산값과 대조한다.", example = "3000")
        Integer deliveryFee,

        @NotEmpty
        @Valid
        @Schema(description = "상품(게시글) 목록")
        List<@NotNull @Valid ProductOrderRequest> products
    ) {
    }

    @Schema(description = "상품(게시글) 단위 주문")
    public record ProductOrderRequest(
        @NotNull
        @Schema(description = "상품 Id (게시글 Board Id)", example = "10")
        Long productId,

        @NotEmpty
        @Valid
        @Schema(description = "선택한 옵션 목록")
        List<@NotNull @Valid OptionOrderRequest> options
    ) {
    }

    @Schema(description = "선택 옵션")
    public record OptionOrderRequest(
        @NotNull
        @Schema(description = "옵션 Id (Product Id)", example = "101")
        Long optionId,

        @Min(value = 1, message = "수량은 1개 이상 입력해주세요.")
        @Max(value = 999, message = "수량은 999개 이하로 입력해주세요.")
        @Schema(description = "수량", example = "2")
        int quantity,

        @NotNull
        @Min(0)
        @Schema(description = "옵션 1개당 할인 적용 단가. 서버 계산값과 대조한다.", example = "12000")
        Integer price
    ) {
    }

    @Schema(description = "주문자 정보")
    public record OrdererRequest(
        @NotBlank
        @Size(max = 20)
        @Schema(description = "주문자명", example = "홍길동")
        String name,

        @NotBlank
        @Pattern(regexp = "\\d{1,20}", message = "연락처는 숫자만 입력해주세요.")
        @Schema(description = "연락처 (숫자만)", example = "01012345678")
        String phone,

        @NotBlank
        @Email
        @Size(max = 100)
        @Schema(description = "이메일", example = "buyer@example.com")
        String email
    ) {
    }

    @Schema(description = "배송지 정보")
    public record ShippingAddressRequest(
        @NotBlank
        @Size(max = 20)
        @Schema(description = "받는 사람", example = "홍길동")
        String recipientName,

        @NotBlank
        @Pattern(regexp = "\\d{1,11}", message = "연락처는 숫자만 입력해주세요.")
        @Schema(description = "받는 사람 연락처 (숫자만, 최대 11자)", example = "01012345678")
        String recipientPhone,

        @NotBlank
        @Size(max = 10)
        @Schema(description = "우편번호", example = "13529")
        String zipCode,

        @NotBlank
        @Size(max = 200)
        @Schema(description = "기본 주소", example = "경기도 성남시 분당구 판교역로 166")
        String address,

        @Size(max = 20)
        @Schema(description = "상세 주소 (최대 20자)", example = "101동 1001호")
        String addressDetail,

        @Size(max = 50)
        @Schema(description = "배송 요청사항 (최대 50자)", example = "문 앞에 놔주세요")
        String deliveryMemo,

        @NotNull
        @Schema(description = "기본 배송지로 설정 여부. 현재는 수신만 하고 처리하지 않는다.", example = "true")
        Boolean saveAsDefault
    ) {
    }

    @Schema(description = "결제 금액. 4개 항목 모두 서버 계산값과 대조한다.")
    public record PaymentAmountRequest(
        @NotNull
        @Min(0)
        @Schema(description = "상품금액(정가 합계)", example = "30000")
        Integer productAmount,

        @NotNull
        @Min(0)
        @Schema(description = "상품 할인금액", example = "0")
        Integer discountAmount,

        @NotNull
        @Min(0)
        @Schema(description = "배송비 합계", example = "3000")
        Integer deliveryFee,

        @NotNull
        @Min(0)
        @Schema(description = "최종 결제금액", example = "33000")
        Integer totalAmount
    ) {
    }
}
