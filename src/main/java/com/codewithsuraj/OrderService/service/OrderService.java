package com.codewithsuraj.OrderService.service;

import com.codewithsuraj.OrderService.model.OrderRequest;
import com.codewithsuraj.OrderService.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
