package com.inventory.activos.infrastructure.metrics;

import com.inventory.activos.application.port.MetricasNegocio;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Adaptador de {@link MetricasNegocio} sobre Micrometer. Si hay una transacción activa, registra
 * la métrica solo después del commit, para que un rollback no cuente operaciones que no se guardaron.
 */
@Component
public class MicrometerMetricasAdapter implements MetricasNegocio {

    public static final String ARTICULOS_CREADOS = "inventario.articulos.creados";
    public static final String ARTICULOS_BAJA = "inventario.articulos.baja";
    public static final String DEPRECIACION_CALCULO = "inventario.depreciacion.calculo";
    public static final String DEPRECIACION_REGISTROS = "inventario.depreciacion.registros";

    private final MeterRegistry registry;

    public MicrometerMetricasAdapter(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void articuloCreado() {
        trasCommit(() -> registry.counter(ARTICULOS_CREADOS).increment());
    }

    @Override
    public void articuloDadoDeBaja() {
        trasCommit(() -> registry.counter(ARTICULOS_BAJA).increment());
    }

    @Override
    public void depreciacionCalculada(int registros, Duration duracion) {
        trasCommit(() -> {
            registry.timer(DEPRECIACION_CALCULO).record(duracion);
            registry.counter(DEPRECIACION_REGISTROS).increment(registros);
        });
    }

    private static void trasCommit(Runnable accion) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    accion.run();
                }
            });
        } else {
            accion.run();
        }
    }
}
