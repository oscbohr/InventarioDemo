package com.inventory.activos.domain.estrategia;

import com.inventory.activos.domain.MetodoDepreciacion;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Resuelve la estrategia por método; agregar una estrategia nueva no modifica esta clase. */
public class EstrategiaFactory {

    private final Map<MetodoDepreciacion, EstrategiaDepreciacion> estrategias = new EnumMap<>(MetodoDepreciacion.class);

    public EstrategiaFactory(List<EstrategiaDepreciacion> disponibles) {
        for (EstrategiaDepreciacion e : disponibles) {
            if (estrategias.putIfAbsent(e.metodo(), e) != null) {
                throw new IllegalArgumentException("Estrategia duplicada para el método " + e.metodo());
            }
        }
    }

    public EstrategiaDepreciacion obtener(MetodoDepreciacion metodo) {
        EstrategiaDepreciacion e = estrategias.get(metodo);
        if (e == null) {
            throw new IllegalArgumentException("No hay estrategia registrada para el método " + metodo);
        }
        return e;
    }
}
