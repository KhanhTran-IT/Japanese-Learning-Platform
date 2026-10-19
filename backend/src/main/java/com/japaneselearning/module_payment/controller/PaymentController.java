package com.japaneselearning.module_payment.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_payment.dto.PaymentRes;
import com.japaneselearning.module_payment.dto.PaymentSimulateReq;
import com.japaneselearning.module_payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "APIs for payment processing and simulation")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/simulate")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Simulate payment completion", description = "Allows a student to simulate a successful payment for their own pending order (demo/local only)")
    public ApiResponse<PaymentRes> simulatePayment(@Valid @RequestBody PaymentSimulateReq req) {
        PaymentRes res = paymentService.simulatePayment(req.getOrderId());
        return ApiResponse.success("Thanh toán thành công", res);
    }

    @PostMapping("/simulate/{orderId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Simulate payment completion by order ID", description = "Allows a student to simulate a successful payment for their own pending order via path variable")
    public ApiResponse<PaymentRes> simulatePaymentByPathVariable(@PathVariable Long orderId) {
        PaymentRes res = paymentService.simulatePayment(orderId);
        return ApiResponse.success("Thanh toán thành công", res);
    }
}
