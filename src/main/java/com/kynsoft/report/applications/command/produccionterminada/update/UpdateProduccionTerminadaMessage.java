package com.kynsoft.report.applications.command.produccionterminada.update;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UpdateProduccionTerminadaMessage implements ICommandMessage {
    private final UUID id;
    private final String command = "UPDATE_PRODUCCION_TERMINADA";

    public UpdateProduccionTerminadaMessage(UUID id) {
        this.id = id;
    }
}
