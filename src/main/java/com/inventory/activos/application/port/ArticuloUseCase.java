package com.inventory.activos.application.port;

import com.inventory.activos.domain.Articulo;
import com.inventory.activos.domain.excepciones.ArticuloInactivoException;
import com.inventory.activos.domain.excepciones.ArticuloNoEncontradoException;

import java.util.List;

public interface ArticuloUseCase {

    Articulo crear(ArticuloCommand command);

    /** @throws ArticuloNoEncontradoException si no existe
     *  @throws ArticuloInactivoException si está dado de baja */
    Articulo actualizar(Integer idArticulo, ArticuloCommand command);

    void darDeBaja(Integer idArticulo);

    Articulo obtenerPorId(Integer idArticulo);

    List<Articulo> listar();

    /** Devuelve el artículo con su relación de depreciaciones (ordenadas por año) cargada. */
    Articulo obtenerConDepreciaciones(Integer idArticulo);
}
