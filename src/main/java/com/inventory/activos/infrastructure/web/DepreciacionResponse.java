package com.inventory.activos.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Depreciación de un artículo en un año")
public record DepreciacionResponse(
        @Schema(description = "Año al que corresponde la depreciación", example = "2024")
        int ano,
        @Schema(description = "Monto depreciado en el año, con 2 decimales", example = "350.00")
        BigDecimal valor,
        @Schema(description = "Porcentaje anual (0–100) con que se calculó", example = "10.00")
        BigDecimal porcentaje,
        @Schema(description = "Valor en libros del artículo (valorEnLibros) usado para calcular este año, con 2 decimales", example = "3500.00")
        BigDecimal vrActualArticulo) {
}
