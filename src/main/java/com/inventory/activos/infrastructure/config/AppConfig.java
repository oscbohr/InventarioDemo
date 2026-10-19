package com.inventory.activos.infrastructure.config;

import com.inventory.activos.domain.ConfiguracionDepreciacion;
import com.inventory.activos.domain.estrategia.EstrategiaDepreciacion;
import com.inventory.activos.domain.estrategia.EstrategiaFactory;
import com.inventory.activos.domain.estrategia.LineaRecta;
import com.inventory.activos.domain.estrategia.SaldoDecreciente;
import java.time.Clock;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DepreciacionProperties.class)
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    /** Configuración de depreciación leída de application.yml; falla al arrancar si el porcentaje no es válido. */
    @Bean
    public ConfiguracionDepreciacion configuracionDepreciacion(DepreciacionProperties propiedades) {
        return new ConfiguracionDepreciacion(propiedades.porcentaje(), propiedades.metodo());
    }

    /** Cada estrategia nueva se registra con un @Bean; la fábrica las recibe todas. */
    @Bean
    public LineaRecta lineaRecta() {
        return new LineaRecta();
    }

    @Bean
    public SaldoDecreciente saldoDecreciente() {
        return new SaldoDecreciente();
    }

    /** Falla al arrancar si no hay estrategia registrada para el método configurado en application.yml. */
    @Bean
    public EstrategiaFactory estrategiaFactory(List<EstrategiaDepreciacion> estrategias,
                                               ConfiguracionDepreciacion configuracion) {
        EstrategiaFactory fabrica = new EstrategiaFactory(estrategias);
        fabrica.obtener(configuracion.metodo());
        return fabrica;
    }
}
