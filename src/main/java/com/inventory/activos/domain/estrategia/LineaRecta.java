package com.inventory.activos.domain.estrategia;

import com.inventory.activos.domain.MetodoDepreciacion;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Mismo monto cada año: porcentaje sobre el valor de compra. */
public class LineaRecta implements EstrategiaDepreciacion {

    @Override
    public MetodoDepreciacion metodo() {
        return MetodoDepreciacion.LINEA_RECTA;
    }

    @Override
    public BigDecimal calcular(BigDecimal valorCompra, BigDecimal valorEnLibros, BigDecimal porcentaje) {
        BigDecimal anual = valorCompra.multiply(porcentaje).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return anual.min(valorEnLibros);
    }
}
