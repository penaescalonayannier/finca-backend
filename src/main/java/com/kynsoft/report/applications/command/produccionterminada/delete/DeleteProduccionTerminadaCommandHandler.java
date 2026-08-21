package com.kynsoft.report.applications.command.produccionterminada.delete;

import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import com.kynsoft.report.domain.dto.ProduccionTerminadaDto;
import com.kynsoft.report.domain.services.IFincaProductoService;
import com.kynsoft.report.domain.services.IProduccionTerminadaService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DeleteProduccionTerminadaCommandHandler implements ICommandHandler<DeleteProduccionTerminadaCommand> {

    private final IProduccionTerminadaService service;
    private final IFincaProductoService fincaProductoService;

    public DeleteProduccionTerminadaCommandHandler(
            IProduccionTerminadaService service,
            IFincaProductoService fincaProductoService) {
        this.service = service;
        this.fincaProductoService = fincaProductoService;
    }

    @Override
    public void handle(DeleteProduccionTerminadaCommand command) {
        // 1. Obtener el registro antes de eliminar para saber cuánto revertir
        ProduccionTerminadaDto produccion = service.findById(command.getId());

        // 2. Revertir el stock (decrementar la cantidad que se había agregado)
        fincaProductoService.decrementarStock(
                produccion.getFincaId(),
                produccion.getProductoId(),
                produccion.getCantidadTerminada()
        );

        // 3. Eliminar el registro
        service.delete(command.getId());
    }
}
