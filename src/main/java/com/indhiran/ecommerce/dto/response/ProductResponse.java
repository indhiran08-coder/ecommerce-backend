package com.indhiran.ecommerce.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private BigDecimal rating;
    private Boolean active;
    private String categoryName;
    private Long categoryId;
    private LocalDateTime createdAt;

}