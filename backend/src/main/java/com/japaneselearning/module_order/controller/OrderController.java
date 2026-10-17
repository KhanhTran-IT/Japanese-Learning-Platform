package com.japaneselearning.module_order.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_order.dto.OrderCreateReq;
import com.japaneselearning.module_order.dto.OrderRes;
import com.japaneselearning.module_order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "APIs for course orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Create an order for a paid course", description = "Allows a student to create a pending order for a paid course")
    public ApiResponse<OrderRes> createOrder(@Valid @RequestBody OrderCreateReq req) {
        OrderRes orderRes = orderService.createOrder(req);
        return ApiResponse.success("Tạo đơn hàng thành công", orderRes);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get current student's orders", description = "Retrieves all orders placed by the current student")
    public ApiResponse<List<OrderRes>> getMyOrders() {
        return ApiResponse.success("Lấy danh sách đơn hàng thành công", orderService.getMyOrders());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get order by ID", description = "Retrieves order details by ID for the order owner")
    public ApiResponse<OrderRes> getOrderById(@PathVariable Long id) {
        return ApiResponse.success("Lấy thông tin đơn hàng thành công", orderService.getOrderById(id));
    }
}
