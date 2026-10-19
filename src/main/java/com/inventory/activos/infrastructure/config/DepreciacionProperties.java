package com.inventory.activos.infrastructure.config;

import com.inventory.activos.domain.MetodoDepreciacion;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("depreciacion")
public record DepreciacionProperties(BigDecimal porcentaje, MetodoDepreciacion metodo) {
}
