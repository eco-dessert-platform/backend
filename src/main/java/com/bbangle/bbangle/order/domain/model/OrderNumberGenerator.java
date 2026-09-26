package com.bbangle.bbangle.order.domain.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 주문번호 · 결제번호 생성기.
 *
 * <p>충돌은 DB UNIQUE 제약({@code uk_orders_order_number}, {@code uk_payment_number})으로 막는다.
 */
public final class OrderNumberGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int SUFFIX_LENGTH = 8;

    private OrderNumberGenerator() {
    }

    /** {@code ORDER-yyyyMMdd-XXXXXXXX} (23자, 컬럼 VARCHAR(30)) */
    public static String orderNumber() {
        return "ORDER-" + today() + "-" + randomSuffix();
    }

    /** {@code PAY-yyyyMMdd-XXXXXXXX} (21자). PG 의 orderId 규격(6~64자, 영숫자·-·_)을 만족한다. */
    public static String paymentNumber() {
        return "PAY-" + today() + "-" + randomSuffix();
    }

    private static String today() {
        return LocalDate.now().format(DATE_FORMAT);
    }

    private static String randomSuffix() {
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, SUFFIX_LENGTH)
            .toUpperCase();
    }
}
