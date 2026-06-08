package com.bbangle.bbangle.order.repository;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderSearchCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.CompletedOrderSearchCommand;
import java.util.Map;
import java.util.Optional;

public interface OrderDSLRepository {

    BbanglePageResponse<Order> searchOrderList(OrderSearchCommand command);

    Map<OrderStatus, Long> countByOrderStatus(OrderSearchCommand command);

    BbanglePageResponse<Order> searchCompletedOrderList(CompletedOrderSearchCommand command);

    Map<OrderStatus, Long> countByCompletedOrderStatus(CompletedOrderSearchCommand command);

    Optional<Order> findByIdWithFullAssociations(Long orderId);
}
