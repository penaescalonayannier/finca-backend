package com.kynsoft.report.applications.command.produccionterminada.update;

import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import com.kynsoft.report.domain.dto.ProduccionTerminadaDto;
import com.kynsoft.report.domain.services.IFincaProductoService;
import com.kynsoft.report.domain.services.IProduccionTerminadaService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class UpdateProduccionTerminadaCommandHandler implements ICommandHandler<UpdateProduccionTerminadaCommand> {

    private final IProduccionTerminadaService service;
    private final IFincaProductoService fincaProductoService;

    public UpdateProduccionTerminadaCommandHandler(
            IProduccionTerminadaService service,
            IFincaProductoService fincaProductoService) {
        this.service = service;
        this.fincaProductoService = fincaProductoService;
    }

    @Override
    public void handle(UpdateProduccionTerminadaCommand command) {
        // 1. Obtener el registro anterior para calcular diferencia
        ProduccionTerminadaDto anterior = service.findById(command.getId());
        Integer cantidadAnterior = anterior.getCantidadTerminada();
        Integer cantidadNueva = command.getCantidadTerminada();

        // 2. Actualizar el registro
        ProduccionTerminadaDto dto = ProduccionTerminadaDto.builder()
                .id(command.getId())
                .fincaId(command.getFincaId())
                .productoId(command.getProductoId())
                .cantidadTerminada(cantidadNueva)
                .trabajadorEntregaId(command.getTrabajadorEntregaId())
                .trabajadorRecibeId(command.getTrabajadorRecibeId())
                .observaciones(command.getObservaciones())
                .build();

        service.update(dto);

        // 3. Ajustar stock según la diferencia
        Integer diferencia = cantidadNueva - cantidadAnterior;
        if (diferencia > 0) {
            // Aumentó la cantidad, agregar al stock
            fincaProductoService.entradaProduccion(
                    command.getFincaId(),
                    command.getProductoId(),
                    diferencia,
                    "Ajuste por edición de producción terminada"
            );
        } else if (diferencia < 0) {
            // Disminuyó la cantidad, restar del stock
            fincaProductoService.decrementarStock(
                    command.getFincaId(),
                    command.getProductoId(),
                    Math.abs(diferencia)
            );
        }
        // Si diferencia == 0, no hay cambio en stock
    }
}
