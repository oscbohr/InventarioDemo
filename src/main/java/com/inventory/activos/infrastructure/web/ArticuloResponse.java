package com.inventory.activos.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Artículo del inventario")
public record ArticuloResponse(
        @Schema(description = "Identificador generado del artículo", example = "1")
        Integer idArticulo,
        @Schema(description = "Nombre del artículo", example = "Laptop Dell Latitude")
        String nombre,
        @Schema(description = "Número de serie del artículo", example = "SN-2024-0001")
        String serial,
        @Schema(description = "Descripción del artículo", example = "Portátil de 14 pulgadas, 16 GB de RAM")
        String descripcion,
        @Schema(description = "Fecha de compra (ISO 8601)", example = "2024-03-01")
        LocalDate fechaCompra,
        @Schema(description = "Valor de compra con 2 decimales", example = "3500.00")
        BigDecimal valorCompra,
        @Schema(description = "false si el artículo fue dado de baja", example = "true")
        boolean activo) {
}
