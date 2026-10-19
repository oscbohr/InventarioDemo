package com.inventory.activos.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.inventory.activos.domain.excepciones.ArticuloInactivoException;
import com.inventory.activos.domain.excepciones.ArticuloNoEncontradoException;
import com.inventory.activos.domain.excepciones.ExcepcionNegocio;
import org.junit.jupiter.api.Test;

class ExcepcionesNegocioTest {

    @Test
    void givenId_whenCreatingArticuloNoEncontrado_thenMessageContainsId() {
        assertThat(new ArticuloNoEncontradoException(7))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("7");
    }

    @Test
    void givenId_whenCreatingArticuloInactivo_thenMessageContainsId() {
        assertThat(new ArticuloInactivoException(3))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("3");
    }
}
