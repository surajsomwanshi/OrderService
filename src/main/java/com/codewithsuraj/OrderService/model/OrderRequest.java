package com.codewithsuraj.OrderService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    private long product_Id;
    private long totalAmount;
    private long quantity;
    private PaymentMode paymentMode;
}
