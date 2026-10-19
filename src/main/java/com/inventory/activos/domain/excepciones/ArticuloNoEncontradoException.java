package com.inventory.activos.domain.excepciones;

public class ArticuloNoEncontradoException extends ExcepcionNegocio {

    public ArticuloNoEncontradoException(Integer idArticulo) {
        super("No existe el artículo con id " + idArticulo);
    }
}
