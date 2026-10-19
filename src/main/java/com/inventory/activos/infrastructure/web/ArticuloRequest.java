package com.inventory.activos.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Datos para crear o actualizar un artículo")
public record ArticuloRequest(
        @Schema(description = "Nombre del artículo", example = "Laptop Dell Latitude")
        @NotBlank String nombre,
        @Schema(description = "Número de serie del artículo", example = "SN-2024-0001")
        @NotBlank String serial,
        @Schema(description = "Descripción opcional", example = "Portátil de 14 pulgadas, 16 GB de RAM")
        String descripcion,
        @Schema(description = "Fecha de compra (ISO 8601)", example = "2024-03-01")
        @NotNull LocalDate fechaCompra,
        @Schema(description = "Valor de compra; debe ser mayor que 0", example = "3500.00", minimum = "0", exclusiveMinimum = true)
        @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal valorCompra) {
}
