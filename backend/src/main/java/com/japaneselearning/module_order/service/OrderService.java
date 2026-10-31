package com.japaneselearning.module_order.service;

import com.japaneselearning.common.response.PageResponse;
import com.japaneselearning.module_order.dto.OrderCreateReq;
import com.japaneselearning.module_order.dto.OrderRes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderRes createOrder(OrderCreateReq req);
    PageResponse<OrderRes> getMyOrders(Pageable pageable);
    default PageResponse<OrderRes> getMyOrders() {
        return getMyOrders(PageRequest.of(0, 10));
    }
    OrderRes getOrderById(Long id);
}
