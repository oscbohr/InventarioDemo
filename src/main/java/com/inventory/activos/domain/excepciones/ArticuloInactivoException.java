package com.inventory.activos.domain.excepciones;

public class ArticuloInactivoException extends ExcepcionNegocio {

    public ArticuloInactivoException(Integer idArticulo) {
        super("El artículo con id " + idArticulo + " está dado de baja y no se puede modificar");
    }
}
