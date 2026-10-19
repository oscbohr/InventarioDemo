package com.inventory.activos.application.port;

import java.time.Duration;

/** Puerto de salida para registrar métricas de negocio sin depender de una librería concreta.  */
public interface MetricasNegocio {

    void articuloCreado();

    void articuloDadoDeBaja();

    /** @param registros cantidad de registros anuales generados
     *  @param duracion  tiempo que tardó el cálculo */
    void depreciacionCalculada(int registros, Duration duracion);
}
