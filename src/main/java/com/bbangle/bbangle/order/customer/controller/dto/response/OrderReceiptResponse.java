package com.bbangle.bbangle.order.customer.controller.dto.response;

import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.payment.domain.CardType;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.payment.domain.PaymentMethod;
import com.bbangle.bbangle.store.domain.Store;
import java.time.LocalDateTime;
import java.util.List;

public record OrderReceiptResponse(
    PaymentInfo paymentInfo,
    PurchaseInfo purchaseInfo,
    StoreInfo storeInfo
) {

    public static OrderReceiptResponse from(
        Payment payment,
        String orderNumber,
        List<OrderItem> activeItems,
        Store store
    ) {
        return new OrderReceiptResponse(
            PaymentInfo.from(payment),
            PurchaseInfo.from(orderNumber, activeItems),
            StoreInfo.from(store)
        );
    }

    public record PaymentInfo(
        String approvalNumber,
        String transactionType,
        String cardType,
        String cardNumber,
        String installment,
        LocalDateTime transactionAt
    ) {

        public static PaymentInfo from(Payment payment) {
            return new PaymentInfo(
                payment.getApprovalNumber(),
                resolveTransactionType(payment.getPaymentMethod()),
                resolveCardType(payment.getCardType()),
                maskCardNumber(payment.getCardNumber()),
                payment.getInstallment(),
                payment.getPaidAt()
            );
        }

        private static String resolveTransactionType(PaymentMethod method) {
            if (method == null) {
                return null;
            }
            return method.getDescription();
        }

        private static String resolveCardType(CardType cardType) {
            if (cardType == null) {
                return null;
            }
            return cardType.getDescription();
        }

        private static final int PREFIX_LENGTH = 6;
        private static final int SUFFIX_LENGTH = 4;

        private static String maskCardNumber(String cardNumber) {
            if (cardNumber == null) {
                return null;
            }
            int len = cardNumber.length();
            if (len <= PREFIX_LENGTH + SUFFIX_LENGTH) {
                return "*".repeat(len);
            }
            return cardNumber.substring(0, PREFIX_LENGTH)
                + "*".repeat(len - PREFIX_LENGTH - SUFFIX_LENGTH)
                + cardNumber.substring(len - SUFFIX_LENGTH);
        }
    }

    public record PurchaseInfo(
        String orderNumber,
        String productName,
        int taxableAmount,
        int nonTaxableAmount,
        int vat,
        int totalAmount
    ) {

        public static PurchaseInfo from(String orderNumber, List<OrderItem> activeItems) {
            int totalAmount = activeItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
            int taxableAmount = (int) Math.round(totalAmount / 1.1);
            int vat = totalAmount - taxableAmount;

            return new PurchaseInfo(
                orderNumber,
                buildProductName(activeItems),
                taxableAmount,
                0,
                vat,
                totalAmount
            );
        }

        private static String buildProductName(List<OrderItem> items) {
            if (items.isEmpty()) {
                return "";
            }
            String firstName = items.get(0).getProduct().getTitle();
            if (items.size() == 1) {
                return firstName;
            }
            return firstName + " 외 " + (items.size() - 1) + "건";
        }
    }

    public record StoreInfo(
        String storeName,
        String businessNumber,
        String storeAddress
    ) {

        public static StoreInfo from(Store store) {
            String address = buildAddress(store.getOriginAddressLine(), store.getOriginAddressDetail());
            return new StoreInfo(
                store.getName(),
                store.getIdentifier(),
                address
            );
        }

        private static String buildAddress(String line, String detail) {
            if (line == null) {
                return detail;
            }
            if (detail == null) {
                return line;
            }
            return line + " " + detail;
        }
    }
}
