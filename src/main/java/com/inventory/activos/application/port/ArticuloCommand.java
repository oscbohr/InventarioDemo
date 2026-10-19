package com.inventory.activos.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos de un artículo para crearlo o modificarlo. */
public record ArticuloCommand(String nombre, String serial, String descripcion,
                              LocalDate fechaCompra, BigDecimal valorCompra) {
}
