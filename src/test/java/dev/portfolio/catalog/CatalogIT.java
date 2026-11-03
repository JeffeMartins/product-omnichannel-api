package dev.portfolio.catalog;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CatalogIT {
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

  @Container
  static GenericContainer<?> redis =
      new GenericContainer<>("redis:7.4-alpine").withExposedPorts(6379);

  @DynamicPropertySource
  static void config(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", postgres::getJdbcUrl);
    r.add("spring.datasource.password", postgres::getPassword);
    r.add("spring.datasource.username", postgres::getUsername);
    r.add("spring.data.redis.host", redis::getHost);
    r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
  }

  @Autowired MockMvc mvc;
  @Autowired StringRedisTemplate cache;

  @Test
  void contextNeverMixesPrices() throws Exception {
    mvc.perform(get("/api/v1/products/FONE-BT-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.offer.price").value("199.90"));
    mvc.perform(get("/api/v1/products/FONE-BT-001?channel=PICKUP&locationId=LOJA-PAULISTA"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.offer.price").value("189.90"));
    assertThat(cache.keys("catalog:offer:v1:*FONE-BT-001*")).hasSize(2);
  }

  @Test
  void stockAndValidation() throws Exception {
    mvc.perform(get("/api/v1/products/FONE-BT-AREIA"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.availability.status").value("OUT_OF_STOCK"));
    mvc.perform(get("/api/v1/products/FONE-BT-001?channel=WEB&locationId=LOJA-CENTRO"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/products/UNKNOWN")).andExpect(status().isNotFound());
    mvc.perform(get("/api/v1/products/lowercase")).andExpect(status().isBadRequest());
    mvc.perform(post("/api/v1/products/FONE-BT-001")).andExpect(status().isForbidden());
  }
}
