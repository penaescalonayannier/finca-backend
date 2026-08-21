package com.kynsoft.report.domain.services;

import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsoft.report.domain.dto.FincaProductoDto;
import com.kynsoft.report.domain.dto.TipoMovimientoStock;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface IFincaProductoService {
    
    void asignarProductoAFinca(UUID fincaId, UUID productoId, Integer stock);
    
    void actualizarStock(UUID fincaId, UUID productoId, Integer stock);
    
    void removerProductoDeFinca(UUID fincaId, UUID productoId);
    
    void removerTodosProductosDeFinca(UUID fincaId);
    
    List<FincaProductoDto> obtenerProductosDeFinca(UUID fincaId);
    
    List<FincaProductoDto> obtenerFincasDeProducto(UUID productoId);
    
    FincaProductoDto obtenerRelacion(UUID fincaId, UUID productoId);
    
    Integer obtenerStock(UUID fincaId, UUID productoId);

    // Entrada de producción: suma cantidad al stock existente
    void entradaProduccion(UUID fincaId, UUID productoId, Integer cantidad, String descripcion);

    // Entrada de producción con referencia para auditoría
    void entradaProduccion(UUID fincaId, UUID productoId, Integer cantidad, String descripcion, UUID referenciaId);

    // Salida/Reverso de producción: resta cantidad del stock existente
    void decrementarStock(UUID fincaId, UUID productoId, Integer cantidad);

    // Decremento con tipo y referencia para auditoría
    void decrementarStock(UUID fincaId, UUID productoId, Integer cantidad,
                          TipoMovimientoStock tipo, UUID referenciaId, String referenciaTabla);

    // NUEVO MÉTODO: Búsqueda paginada con filtros
    PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria);
}