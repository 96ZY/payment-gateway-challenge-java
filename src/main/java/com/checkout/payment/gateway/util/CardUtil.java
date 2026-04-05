package com.checkout.payment.gateway.util;

/**
 * Utility class for card-related operations.
 */
public class CardUtil {

  /**
   * Extracts the last four digits from a card number.
   *
   * @param cardNumber The card number from which to extract the last four digits.
   * @return The last four digits of the card number.
   */
  public static String getLastFourDigits(String cardNumber) {
    return cardNumber.substring(cardNumber.length() - 4);
  }
}
