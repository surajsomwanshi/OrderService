package com.codewithsuraj.OrderService.service;

import com.codewithsuraj.OrderService.entity.Order;
import com.codewithsuraj.OrderService.exception.CustomException;
import com.codewithsuraj.OrderService.external.client.PaymentService;
import com.codewithsuraj.OrderService.external.client.ProductService;
import com.codewithsuraj.OrderService.external.request.PaymentRequest;
import com.codewithsuraj.OrderService.external.response.PaymentResponse;
import com.codewithsuraj.OrderService.model.OrderRequest;
import com.codewithsuraj.OrderService.model.OrderResponse;
import com.codewithsuraj.OrderService.model.ProductResponse;
import com.codewithsuraj.OrderService.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;

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

        log.info("Calling payment service to complete the payment");
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus = null;
        try{
            paymentService.doPayment(paymentRequest);
            log.info("Payment done successfully. changing the order status");
            orderStatus = "PLACED";
        } catch (Exception e) {
            log.error("Error occurred in payment. Changing the order status");
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        log.info("Order places successfully with order id: {}", order.getId());

        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Getting order details for order id: {}", orderId);
         Order order = orderRepository.findById(orderId).orElseThrow(()-> new CustomException("Order not found with id: "+orderId,
                 "NOT_FOUND",404));

         log.info("Invoking Product service to get the product details for product id: {}", order.getProduct_id());
            ProductResponse  productResponse= restTemplate.getForObject("http://PRODUCT-SERVICE/product/"+order.getProduct_id(),
                    ProductResponse.class);

         log.info("Getting payment details from payment service for order id: {}", orderId);
            PaymentResponse paymentResponse = restTemplate.getForObject("http://PAYMENT-SERVICE/payment/"+orderId,PaymentResponse.class);

         OrderResponse.ProductDetails productDetails = OrderResponse.ProductDetails.builder()
                  .productId(order.getProduct_id())
                  .productName(productResponse.getProductName())
                  .price(productResponse.getPrice())
                  .quantity(order.getQuantity())
                  .build();


        OrderResponse.PaymentDetails paymentDetails = OrderResponse.PaymentDetails.builder()
                .paymentId(paymentResponse.getPaymentId())
                .paymentMode(paymentResponse.getPaymentMode())
                .amount(paymentResponse.getAmount())
                .paymentDate(paymentResponse.getPaymentDate())
                .status(paymentResponse.getStatus())
                .orderId(paymentResponse.getOrderId())
                .build();

         OrderResponse orderResponse = OrderResponse.builder()
                 .orderId(order.getId())
                 .orderDate(order.getOrderDate())
                 .orderStatus(order.getOrderStatus())
                 .amount(order.getAmount())
                 .productDetails(productDetails)
                 .paymentDetails(paymentDetails)
                 .build();
         return orderResponse;
    }


}
