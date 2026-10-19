package com.inventory.activos.domain.excepciones;

import java.math.BigDecimal;

public class PorcentajeInvalidoException extends ExcepcionNegocio {

    public PorcentajeInvalidoException(BigDecimal porcentaje) {
        super("El porcentaje debe estar entre 0 y 100, pero fue " + porcentaje);
    }
}
