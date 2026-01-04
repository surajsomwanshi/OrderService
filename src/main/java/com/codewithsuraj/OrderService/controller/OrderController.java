package com.codewithsuraj.OrderService.controller;

import com.codewithsuraj.OrderService.model.OrderRequest;
import com.codewithsuraj.OrderService.service.OrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Log4j2
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/placeOrder")
    public ResponseEntity<Long> placeOrder(@RequestBody OrderRequest orderRequest){
            long orderId=orderService.placeOrder(orderRequest);
            log.info("order Id:{}",orderId);
            return new ResponseEntity<>(orderId, HttpStatus.OK);
    }
}
