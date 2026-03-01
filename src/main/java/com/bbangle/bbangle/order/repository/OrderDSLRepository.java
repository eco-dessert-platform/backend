package com.bbangle.bbangle.order.repository;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderSearchCommand;
import java.util.Map;

public interface OrderDSLRepository {

    BbanglePageResponse<Order> searchOrderList(OrderSearchCommand command);

    Map<OrderStatus, Long> countByOrderStatus(OrderSearchCommand command);

}
