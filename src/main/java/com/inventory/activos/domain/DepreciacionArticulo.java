package com.inventory.activos.domain;

import com.inventory.activos.domain.excepciones.PorcentajeInvalidoException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Depreciación de un artículo en un año; guarda el porcentaje con que se calculó y el valor en libros del artículo. */
public record DepreciacionArticulo(Integer id, Integer idArticulo, int ano,
                                   BigDecimal valor, BigDecimal porcentaje,
                                   BigDecimal vrActualArticulo) {

    public DepreciacionArticulo {
        if (porcentaje == null || porcentaje.signum() < 0 || porcentaje.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new PorcentajeInvalidoException(porcentaje);
        }
        valor = valor.setScale(2, RoundingMode.HALF_UP);
        porcentaje = porcentaje.setScale(2, RoundingMode.HALF_UP);
        vrActualArticulo = vrActualArticulo.setScale(2, RoundingMode.HALF_UP);
    }
}
