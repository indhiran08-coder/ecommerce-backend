package com.indhiran.ecommerce.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String notes;
    private List<OrderItemResponse> items;
    private LocalDateTime placedAt;

}