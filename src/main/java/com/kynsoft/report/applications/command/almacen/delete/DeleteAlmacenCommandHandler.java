package com.kynsoft.report.applications.command.almacen.delete;

import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import com.kynsoft.report.domain.services.IAlmacenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DeleteAlmacenCommandHandler implements ICommandHandler<DeleteAlmacenCommand> {

    private final IAlmacenService serviceImpl;

    @Override
    public void handle(DeleteAlmacenCommand command) {
        serviceImpl.delete(command.getId());
    }
}
