package com.inventory.activos.infrastructure.persistence.repository;

import com.inventory.activos.infrastructure.persistence.ArticuloJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticuloJpaRepository extends JpaRepository<ArticuloJpaEntity, Integer> {
}
