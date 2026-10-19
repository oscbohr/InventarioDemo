package com.inventory.activos.domain.estrategia;

import com.inventory.activos.domain.MetodoDepreciacion;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Porcentaje sobre el valor en libros de cada año. */
public class SaldoDecreciente implements EstrategiaDepreciacion {

    @Override
    public MetodoDepreciacion metodo() {
        return MetodoDepreciacion.SALDO_DECRECIENTE;
    }

    @Override
    public BigDecimal calcular(BigDecimal valorCompra, BigDecimal valorEnLibros, BigDecimal porcentaje) {
        BigDecimal anual = valorEnLibros.multiply(porcentaje).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return anual.min(valorEnLibros);
    }
}
