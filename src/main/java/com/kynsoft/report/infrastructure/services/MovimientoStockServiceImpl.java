package com.kynsoft.report.infrastructure.services;

import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.specifications.GenericSpecificationsBuilder;
import com.kynsoft.report.domain.dto.MovimientoStockDto;
import com.kynsoft.report.domain.dto.TipoMovimientoStock;
import com.kynsoft.report.domain.services.IMovimientoStockService;
import com.kynsoft.report.infrastructure.entity.Finca;
import com.kynsoft.report.infrastructure.entity.MovimientoStock;
import com.kynsoft.report.infrastructure.entity.Producto;
import com.kynsoft.report.infrastructure.repository.command.MovimientoStockWriteDataJPARepository;
import com.kynsoft.report.infrastructure.repository.query.FincaReadDataJPARepository;
import com.kynsoft.report.infrastructure.repository.query.MovimientoStockReadDataJPARepository;
import com.kynsoft.report.infrastructure.repository.query.ProductoReadDataJPARepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MovimientoStockServiceImpl implements IMovimientoStockService {

    private final MovimientoStockWriteDataJPARepository repositoryCommand;
    private final MovimientoStockReadDataJPARepository repositoryQuery;
    private final FincaReadDataJPARepository fincaRepository;
    private final ProductoReadDataJPARepository productoRepository;

    public MovimientoStockServiceImpl(
            MovimientoStockWriteDataJPARepository repositoryCommand,
            MovimientoStockReadDataJPARepository repositoryQuery,
            FincaReadDataJPARepository fincaRepository,
            ProductoReadDataJPARepository productoRepository) {
        this.repositoryCommand = repositoryCommand;
        this.repositoryQuery = repositoryQuery;
        this.fincaRepository = fincaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public void registrar(MovimientoStockDto dto) {
        MovimientoStock entity = new MovimientoStock(dto);
        repositoryCommand.save(entity);
    }

    @Override
    public void registrarMovimiento(UUID fincaProductoId, UUID fincaId, UUID productoId,
                                     TipoMovimientoStock tipo, Integer cantidad,
                                     Integer stockAnterior, Integer stockNuevo,
                                     UUID referenciaId, String referenciaTabla, String descripcion) {
        MovimientoStockDto dto = MovimientoStockDto.builder()
                .id(UUID.randomUUID())
                .fincaProductoId(fincaProductoId)
                .fincaId(fincaId)
                .productoId(productoId)
                .tipo(tipo)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .referenciaId(referenciaId)
                .referenciaTabla(referenciaTabla)
                .descripcion(descripcion)
                .fecha(LocalDateTime.now())
                .build();

        registrar(dto);
    }

    @Override
    public List<MovimientoStockDto> findByFincaProductoId(UUID fincaProductoId) {
        return repositoryQuery.findByFincaProductoId(fincaProductoId)
                .stream()
                .map(MovimientoStock::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovimientoStockDto> findByFincaId(UUID fincaId) {
        return repositoryQuery.findByFincaId(fincaId)
                .stream()
                .map(MovimientoStock::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovimientoStockDto> findByProductoId(UUID productoId) {
        return repositoryQuery.findByProductoId(productoId)
                .stream()
                .map(MovimientoStock::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovimientoStockDto> findByReferencia(UUID referenciaId, String referenciaTabla) {
        return repositoryQuery.findByReferencia(referenciaId, referenciaTabla)
                .stream()
                .map(MovimientoStock::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovimientoStockDto> findByFincaIdAndFechaBetween(UUID fincaId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return repositoryQuery.findByFincaIdAndFechaBetween(fincaId, fechaInicio, fechaFin)
                .stream()
                .map(MovimientoStock::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria) {
        GenericSpecificationsBuilder<MovimientoStock> specifications = new GenericSpecificationsBuilder<>(filterCriteria);
        Page<MovimientoStock> data = repositoryQuery.findAll(specifications, pageable);

        // Obtener IDs únicos de fincas y productos
        List<UUID> fincaIds = data.getContent().stream()
                .map(MovimientoStock::getFincaId)
                .distinct()
                .collect(Collectors.toList());
        List<UUID> productoIds = data.getContent().stream()
                .map(MovimientoStock::getProductoId)
                .distinct()
                .collect(Collectors.toList());

        // Cargar fincas y productos en batch
        Map<UUID, String> fincaNombres = fincaRepository.findAllById(fincaIds).stream()
                .collect(Collectors.toMap(Finca::getId, Finca::getName));
        Map<UUID, String> productoNombres = productoRepository.findAllById(productoIds).stream()
                .collect(Collectors.toMap(Producto::getId, Producto::getName));

        // Mapear con nombres
        List<MovimientoStockDto> responses = data.getContent().stream()
                .map(m -> {
                    MovimientoStockDto dto = m.toAggregate();
                    dto.setFincaNombre(fincaNombres.get(m.getFincaId()));
                    dto.setProductoNombre(productoNombres.get(m.getProductoId()));
                    return dto;
                })
                .collect(Collectors.toList());

        return new PaginatedResponse(responses, data.getTotalPages(), data.getNumberOfElements(),
                data.getTotalElements(), data.getSize(), data.getNumber());
    }
}
