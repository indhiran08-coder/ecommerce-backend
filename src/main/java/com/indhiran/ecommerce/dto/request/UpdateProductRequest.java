package com.indhiran.ecommerce.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class UpdateProductRequest {

    @Size(min = 2, max = 200)
    private String name;

    private String description;

    @DecimalMin(value = "0.01")
    private BigDecimal price;

    @Min(value = 0)
    private Integer stock;

    private Long categoryId;

    private Boolean active;

}