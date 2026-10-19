package com.inventory.activos.infrastructure.persistence;

import com.inventory.activos.domain.Articulo;
import com.inventory.activos.domain.DepreciacionArticulo;

public final class PersistenceMapper {

    private PersistenceMapper() {
    }

    public static Articulo toDomain(ArticuloJpaEntity e) {
        return new Articulo(e.getIdArticulo(), e.getNombre(), e.getSerial(), e.getDescripcion(),
                e.getFechaCompra(), e.getValorCompra(), e.isActivo());
    }

    public static ArticuloJpaEntity toEntity(Articulo a) {
        ArticuloJpaEntity e = new ArticuloJpaEntity();
        e.setIdArticulo(a.getIdArticulo());
        e.setNombre(a.getNombre());
        e.setSerial(a.getSerial());
        e.setDescripcion(a.getDescripcion());
        e.setFechaCompra(a.getFechaCompra());
        e.setValorCompra(a.getValorCompra());
        e.setActivo(a.isActivo());
        return e;
    }

    public static DepreciacionArticulo toDomain(DepreciacionJpaEntity e) {
        return new DepreciacionArticulo(e.getId(), e.getIdArticulo(), e.getAno(), e.getValor(), e.getPorcentaje(),
                e.getVrActualArticulo());
    }

    public static DepreciacionJpaEntity toEntity(DepreciacionArticulo d) {
        DepreciacionJpaEntity e = new DepreciacionJpaEntity();
        e.setId(d.id());
        e.setIdArticulo(d.idArticulo());
        e.setAno(d.ano());
        e.setValor(d.valor());
        e.setPorcentaje(d.porcentaje());
        e.setVrActualArticulo(d.vrActualArticulo());
        return e;
    }
}
