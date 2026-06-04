package com.github.leoyakubov.twofactorauth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void shouldReuseIncomingRequestIdAndExposeItInResponseHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "request-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> requestIdSeenInChain = new AtomicReference<>();

        filter.doFilter(request, response, chain(requestIdSeenInChain));

        assertThat(response.getHeader("X-Request-Id")).isEqualTo("request-123");
        assertThat(requestIdSeenInChain.get()).isEqualTo("request-123");
        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    void shouldGenerateRequestIdWhenMissingFromRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> requestIdSeenInChain = new AtomicReference<>();

        filter.doFilter(request, response, chain(requestIdSeenInChain));

        assertThat(response.getHeader("X-Request-Id")).isNotBlank();
        assertThat(requestIdSeenInChain.get()).isEqualTo(response.getHeader("X-Request-Id"));
        assertThat(MDC.get("requestId")).isNull();
    }

    private FilterChain chain(AtomicReference<String> requestIdSeenInChain) {
        return (request, response) -> requestIdSeenInChain.set(MDC.get("requestId"));
    }
}
