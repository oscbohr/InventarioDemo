package com.inventory.activos.domain;

import com.inventory.activos.domain.excepciones.PorcentajeInvalidoException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Configuración global de depreciación; viene de application.yml, no se guarda en base de datos. */
public record ConfiguracionDepreciacion(BigDecimal porcentaje, MetodoDepreciacion metodo) {

    public ConfiguracionDepreciacion {
        if (porcentaje == null || porcentaje.signum() < 0 || porcentaje.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new PorcentajeInvalidoException(porcentaje);
        }
        porcentaje = porcentaje.setScale(2, RoundingMode.HALF_UP);
    }
}
