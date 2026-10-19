package com.inventory.activos.application.port;

import com.inventory.activos.domain.Articulo;
import java.util.List;
import java.util.Optional;

public interface ArticuloRepository {

    Articulo save(Articulo articulo);

    Optional<Articulo> findById(Integer idArticulo);

    List<Articulo> findAll();
}
