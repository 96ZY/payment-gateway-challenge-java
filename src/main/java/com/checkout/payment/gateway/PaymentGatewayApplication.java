package com.checkout.payment.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Payment Gateway Spring Boot application.
 * <p>
 * This application provides REST APIs for processing payments and retrieving
 * payment information. It integrates with a bank simulator for payment authorization.
 */
@SpringBootApplication
public class PaymentGatewayApplication {

  /**
   * Application entry point.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(PaymentGatewayApplication.class, args);
  }

}
