package com.kynsoft.report.applications.command.produccionterminada.update;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class UpdateProduccionTerminadaCommand implements ICommand {
    private UUID id;
    private UUID fincaId;
    private UUID productoId;
    private Integer cantidadTerminada;
    private UUID trabajadorEntregaId;
    private UUID trabajadorRecibeId;
    private String observaciones;

    public static UpdateProduccionTerminadaCommand fromRequest(UpdateProduccionTerminadaRequest request) {
        return new UpdateProduccionTerminadaCommand(
                request.getId(),
                request.getFincaId(),
                request.getProductoId(),
                request.getCantidadTerminada(),
                request.getTrabajadorEntregaId(),
                request.getTrabajadorRecibeId(),
                request.getObservaciones()
        );
    }

    @Override
    public ICommandMessage getMessage() {
        return new UpdateProduccionTerminadaMessage(id);
    }
}
