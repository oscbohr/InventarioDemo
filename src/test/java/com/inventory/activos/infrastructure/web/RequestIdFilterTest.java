package com.inventory.activos.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    private String ejecutar(MockHttpServletRequest request, MockHttpServletResponse response, AtomicReference<String> enMdc)
            throws Exception {
        filter.doFilter(request, response, new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                enMdc.set(MDC.get(RequestIdFilter.MDC_KEY));
            }
        });
        return response.getHeader(RequestIdFilter.HEADER);
    }

    @Test
    void givenRequestWithoutHeader_whenFilter_thenGeneratesIdInMdcAndResponseHeader() throws Exception {
        var enMdc = new AtomicReference<String>();

        String header = ejecutar(new MockHttpServletRequest("GET", "/api/articulos"), new MockHttpServletResponse(), enMdc);

        assertThat(header).isNotBlank();
        assertThat(enMdc.get()).isEqualTo(header);
    }

    @Test
    void givenValidRequestIdHeader_whenFilter_thenReusesIt() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/articulos");
        request.addHeader(RequestIdFilter.HEADER, "abc-123");
        var enMdc = new AtomicReference<String>();

        String header = ejecutar(request, new MockHttpServletResponse(), enMdc);

        assertThat(header).isEqualTo("abc-123");
        assertThat(enMdc.get()).isEqualTo("abc-123");
    }

    @Test
    void givenUnsafeRequestIdHeader_whenFilter_thenReplacesIt() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/articulos");
        request.addHeader(RequestIdFilter.HEADER, "x\nFAKE LOG LINE");

        String header = ejecutar(request, new MockHttpServletResponse(), new AtomicReference<>());

        assertThat(header).matches("[A-Za-z0-9-]{1,64}").isNotEqualTo("x\nFAKE LOG LINE");
    }

    @Test
    void givenCompletedRequest_whenFilter_thenClearsMdc() throws Exception {
        ejecutar(new MockHttpServletRequest("GET", "/api/articulos"), new MockHttpServletResponse(), new AtomicReference<>());

        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
    }
}
