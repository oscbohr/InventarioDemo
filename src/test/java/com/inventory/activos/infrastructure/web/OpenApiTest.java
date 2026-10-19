package com.inventory.activos.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiTest {

    @Autowired
    MockMvc mvc;
    @Value("${springdoc.api-docs.path}")
    String apiDocs;

    @Test
    void givenApplicationRunning_whenGetApiDocs_thenReturnsOpenApi3Contract() throws Exception {
        mvc.perform(get(apiDocs))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").value(org.hamcrest.Matchers.startsWith("3.")))
                .andExpect(jsonPath("$.info.title").value("InventoryDemo API"))
                .andExpect(jsonPath("$.paths['/api/articulos']").exists())
                .andExpect(jsonPath("$.paths['/api/articulos/{id}/depreciaciones']").exists());
    }

    @Test
    void givenApiDocs_whenInspectDtoSchemas_thenFieldsHaveDescriptionAndExample() throws Exception {
        mvc.perform(get(apiDocs))
                .andExpect(jsonPath("$.components.schemas.ArticuloRequest.properties.nombre.example").value("Laptop Dell Latitude"))
                .andExpect(jsonPath("$.components.schemas.ArticuloResponse.properties.activo.description").exists())
                .andExpect(jsonPath("$.components.schemas.DepreciacionResponse.properties.valor.example").exists())
                .andExpect(jsonPath("$.components.schemas.ArticuloDepreciacionesResponse.properties.depreciaciones").exists());
    }

    @Test
    void givenApplicationRunning_whenGetSwaggerUi_thenReturnsOk() throws Exception {
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
