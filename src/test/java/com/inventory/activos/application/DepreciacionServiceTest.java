package com.inventory.activos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inventory.activos.application.port.DepreciacionRepository;
import com.inventory.activos.application.port.MetricasNegocio;
import com.inventory.activos.domain.Articulo;
import com.inventory.activos.domain.ConfiguracionDepreciacion;
import com.inventory.activos.domain.DepreciacionArticulo;
import com.inventory.activos.domain.MetodoDepreciacion;
import com.inventory.activos.domain.estrategia.EstrategiaFactory;
import com.inventory.activos.domain.estrategia.LineaRecta;
import com.inventory.activos.domain.estrategia.SaldoDecreciente;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DepreciacionServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    DepreciacionRepository depreciaciones;
    @Mock
    MetricasNegocio metricas;
    DepreciacionService service;

    private Articulo getArticuloStatic(int anoCompra, String valor) {
        return new Articulo(1, "Laptop", "SN", "d", LocalDate.of(anoCompra, 12, 31), new BigDecimal(valor), true);
    }

    private void configurar(String porcentaje, MetodoDepreciacion metodo) {
        service = new DepreciacionService(depreciaciones,
                new ConfiguracionDepreciacion(new BigDecimal(porcentaje), metodo),
                new EstrategiaFactory(List.of(new LineaRecta(), new SaldoDecreciente())), CLOCK, metricas);
    }

    @SuppressWarnings("unchecked")
    private List<DepreciacionArticulo> registrosGuardados() {
        when(depreciaciones.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        return service.calcularYRegistrar(articuloEnPrueba);
    }

    private Articulo articuloEnPrueba;

    @Test
    void givenArticuloBoughtInPastYear_whenCalcular_thenOneRecordPerYearUntilCurrentYear() {
        articuloEnPrueba = getArticuloStatic(2024, "1000.00");
        configurar("10", MetodoDepreciacion.LINEA_RECTA);

        List<DepreciacionArticulo> r = registrosGuardados();

        assertThat(r).extracting(DepreciacionArticulo::ano).containsExactly(2024, 2025, 2026);
        assertThat(r).extracting(DepreciacionArticulo::valor).allSatisfy(v -> assertThat(v).isEqualByComparingTo("100.00"));
        assertThat(r).extracting(DepreciacionArticulo::porcentaje).allSatisfy(p -> assertThat(p).isEqualByComparingTo("10.00"));
        assertThat(r).extracting(DepreciacionArticulo::vrActualArticulo)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(new BigDecimal("1000.00"), new BigDecimal("900.00"), new BigDecimal("800.00"));
        verify(depreciaciones).deleteByIdArticulo(1);
        verify(metricas).depreciacionCalculada(eq(3), any(Duration.class));
    }

    @Test
    void givenValueExhaustedBeforeCurrentYear_whenCalcular_thenStopsGeneratingRecords() {
        articuloEnPrueba = getArticuloStatic(2020, "250.00");
        configurar("40", MetodoDepreciacion.LINEA_RECTA);

        List<DepreciacionArticulo> r = registrosGuardados();

        assertThat(r).extracting(DepreciacionArticulo::ano).containsExactly(2020, 2021, 2022);
        assertThat(r).extracting(DepreciacionArticulo::valor)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(new BigDecimal("100.00"), new BigDecimal("100.00"), new BigDecimal("50.00"));
    }

    @Test
    void givenSaldoDecreciente_whenCalcular_thenUsesValorEnLibrosOfEachYear() {
        articuloEnPrueba = getArticuloStatic(2025, "1000.00");
        configurar("10", MetodoDepreciacion.SALDO_DECRECIENTE);

        List<DepreciacionArticulo> r = registrosGuardados();

        assertThat(r).extracting(DepreciacionArticulo::valor)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(new BigDecimal("100.00"), new BigDecimal("90.00"));
    }

    @Test
    void givenArticuloBoughtInFutureYear_whenCalcular_thenGeneratesNoRecords() {
        articuloEnPrueba = getArticuloStatic(2030, "1000.00");
        configurar("10", MetodoDepreciacion.LINEA_RECTA);

        assertThat(registrosGuardados()).isEmpty();
    }
}
