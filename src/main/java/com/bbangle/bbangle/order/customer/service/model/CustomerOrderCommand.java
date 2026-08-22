package com.bbangle.bbangle.order.customer.service.model;

import lombok.Builder;
import org.springframework.data.domain.Pageable;

/**
 * 소비자 주문 관련 커맨드(Command) 모음.
 * Controller → Service 로 요청 정보를 전달하는 불변 값 객체(record)입니다.
 */
public class CustomerOrderCommand {

    /**
     * 소비자 주문목록 조회 커맨드.
     * 인증된 회원(memberId)의 주문을 주문일 기준 최신순으로 페이징 조회할 때 사용합니다.
     */
    @Builder
    public record CustomerOrderSearchCommand(
        Long memberId,
        Pageable pageable
    ) {

    }

    /**
     * 소비자 주문 상세 조회 커맨드.
     * 인증된 회원(memberId)이 본인 소유의 단일 주문(orderId) 상세를 조회할 때 사용합니다.
     */
    @Builder
    public record CustomerOrderDetailCommand(
        Long memberId,
        Long orderId
    ) {

    }
}
