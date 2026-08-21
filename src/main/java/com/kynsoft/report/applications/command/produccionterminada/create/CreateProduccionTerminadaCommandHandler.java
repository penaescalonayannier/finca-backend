package com.kynsoft.report.applications.command.produccionterminada.create;

import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import com.kynsoft.report.domain.dto.ProduccionTerminadaDto;
import com.kynsoft.report.domain.services.IFincaProductoService;
import com.kynsoft.report.domain.services.IProduccionTerminadaService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class CreateProduccionTerminadaCommandHandler implements ICommandHandler<CreateProduccionTerminadaCommand> {

    private final IProduccionTerminadaService service;
    private final IFincaProductoService fincaProductoService;

    public CreateProduccionTerminadaCommandHandler(
            IProduccionTerminadaService service,
            IFincaProductoService fincaProductoService) {
        this.service = service;
        this.fincaProductoService = fincaProductoService;
    }

    @Override
    public void handle(CreateProduccionTerminadaCommand command) {
        ProduccionTerminadaDto dto = ProduccionTerminadaDto.builder()
                .id(command.getId())
                .fincaId(command.getFincaId())
                .productoId(command.getProductoId())
                .cantidadTerminada(command.getCantidadTerminada())
                .trabajadorEntregaId(command.getTrabajadorEntregaId())
                .trabajadorRecibeId(command.getTrabajadorRecibeId())
                .observaciones(command.getObservaciones())
                .build();

        // Crear registro de producción terminada
        service.create(dto);

        // Actualizar stock en la relación FincaProducto
        fincaProductoService.entradaProduccion(
                command.getFincaId(),
                command.getProductoId(),
                command.getCantidadTerminada(),
                command.getObservaciones()
        );
    }
}
