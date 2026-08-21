package com.kynsoft.report.domain.services;

import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsoft.report.domain.dto.DeudaTrabajadorDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IDeudaTrabajadorService {

    UUID create(DeudaTrabajadorDto dto);

    void update(DeudaTrabajadorDto dto);

    void delete(UUID id);

    DeudaTrabajadorDto findById(UUID id);

    PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria);

    DeudaTrabajadorDto findByTrabajadorId(UUID trabajadorId);

    void incrementarDeuda(UUID trabajadorId, Double importe);
}
