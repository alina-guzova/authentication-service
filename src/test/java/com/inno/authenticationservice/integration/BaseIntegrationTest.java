package com.inno.authenticationservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

  static final PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:17-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test")
          .withStartupTimeout(Duration.ofSeconds(120));

  protected static final WireMockServer wireMockServer =
      new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

  @BeforeAll
  static void startContainers() {
    postgres.start();
    wireMockServer.start();
  }

  @AfterAll
  static void stopContainers() {
    postgres.stop();
    wireMockServer.stop();
  }

  @BeforeEach
  void resetWireMock() {
    wireMockServer.resetAll();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);

    registry.add("application.integration.user-service.url", wireMockServer::baseUrl);
  }
}
