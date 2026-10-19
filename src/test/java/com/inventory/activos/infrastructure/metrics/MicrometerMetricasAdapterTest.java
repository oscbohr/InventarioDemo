package com.inventory.activos.infrastructure.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class MicrometerMetricasAdapterTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final MicrometerMetricasAdapter adapter = new MicrometerMetricasAdapter(registry);

    @Test
    void givenNoTransaction_whenArticuloCreado_thenIncrementsCounter() {
        adapter.articuloCreado();
        adapter.articuloCreado();

        assertThat(registry.counter(MicrometerMetricasAdapter.ARTICULOS_CREADOS).count()).isEqualTo(2.0);
    }

    @Test
    void givenNoTransaction_whenArticuloDadoDeBaja_thenIncrementsCounter() {
        adapter.articuloDadoDeBaja();

        assertThat(registry.counter(MicrometerMetricasAdapter.ARTICULOS_BAJA).count()).isEqualTo(1.0);
    }

    @Test
    void givenNoTransaction_whenDepreciacionCalculada_thenRecordsTimerAndRegistros() {
        adapter.depreciacionCalculada(3, Duration.ofMillis(50));

        var timer = registry.timer(MicrometerMetricasAdapter.DEPRECIACION_CALCULO);
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isEqualTo(50.0);
        assertThat(registry.counter(MicrometerMetricasAdapter.DEPRECIACION_REGISTROS).count()).isEqualTo(3.0);
    }

    @Test
    void givenActiveTransaction_whenArticuloCreado_thenCountsOnlyAfterCommit() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            adapter.articuloCreado();
            assertThat(registry.counter(MicrometerMetricasAdapter.ARTICULOS_CREADOS).count()).isZero();

            TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

            assertThat(registry.counter(MicrometerMetricasAdapter.ARTICULOS_CREADOS).count()).isEqualTo(1.0);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void givenActiveTransaction_whenRollback_thenDoesNotCount() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            adapter.articuloCreado();

            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

            assertThat(registry.counter(MicrometerMetricasAdapter.ARTICULOS_CREADOS).count()).isZero();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
