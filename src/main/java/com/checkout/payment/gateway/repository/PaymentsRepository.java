package com.checkout.payment.gateway.repository;

import com.checkout.payment.gateway.domain.Payment;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * An in-memory idempotency mechanism that ensures in-flight request deduplication using
 * ConcurrentHashMap and CompletableFuture.
 * <p>
 * Key properties: - Only one in-flight execution per idempotency key - Concurrent callers share the
 * same computation result - Avoids explicit locking via CAS-based coordination
 * <p>
 * Limitations: - Only suitable for single-instance deployments - Does not guarantee strict
 * idempotency across time - Does not cancel underlying execution on timeout
 */
@Repository
public class PaymentsRepository {

  /**
   * Stores successfully processed payments by their ID.
   */
  private final Map<UUID, Payment> payments = new ConcurrentHashMap<>();

  /**
   * Tracks in-flight requests for idempotency. Key -> ongoing computation result
   * (CompletableFuture)
   */
  private final Map<String, CompletableFuture<Payment>> idempotencyMap = new ConcurrentHashMap<>();

  /**
   * Timeout for processing a payment (to avoid indefinite blocking).
   */
  private static final long TIMEOUT_MS = 3000;

  /**
   * Retrieves a previously processed payment by its unique ID.
   * <p>
   * This is a simple read operation backed by ConcurrentHashMap, providing thread-safe and
   * efficient concurrent access with O(1) lookup.
   *
   * @param id the unique identifier of the payment
   * @return an Optional containing the payment if present, otherwise empty
   */
  public Optional<Payment> get(UUID id) {
    return Optional.ofNullable(payments.get(id));
  }

  /**
   * Processes a payment request with in-flight idempotency guarantees.
   *
   * <p>This method ensures that for a given idempotency key:
   * <ul>
   *   <li>Only one thread (the "owner") performs the computation</li>
   *   <li>Concurrent callers reuse the same in-flight result</li>
   *   <li>No explicit locking is used (CAS-based coordination via ConcurrentHashMap)</li>
   * </ul>
   *
   * <p><b>Execution model:</b>
   * <ul>
   *   <li>The first thread installs a new CompletableFuture via {@code putIfAbsent}</li>
   *   <li>This thread executes the supplier and completes the future</li>
   *   <li>Other threads observe the existing future and wait via {@code join()}</li>
   * </ul>
   *
   * <p><b>Concurrency guarantees:</b>
   * <ul>
   *   <li>At most one execution per key at any given time (in-flight deduplication)</li>
   *   <li>Memory visibility is ensured by the happens-before relationship between
   *       {@code complete()} and {@code join()}</li>
   * </ul>
   *
   * <p><b>Trade-offs:</b>
   * <ul>
   *   <li>Does not guarantee strict idempotency across time (due to key removal)</li>
   *   <li>May re-execute after completion if a new request arrives</li>
   *   <li>Suitable for single-instance deployments only</li>
   * </ul>
   *
   * @param key      idempotency key identifying the request
   * @param supplier computation that produces the payment result
   * @return the processed payment
   * @throws RuntimeException if execution fails or times out
   */
  public Payment process(String key, Supplier<Payment> supplier) {
    // Fail-fast validation
    if (key == null || supplier == null) {
      throw new IllegalArgumentException("key and supplier must not be null");
    }

    // Try to register a new in-flight computation.
    //
    // putIfAbsent guarantees atomicity and safe publication of the future
    // (only one thread can successfully install a new CompletableFuture for the given key),
    // so that other threads will observe a fully constructed CompletableFuture.
    //
    // The winning thread becomes the "owner" responsible for executing the supplier,
    // while other threads will observe the existing future and wait for its result.
    CompletableFuture<Payment> newFuture = new CompletableFuture<>();
    CompletableFuture<Payment> existing = idempotencyMap.putIfAbsent(key, newFuture);

    if (existing == null) {
      // Owner thread executes supplier
      try {
        Payment result = supplier.get();

        // Ensure shared state is published before completing the future.
        // Due to the happens-before guarantee between complete() and join(),
        // all threads unblocked by join() will observe this write.
        payments.put(result.getId(), result);

        newFuture.complete(result);
        return result;

      } catch (Exception e) {
        newFuture.completeExceptionally(e);
        throw e;

      } finally {
        // Cleanup to avoid memory leak.
        //
        // Trade-off:
        // - Removing the key allows the map to stay bounded
        // - However, it introduces a race window where the same key may be processed again
        //   if a new request arrives after completion
        //
        // Therefore, this implementation guarantees:
        // - In-flight deduplication (only one execution at a time)
        // But NOT:
        // - Strict idempotency across time
        idempotencyMap.remove(key);
      }
    }

    try {
      // Other threads wait for owner execution
      return existing.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      throw new RuntimeException("Payment processing timed out after " + TIMEOUT_MS + " ms", e);
    } catch (ExecutionException e) {
      throw unwrap(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Payment processing interrupted", e);
    }
  }

  /**
   * Unwraps {@link ExecutionException} to expose the original cause.
   *
   * @param e ExecutionException to unwrap
   * @return the original cause as RuntimeException
   */
  private RuntimeException unwrap(ExecutionException e) {
    Throwable cause = e.getCause();
    if (cause == null) {
      return new RuntimeException("Unknown execution exception", e);
    }
    return cause instanceof RuntimeException ? (RuntimeException) cause
        : new RuntimeException(cause);
  }
}