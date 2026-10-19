package com.inventory.activos.infrastructure.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"depreciacion.porcentaje=10", "depreciacion.metodo=LINEA_RECTA"})
@AutoConfigureMockMvc
class ActuatorMetricasTest {

    private static final String BODY =
            "{\"nombre\":\"Laptop\",\"serial\":\"SN\",\"descripcion\":\"d\",\"fechaCompra\":\"2025-01-10\",\"valorCompra\":1000}";

    @Autowired
    MockMvc mvc;

    private double valor(String metrica, String estadistica) throws Exception {
        String json = mvc.perform(get("/actuator/metrics/" + metrica)).andReturn().getResponse().getContentAsString();
        java.util.List<Number> valores = JsonPath.read(json, "$.measurements[?(@.statistic=='" + estadistica + "')].value");
        return valores.get(0).doubleValue();
    }

    @Test
    void givenApplicationRunning_whenGetHealth_thenReturnsUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void givenNotExposedEndpoint_whenGetEnv_thenReturns404() throws Exception {
        mvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
    }

    @Test
    void givenArticuloCreatedAndDeleted_whenGetMetrics_thenBusinessCountersIncrease() throws Exception {
        double creadosAntes = existe(MicrometerMetricasAdapter.ARTICULOS_CREADOS)
                ? valor(MicrometerMetricasAdapter.ARTICULOS_CREADOS, "COUNT") : 0;
        double bajasAntes = existe(MicrometerMetricasAdapter.ARTICULOS_BAJA)
                ? valor(MicrometerMetricasAdapter.ARTICULOS_BAJA, "COUNT") : 0;

        String json = mvc.perform(post("/api/articulos").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(json, "$.idArticulo");
        mvc.perform(delete("/api/articulos/{id}", id)).andExpect(status().isNoContent());

        assertThat(valor(MicrometerMetricasAdapter.ARTICULOS_CREADOS, "COUNT"))
                .isEqualTo(creadosAntes + 1);
        assertThat(valor(MicrometerMetricasAdapter.ARTICULOS_BAJA, "COUNT"))
                .isEqualTo(bajasAntes + 1);
        assertThat(valor(MicrometerMetricasAdapter.DEPRECIACION_CALCULO, "COUNT"))
                .isGreaterThanOrEqualTo(1);
        mvc.perform(get("/actuator/metrics/http.server.requests")).andExpect(status().isOk());
    }

    private boolean existe(String metrica) throws Exception {
        return mvc.perform(get("/actuator/metrics/" + metrica)).andReturn().getResponse().getStatus() == 200;
    }
}
