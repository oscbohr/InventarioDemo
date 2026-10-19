package com.inventory.activos.infrastructure.flujo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.inventory.activos.domain.estrategia.EstrategiaFactory;
import com.jayway.jsonpath.JsonPath;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;

/** Si el cálculo de depreciación falla, la transacción revierte también el artículo. */
@SpringBootTest(properties = {"depreciacion.porcentaje=10", "depreciacion.metodo=LINEA_RECTA"})
@AutoConfigureMockMvc
class ArticuloRollbackTest {

    @TestConfiguration
    static class RelojFijo {
        @Bean
        @Primary
        Clock relojFijo() {
            return Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);
        }
    }

    private static final IllegalStateException FALLO = new IllegalStateException("fallo simulado del cálculo");

    @Autowired
    MockMvc mvc;
    @SpyBean
    EstrategiaFactory estrategias;

    private static String body(String nombre, String valor) {
        return "{\"nombre\":\"%s\",\"serial\":\"SN\",\"descripcion\":\"d\",\"fechaCompra\":\"2025-01-10\",\"valorCompra\":%s}"
                .formatted(nombre, valor);
    }

    private double articulosCreados() throws Exception {
        var res = mvc.perform(get("/actuator/metrics/inventario.articulos.creados")).andReturn().getResponse();
        return res.getStatus() == 200 ? ((Number) JsonPath.read(res.getContentAsString(), "$.measurements[0].value")).doubleValue() : 0;
    }

    private int totalArticulos() throws Exception {
        String json = mvc.perform(get("/api/articulos")).andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.length()");
    }

    @Test
    void givenCalculationFails_whenCreateArticulo_thenArticuloIsNotSaved() throws Exception {
        doThrow(FALLO).when(estrategias).obtener(any());
        int antes = totalArticulos();
        double creadosAntes = articulosCreados();

        assertThatThrownBy(() -> mvc.perform(post("/api/articulos").contentType(MediaType.APPLICATION_JSON)
                .content(body("Laptop", "1000"))))
                .hasRootCause(FALLO);

        assertThat(totalArticulos()).isEqualTo(antes);
        assertThat(articulosCreados()).isEqualTo(creadosAntes);
    }

    @Test
    void givenCalculationFails_whenUpdateArticulo_thenDataAndDepreciacionesArePreserved() throws Exception {
        String json = mvc.perform(post("/api/articulos").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Laptop", "1000")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(json, "$.idArticulo");

        doThrow(FALLO).when(estrategias).obtener(any());
        assertThatThrownBy(() -> mvc.perform(put("/api/articulos/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(body("Portátil", "5000"))))
                .hasRootCause(FALLO);

        mvc.perform(get("/api/articulos/{id}", id))
                .andExpect(jsonPath("$.nombre").value("Laptop"))
                .andExpect(jsonPath("$.valorCompra").value(1000.0));
        mvc.perform(get("/api/articulos/{id}/depreciaciones", id))
                .andExpect(jsonPath("$.depreciaciones.length()").value(2))
                .andExpect(jsonPath("$.depreciaciones[0].valor").value(100.0));
    }
}
