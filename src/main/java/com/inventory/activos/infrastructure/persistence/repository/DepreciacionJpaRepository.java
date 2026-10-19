package com.inventory.activos.infrastructure.persistence.repository;

import com.inventory.activos.infrastructure.persistence.DepreciacionJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepreciacionJpaRepository extends JpaRepository<DepreciacionJpaEntity, Integer> {

    List<DepreciacionJpaEntity> findByIdArticuloOrderByAno(Integer idArticulo);

    /** Borrado inmediato (flush previo y limpieza del contexto) para poder reinsertar el mismo año. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from DepreciacionJpaEntity d where d.idArticulo = :idArticulo")
    void deleteByIdArticulo(@Param("idArticulo") Integer idArticulo);
}
