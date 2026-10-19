package com.inventory.activos.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.inventory.activos.application.port.ArticuloUseCase;
import com.inventory.activos.domain.excepciones.ArticuloInactivoException;
import com.inventory.activos.domain.excepciones.ArticuloNoEncontradoException;
import com.inventory.activos.domain.Articulo;
import com.inventory.activos.domain.DepreciacionArticulo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticuloController.class)
class ArticuloControllerTest {

    private static final String BODY = """
            {"nombre":"Laptop","serial":"SN-1","descripcion":"d","fechaCompra":"2024-03-01","valorCompra":1000}""";

    @Autowired
    MockMvc mvc;
    @MockBean
    ArticuloUseCase useCase;

    private Articulo articulo() {
        return new Articulo(1, "Laptop", "SN-1", "d", LocalDate.of(2024, 3, 1), new BigDecimal("1000.00"), true);
    }

    @Test
    void givenValidBody_whenPostArticulo_thenReturns201WithArticulo() throws Exception {
        when(useCase.crear(any())).thenReturn(articulo());

        mvc.perform(post("/api/articulos").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idArticulo").value(1))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void givenInvalidBody_whenPostArticulo_thenReturns400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/articulos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"valorCompra\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.serial").exists())
                .andExpect(jsonPath("$.errores.fechaCompra").exists())
                .andExpect(jsonPath("$.errores.valorCompra").exists());
    }

    @Test
    void givenMalformedJson_whenPostArticulo_thenReturns400() throws Exception {
        mvc.perform(post("/api/articulos").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenNonExistentId_whenGetArticulo_thenReturns404() throws Exception {
        when(useCase.obtenerPorId(9)).thenThrow(new ArticuloNoEncontradoException(9));

        mvc.perform(get("/api/articulos/9")).andExpect(status().isNotFound());
    }

    @Test
    void givenInactiveArticulo_whenPutArticulo_thenReturns409() throws Exception {
        when(useCase.actualizar(eq(1), any())).thenThrow(new ArticuloInactivoException(1));

        mvc.perform(put("/api/articulos/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void givenValidBody_whenPutArticulo_thenReturns200() throws Exception {
        when(useCase.actualizar(eq(1), any())).thenReturn(articulo());

        mvc.perform(put("/api/articulos/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laptop"));
    }

    @Test
    void givenExistingArticulo_whenDeleteArticulo_thenReturns204AndDarDeBaja() throws Exception {
        mvc.perform(delete("/api/articulos/1")).andExpect(status().isNoContent());

        verify(useCase).darDeBaja(1);
    }

    @Test
    void givenExistingArticulos_whenGetArticulos_thenReturnsList() throws Exception {
        when(useCase.listar()).thenReturn(List.of(articulo()));

        mvc.perform(get("/api/articulos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serial").value("SN-1"));
    }

    @Test
    void givenDepreciaciones_whenGetDepreciaciones_thenReturnsArticuloWithDepreciacionesWithoutInternalIds() throws Exception {
        Articulo articulo = articulo();
        articulo.setDepreciaciones(List.of(
                new DepreciacionArticulo(7, 1, 2024, new BigDecimal("100"), new BigDecimal("10"),
                        new BigDecimal("1000"))));
        when(useCase.obtenerConDepreciaciones(1)).thenReturn(articulo);

        mvc.perform(get("/api/articulos/1/depreciaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idArticulo").value(1))
                .andExpect(jsonPath("$.nombre").value("Laptop"))
                .andExpect(jsonPath("$.depreciaciones[0].ano").value(2024))
                .andExpect(jsonPath("$.depreciaciones[0].valor").value(100.00))
                .andExpect(jsonPath("$.depreciaciones[0].porcentaje").value(10.00))
                .andExpect(jsonPath("$.depreciaciones[0].vrActualArticulo").value(1000.00))
                .andExpect(jsonPath("$.depreciaciones[0].id").doesNotExist());
    }
}
