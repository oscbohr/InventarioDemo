package com.inventory.activos.application.port;

import com.inventory.activos.domain.DepreciacionArticulo;
import java.util.List;

public interface DepreciacionRepository {

    List<DepreciacionArticulo> saveAll(List<DepreciacionArticulo> depreciaciones);

    List<DepreciacionArticulo> findByIdArticulo(Integer idArticulo);

    void deleteByIdArticulo(Integer idArticulo);
}
