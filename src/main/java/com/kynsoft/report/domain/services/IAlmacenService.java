package com.kynsoft.report.domain.services;

import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsoft.report.domain.dto.AlmacenDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IAlmacenService {

    void create(AlmacenDto object);

    void update(AlmacenDto object);

    void delete(UUID id);

    AlmacenDto findById(UUID id);

    AlmacenDto findByInventario(String inventario);

    PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria);

    void addProducto(UUID almacenId, UUID fincaProductoId);

    void removeProducto(UUID almacenId, UUID fincaProductoId);
}
