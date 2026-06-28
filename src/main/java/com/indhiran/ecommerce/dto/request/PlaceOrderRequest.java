package com.indhiran.ecommerce.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceOrderRequest {

    private String shippingAddress;
    private String notes;

}