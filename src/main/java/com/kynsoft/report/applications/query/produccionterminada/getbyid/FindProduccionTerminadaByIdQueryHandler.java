package com.kynsoft.report.applications.query.produccionterminada.getbyid;

import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import com.kynsoft.report.applications.query.responseObject.ProduccionTerminadaResponse;
import com.kynsoft.report.domain.dto.ProduccionTerminadaDto;
import com.kynsoft.report.domain.services.IProduccionTerminadaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FindProduccionTerminadaByIdQueryHandler 
        implements IQueryHandler<FindProduccionTerminadaByIdQuery, ProduccionTerminadaResponse> {

    private final IProduccionTerminadaService service;

    @Override
    public ProduccionTerminadaResponse handle(FindProduccionTerminadaByIdQuery query) {
        ProduccionTerminadaDto dto = service.findById(query.getId());
        
        return new ProduccionTerminadaResponse(
                dto.getId(),
                dto.getFincaId(),
                dto.getFincaCode(),
                dto.getFincaName(),
                dto.getProductoId(),
                dto.getProductoCode(),
                dto.getProductoName(),
                dto.getFecha(),
                dto.getCantidadTerminada(),
                dto.getTrabajadorEntregaId(),
                dto.getTrabajadorEntregaNombre(),
                dto.getTrabajadorRecibeId(),
                dto.getTrabajadorRecibeNombre(),
                dto.getObservaciones()
        );
    }
}
