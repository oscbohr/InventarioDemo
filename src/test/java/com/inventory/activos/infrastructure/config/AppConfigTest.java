package com.inventory.activos.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.inventory.activos.domain.ConfiguracionDepreciacion;
import com.inventory.activos.domain.MetodoDepreciacion;
import com.inventory.activos.domain.estrategia.LineaRecta;
import com.inventory.activos.domain.estrategia.SaldoDecreciente;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class AppConfigTest {

    private final AppConfig config = new AppConfig();

    @Test
    void givenConfiguredMetodoWithoutStrategy_whenCreatingFactory_thenFailsAtStartup() {
        var configuracion = new ConfiguracionDepreciacion(new BigDecimal("10"), MetodoDepreciacion.SALDO_DECRECIENTE);

        assertThatThrownBy(() -> config.estrategiaFactory(List.of(new LineaRecta()), configuracion))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SALDO_DECRECIENTE");
    }

    @Test
    void givenConfiguredMetodoWithStrategy_whenCreatingFactory_thenReturnsFactory() {
        var configuracion = new ConfiguracionDepreciacion(new BigDecimal("10"), MetodoDepreciacion.SALDO_DECRECIENTE);
        var saldo = new SaldoDecreciente();

        var fabrica = config.estrategiaFactory(List.of(new LineaRecta(), saldo), configuracion);

        assertThat(fabrica.obtener(MetodoDepreciacion.SALDO_DECRECIENTE)).isSameAs(saldo);
    }
}
