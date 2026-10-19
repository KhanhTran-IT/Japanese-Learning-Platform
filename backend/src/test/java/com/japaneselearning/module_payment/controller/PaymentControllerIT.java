package com.japaneselearning.module_payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_order.enums.OrderStatus;
import com.japaneselearning.module_payment.dto.PaymentRes;
import com.japaneselearning.module_payment.dto.PaymentSimulateReq;
import com.japaneselearning.module_payment.enums.PaymentMethod;
import com.japaneselearning.module_payment.enums.PaymentStatus;
import com.japaneselearning.module_payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("POST /api/v1/payments/simulate: Chưa đăng nhập trả về 401")
    void simulatePayment_Unauthenticated_ReturnsUnauthorized() throws Exception {
        PaymentSimulateReq req = PaymentSimulateReq.builder().orderId(1L).build();

        mockMvc.perform(post("/api/v1/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    @DisplayName("POST /api/v1/payments/simulate: Role TEACHER không được phép gọi (403 Forbidden)")
    void simulatePayment_TeacherRole_ReturnsForbidden() throws Exception {
        PaymentSimulateReq req = PaymentSimulateReq.builder().orderId(1L).build();

        mockMvc.perform(post("/api/v1/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    @DisplayName("POST /api/v1/payments/simulate: Role ADMIN không được phép gọi endpoint student-only (403 Forbidden)")
    void simulatePayment_AdminRole_ReturnsForbidden() throws Exception {
        PaymentSimulateReq req = PaymentSimulateReq.builder().orderId(1L).build();

        mockMvc.perform(post("/api/v1/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    @DisplayName("POST /api/v1/payments/simulate: STUDENT gọi thành công trả về 200 và ApiResponse chứa PaymentRes")
    void simulatePayment_StudentRole_Success() throws Exception {
        PaymentSimulateReq req = PaymentSimulateReq.builder().orderId(1L).build();

        PaymentRes res = PaymentRes.builder()
                .id(10L)
                .orderId(1L)
                .orderCode("ORD-20261019-123456")
                .paymentMethod(PaymentMethod.SIMULATION)
                .paymentProvider("LOCAL_SIMULATION")
                .transactionCode("SIM-ORD-20261019-123456-ABCDEF")
                .amount(BigDecimal.valueOf(500000))
                .status(PaymentStatus.SUCCESS)
                .orderStatus(OrderStatus.PAID)
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentService.simulatePayment(eq(1L))).thenReturn(res);

        mockMvc.perform(post("/api/v1/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(10))
                .andExpect(jsonPath("$.result.orderId").value(1))
                .andExpect(jsonPath("$.result.status").value("SUCCESS"))
                .andExpect(jsonPath("$.result.orderStatus").value("PAID"))
                .andExpect(jsonPath("$.result.paymentMethod").value("SIMULATION"))
                .andExpect(jsonPath("$.result.transactionCode").value("SIM-ORD-20261019-123456-ABCDEF"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    @DisplayName("POST /api/v1/payments/simulate/{orderId}: Gọi qua path variable thành công (200)")
    void simulatePayment_PathVariable_Success() throws Exception {
        PaymentRes res = PaymentRes.builder()
                .id(11L)
                .orderId(2L)
                .orderCode("ORD-20261019-222222")
                .paymentMethod(PaymentMethod.SIMULATION)
                .paymentProvider("LOCAL_SIMULATION")
                .transactionCode("SIM-ORD-20261019-222222-XYZ789")
                .amount(BigDecimal.valueOf(350000))
                .status(PaymentStatus.SUCCESS)
                .orderStatus(OrderStatus.PAID)
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentService.simulatePayment(eq(2L))).thenReturn(res);

        mockMvc.perform(post("/api/v1/payments/simulate/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(11))
                .andExpect(jsonPath("$.result.orderId").value(2))
                .andExpect(jsonPath("$.result.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    @DisplayName("POST /api/v1/payments/simulate: Cross-user access ném 403 Forbidden")
    void simulatePayment_CrossUser_ReturnsForbidden() throws Exception {
        PaymentSimulateReq req = PaymentSimulateReq.builder().orderId(99L).build();

        when(paymentService.simulatePayment(eq(99L)))
                .thenThrow(new AppException(ErrorCode.ORDER_ACCESS_DENIED));

        mockMvc.perform(post("/api/v1/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(5005));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    @DisplayName("POST /api/v1/payments/simulate: Order trạng thái không hợp lệ ném 400 Bad Request")
    void simulatePayment_InvalidState_ReturnsBadRequest() throws Exception {
        PaymentSimulateReq req = PaymentSimulateReq.builder().orderId(88L).build();

        when(paymentService.simulatePayment(eq(88L)))
                .thenThrow(new AppException(ErrorCode.ORDER_INVALID_STATUS));

        mockMvc.perform(post("/api/v1/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(5006));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    @DisplayName("POST /api/v1/payments/simulate: Khi tính năng bị disable ném 403 Forbidden")
    void simulatePayment_SimulationDisabled_ReturnsForbidden() throws Exception {
        PaymentSimulateReq req = PaymentSimulateReq.builder().orderId(77L).build();

        when(paymentService.simulatePayment(eq(77L)))
                .thenThrow(new AppException(ErrorCode.PAYMENT_SIMULATION_DISABLED));

        mockMvc.perform(post("/api/v1/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(5007));
    }
}
