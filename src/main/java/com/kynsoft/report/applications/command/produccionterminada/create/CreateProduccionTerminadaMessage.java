package com.kynsoft.report.applications.command.produccionterminada.create;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CreateProduccionTerminadaMessage implements ICommandMessage {
    private final UUID id;
    private final String command = "CREATE_PRODUCCION_TERMINADA";

    public CreateProduccionTerminadaMessage(UUID id) {
        this.id = id;
    }
}
