package dev.portfolio.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CacheStore {
  private final StringRedisTemplate redis;
  private final ObjectMapper json;
  private final CircuitBreaker breaker;
  private final MeterRegistry metrics;

  public CacheStore(
      StringRedisTemplate redis,
      ObjectMapper json,
      CircuitBreakerRegistry registry,
      MeterRegistry metrics) {
    this.redis = redis;
    this.json = json;
    this.breaker = registry.circuitBreaker("redis");
    this.metrics = metrics;
  }

  public <T> Optional<T> get(String key, Class<T> type) {
    try {
      String value = breaker.executeSupplier(() -> redis.opsForValue().get(key));
      metrics.counter("catalog.cache", "result", value == null ? "miss" : "hit").increment();
      return value == null ? Optional.empty() : Optional.of(json.readValue(value, type));
    } catch (Exception ex) {
      metrics.counter("catalog.cache", "result", "error").increment();
      return Optional.empty();
    }
  }

  public void put(String key, Object value, Duration ttl) {
    try {
      String encoded = json.writeValueAsString(value);
      breaker.executeRunnable(() -> redis.opsForValue().set(key, encoded, ttl));
    } catch (Exception ex) {
      metrics.counter("catalog.cache", "result", "error").increment();
    }
  }
}
