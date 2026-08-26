package com.inno.authenticationservice.configuration;

import static com.inno.authenticationservice.util.ConfigConstants.TRACE_ID_HEADER;
import static com.inno.authenticationservice.util.ConfigConstants.TRACE_ID_KEY;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inno.authenticationservice.exception.FeignErrorDecoder;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

  @Bean
  public ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
    return new FeignErrorDecoder(objectMapper);
  }

  @Bean
  public RequestInterceptor traceIdRequestInterceptor() {
    return template -> {
      String traceId = MDC.get(TRACE_ID_KEY);
      if (traceId != null && !traceId.isEmpty()) {
        template.header(TRACE_ID_HEADER, traceId);
      }
    };
  }
}
