package com.kynsoft.report.infrastructure.services;

import com.kynsof.share.core.domain.exception.BusinessNotFoundException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.exception.GlobalBusinessException;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.ErrorField;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.specifications.GenericSpecificationsBuilder;
import com.kynsoft.report.applications.query.responseObject.DeudaTrabajadorResponse;
import com.kynsoft.report.domain.dto.DeudaTrabajadorDto;
import com.kynsoft.report.domain.services.IDeudaTrabajadorService;
import com.kynsoft.report.infrastructure.entity.DeudaTrabajador;
import com.kynsoft.report.infrastructure.repository.command.DeudaTrabajadorWriteDataJPARepository;
import com.kynsoft.report.infrastructure.repository.query.DeudaTrabajadorReadDataJPARepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeudaTrabajadorServiceImpl implements IDeudaTrabajadorService {

    private final DeudaTrabajadorWriteDataJPARepository repositoryCommand;
    private final DeudaTrabajadorReadDataJPARepository repositoryQuery;

    public DeudaTrabajadorServiceImpl(DeudaTrabajadorWriteDataJPARepository repositoryCommand,
                                       DeudaTrabajadorReadDataJPARepository repositoryQuery) {
        this.repositoryCommand = repositoryCommand;
        this.repositoryQuery = repositoryQuery;
    }

    @Override
    public UUID create(DeudaTrabajadorDto dto) {
        DeudaTrabajador entity = new DeudaTrabajador(dto);
        DeudaTrabajador saved = repositoryCommand.save(entity);
        return saved.getId();
    }

    @Override
    public void update(DeudaTrabajadorDto dto) {
        repositoryCommand.save(new DeudaTrabajador(dto));
    }

    @Override
    public void delete(UUID id) {
        try {
            repositoryCommand.deleteById(id);
        } catch (Exception e) {
            throw new BusinessNotFoundException(new GlobalBusinessException(
                    DomainErrorMessage.NOT_DELETE,
                    new ErrorField("id", "El elemento no puede ser eliminado.")));
        }
    }

    @Override
    public DeudaTrabajadorDto findById(UUID id) {
        return repositoryQuery.findById(id)
                .map(DeudaTrabajador::toAggregate)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.BUSINESS_NOT_FOUND,
                        new ErrorField("id", "Deuda de trabajador no encontrada."))));
    }

    @Override
    public PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria) {
        GenericSpecificationsBuilder<DeudaTrabajador> specifications = new GenericSpecificationsBuilder<>(filterCriteria);
        Page<DeudaTrabajador> data = repositoryQuery.findAll(specifications, pageable);
        return createPaginatedResponse(data);
    }

    private PaginatedResponse createPaginatedResponse(Page<DeudaTrabajador> data) {
        List<DeudaTrabajadorResponse> responses = data.getContent().stream()
                .map(DeudaTrabajador::toAggregate)
                .map(DeudaTrabajadorResponse::new)
                .collect(Collectors.toList());

        return new PaginatedResponse(responses, data.getTotalPages(), data.getNumberOfElements(),
                data.getTotalElements(), data.getSize(), data.getNumber());
    }

    @Override
    public DeudaTrabajadorDto findByTrabajadorId(UUID trabajadorId) {
        return repositoryQuery.findByTrabajadorId(trabajadorId)
                .map(DeudaTrabajador::toAggregate)
                .orElse(null);
    }

    @Override
    public void incrementarDeuda(UUID trabajadorId, Double importe) {
        DeudaTrabajador deuda = repositoryQuery.findByTrabajadorId(trabajadorId).orElse(null);

        if (deuda == null) {
            // Crear nueva deuda si no existe
            DeudaTrabajadorDto dto = DeudaTrabajadorDto.builder()
                    .id(UUID.randomUUID())
                    .trabajadorId(trabajadorId)
                    .importe(importe)
                    .build();
            repositoryCommand.save(new DeudaTrabajador(dto));
        } else {
            // Incrementar deuda existente
            deuda.setImporte(deuda.getImporte() + importe);
            repositoryCommand.save(deuda);
        }
    }
}
