package com.japaneselearning.module_payment.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_payment.dto.*;
import com.japaneselearning.module_payment.enums.PaymentMethod;
import com.japaneselearning.module_payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "APIs for payment processing, initiation, and webhooks")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Initiate payment", description = "Generates a payment initiation URL (e.g., VNPay redirect) for a pending student order")
    public ApiResponse<PaymentInitRes> createPayment(@Valid @RequestBody PaymentCreateReq req) {
        PaymentInitRes res = paymentService.createPayment(req);
        return ApiResponse.success("Khởi tạo thanh toán thành công", res);
    }

    @GetMapping("/vnpay/ipn")
    @Operation(summary = "VNPay IPN webhook (GET)", description = "Public IPN callback endpoint invoked by VNPay server with signed query parameters")
    public VnPayIpnResponse handleVnPayIpnGet(@RequestParam Map<String, String> allParams) {
        return paymentService.handleVnPayIpn(allParams);
    }

    @PostMapping("/vnpay/ipn")
    @Operation(summary = "VNPay IPN webhook (POST)", description = "Public IPN callback endpoint invoked by VNPay server with form parameters")
    public VnPayIpnResponse handleVnPayIpnPost(@RequestParam Map<String, String> allParams) {
        return paymentService.handleVnPayIpn(allParams);
    }

    @PostMapping("/webhook")
    @Operation(summary = "Generic signed payment webhook", description = "Public webhook endpoint for signed provider notifications")
    public ApiResponse<PaymentRes> handleGenericWebhook(
            @RequestParam(required = false, defaultValue = "VNPAY") PaymentMethod method,
            @RequestParam Map<String, String> params,
            @RequestBody(required = false) String rawBody,
            @RequestHeader(value = "X-Signature", required = false) String signature
    ) {
        PaymentRes res = paymentService.handleGenericWebhook(method, params, rawBody, signature);
        return ApiResponse.success("Xử lý webhook thanh toán thành công", res);
    }

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
