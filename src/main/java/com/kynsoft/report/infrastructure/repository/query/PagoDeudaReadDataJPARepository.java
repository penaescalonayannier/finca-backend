package com.kynsoft.report.infrastructure.repository.query;

import com.kynsoft.report.infrastructure.entity.PagoDeuda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface PagoDeudaReadDataJPARepository extends JpaRepository<PagoDeuda, UUID>, JpaSpecificationExecutor<PagoDeuda> {
    @Override
    Page<PagoDeuda> findAll(Specification specification, Pageable pageable);

    List<PagoDeuda> findByTrabajadorIdOrderByFechaDesc(UUID trabajadorId);
}
