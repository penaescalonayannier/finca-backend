package com.kynsoft.report.domain.services;

import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsoft.report.domain.dto.MovimientoStockDto;
import com.kynsoft.report.domain.dto.TipoMovimientoStock;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface IMovimientoStockService {

    void registrar(MovimientoStockDto dto);

    void registrarMovimiento(UUID fincaProductoId, UUID fincaId, UUID productoId,
                             TipoMovimientoStock tipo, Integer cantidad,
                             Integer stockAnterior, Integer stockNuevo,
                             UUID referenciaId, String referenciaTabla, String descripcion);

    List<MovimientoStockDto> findByFincaProductoId(UUID fincaProductoId);

    List<MovimientoStockDto> findByFincaId(UUID fincaId);

    List<MovimientoStockDto> findByProductoId(UUID productoId);

    List<MovimientoStockDto> findByReferencia(UUID referenciaId, String referenciaTabla);

    List<MovimientoStockDto> findByFincaIdAndFechaBetween(UUID fincaId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria);
}
