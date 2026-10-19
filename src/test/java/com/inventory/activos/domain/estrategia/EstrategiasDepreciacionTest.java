package com.inventory.activos.domain.estrategia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.inventory.activos.domain.MetodoDepreciacion;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class EstrategiasDepreciacionTest {

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    @Test
    void givenLineaRecta_whenCalcular_thenDepreciatesSameAmountOverValorCompra() {
        var e = new LineaRecta();
        assertThat(e.calcular(bd("1000.00"), bd("1000.00"), bd("10"))).isEqualByComparingTo("100.00");
        assertThat(e.calcular(bd("1000.00"), bd("900.00"), bd("10"))).isEqualByComparingTo("100.00");
    }

    @Test
    void givenAmountAboveValorEnLibros_whenCalcularLineaRecta_thenDoesNotExceedValorEnLibros() {
        assertThat(new LineaRecta().calcular(bd("1000.00"), bd("40.00"), bd("10"))).isEqualByComparingTo("40.00");
    }

    @Test
    void givenAmountWithManyDecimals_whenCalcularLineaRecta_thenRoundsHalfUp() {
        assertThat(new LineaRecta().calcular(bd("100.05"), bd("100.05"), bd("10"))).isEqualByComparingTo("10.01");
    }

    @Test
    void givenSaldoDecreciente_whenCalcular_thenAppliesPorcentajeOverValorEnLibros() {
        var e = new SaldoDecreciente();
        assertThat(e.calcular(bd("1000.00"), bd("1000.00"), bd("10"))).isEqualByComparingTo("100.00");
        assertThat(e.calcular(bd("1000.00"), bd("900.00"), bd("10"))).isEqualByComparingTo("90.00");
    }

    @Test
    void givenSaldoDecreciente_whenValorEnLibrosIsZero_thenReturnsZero() {
        assertThat(new SaldoDecreciente().calcular(bd("1000.00"), bd("0.00"), bd("10"))).isEqualByComparingTo("0.00");
    }

    @Test
    void givenRegisteredStrategy_whenObtenerByMetodo_thenReturnsThatStrategy() {
        var lr = new LineaRecta();
        var sd = new SaldoDecreciente();
        var fabrica = new EstrategiaFactory(List.of(lr, sd));
        assertThat(fabrica.obtener(MetodoDepreciacion.LINEA_RECTA)).isSameAs(lr);
        assertThat(fabrica.obtener(MetodoDepreciacion.SALDO_DECRECIENTE)).isSameAs(sd);
    }

    @Test
    void givenNoStrategyRegistered_whenObtener_thenThrowsIllegalArgument() {
        var fabrica = new EstrategiaFactory(List.of(new LineaRecta()));
        assertThatThrownBy(() -> fabrica.obtener(MetodoDepreciacion.SALDO_DECRECIENTE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenDuplicateStrategies_whenCreatingFactory_thenThrowsIllegalArgument() {
        assertThatThrownBy(() -> new EstrategiaFactory(List.of(new LineaRecta(), new LineaRecta())))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
