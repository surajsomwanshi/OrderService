package com.codewithsuraj.OrderService.service;

import com.codewithsuraj.OrderService.entity.Order;
import com.codewithsuraj.OrderService.exception.CustomException;
import com.codewithsuraj.OrderService.external.client.PaymentService;
import com.codewithsuraj.OrderService.external.client.ProductService;
import com.codewithsuraj.OrderService.external.request.PaymentRequest;
import com.codewithsuraj.OrderService.external.response.PaymentResponse;
import com.codewithsuraj.OrderService.model.OrderRequest;
import com.codewithsuraj.OrderService.model.OrderResponse;
import com.codewithsuraj.OrderService.model.PaymentMode;
import com.codewithsuraj.OrderService.model.ProductResponse;
import com.codewithsuraj.OrderService.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;

@SpringBootTest
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    OrderService orderService = new OrderServiceImpl();

    @DisplayName("Get Order - Success Scenario")
    @Test
    void test_When_Order_Success(){
        //Mocking
        Order order = getMockOrder();
         Mockito.when(orderRepository.findById(anyLong())).
                thenReturn(Optional.of(order));
         Mockito.when(restTemplate.getForObject("http://PRODUCT-SERVICE/product/"+order.getProduct_id(),
                         ProductResponse.class)).
                 thenReturn(getMockProductResponse());
         Mockito.when(restTemplate.getForObject("http://PAYMENT-SERVICE/payment/"+order.getId(),
                         PaymentResponse.class))
                 .thenReturn(getMockPaymentResponse());

         //Actual method call
        OrderResponse orderResponse = orderService.getOrderDetails(1);
         //Verification
         Mockito.verify(orderRepository,Mockito.times(1)).findById(anyLong());
            Mockito.verify(restTemplate,Mockito.times(1)).getForObject("http://PRODUCT-SERVICE/product/"+order.getProduct_id(),
                    ProductResponse.class);
            Mockito.verify(restTemplate,Mockito.times(1)).getForObject("http://PAYMENT-SERVICE/payment/"+order.getId(),
                    PaymentResponse.class);
         //Assert
        Assertions.assertNotNull(orderResponse);
        Assertions.assertEquals(order.getId(),orderResponse.getOrderId());
    }

    @DisplayName("Get Order - Order Not Found Scenario")
    @Test
    void test_When_Get_Order_Not_Found() {
        Mockito.when(orderRepository.findById(anyLong())).
                thenReturn(Optional.ofNullable(null));
        OrderResponse orderResponse = orderService.getOrderDetails(1);

        CustomException exception = Assertions.assertThrows(CustomException.class, () -> {
            orderService.getOrderDetails(1);
        });
        Assertions.assertEquals("NOT_FOUND", exception.getErrorCode());
        Assertions.assertEquals(404, exception.getStatus());

        Mockito.verify(orderRepository, Mockito.times(1)).findById(anyLong());
    }



    @DisplayName("Place Order - Success Scenario")
    @Test
    void test_When_Place_Order_Success(){
        //Mocking
        Order order = getMockOrder();
        OrderRequest orderRequest = getMockOrderRequest();

        Mockito.when(orderRepository.save(Mockito.any(Order.class))).
                    thenReturn(order);
        Mockito.when(productService.reduceQuantity(orderRequest.getProduct_Id(), orderRequest.getQuantity())).
                thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
        Mockito.when(paymentService.doPayment(Mockito.any(PaymentRequest.class))).
                thenReturn(new ResponseEntity<Long>(1L, HttpStatus.OK));

        long orderId = orderService.placeOrder(orderRequest);

        Mockito.verify(orderRepository, Mockito.times(2)).save(Mockito.any());
        Mockito.verify(productService, Mockito.times(1)).reduceQuantity(anyLong(), anyLong());
        Mockito.verify(paymentService, Mockito.times(1)).doPayment(Mockito.any(PaymentRequest.class));

         Assertions.assertEquals(order.getId(), orderId);
    }

    @DisplayName("Place Order - Payment Failed Scenario")
    @Test
    void test_when_Place_Order_Payment_Fails_then_Order_Placed_But_Payment_Failed() {
        Order order = getMockOrder();
        OrderRequest orderRequest = getMockOrderRequest();

        Mockito.when(orderRepository.save(Mockito.any(Order.class))).
                thenReturn(order);
        Mockito.when(productService.reduceQuantity(orderRequest.getProduct_Id(), orderRequest.getQuantity())).
                thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
        Mockito.when(paymentService.doPayment(Mockito.any(PaymentRequest.class))).
                thenThrow(new RuntimeException());

        long orderId = orderService.placeOrder(orderRequest);

        Mockito.verify(orderRepository, Mockito.times(2)).save(Mockito.any());
        Mockito.verify(productService, Mockito.times(1)).reduceQuantity(anyLong(), anyLong());
        Mockito.verify(paymentService, Mockito.times(1)).doPayment(Mockito.any(PaymentRequest.class));

        Assertions.assertEquals(order.getId(), orderId);

    }


    private OrderRequest getMockOrderRequest() {
        return OrderRequest.builder()
                .product_Id(1)
                .quantity(10)
                .paymentMode(PaymentMode.CASH)
                .totalAmount(1100)
                .build();
    }


    private PaymentResponse getMockPaymentResponse() {
        return PaymentResponse.builder()
                .paymentId(1)
                .orderId(1)
                .status("SUCCESS")
                .paymentDate(Instant.now())
                .paymentMode(PaymentMode.CASH)
                .amount(200)
                .build();
    }

    private ProductResponse getMockProductResponse() {
        return ProductResponse.builder()
                .productId(2)
                .productName("iPhone 14 Pro")
                .price(75500)
                .quantity(50)
                .build();
    }

    private Order getMockOrder() {
        return Order.builder()
                .id(1)
                .amount(1100)
                .orderStatus("PLACED")
                .product_id(1)
                .orderDate(Instant.now())
                .quantity(1)
                .build();
    }
}