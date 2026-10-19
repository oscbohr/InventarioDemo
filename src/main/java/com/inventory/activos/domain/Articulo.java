package com.inventory.activos.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class Articulo {

    private Integer idArticulo;
    private String nombre;
    private String serial;
    private String descripcion;
    private LocalDate fechaCompra;
    private BigDecimal valorCompra;
    private boolean activo;
    private List<DepreciacionArticulo> depreciaciones = List.of();

    public Articulo(Integer idArticulo, String nombre, String serial, String descripcion,
                    LocalDate fechaCompra, BigDecimal valorCompra, boolean activo) {
        this.idArticulo = idArticulo;
        this.nombre = nombre;
        this.serial = serial;
        this.descripcion = descripcion;
        this.fechaCompra = fechaCompra;
        this.valorCompra = escalar(valorCompra);
        this.activo = activo;
    }

    public static Articulo nuevo(String nombre, String serial, String descripcion,
                                 LocalDate fechaCompra, BigDecimal valorCompra) {
        return new Articulo(null, nombre, serial, descripcion, fechaCompra, valorCompra, true);
    }

    public void darDeBaja() {
        this.activo = false;
    }

    private static BigDecimal escalar(BigDecimal valor) {
        return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP);
    }

    public Integer getIdArticulo() { return idArticulo; }
    public void setIdArticulo(Integer idArticulo) { this.idArticulo = idArticulo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(LocalDate fechaCompra) { this.fechaCompra = fechaCompra; }
    public BigDecimal getValorCompra() { return valorCompra; }
    public void setValorCompra(BigDecimal valorCompra) { this.valorCompra = escalar(valorCompra); }
    public boolean isActivo() { return activo; }
    public List<DepreciacionArticulo> getDepreciaciones() { return depreciaciones; }
    public void setDepreciaciones(List<DepreciacionArticulo> depreciaciones) { this.depreciaciones = List.copyOf(depreciaciones); }
}
