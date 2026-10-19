package com.inventory.activos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inventory.activos.application.port.ArticuloCommand;
import com.inventory.activos.application.port.CalcularDepreciacionUseCase;
import com.inventory.activos.application.port.ArticuloRepository;
import com.inventory.activos.application.port.DepreciacionRepository;
import com.inventory.activos.application.port.MetricasNegocio;
import com.inventory.activos.domain.excepciones.ArticuloInactivoException;
import com.inventory.activos.domain.excepciones.ArticuloNoEncontradoException;
import com.inventory.activos.domain.Articulo;
import com.inventory.activos.domain.DepreciacionArticulo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticuloServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2024, 3, 1);

    @Mock
    ArticuloRepository articulos;
    @Mock
    DepreciacionRepository depreciaciones;
    @Mock
    CalcularDepreciacionUseCase calcular;
    @Mock
    MetricasNegocio metricas;

    ArticuloService service;

    @BeforeEach
    void setUp() {
        service = new ArticuloService(articulos, depreciaciones, calcular, metricas);
    }

    private Articulo existente(boolean activo) {
        return new Articulo(5, "Laptop", "SN", "d", FECHA, new BigDecimal("1000.00"), activo);
    }

    private ArticuloCommand getArticuloStatic(String valor, LocalDate fecha, String nombre) {
        return new ArticuloCommand(nombre, "SN", "d", fecha, new BigDecimal(valor));
    }

    @Test
    void givenValidCommand_whenCrear_thenSavesAndCalculatesDepreciacion() {
        Articulo guardado = new Articulo(9, "Laptop", "SN", "d", FECHA, new BigDecimal("1000.00"), true);
        when(articulos.save(any(Articulo.class))).thenReturn(guardado);

        Articulo r = service.crear(new ArticuloCommand("Laptop", "SN", "d", FECHA, new BigDecimal("1000")));

        assertThat(r.getIdArticulo()).isEqualTo(9);
        assertThat(r.isActivo()).isTrue();
        verify(calcular).calcularYRegistrar(guardado);
        verify(metricas).articuloCreado();
    }

    @Test
    void givenDifferentValor_whenActualizar_thenRecalculates() {
        Articulo a = existente(true);
        when(articulos.findById(5)).thenReturn(Optional.of(a));
        when(articulos.save(a)).thenReturn(a);

        service.actualizar(5, getArticuloStatic("1500.00", FECHA, "Laptop"));

        verify(calcular).calcularYRegistrar(a);
    }

    @Test
    void givenDifferentFechaCompra_whenActualizar_thenRecalculates() {
        Articulo a = existente(true);
        when(articulos.findById(5)).thenReturn(Optional.of(a));
        when(articulos.save(a)).thenReturn(a);

        service.actualizar(5, getArticuloStatic("1000.00", FECHA.plusYears(1), "Laptop"));

        verify(calcular).calcularYRegistrar(a);
    }

    @Test
    void givenOnlyNombreChanged_whenActualizar_thenDoesNotRecalculate() {
        Articulo a = existente(true);
        when(articulos.findById(5)).thenReturn(Optional.of(a));
        when(articulos.save(a)).thenReturn(a);

        Articulo r = service.actualizar(5, getArticuloStatic("1000", FECHA, "Portátil"));

        assertThat(r.getNombre()).isEqualTo("Portátil");
        verify(calcular, never()).calcularYRegistrar(any());
    }

    @Test
    void givenInactiveArticulo_whenActualizar_thenThrowsWithoutSaving() {
        when(articulos.findById(5)).thenReturn(Optional.of(existente(false)));

        assertThatThrownBy(() -> service.actualizar(5, getArticuloStatic("1000", FECHA, "X")))
                .isInstanceOf(ArticuloInactivoException.class);
        verify(articulos, never()).save(any());
        verify(calcular, never()).calcularYRegistrar(any());
    }

    @Test
    void givenNonExistentArticulo_whenActualizar_thenThrowsNotFound() {
        when(articulos.findById(5)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(5, getArticuloStatic("1000", FECHA, "X")))
                .isInstanceOf(ArticuloNoEncontradoException.class);
    }

    @Test
    void givenActiveArticulo_whenDarDeBaja_thenMarksInactiveAndKeepsDepreciaciones() {
        Articulo a = existente(true);
        when(articulos.findById(5)).thenReturn(Optional.of(a));

        service.darDeBaja(5);

        assertThat(a.isActivo()).isFalse();
        verify(articulos).save(a);
        verify(depreciaciones, never()).deleteByIdArticulo(any());
        verify(metricas).articuloDadoDeBaja();
    }

    @Test
    void givenNonExistentArticulo_whenObtenerConDepreciaciones_thenThrowsNotFound() {
        when(articulos.findById(5)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerConDepreciaciones(5))
                .isInstanceOf(ArticuloNoEncontradoException.class);
    }

    @Test
    void givenExistingArticulo_whenObtenerConDepreciaciones_thenReturnsArticuloWithDepreciaciones() {
        Articulo a = existente(true);
        List<DepreciacionArticulo> registros = List.of(
                new DepreciacionArticulo(1, 5, 2024, new BigDecimal("100"), new BigDecimal("10"), new BigDecimal("1000")),
                new DepreciacionArticulo(2, 5, 2025, new BigDecimal("100"), new BigDecimal("10"), new BigDecimal("900")));
        when(articulos.findById(5)).thenReturn(Optional.of(a));
        when(depreciaciones.findByIdArticulo(5)).thenReturn(registros);

        Articulo resultado = service.obtenerConDepreciaciones(5);

        assertThat(resultado.getIdArticulo()).isEqualTo(5);
        assertThat(resultado.getDepreciaciones()).containsExactlyElementsOf(registros);
    }
}
