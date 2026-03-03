package com.codewithsuraj.OrderService.external.client;

import com.codewithsuraj.OrderService.exception.CustomException;
import com.codewithsuraj.OrderService.external.request.PaymentRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CircuitBreaker(name = "external",fallbackMethod = "paymentFallback")
@FeignClient(name = "PAYMENT-SERVICE/payment")
public interface PaymentService {

    @PostMapping
    public ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest);

    default void paymentFallback(Exception e){
        throw new CustomException("Payment service is not available",
            "PAYMENT_SERVICE_NOT_AVAILABLE",
                    500);
    }
}
