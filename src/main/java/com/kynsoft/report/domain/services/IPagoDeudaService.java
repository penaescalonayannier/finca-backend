package com.kynsoft.report.domain.services;

import com.kynsoft.report.domain.dto.PagoDeudaDto;

import java.util.List;
import java.util.UUID;

public interface IPagoDeudaService {

    UUID registrarPago(PagoDeudaDto dto);

    List<PagoDeudaDto> findByTrabajadorId(UUID trabajadorId);
}
