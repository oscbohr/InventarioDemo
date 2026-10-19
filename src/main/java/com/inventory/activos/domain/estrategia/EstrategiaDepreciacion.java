package com.inventory.activos.domain.estrategia;

import com.inventory.activos.domain.MetodoDepreciacion;
import java.math.BigDecimal;

public interface EstrategiaDepreciacion {

    MetodoDepreciacion metodo();

    /**
     * Depreciación de un año, con 2 decimales y redondeo HALF_UP. Nunca supera el valor en libros.
     *
     * @param valorCompra   costo original del artículo
     * @param valorEnLibros valor pendiente de depreciar al inicio del año
     * @param porcentaje    porcentaje anual (por ejemplo 10 para 10 %)
     */
    BigDecimal calcular(BigDecimal valorCompra, BigDecimal valorEnLibros, BigDecimal porcentaje);
}
