package com.inventory.activos.domain.excepciones;

public abstract class ExcepcionNegocio extends RuntimeException {

    protected ExcepcionNegocio(String mensaje) {
        super(mensaje);
    }
}
