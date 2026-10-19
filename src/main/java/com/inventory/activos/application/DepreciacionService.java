package com.inventory.activos.application;

import com.inventory.activos.application.port.CalcularDepreciacionUseCase;
import com.inventory.activos.application.port.DepreciacionRepository;
import com.inventory.activos.application.port.MetricasNegocio;
import com.inventory.activos.domain.Articulo;
import com.inventory.activos.domain.ConfiguracionDepreciacion;
import com.inventory.activos.domain.DepreciacionArticulo;
import com.inventory.activos.domain.estrategia.EstrategiaDepreciacion;
import com.inventory.activos.domain.estrategia.EstrategiaFactory;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepreciacionService implements CalcularDepreciacionUseCase {

    private final DepreciacionRepository depreciaciones;
    private final ConfiguracionDepreciacion configuracion;
    private final EstrategiaFactory estrategias;
    private final Clock clock;
    private final MetricasNegocio metricas;

    public DepreciacionService(DepreciacionRepository depreciaciones,
                               ConfiguracionDepreciacion configuracion,
                               EstrategiaFactory estrategias,
                               Clock clock,
                               MetricasNegocio metricas) {
        this.depreciaciones = depreciaciones;
        this.configuracion = configuracion;
        this.estrategias = estrategias;
        this.clock = clock;
        this.metricas = metricas;
    }

    @Override
    @Transactional
    public List<DepreciacionArticulo> calcularYRegistrar(Articulo articulo) {
        long inicio = System.nanoTime();
        EstrategiaDepreciacion estrategia = estrategias.obtener(configuracion.metodo());

        depreciaciones.deleteByIdArticulo(articulo.getIdArticulo());

        List<DepreciacionArticulo> registros = new ArrayList<>();
        BigDecimal valorEnLibros = articulo.getValorCompra();
        int anoActual = LocalDate.now(clock).getYear();
        for (int ano = articulo.getFechaCompra().getYear(); ano <= anoActual; ano++) {
            BigDecimal valor = estrategia.calcular(articulo.getValorCompra(), valorEnLibros, configuracion.porcentaje());
            if (valor.signum() <= 0) {
                break;
            }
            registros.add(new DepreciacionArticulo(null, articulo.getIdArticulo(), ano, valor, configuracion.porcentaje(),
                    valorEnLibros));
            valorEnLibros = valorEnLibros.subtract(valor);
        }
        List<DepreciacionArticulo> guardados = depreciaciones.saveAll(registros);
        metricas.depreciacionCalculada(guardados.size(), Duration.ofNanos(System.nanoTime() - inicio));
        return guardados;
    }
}
