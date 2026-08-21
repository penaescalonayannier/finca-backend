package com.kynsoft.report.applications.command.almacen.create;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class CreateAlmacenCommand implements ICommand {
    private UUID id;
    private String nombre;
    private String inventario;
    private UUID fincaId;

    public static CreateAlmacenCommand fromRequest(CreateAlmacenRequest request) {
        return new CreateAlmacenCommand(
                UUID.randomUUID(),
                request.getNombre(),
                request.getInventario(),
                request.getFincaId()
        );
    }

    @Override
    public ICommandMessage getMessage() {
        return new CreateAlmacenMessage(id);
    }
}
