package com.japaneselearning.module_order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.response.PageResponse;
import com.japaneselearning.module_order.dto.OrderCreateReq;
import com.japaneselearning.module_order.dto.OrderItemRes;
import com.japaneselearning.module_order.dto.OrderRes;
import com.japaneselearning.module_order.enums.OrderStatus;
import com.japaneselearning.module_order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /api/v1/orders: Chưa đăng nhập trả về 401")
    void createOrder_Unauthenticated_ReturnsUnauthorized() throws Exception {
        OrderCreateReq req = OrderCreateReq.builder().courseId(1L).build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    @DisplayName("POST /api/v1/orders: Role TEACHER không được tạo order (403 Forbidden)")
    void createOrder_TeacherRole_ReturnsForbidden() throws Exception {
        OrderCreateReq req = OrderCreateReq.builder().courseId(1L).build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    @DisplayName("POST /api/v1/orders: Role ADMIN không được tạo order (403 Forbidden)")
    void createOrder_AdminRole_ReturnsForbidden() throws Exception {
        OrderCreateReq req = OrderCreateReq.builder().courseId(1L).build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    @DisplayName("POST /api/v1/orders: Học viên tạo order thành công trả về 200 và ApiResponse OrderRes")
    void createOrder_Student_Success() throws Exception {
        OrderCreateReq req = OrderCreateReq.builder().courseId(1L).note("Đơn hàng mới").build();

        OrderRes mockRes = OrderRes.builder()
                .id(1L)
                .orderCode("ORD-20261017-ABCDEF")
                .userId(100L)
                .userEmail("student@example.com")
                .totalAmount(new BigDecimal("350000.00"))
                .finalAmount(new BigDecimal("350000.00"))
                .discountAmount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .note("Đơn hàng mới")
                .items(List.of(
                        OrderItemRes.builder()
                                .id(1L)
                                .courseId(1L)
                                .courseTitle("Khóa học N3 Cấp Tốc")
                                .price(new BigDecimal("350000.00"))
                                .quantity(1)
                                .build()
                ))
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.createOrder(any(OrderCreateReq.class))).thenReturn(mockRes);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(1))
                .andExpect(jsonPath("$.result.orderCode").value("ORD-20261017-ABCDEF"))
                .andExpect(jsonPath("$.result.status").value("PENDING"))
                .andExpect(jsonPath("$.result.finalAmount").value(350000.00))
                .andExpect(jsonPath("$.result.items[0].courseTitle").value("Khóa học N3 Cấp Tốc"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/me: Chưa đăng nhập trả về 401")
    void getMyOrders_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/orders/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    @DisplayName("GET /api/v1/orders/me: Non-student trả về 403 Forbidden")
    void getMyOrders_NonStudent_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/orders/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    @DisplayName("GET /api/v1/orders/me: Default pagination (page=0, size=10) trả về PageResponse thành công")
    void getMyOrders_Student_DefaultPagination_Success() throws Exception {
        OrderRes orderRes = OrderRes.builder()
                .id(1L)
                .orderCode("ORD-20261017-ABCDEF")
                .status(OrderStatus.PENDING)
                .finalAmount(new BigDecimal("350000.00"))
                .build();

        PageResponse<OrderRes> mockPageResponse = PageResponse.<OrderRes>builder()
                .currentPage(0)
                .pageSize(10)
                .totalPages(1)
                .totalElements(1)
                .data(List.of(orderRes))
                .build();

        when(orderService.getMyOrders(any(Pageable.class))).thenReturn(mockPageResponse);

        mockMvc.perform(get("/api/v1/orders/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.currentPage").value(0))
                .andExpect(jsonPath("$.result.pageSize").value(10))
                .andExpect(jsonPath("$.result.totalPages").value(1))
                .andExpect(jsonPath("$.result.totalElements").value(1))
                .andExpect(jsonPath("$.result.data[0].orderCode").value("ORD-20261017-ABCDEF"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    @DisplayName("GET /api/v1/orders/me: Custom pagination params (page=1, size=5) trả về 200")
    void getMyOrders_Student_CustomPagination_Success() throws Exception {
        PageResponse<OrderRes> mockPageResponse = PageResponse.<OrderRes>builder()
                .currentPage(1)
                .pageSize(5)
                .totalPages(2)
                .totalElements(6)
                .data(List.of())
                .build();

        when(orderService.getMyOrders(any(Pageable.class))).thenReturn(mockPageResponse);

        mockMvc.perform(get("/api/v1/orders/me")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.currentPage").value(1))
                .andExpect(jsonPath("$.result.pageSize").value(5))
                .andExpect(jsonPath("$.result.totalPages").value(2))
                .andExpect(jsonPath("$.result.totalElements").value(6));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    @DisplayName("GET /api/v1/orders/me: Negative page index trả về 400 VALIDATION_ERROR")
    void getMyOrders_NegativePage_ReturnsValidationError() throws Exception {
        mockMvc.perform(get("/api/v1/orders/me")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    @DisplayName("GET /api/v1/orders/me: Zero page size trả về 400 VALIDATION_ERROR")
    void getMyOrders_ZeroSize_ReturnsValidationError() throws Exception {
        mockMvc.perform(get("/api/v1/orders/me")
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    @DisplayName("GET /api/v1/orders/me: Oversized page size (> 100) trả về 400 VALIDATION_ERROR")
    void getMyOrders_OversizedSize_ReturnsValidationError() throws Exception {
        mockMvc.perform(get("/api/v1/orders/me")
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()));
    }

    @Test
    @DisplayName("GET /api/v1/orders/1: Chưa đăng nhập trả về 401")
    void getOrderById_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    @DisplayName("GET /api/v1/orders/1: Non-student trả về 403 Forbidden")
    void getOrderById_NonStudent_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    @DisplayName("GET /api/v1/orders/1: Owner xem đơn hàng thành công")
    void getOrderById_Owner_Success() throws Exception {
        OrderRes orderRes = OrderRes.builder()
                .id(1L)
                .orderCode("ORD-20261017-ABCDEF")
                .status(OrderStatus.PENDING)
                .finalAmount(new BigDecimal("350000.00"))
                .build();

        when(orderService.getOrderById(1L)).thenReturn(orderRes);

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(1));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    @DisplayName("GET /api/v1/orders/1: Cross-user xem đơn hàng của người khác trả về 403 Forbidden")
    void getOrderById_CrossUser_ReturnsForbidden() throws Exception {
        when(orderService.getOrderById(1L))
                .thenThrow(new AppException(ErrorCode.ORDER_ACCESS_DENIED));

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_ACCESS_DENIED.getCode()));
    }
}
