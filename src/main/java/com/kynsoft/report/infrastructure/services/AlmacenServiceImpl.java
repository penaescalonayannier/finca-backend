package com.kynsoft.report.infrastructure.services;

import com.kynsof.share.core.domain.exception.BusinessNotFoundException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.exception.GlobalBusinessException;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.ErrorField;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.specifications.GenericSpecificationsBuilder;
import com.kynsoft.report.applications.query.responseObject.AlmacenResponse;
import com.kynsoft.report.domain.dto.AlmacenDto;
import com.kynsoft.report.domain.dto.FincaProductoDto;
import com.kynsoft.report.domain.services.IAlmacenService;
import com.kynsoft.report.infrastructure.entity.Almacen;
import com.kynsoft.report.infrastructure.entity.Finca;
import com.kynsoft.report.infrastructure.entity.FincaProducto;
import com.kynsoft.report.infrastructure.repository.command.AlmacenWriteDataJPARepository;
import com.kynsoft.report.infrastructure.repository.query.AlmacenReadDataJPARepository;
import com.kynsoft.report.infrastructure.repository.query.FincaProductoReadDataJPARepository;
import com.kynsoft.report.infrastructure.repository.query.FincaReadDataJPARepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlmacenServiceImpl implements IAlmacenService {

    private final AlmacenWriteDataJPARepository repositoryCommand;
    private final AlmacenReadDataJPARepository repositoryQuery;
    private final FincaProductoReadDataJPARepository fincaProductoRepository;
    private final FincaReadDataJPARepository fincaRepository;

    public AlmacenServiceImpl(AlmacenWriteDataJPARepository repositoryCommand,
                              AlmacenReadDataJPARepository repositoryQuery,
                              FincaProductoReadDataJPARepository fincaProductoRepository,
                              FincaReadDataJPARepository fincaRepository) {
        this.repositoryCommand = repositoryCommand;
        this.repositoryQuery = repositoryQuery;
        this.fincaProductoRepository = fincaProductoRepository;
        this.fincaRepository = fincaRepository;
    }

    @Override
    public void create(AlmacenDto object) {
        // Validar que el inventario no exista
        if (object.getInventario() != null) {
            repositoryQuery.findByInventario(object.getInventario())
                .ifPresent(almacen -> {
                    throw new BusinessNotFoundException(new GlobalBusinessException(
                            DomainErrorMessage.BUSINESS_NOT_FOUND,
                            new ErrorField("inventario", "Almacen con inventario " + object.getInventario() + " ya existe.")));
                });
        }

        Almacen almacen = new Almacen(object);

        // Buscar y asignar la finca
        if (object.getFincaId() != null) {
            Finca finca = fincaRepository.findById(object.getFincaId())
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.BUSINESS_NOT_FOUND,
                        new ErrorField("fincaId", "Finca no encontrada."))));
            almacen.setFinca(finca);
        }

        repositoryCommand.save(almacen);
    }

    @Override
    public void update(AlmacenDto object) {
        Almacen existing = repositoryQuery.findById(object.getId())
            .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                    DomainErrorMessage.BUSINESS_NOT_FOUND,
                    new ErrorField("id", "Almacen no encontrado."))));

        // Verificar que el inventario no este duplicado
        if (object.getInventario() != null) {
            repositoryQuery.findByInventario(object.getInventario())
                .ifPresent(almacen -> {
                    if (!almacen.getId().equals(object.getId())) {
                        throw new BusinessNotFoundException(new GlobalBusinessException(
                                DomainErrorMessage.BUSINESS_NOT_FOUND,
                                new ErrorField("inventario", "Almacen con inventario " + object.getInventario() + " ya existe.")));
                    }
                });
        }

        existing.setNombre(object.getNombre());
        existing.setInventario(object.getInventario());

        // Actualizar finca si se proporciona
        if (object.getFincaId() != null) {
            Finca finca = fincaRepository.findById(object.getFincaId())
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.BUSINESS_NOT_FOUND,
                        new ErrorField("fincaId", "Finca no encontrada."))));
            existing.setFinca(finca);
        }

        repositoryCommand.save(existing);
    }

    @Override
    public void delete(UUID id) {
        Almacen almacen = repositoryQuery.findById(id)
            .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                    DomainErrorMessage.BUSINESS_NOT_FOUND,
                    new ErrorField("id", "Almacen no encontrado."))));

        // Soft delete
        almacen.setActivo(false);
        repositoryCommand.save(almacen);
    }

    @Override
    public AlmacenDto findById(UUID id) {
        // Usar EntityGraph para cargar productos con sus relaciones
        Almacen almacen = repositoryQuery.findByIdWithProductos(id)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.BUSINESS_NOT_FOUND,
                        new ErrorField("id", "Almacen no encontrado."))));

        AlmacenDto dto = almacen.toAggregate();

        // Cargar productos con informacion completa
        if (almacen.getProductos() != null) {
            List<FincaProductoDto> productosDto = almacen.getProductos().stream()
                .map(fp -> FincaProductoDto.builder()
                    .id(fp.getId())
                    .fincaId(fp.getFinca() != null ? fp.getFinca().getId() : null)
                    .fincaCode(fp.getFinca() != null ? fp.getFinca().getCode() : null)
                    .fincaName(fp.getFinca() != null ? fp.getFinca().getName() : null)
                    .productoId(fp.getProducto() != null ? fp.getProducto().getId() : null)
                    .productoCode(fp.getProducto() != null ? fp.getProducto().getCode() : null)
                    .productoName(fp.getProducto() != null ? fp.getProducto().getName() : null)
                    .productoPrice(fp.getProducto() != null ? fp.getProducto().getPrice() : null)
                    .stock(fp.getStock())
                    .activo(fp.getActivo())
                    .build())
                .collect(Collectors.toList());
            dto.setProductos(productosDto);
        }

        return dto;
    }

    @Override
    public AlmacenDto findByInventario(String inventario) {
        return repositoryQuery.findByInventario(inventario)
                .map(Almacen::toAggregate)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.BUSINESS_NOT_FOUND,
                        new ErrorField("inventario", "Almacen con inventario " + inventario + " no encontrado."))));
    }

    @Override
    public PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria) {
        GenericSpecificationsBuilder<Almacen> specifications = new GenericSpecificationsBuilder<>(filterCriteria);

        // Filtrar solo activos
        Specification<Almacen> activoSpec = (root, query, cb) -> cb.equal(root.get("activo"), true);
        Specification<Almacen> combinedSpec = Specification.where(specifications).and(activoSpec);

        Page<Almacen> data = repositoryQuery.findAll(combinedSpec, pageable);
        return createPaginatedResponse(data);
    }

    @Override
    public void addProducto(UUID almacenId, UUID fincaProductoId) {
        Almacen almacen = repositoryQuery.findByIdWithProductos(almacenId)
            .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                    DomainErrorMessage.BUSINESS_NOT_FOUND,
                    new ErrorField("almacenId", "Almacen no encontrado."))));

        FincaProducto fincaProducto = fincaProductoRepository.findById(fincaProductoId)
            .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                    DomainErrorMessage.BUSINESS_NOT_FOUND,
                    new ErrorField("fincaProductoId", "Producto no encontrado."))));

        // Verificar si ya existe
        if (!almacen.getProductos().contains(fincaProducto)) {
            almacen.getProductos().add(fincaProducto);
            repositoryCommand.save(almacen);
        }
    }

    @Override
    public void removeProducto(UUID almacenId, UUID fincaProductoId) {
        Almacen almacen = repositoryQuery.findByIdWithProductos(almacenId)
            .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                    DomainErrorMessage.BUSINESS_NOT_FOUND,
                    new ErrorField("almacenId", "Almacen no encontrado."))));

        almacen.getProductos().removeIf(fp -> fp.getId().equals(fincaProductoId));
        repositoryCommand.save(almacen);
    }

    private PaginatedResponse createPaginatedResponse(Page<Almacen> data) {
        List<AlmacenResponse> responses = data.getContent().stream()
                .map(almacen -> {
                    AlmacenDto dto = almacen.toAggregate();
                    // Usar query para contar productos (evita LazyInitializationException)
                    int productCount = repositoryQuery.countProductosByAlmacenId(almacen.getId());
                    return new AlmacenResponse(dto, productCount);
                })
                .collect(Collectors.toList());

        return new PaginatedResponse(responses, data.getTotalPages(), data.getNumberOfElements(),
                data.getTotalElements(), data.getSize(), data.getNumber());
    }
}
