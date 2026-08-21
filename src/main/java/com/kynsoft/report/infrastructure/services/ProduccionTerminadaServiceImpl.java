package com.kynsoft.report.infrastructure.services;

import com.kynsof.share.core.domain.exception.BusinessNotFoundException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.exception.GlobalBusinessException;
import com.kynsof.share.core.domain.response.ErrorField;
import com.kynsoft.report.domain.dto.ProduccionTerminadaDto;
import com.kynsoft.report.domain.services.IProduccionTerminadaService;
import com.kynsoft.report.infrastructure.entity.ProduccionTerminada;
import com.kynsoft.report.infrastructure.repository.command.ProduccionTerminadaWriteDataJPARepository;
import com.kynsoft.report.infrastructure.repository.query.ProduccionTerminadaReadDataJPARepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProduccionTerminadaServiceImpl implements IProduccionTerminadaService {

    private final ProduccionTerminadaWriteDataJPARepository repositoryCommand;
    private final ProduccionTerminadaReadDataJPARepository repositoryQuery;

    public ProduccionTerminadaServiceImpl(
            ProduccionTerminadaWriteDataJPARepository repositoryCommand,
            ProduccionTerminadaReadDataJPARepository repositoryQuery) {
        this.repositoryCommand = repositoryCommand;
        this.repositoryQuery = repositoryQuery;
    }

    @Override
    public UUID create(ProduccionTerminadaDto dto) {
        ProduccionTerminada entity = new ProduccionTerminada(dto);
        ProduccionTerminada saved = repositoryCommand.save(entity);
        return saved.getId();
    }

    @Override
    public void update(ProduccionTerminadaDto dto) {
        ProduccionTerminada entity = repositoryQuery.findById(dto.getId())
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.BUSINESS_NOT_FOUND,
                        new ErrorField("id", "Producción terminada no encontrada."))));

        entity.setProductoId(dto.getProductoId());
        entity.setCantidadTerminada(dto.getCantidadTerminada());
        entity.setTrabajadorEntregaId(dto.getTrabajadorEntregaId());
        entity.setTrabajadorRecibeId(dto.getTrabajadorRecibeId());
        entity.setObservaciones(dto.getObservaciones());
        
        repositoryCommand.save(entity);
    }

    @Override
    public void delete(UUID id) {
        ProduccionTerminada entity = repositoryQuery.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.BUSINESS_NOT_FOUND,
                        new ErrorField("id", "Producción terminada no encontrada."))));

        // Soft delete: marcar como inactivo
        entity.setActivo(false);
        repositoryCommand.save(entity);
    }

    @Override
    public ProduccionTerminadaDto findById(UUID id) {
        return repositoryQuery.findByIdWithDetails(id)
                .map(ProduccionTerminada::toAggregate)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.BUSINESS_NOT_FOUND,
                        new ErrorField("id", "Producción terminada no encontrada."))));
    }

    @Override
    public List<ProduccionTerminadaDto> findByProductoId(UUID productoId) {
        return repositoryQuery.findByProductoIdAndActivoTrue(productoId).stream()
                .map(ProduccionTerminada::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProduccionTerminadaDto> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return repositoryQuery.findByFechaBetweenAndActivoTrue(fechaInicio, fechaFin).stream()
                .map(ProduccionTerminada::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProduccionTerminadaDto> findByTrabajadorEntregaId(UUID trabajadorId) {
        return repositoryQuery.findByTrabajadorEntregaIdAndActivoTrue(trabajadorId).stream()
                .map(ProduccionTerminada::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProduccionTerminadaDto> findByTrabajadorRecibeId(UUID trabajadorId) {
        return repositoryQuery.findByTrabajadorRecibeIdAndActivoTrue(trabajadorId).stream()
                .map(ProduccionTerminada::toAggregate)
                .collect(Collectors.toList());
    }
}
