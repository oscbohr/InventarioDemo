package com.inventory.activos.application.port;

import com.inventory.activos.domain.Articulo;
import com.inventory.activos.domain.DepreciacionArticulo;
import java.util.List;

public interface CalcularDepreciacionUseCase {

    /** Reemplaza las depreciaciones previas del artículo por las recién calculadas. */
    List<DepreciacionArticulo> calcularYRegistrar(Articulo articulo);
}
