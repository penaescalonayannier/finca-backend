package com.kynsoft.report.applications.command.almacen.create;

import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import com.kynsoft.report.domain.dto.AlmacenDto;
import com.kynsoft.report.domain.services.IAlmacenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CreateAlmacenCommandHandler implements ICommandHandler<CreateAlmacenCommand> {

    private final IAlmacenService serviceImpl;

    @Override
    public void handle(CreateAlmacenCommand command) {
        serviceImpl.create(AlmacenDto.builder()
                .id(command.getId())
                .nombre(command.getNombre())
                .inventario(command.getInventario())
                .fincaId(command.getFincaId())
                .activo(true)
                .build());
    }
}
