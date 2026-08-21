package com.kynsoft.report.domain.services;

import com.kynsoft.report.domain.dto.ProduccionTerminadaDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface IProduccionTerminadaService {
    
    UUID create(ProduccionTerminadaDto dto);
    
    void update(ProduccionTerminadaDto dto);
    
    void delete(UUID id);
    
    ProduccionTerminadaDto findById(UUID id);
    
    List<ProduccionTerminadaDto> findByProductoId(UUID productoId);
    
    List<ProduccionTerminadaDto> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    List<ProduccionTerminadaDto> findByTrabajadorEntregaId(UUID trabajadorId);
    
    List<ProduccionTerminadaDto> findByTrabajadorRecibeId(UUID trabajadorId);
}
