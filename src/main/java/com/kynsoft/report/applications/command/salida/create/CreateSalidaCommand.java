package com.kynsoft.report.applications.command.salida.create;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import com.kynsoft.report.domain.dto.DestinoSalida;
import com.kynsoft.report.domain.dto.ItemSalidaDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateSalidaCommand implements ICommand {

    private UUID id;
    // RN-09: tipo se determina automáticamente según destino en el servicio
    private DestinoSalida destino;
    private UUID fincaProductoId;
    private String observaciones;
    private List<ItemSalidaDto> items;

    public CreateSalidaCommand(DestinoSalida destino, UUID fincaProductoId, String observaciones, List<ItemSalidaDto> items) {
        this.id = UUID.randomUUID();
        this.destino = destino;
        this.fincaProductoId = fincaProductoId;
        this.observaciones = observaciones;
        this.items = items;
    }

    public static CreateSalidaCommand fromRequest(CreateSalidaRequest request) {
        return new CreateSalidaCommand(
                request.getDestino(),
                request.getFincaProductoId(),
                request.getObservaciones(),
                request.getItems()
        );
    }

    @Override
    public ICommandMessage getMessage() {
        return new CreateSalidaMessage(id);
    }
}
