package com.inventory.activos.domain;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.inventory.activos.domain.excepciones.PorcentajeInvalidoException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DepreciacionArticuloTest {

    @Test
    void givenValorAndPorcentaje_whenCreatingDepreciacion_thenNormalizesToTwoDecimals() {
        var d = new DepreciacionArticulo(null, 1, 2024, new BigDecimal("100.005"), new BigDecimal("10"),
                new BigDecimal("1000.005"));
        assertThat(d.valor()).isEqualTo(new BigDecimal("100.01"));
        assertThat(d.porcentaje()).isEqualTo(new BigDecimal("10.00"));
        assertThat(d.vrActualArticulo()).isEqualTo(new BigDecimal("1000.01"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "100.01", "150"})
    void givenPorcentajeOutOfRange_whenCreatingConfiguracion_thenThrowsPorcentajeInvalido(String porcentaje) {
        assertThatThrownBy(() -> new ConfiguracionDepreciacion(new BigDecimal(porcentaje), MetodoDepreciacion.LINEA_RECTA))
                .isInstanceOf(PorcentajeInvalidoException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "100"})
    void givenPorcentajeAtLimits_whenCreatingConfiguracion_thenAccepts(String porcentaje) {
        var c = new ConfiguracionDepreciacion(new BigDecimal(porcentaje), MetodoDepreciacion.LINEA_RECTA);
        assertThat(c.porcentaje()).isEqualByComparingTo(porcentaje);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "101"})
    void givenPorcentajeOutOfRange_whenCreatingDepreciacion_thenThrowsPorcentajeInvalido(String porcentaje) {
        assertThatThrownBy(() -> new DepreciacionArticulo(null, 1, 2024, BigDecimal.TEN, new BigDecimal(porcentaje),
                BigDecimal.TEN))
                .isInstanceOf(PorcentajeInvalidoException.class);
    }

    @Test
    void givenPorcentajeWithOneDecimal_whenCreatingConfiguracion_thenNormalizesToTwoDecimals() {
        var c = new ConfiguracionDepreciacion(new BigDecimal("12.5"), MetodoDepreciacion.LINEA_RECTA);
        assertThat(c.porcentaje()).isEqualTo(new BigDecimal("12.50"));
    }
}
