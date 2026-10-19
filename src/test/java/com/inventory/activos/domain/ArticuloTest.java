package com.inventory.activos.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ArticuloTest {

    private Articulo articulo() {
        return Articulo.nuevo("Laptop", "SN-1", "Portátil",
                LocalDate.of(2024, 3, 1), new BigDecimal("1000.456"));
    }

    @Test
    void givenNewArticulo_whenCreated_thenIsActiveAndHasNoId() {
        Articulo a = articulo();
        assertThat(a.isActivo()).isTrue();
        assertThat(a.getIdArticulo()).isNull();
    }

    @Test
    void givenValorCompraWithManyDecimals_whenCreated_thenRoundsToTwoDecimalsHalfUp() {
        assertThat(articulo().getValorCompra()).isEqualByComparingTo("1000.46");
        assertThat(articulo().getValorCompra().scale()).isEqualTo(2);
    }

    @Test
    void givenActiveArticulo_whenDarDeBaja_thenIsInactive() {
        Articulo a = articulo();
        a.darDeBaja();
        assertThat(a.isActivo()).isFalse();
    }
}
