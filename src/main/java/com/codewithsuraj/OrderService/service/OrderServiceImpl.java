package com.codewithsuraj.OrderService.service;

import com.codewithsuraj.OrderService.entity.Order;
import com.codewithsuraj.OrderService.external.client.ProductService;
import com.codewithsuraj.OrderService.model.OrderRequest;
import com.codewithsuraj.OrderService.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    /**
     * @param orderRequest
     * @return
     */
    @Override
    public long placeOrder(OrderRequest orderRequest) {
        //Order Entity -> Save the data with status oder created
        //Product Service -> Block Products (Reduce the quantity)
        //Payment Service -> Payments -> success -> Complete else Cancelled
        log.info("Placing Order Request:{}",orderRequest);

        productService.reduceQuantity(orderRequest.getProduct_Id(),orderRequest.getQuantity());

        log.info("creating order with status CREATED");
        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .product_id(orderRequest.getProduct_Id())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();

        order = orderRepository.save(order);

        log.info("Order places successfully with order id: {}", order.getId());

        return order.getId();
    }
}
