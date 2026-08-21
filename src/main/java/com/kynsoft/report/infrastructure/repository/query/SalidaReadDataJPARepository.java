package com.kynsoft.report.infrastructure.repository.query;

import com.kynsoft.report.domain.dto.TipoSalida;
import com.kynsoft.report.infrastructure.entity.Salida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalidaReadDataJPARepository extends JpaRepository<Salida, UUID>, JpaSpecificationExecutor<Salida> {

    @Override
    @EntityGraph(attributePaths = {"fincaProducto", "fincaProducto.finca", "fincaProducto.producto", "items", "items.trabajador"})
    Page<Salida> findAll(Specification specification, Pageable pageable);

    @Query("SELECT s FROM Salida s " +
           "LEFT JOIN FETCH s.fincaProducto fp " +
           "LEFT JOIN FETCH fp.finca " +
           "LEFT JOIN FETCH fp.producto " +
           "LEFT JOIN FETCH s.items i " +
           "LEFT JOIN FETCH i.trabajador " +
           "WHERE s.id = :id")
    Optional<Salida> findByIdWithDetails(@Param("id") UUID id);

    List<Salida> findByTipo(TipoSalida tipo);

    List<Salida> findByFincaProductoId(UUID fincaProductoId);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(s.numero, LENGTH(:prefix) + 1) AS int)), 0) FROM Salida s WHERE s.numero LIKE :prefix%")
    Integer findMaxNumeroByPrefix(@Param("prefix") String prefix);
}
