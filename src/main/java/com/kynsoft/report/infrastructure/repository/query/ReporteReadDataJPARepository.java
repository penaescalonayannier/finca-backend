package com.kynsoft.report.infrastructure.repository.query;

import com.kynsoft.report.infrastructure.entity.Reporte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReporteReadDataJPARepository extends JpaRepository<Reporte, UUID>, JpaSpecificationExecutor<Reporte> {

    @Override
    Page<Reporte> findAll(Specification specification, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Reporte r WHERE r.year = :year AND r.mes = :mes AND r.activo = true")
    long countByYearAndMes(@Param("year") String year, @Param("mes") String mes);
}