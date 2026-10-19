package com.inventory.activos.application;

import com.inventory.activos.application.port.ArticuloCommand;
import com.inventory.activos.application.port.ArticuloRepository;
import com.inventory.activos.application.port.ArticuloUseCase;
import com.inventory.activos.application.port.CalcularDepreciacionUseCase;
import com.inventory.activos.application.port.DepreciacionRepository;
import com.inventory.activos.application.port.MetricasNegocio;
import com.inventory.activos.domain.excepciones.ArticuloInactivoException;
import com.inventory.activos.domain.excepciones.ArticuloNoEncontradoException;
import com.inventory.activos.domain.Articulo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticuloService implements ArticuloUseCase {

    private final ArticuloRepository articulos;
    private final DepreciacionRepository depreciaciones;
    private final CalcularDepreciacionUseCase calcularDepreciacion;
    private final MetricasNegocio metricas;

    public ArticuloService(ArticuloRepository articulos,
                           DepreciacionRepository depreciaciones,
                           CalcularDepreciacionUseCase calcularDepreciacion,
                           MetricasNegocio metricas) {
        this.articulos = articulos;
        this.depreciaciones = depreciaciones;
        this.calcularDepreciacion = calcularDepreciacion;
        this.metricas = metricas;
    }

    @Override
    @Transactional
    public Articulo crear(ArticuloCommand command) {
        Articulo nuevo = Articulo.nuevo(command.nombre(), command.serial(), command.descripcion(),
                command.fechaCompra(), command.valorCompra());
        Articulo guardado = articulos.save(nuevo);
        calcularDepreciacion.calcularYRegistrar(guardado);
        metricas.articuloCreado();
        return guardado;
    }

    @Override
    @Transactional
    public Articulo actualizar(Integer idArticulo, ArticuloCommand command) {
        Articulo articulo = buscar(idArticulo);
        if (!articulo.isActivo()) {
            throw new ArticuloInactivoException(idArticulo);
        }
        BigDecimal valorAnterior = articulo.getValorCompra();
        LocalDate fechaAnterior = articulo.getFechaCompra();

        articulo.setNombre(command.nombre());
        articulo.setSerial(command.serial());
        articulo.setDescripcion(command.descripcion());
        articulo.setFechaCompra(command.fechaCompra());
        articulo.setValorCompra(command.valorCompra());

        Articulo guardado = articulos.save(articulo);
        boolean cambioCalculo = articulo.getValorCompra().compareTo(valorAnterior) != 0
                || !articulo.getFechaCompra().equals(fechaAnterior);
        if (cambioCalculo) {
            calcularDepreciacion.calcularYRegistrar(guardado);
        }
        return guardado;
    }

    @Override
    @Transactional
    public void darDeBaja(Integer idArticulo) {
        Articulo articulo = buscar(idArticulo);
        articulo.darDeBaja();
        articulos.save(articulo);
        metricas.articuloDadoDeBaja();
    }

    @Override
    @Transactional(readOnly = true)
    public Articulo obtenerPorId(Integer idArticulo) {
        return buscar(idArticulo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Articulo> listar() {
        return articulos.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Articulo obtenerConDepreciaciones(Integer idArticulo) {
        Articulo articulo = buscar(idArticulo);
        articulo.setDepreciaciones(depreciaciones.findByIdArticulo(idArticulo));
        return articulo;
    }

    private Articulo buscar(Integer idArticulo) {
        return articulos.findById(idArticulo).orElseThrow(() -> new ArticuloNoEncontradoException(idArticulo));
    }
}
