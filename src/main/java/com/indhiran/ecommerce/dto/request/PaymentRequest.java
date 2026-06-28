package com.indhiran.ecommerce.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    // Mock card details — never store real card data!
    private String cardNumber;
    private String cardHolder;
    private String expiryDate;
    private String cvv;

}