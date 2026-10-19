package com.inventory.activos.infrastructure.flujo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"depreciacion.porcentaje=10", "depreciacion.metodo=LINEA_RECTA"})
@AutoConfigureMockMvc
class ArticuloFlujoTest {

    @TestConfiguration
    static class RelojFijo {
        @Bean
        @Primary
        Clock relojFijo() {
            return Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    MockMvc mvc;

    private static String body(String nombre, String fecha, String valor) {
        return "{\"nombre\":\"%s\",\"serial\":\"SN\",\"descripcion\":\"d\",\"fechaCompra\":\"%s\",\"valorCompra\":%s}"
                .formatted(nombre, fecha, valor);
    }

    private int crear(String fecha, String valor) throws Exception {
        String json = mvc.perform(post("/api/articulos").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Laptop", fecha, valor)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.idArticulo");
    }

    @Test
    void givenInitialConfiguration_whenCreateArticulo_thenCalculatesOneRecordPerYear() throws Exception {
        int id = crear("2024-03-01", "1000");

        mvc.perform(get("/api/articulos/{id}/depreciaciones", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.depreciaciones.length()").value(3))
                .andExpect(jsonPath("$.depreciaciones[0].ano").value(2024))
                .andExpect(jsonPath("$.depreciaciones[2].ano").value(2026))
                .andExpect(jsonPath("$.depreciaciones[0].vrActualArticulo").value(1000.0))
                .andExpect(jsonPath("$.depreciaciones[1].vrActualArticulo").value(900.0))
                .andExpect(jsonPath("$.depreciaciones[2].vrActualArticulo").value(800.0))
                .andExpect(jsonPath("$.depreciaciones[0].valor").value(100.0))
                .andExpect(jsonPath("$.depreciaciones[0].porcentaje").value(10.0));
    }

    @Test
    void givenExistingArticulo_whenUpdateValor_thenReplacesDepreciaciones() throws Exception {
        int id = crear("2025-01-10", "1000");

        mvc.perform(put("/api/articulos/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Laptop", "2025-01-10", "2000")))
                .andExpect(status().isOk());

        mvc.perform(get("/api/articulos/{id}/depreciaciones", id))
                .andExpect(jsonPath("$.depreciaciones.length()").value(2))
                .andExpect(jsonPath("$.depreciaciones[0].valor").value(200.0));
    }

    @Test
    void givenExistingArticulo_whenUpdateOnlyNombre_thenKeepsDepreciaciones() throws Exception {
        int id = crear("2025-01-10", "1000");

        mvc.perform(put("/api/articulos/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Portátil", "2025-01-10", "1000")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Portátil"));

        mvc.perform(get("/api/articulos/{id}/depreciaciones", id))
                .andExpect(jsonPath("$.depreciaciones.length()").value(2))
                .andExpect(jsonPath("$.depreciaciones[0].valor").value(100.0));
    }

    @Test
    void givenExistingArticulo_whenDarDeBaja_thenKeepsDepreciacionesAndBlocksUpdate() throws Exception {
        int id = crear("2025-01-10", "1000");

        mvc.perform(delete("/api/articulos/{id}", id)).andExpect(status().isNoContent());

        mvc.perform(get("/api/articulos/{id}", id)).andExpect(jsonPath("$.activo").value(false));
        mvc.perform(get("/api/articulos/{id}/depreciaciones", id)).andExpect(jsonPath("$.depreciaciones.length()").value(2));
        mvc.perform(put("/api/articulos/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("X", "2025-01-10", "1000")))
                .andExpect(status().isConflict());
    }
}
