package com.inventory.activos.infrastructure.persistence.adapter;

import com.inventory.activos.application.port.DepreciacionRepository;
import com.inventory.activos.domain.DepreciacionArticulo;
import com.inventory.activos.infrastructure.persistence.PersistenceMapper;
import com.inventory.activos.infrastructure.persistence.repository.DepreciacionJpaRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DepreciacionAdapter implements DepreciacionRepository {

    private final DepreciacionJpaRepository repository;

    DepreciacionAdapter(DepreciacionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DepreciacionArticulo> saveAll(List<DepreciacionArticulo> depreciaciones) {
        return repository.saveAll(depreciaciones.stream().map(PersistenceMapper::toEntity).toList())
                .stream().map(PersistenceMapper::toDomain).toList();
    }

    @Override
    public List<DepreciacionArticulo> findByIdArticulo(Integer idArticulo) {
        return repository.findByIdArticuloOrderByAno(idArticulo).stream().map(PersistenceMapper::toDomain).toList();
    }

    @Override
    public void deleteByIdArticulo(Integer idArticulo) {
        repository.deleteByIdArticulo(idArticulo);
    }
}
