package com.checkout.payment.gateway.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.domain.Card;
import com.checkout.payment.gateway.domain.Money;
import com.checkout.payment.gateway.domain.Payment;
import com.checkout.payment.gateway.enums.Currency;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.repository.PaymentsRepository;

import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private PaymentsRepository paymentsRepository;

  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {

    // Arrange: create and save domain object
    UUID id = UUID.randomUUID();

    Payment payment = new Payment(id, new Card("4242424242424321", 12, 2024),
        new Money(10L, Currency.USD), PaymentStatus.AUTHORIZED,
        "35b30263-f9f5-458f-be26-447470a4d1b7");

    paymentsRepository.process("35b30263-f9f5-459f-be27-447470a1dcbc", new Supplier<Payment>() {
      @Override
      public Payment get() {
        return payment;
      }
    });

    mvc.perform(MockMvcRequestBuilders.get("/api/payment/" + id)).andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.cardNumberLastFour").value("4321"))
        .andExpect(jsonPath("$.expiryMonth").value(12))
        .andExpect(jsonPath("$.expiryYear").value(2024))
        .andExpect(jsonPath("$.currency").value("USD")).andExpect(jsonPath("$.amount").value(10));
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {

    mvc.perform(MockMvcRequestBuilders.get("/api/payment/" + UUID.randomUUID()))
        .andExpect(status().isNotFound());
  }

  /**
   * Verify that a valid payment request is processed successfully. Ensures correct API response
   * structure and mapping.
   */
  @Test
  void shouldCreatePaymentSuccessfully() throws Exception {

    String requestJson = """
        {
          "card_number": "4242424242424231",
          "expiry_month": 12,
          "expiry_year": 2028,
          "currency": "GBP",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/api/payment")
            .header("idempotency-key", UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE).content(requestJson))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.status").exists())
        .andExpect(jsonPath("$.cardNumberLastFour").value("4231"))
        .andExpect(jsonPath("$.amount").value(1000)).andExpect(jsonPath("$.currency").value("GBP"));
  }

  /**
   * Verify that validation fails when amount is negative.
   */
  @Test
  void shouldReturn400WhenAmountInvalid() throws Exception {

    String requestJson = """
        {
          "card_number": "4242424242424242",
          "expiry_month": 12,
          "expiry_year": 2028,
          "currency": "GBP",
          "amount": -100,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/api/payment")
            .header("idempotency-key", UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE).content(requestJson))
        .andExpect(status().isBadRequest());
  }

  /**
   * Verify that validation fails when expiry month is missing.
   */
  @Test
  void shouldReturn400WhenExpiryMonthMissing() throws Exception {

    String requestJson = """
        {
          "card_number": "4242424242424242",
          "expiry_year": 2028,
          "currency": "GBP",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/api/payment")
            .header("idempotency-key", UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON_VALUE).content(requestJson))
        .andExpect(status().isBadRequest());
  }

  /**
   * Verify that validation fails when card number format is invalid.
   */
  @Test
  void shouldReturn400WhenCardNumberInvalid() throws Exception {

    String requestJson = """
        {
          "card_number": "123",
          "expiry_month": 12,
          "expiry_year": 2028,
          "currency": "GBP",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/api/payment").header("idempotency-key", "1234567890")
            .contentType(
                MediaType.APPLICATION_JSON_VALUE).content(requestJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnSameResponseForSameIdempotencyKey() throws Exception {

    String requestJson = """
        {
          "card_number": "4242424242424242",
          "expiry_month": 12,
          "expiry_year": 2028,
          "currency": "GBP",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    String key = UUID.randomUUID().toString();

    // first request
    String response1 = mvc.perform(
            MockMvcRequestBuilders.post("/api/payment").header("Idempotency-Key", key)
                .contentType(
                    MediaType.APPLICATION_JSON_VALUE).content(requestJson))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    // second request (same key)
    String response2 = mvc.perform(
            MockMvcRequestBuilders.post("/api/payment").header("Idempotency-Key", key)
                .contentType(
                    MediaType.APPLICATION_JSON_VALUE).content(requestJson))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    // assert same response
    assertEquals(response1, response2);
  }

  @Test
  void shouldReturnDeclinedWhenBankRejects() throws Exception {

    String requestJson = """
        {
          "card_number": "4000000000000002",
          "expiry_month": 12,
          "expiry_year": 2028,
          "currency": "GBP",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/api/payment")
            .header("idempotency-key", UUID.randomUUID().toString()).contentType(
                MediaType.APPLICATION_JSON_VALUE).content(requestJson)).andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("Declined"));
  }

  @Test
  void shouldReturn400WhenCardExpired() throws Exception {

    int lastYear = java.time.Year.now().getValue() - 1;

    String requestJson = String.format("""
        {
          "card_number": "4242424242424242",
          "expiry_month": 12,
          "expiry_year": %d,
          "currency": "GBP",
          "amount": 1000,
          "cvv": "123"
        }
        """, lastYear);

    mvc.perform(MockMvcRequestBuilders.post("/api/payment")
            .header("idempotency-key", UUID.randomUUID().toString()).contentType(
                MediaType.APPLICATION_JSON_VALUE).content(requestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("expiryMonth Card expiry date must be in the future"));
  }

  @Test
  void shouldReturn400WhenCurrencyInvalid() throws Exception {

    String requestJson = """
        {
          "card_number": "4242424242424242",
          "expiry_month": 12,
          "expiry_year": 2028,
          "currency": "AAA",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/api/payment")
            .header("idempotency-key", UUID.randomUUID().toString()).contentType(
                MediaType.APPLICATION_JSON_VALUE).content(requestJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("currency Invalid currency")); // 前提：你的异常处理返回字段级错误
  }
}