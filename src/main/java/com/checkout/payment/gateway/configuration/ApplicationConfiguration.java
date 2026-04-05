package com.checkout.payment.gateway.configuration;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Spring configuration class for application-wide beans.
 * <p>
 * Configures HTTP clients and other infrastructure components.
 */
@Configuration
public class ApplicationConfiguration {

  /**
   * Creates a configured {@link RestTemplate} for making HTTP calls to external services.
   * <p>
   * The template is configured with:
   * <ul>
   *   <li>Connection timeout: 10 seconds</li>
   *   <li>Read timeout: 10 seconds</li>
   * </ul>
   *
   * @param builder the RestTemplateBuilder provided by Spring Boot
   * @return a configured RestTemplate instance
   */
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofMillis(10000))
        .setReadTimeout(Duration.ofMillis(10000))
        .build();
  }
}
