package com.kynsoft.report.applications.command.fincaproducto.entradaproduccion;

import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import com.kynsoft.report.domain.services.IFincaProductoService;
import org.springframework.stereotype.Component;

@Component
public class EntradaProduccionCommandHandler implements ICommandHandler<EntradaProduccionCommand> {

    private final IFincaProductoService fincaProductoService;

    public EntradaProduccionCommandHandler(IFincaProductoService fincaProductoService) {
        this.fincaProductoService = fincaProductoService;
    }

    @Override
    public void handle(EntradaProduccionCommand command) {
        fincaProductoService.entradaProduccion(
                command.getFincaId(),
                command.getProductoId(),
                command.getCantidad(),
                command.getDescripcion()
        );

        // Obtener el nuevo stock para incluirlo en el mensaje
        Integer nuevoStock = fincaProductoService.obtenerStock(command.getFincaId(), command.getProductoId());
        EntradaProduccionMessage message = (EntradaProduccionMessage) command.getMessage();
        message.setNuevoStock(nuevoStock);
        message.setMensaje("Entrada de " + command.getCantidad() + " unidades registrada. Nuevo stock: " + nuevoStock);
    }
}
