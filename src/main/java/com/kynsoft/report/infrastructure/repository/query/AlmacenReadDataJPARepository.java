package com.kynsoft.report.infrastructure.repository.query;

import com.kynsoft.report.infrastructure.entity.Almacen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AlmacenReadDataJPARepository extends JpaRepository<Almacen, UUID>, JpaSpecificationExecutor<Almacen> {

    @Override
    @EntityGraph(attributePaths = {"finca"})
    Page<Almacen> findAll(Specification specification, Pageable pageable);

    Optional<Almacen> findByInventario(String inventario);

    Optional<Almacen> findByNombre(String nombre);

    @Query("SELECT COUNT(p) FROM Almacen a JOIN a.productos p WHERE a.id = :almacenId")
    int countProductosByAlmacenId(@Param("almacenId") UUID almacenId);

    @EntityGraph(attributePaths = {"finca", "productos", "productos.finca", "productos.producto"})
    @Query("SELECT a FROM Almacen a WHERE a.id = :id")
    Optional<Almacen> findByIdWithProductos(@Param("id") UUID id);
}
