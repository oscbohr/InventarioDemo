package com.inventory.activos.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(name = "depreciaciones_articulo",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_articulo", "ano"}))
public class DepreciacionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_articulo", nullable = false)
    private Integer idArticulo;

    @Column(nullable = false)
    private int ano;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "vr_actual_articulo", nullable = false, precision = 19, scale = 2)
    private BigDecimal vrActualArticulo;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getIdArticulo() { return idArticulo; }
    public void setIdArticulo(Integer idArticulo) { this.idArticulo = idArticulo; }
    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }
    public BigDecimal getVrActualArticulo() { return vrActualArticulo; }
    public void setVrActualArticulo(BigDecimal vrActualArticulo) { this.vrActualArticulo = vrActualArticulo; }
}
