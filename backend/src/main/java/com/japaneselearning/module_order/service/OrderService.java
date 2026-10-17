package com.japaneselearning.module_order.service;

import com.japaneselearning.module_order.dto.OrderCreateReq;
import com.japaneselearning.module_order.dto.OrderRes;

import java.util.List;

public interface OrderService {
    OrderRes createOrder(OrderCreateReq req);
    List<OrderRes> getMyOrders();
    OrderRes getOrderById(Long id);
}
