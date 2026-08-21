package com.kynsoft.report.applications.command.produccionterminada.delete;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class DeleteProduccionTerminadaMessage implements ICommandMessage {
    private final UUID id;
    private final String command = "DELETE_PRODUCCION_TERMINADA";

    public DeleteProduccionTerminadaMessage(UUID id) {
        this.id = id;
    }
}
