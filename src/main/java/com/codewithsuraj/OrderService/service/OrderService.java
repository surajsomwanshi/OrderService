package com.codewithsuraj.OrderService.service;

import com.codewithsuraj.OrderService.model.OrderRequest;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);
}
