package com.inventory.activos.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.inventory.activos.domain.Articulo;
import com.inventory.activos.domain.DepreciacionArticulo;
import com.inventory.activos.infrastructure.persistence.adapter.ArticuloAdapter;
import com.inventory.activos.infrastructure.persistence.adapter.DepreciacionAdapter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({ArticuloAdapter.class, DepreciacionAdapter.class})
class AdaptadoresPersistenciaTest {

    @Autowired
    ArticuloAdapter articulos;
    @Autowired
    DepreciacionAdapter depreciaciones;

    private Articulo nuevoArticulo() {
        return articulos.save(Articulo.nuevo("Laptop", "SN-1", "d", LocalDate.of(2024, 3, 1), new BigDecimal("1000")));
    }

    private DepreciacionArticulo dep(Integer idArticulo, int ano, String valor) {
        return new DepreciacionArticulo(null, idArticulo, ano, new BigDecimal(valor), new BigDecimal("10"),
                new BigDecimal("1000"));
    }

    @Test
    void givenNewArticulo_whenSaveAndFind_thenReturnsArticuloWithId() {
        Articulo guardado = nuevoArticulo();

        assertThat(guardado.getIdArticulo()).isNotNull();
        Articulo leido = articulos.findById(guardado.getIdArticulo()).orElseThrow();
        assertThat(leido.getNombre()).isEqualTo("Laptop");
        assertThat(leido.getValorCompra()).isEqualByComparingTo("1000.00");
        assertThat(leido.isActivo()).isTrue();
    }

    @Test
    void givenArticuloDadoDeBaja_whenSaveAndFind_thenArticuloIsKeptAsInactive() {
        Articulo a = nuevoArticulo();
        a.darDeBaja();
        articulos.save(a);

        assertThat(articulos.findById(a.getIdArticulo()).orElseThrow().isActivo()).isFalse();
        assertThat(articulos.findAll()).hasSize(1);
    }

    @Test
    void givenDepreciacionesSaved_whenFindByArticulo_thenReturnsOrderedByAno() {
        Articulo a = nuevoArticulo();
        depreciaciones.saveAll(List.of(dep(a.getIdArticulo(), 2025, "100"), dep(a.getIdArticulo(), 2024, "100")));

        assertThat(depreciaciones.findByIdArticulo(a.getIdArticulo()))
                .extracting(DepreciacionArticulo::ano).containsExactly(2024, 2025);
    }

    @Test
    void givenExistingYear_whenDeleteAndReinsertSameYear_thenSucceedsInSameTransaction() {
        Articulo a = nuevoArticulo();
        depreciaciones.saveAll(List.of(dep(a.getIdArticulo(), 2024, "100")));

        depreciaciones.deleteByIdArticulo(a.getIdArticulo());
        depreciaciones.saveAll(List.of(dep(a.getIdArticulo(), 2024, "150")));

        List<DepreciacionArticulo> r = depreciaciones.findByIdArticulo(a.getIdArticulo());
        assertThat(r).hasSize(1);
        assertThat(r.get(0).valor()).isEqualByComparingTo("150.00");
    }

    @Test
    void givenTwoArticulosWithDepreciaciones_whenDeleteOne_thenOtherIsUnaffected() {
        Articulo a = nuevoArticulo();
        Articulo b = nuevoArticulo();
        depreciaciones.saveAll(List.of(dep(a.getIdArticulo(), 2024, "100"), dep(b.getIdArticulo(), 2024, "100")));

        depreciaciones.deleteByIdArticulo(a.getIdArticulo());

        assertThat(depreciaciones.findByIdArticulo(a.getIdArticulo())).isEmpty();
        assertThat(depreciaciones.findByIdArticulo(b.getIdArticulo())).hasSize(1);
    }
}
