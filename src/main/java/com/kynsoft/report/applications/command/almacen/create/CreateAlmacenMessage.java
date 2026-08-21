package com.kynsoft.report.applications.command.almacen.create;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CreateAlmacenMessage implements ICommandMessage {
    private final UUID id;
    private final String command = "CREATE_ALMACEN";

    public CreateAlmacenMessage(UUID id) {
        this.id = id;
    }
}
