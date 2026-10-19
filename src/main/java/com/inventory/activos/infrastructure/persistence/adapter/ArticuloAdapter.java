package com.inventory.activos.infrastructure.persistence.adapter;

import com.inventory.activos.application.port.ArticuloRepository;
import com.inventory.activos.domain.Articulo;
import com.inventory.activos.infrastructure.persistence.PersistenceMapper;
import com.inventory.activos.infrastructure.persistence.repository.ArticuloJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ArticuloAdapter implements ArticuloRepository {

    private final ArticuloJpaRepository repository;

    ArticuloAdapter(ArticuloJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Articulo save(Articulo articulo) {
        return PersistenceMapper.toDomain(repository.save(PersistenceMapper.toEntity(articulo)));
    }

    @Override
    public Optional<Articulo> findById(Integer idArticulo) {
        return repository.findById(idArticulo).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<Articulo> findAll() {
        return repository.findAll().stream().map(PersistenceMapper::toDomain).toList();
    }
}
