package com.kynsoft.report.applications.command.deudaTrabajador.delete;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class DeleteDeudaTrabajadorCommand implements ICommand {

    private UUID id;

    public DeleteDeudaTrabajadorCommand(UUID id) {
        this.id = id;
    }

    @Override
    public ICommandMessage getMessage() {
        return new DeleteDeudaTrabajadorMessage(id);
    }
}
