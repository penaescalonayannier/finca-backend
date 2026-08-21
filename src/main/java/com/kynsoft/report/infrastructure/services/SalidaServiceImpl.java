package com.kynsoft.report.infrastructure.services;

import com.kynsof.share.core.domain.exception.BusinessNotFoundException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.exception.GlobalBusinessException;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.ErrorField;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.specifications.GenericSpecificationsBuilder;
import com.kynsoft.report.applications.query.responseObject.SalidaResponse;
import com.kynsoft.report.domain.dto.DeudaTrabajadorDetalleDto;
import com.kynsoft.report.domain.dto.DestinoSalida;
import com.kynsoft.report.domain.dto.ItemSalidaDto;
import com.kynsoft.report.domain.dto.SalidaDto;
import com.kynsoft.report.domain.dto.TipoMovimiento;
import com.kynsoft.report.domain.dto.TipoMovimientoStock;
import com.kynsoft.report.domain.dto.TipoSalida;
import com.kynsoft.report.domain.services.IDeudaTrabajadorDetalleService;
import com.kynsoft.report.domain.services.IDeudaTrabajadorService;
import com.kynsoft.report.domain.services.IMovimientoStockService;

import java.time.LocalDateTime;
import com.kynsoft.report.domain.services.ISalidaService;
import com.kynsoft.report.infrastructure.entity.FincaProducto;
import com.kynsoft.report.infrastructure.entity.ItemSalida;
import com.kynsoft.report.infrastructure.entity.Salida;
import com.kynsoft.report.infrastructure.repository.command.ItemSalidaWriteDataJPARepository;
import com.kynsoft.report.infrastructure.repository.command.SalidaWriteDataJPARepository;
import com.kynsoft.report.infrastructure.repository.command.FincaProductoWriteDataJPARepository;
import com.kynsoft.report.infrastructure.repository.query.FincaProductoReadDataJPARepository;
import com.kynsoft.report.infrastructure.repository.query.ItemSalidaReadDataJPARepository;
import com.kynsoft.report.infrastructure.repository.query.SalidaReadDataJPARepository;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SalidaServiceImpl implements ISalidaService {

    private final SalidaWriteDataJPARepository repositoryCommand;
    private final SalidaReadDataJPARepository repositoryQuery;
    private final ItemSalidaWriteDataJPARepository itemRepositoryCommand;
    private final ItemSalidaReadDataJPARepository itemRepositoryQuery;
    private final FincaProductoReadDataJPARepository fincaProductoReadRepository;
    private final FincaProductoWriteDataJPARepository fincaProductoWriteRepository;
    private final IDeudaTrabajadorService deudaTrabajadorService;
    private final IDeudaTrabajadorDetalleService deudaDetalleService;
    private final IMovimientoStockService movimientoStockService;

    public SalidaServiceImpl(
            SalidaWriteDataJPARepository repositoryCommand,
            SalidaReadDataJPARepository repositoryQuery,
            ItemSalidaWriteDataJPARepository itemRepositoryCommand,
            ItemSalidaReadDataJPARepository itemRepositoryQuery,
            FincaProductoReadDataJPARepository fincaProductoReadRepository,
            FincaProductoWriteDataJPARepository fincaProductoWriteRepository,
            IDeudaTrabajadorService deudaTrabajadorService,
            IDeudaTrabajadorDetalleService deudaDetalleService,
            IMovimientoStockService movimientoStockService) {
        this.repositoryCommand = repositoryCommand;
        this.repositoryQuery = repositoryQuery;
        this.itemRepositoryCommand = itemRepositoryCommand;
        this.itemRepositoryQuery = itemRepositoryQuery;
        this.fincaProductoReadRepository = fincaProductoReadRepository;
        this.fincaProductoWriteRepository = fincaProductoWriteRepository;
        this.deudaTrabajadorService = deudaTrabajadorService;
        this.deudaDetalleService = deudaDetalleService;
        this.movimientoStockService = movimientoStockService;
    }

    @Override
    public UUID create(SalidaDto dto, List<ItemSalidaDto> items) {
        // Validar que existe el FincaProducto (con producto cargado para obtener precios)
        FincaProducto fincaProducto = fincaProductoReadRepository.findByIdWithDetails(dto.getFincaProductoId())
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.BUSINESS_NOT_FOUND,
                        new ErrorField("fincaProductoId", "No se encontró la relación Finca-Producto."))));

        // Forzar inicialización del producto para obtener precios
        Hibernate.initialize(fincaProducto.getProducto());

        // Calcular cantidad total
        int cantidadTotal = items.stream().mapToInt(ItemSalidaDto::getCantidad).sum();

        // Validar stock suficiente
        if (fincaProducto.getStock() < cantidadTotal) {
            throw new BusinessNotFoundException(new GlobalBusinessException(
                    DomainErrorMessage.BUSINESS_NOT_FOUND,
                    new ErrorField("cantidad", "Stock insuficiente. Stock actual: " + fincaProducto.getStock() + ", Cantidad solicitada: " + cantidadTotal)));
        }

        // RN-09: Determinar tipo automáticamente según destino
        TipoSalida tipoFinal = determinarTipoSegunDestino(dto.getDestino());
        dto.setTipo(tipoFinal);

        // Generar número automáticamente
        dto.setNumero(generarNumero(tipoFinal));

        // Crear la salida
        Salida salida = new Salida(dto);
        repositoryCommand.save(salida);

        // Obtener el precio según el destino
        Double precio = obtenerPrecioSegunDestino(fincaProducto, dto.getDestino());

        // Crear los items y actualizar deudas si es para trabajadores
        for (ItemSalidaDto itemDto : items) {
            itemDto.setId(UUID.randomUUID());
            itemDto.setSalidaId(salida.getId());
            itemDto.setPrecio(precio);
            ItemSalida item = new ItemSalida(itemDto);
            itemRepositoryCommand.save(item);

            // Registrar para trabajadores (con o sin deuda según si pagó)
            if (dto.getDestino() == DestinoSalida.TRABAJADORES && itemDto.getTrabajadorId() != null) {
                Double valorItem = itemDto.getCantidad() * precio;
                Boolean yaPago = itemDto.getPagado() != null && itemDto.getPagado();

                // Solo incrementar deuda si NO ha pagado
                if (!yaPago) {
                    deudaTrabajadorService.incrementarDeuda(itemDto.getTrabajadorId(), valorItem);
                }

                // Siempre registrar detalle de auditoria (pagado o no)
                DeudaTrabajadorDetalleDto detalle = DeudaTrabajadorDetalleDto.builder()
                        .id(UUID.randomUUID())
                        .trabajadorId(itemDto.getTrabajadorId())
                        .salidaId(salida.getId())
                        .salidaNumero(dto.getNumero())
                        .salidaTipo(dto.getTipo())
                        .productoId(fincaProducto.getProducto().getId())
                        .productoCodigo(fincaProducto.getProducto().getCode())
                        .productoNombre(fincaProducto.getProducto().getName())
                        .cantidad(itemDto.getCantidad())
                        .precioUnitario(precio)
                        .importe(valorItem)
                        .fecha(LocalDateTime.now())
                        .activo(true)
                        .pagado(yaPago)
                        .tipoMovimiento(TipoMovimiento.COMPRA)
                        .build();
                deudaDetalleService.registrar(detalle);
            }
        }

        // Rebajar el stock con auditoría
        Integer stockAnterior = fincaProducto.getStock();
        Integer stockNuevo = stockAnterior - cantidadTotal;
        fincaProducto.setStock(stockNuevo);
        fincaProductoWriteRepository.save(fincaProducto);

        // Registrar movimiento de stock
        movimientoStockService.registrarMovimiento(
                fincaProducto.getId(),
                fincaProducto.getFinca().getId(),
                fincaProducto.getProducto().getId(),
                TipoMovimientoStock.SALIDA_VENTA,
                -cantidadTotal,
                stockAnterior,
                stockNuevo,
                salida.getId(),
                "salida",
                "Salida " + dto.getNumero() + " - " + dto.getDestino()
        );

        return salida.getId();
    }

    @Override
    public void update(SalidaDto dto, List<ItemSalidaDto> items) {
        Salida salida = repositoryQuery.findById(dto.getId())
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.BUSINESS_NOT_FOUND,
                        new ErrorField("id", "No se encontró la salida."))));

        // Obtener cantidad anterior
        List<ItemSalida> itemsAnteriores = itemRepositoryQuery.findBySalidaId(dto.getId());
        int cantidadAnterior = itemsAnteriores.stream().mapToInt(ItemSalida::getCantidad).sum();

        // Calcular nueva cantidad total
        int nuevaCantidadTotal = items.stream().mapToInt(ItemSalidaDto::getCantidad).sum();

        // Obtener FincaProducto (con producto cargado para obtener precios)
        FincaProducto fincaProducto = fincaProductoReadRepository.findByIdWithDetails(dto.getFincaProductoId())
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.BUSINESS_NOT_FOUND,
                        new ErrorField("fincaProductoId", "No se encontró la relación Finca-Producto."))));

        // Forzar inicialización del producto para obtener precios
        Hibernate.initialize(fincaProducto.getProducto());

        // Validar stock (devolver cantidad anterior y restar nueva)
        int stockDisponible = fincaProducto.getStock() + cantidadAnterior;
        if (stockDisponible < nuevaCantidadTotal) {
            throw new BusinessNotFoundException(new GlobalBusinessException(
                    DomainErrorMessage.BUSINESS_NOT_FOUND,
                    new ErrorField("cantidad", "Stock insuficiente. Stock disponible: " + stockDisponible + ", Cantidad solicitada: " + nuevaCantidadTotal)));
        }

        // Actualizar salida
        salida.setTipo(dto.getTipo());
        salida.setObservaciones(dto.getObservaciones());
        repositoryCommand.save(salida);

        // Obtener el precio según el destino
        Double precio = obtenerPrecioSegunDestino(fincaProducto, dto.getDestino());

        // Revertir deudas de items anteriores si el destino era TRABAJADORES
        if (salida.getDestino() == DestinoSalida.TRABAJADORES) {
            for (ItemSalida itemAnterior : itemsAnteriores) {
                // Solo revertir si no estaba pagado
                if (itemAnterior.getTrabajadorId() != null && (itemAnterior.getPagado() == null || !itemAnterior.getPagado())) {
                    Double valorItem = itemAnterior.getCantidad() * itemAnterior.getPrecio();
                    deudaTrabajadorService.incrementarDeuda(itemAnterior.getTrabajadorId(), -valorItem);
                }
            }
            // Desactivar registros de auditoria anteriores
            deudaDetalleService.desactivarBySalidaId(dto.getId());
        }

        // Eliminar items anteriores y crear nuevos
        itemRepositoryCommand.deleteBySalidaId(dto.getId());
        for (ItemSalidaDto itemDto : items) {
            itemDto.setId(UUID.randomUUID());
            itemDto.setSalidaId(salida.getId());
            itemDto.setPrecio(precio);
            ItemSalida item = new ItemSalida(itemDto);
            itemRepositoryCommand.save(item);

            // Registrar para trabajadores (con o sin deuda según si pagó)
            if (dto.getDestino() == DestinoSalida.TRABAJADORES && itemDto.getTrabajadorId() != null) {
                Double valorItem = itemDto.getCantidad() * precio;
                Boolean yaPago = itemDto.getPagado() != null && itemDto.getPagado();

                // Solo incrementar deuda si NO ha pagado
                if (!yaPago) {
                    deudaTrabajadorService.incrementarDeuda(itemDto.getTrabajadorId(), valorItem);
                }

                // Siempre registrar nuevo detalle de auditoria
                DeudaTrabajadorDetalleDto detalle = DeudaTrabajadorDetalleDto.builder()
                        .id(UUID.randomUUID())
                        .trabajadorId(itemDto.getTrabajadorId())
                        .salidaId(salida.getId())
                        .salidaNumero(salida.getNumero())
                        .salidaTipo(dto.getTipo())
                        .productoId(fincaProducto.getProducto().getId())
                        .productoCodigo(fincaProducto.getProducto().getCode())
                        .productoNombre(fincaProducto.getProducto().getName())
                        .cantidad(itemDto.getCantidad())
                        .precioUnitario(precio)
                        .importe(valorItem)
                        .fecha(LocalDateTime.now())
                        .activo(true)
                        .pagado(yaPago)
                        .tipoMovimiento(TipoMovimiento.COMPRA)
                        .build();
                deudaDetalleService.registrar(detalle);
            }
        }

        // Actualizar stock con auditoría
        Integer stockAnterior = fincaProducto.getStock();
        Integer stockNuevo = stockDisponible - nuevaCantidadTotal;
        fincaProducto.setStock(stockNuevo);
        fincaProductoWriteRepository.save(fincaProducto);

        // Registrar movimiento de stock solo si hay cambio
        Integer diferencia = stockNuevo - stockAnterior;
        if (diferencia != 0) {
            TipoMovimientoStock tipoMov = diferencia > 0 ? TipoMovimientoStock.DEVOLUCION : TipoMovimientoStock.SALIDA_VENTA;
            movimientoStockService.registrarMovimiento(
                    fincaProducto.getId(),
                    fincaProducto.getFinca().getId(),
                    fincaProducto.getProducto().getId(),
                    tipoMov,
                    diferencia,
                    stockAnterior,
                    stockNuevo,
                    salida.getId(),
                    "salida",
                    "Ajuste por edición de Salida " + salida.getNumero()
            );
        }
    }

    @Override
    public void delete(UUID id) {
        Salida salida = repositoryQuery.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.BUSINESS_NOT_FOUND,
                        new ErrorField("id", "No se encontró la salida."))));

        // Validar que la salida no esté ya anulada
        if (!salida.getActivo()) {
            throw new BusinessNotFoundException(new GlobalBusinessException(
                    DomainErrorMessage.BUSINESS_NOT_FOUND,
                    new ErrorField("id", "La salida ya está anulada.")));
        }

        // Obtener cantidad total para devolver al stock
        List<ItemSalida> items = itemRepositoryQuery.findBySalidaId(id);

        // RN-06: Validar que no haya items pagados
        boolean tieneItemsPagados = items.stream()
                .anyMatch(item -> item.getPagado() != null && item.getPagado());
        if (tieneItemsPagados) {
            throw new BusinessNotFoundException(new GlobalBusinessException(
                    DomainErrorMessage.BUSINESS_NOT_FOUND,
                    new ErrorField("id", "No se puede eliminar la salida: tiene items pagados.")));
        }

        int cantidadTotal = items.stream().mapToInt(ItemSalida::getCantidad).sum();

        // Revertir deudas si el destino era TRABAJADORES
        if (salida.getDestino() == DestinoSalida.TRABAJADORES) {
            for (ItemSalida item : items) {
                // Solo revertir si no estaba pagado
                if (item.getTrabajadorId() != null && (item.getPagado() == null || !item.getPagado())) {
                    Double valorItem = item.getCantidad() * item.getPrecio();
                    deudaTrabajadorService.incrementarDeuda(item.getTrabajadorId(), -valorItem);
                }
            }
            // Desactivar registros de auditoria
            deudaDetalleService.desactivarBySalidaId(id);
        }

        // Devolver stock con auditoría
        FincaProducto fincaProducto = fincaProductoReadRepository.findByIdWithDetails(salida.getFincaProductoId())
                .orElse(null);
        if (fincaProducto != null) {
            Integer stockAnterior = fincaProducto.getStock();
            Integer stockNuevo = stockAnterior + cantidadTotal;
            fincaProducto.setStock(stockNuevo);
            fincaProductoWriteRepository.save(fincaProducto);

            // Registrar movimiento de devolución de stock
            movimientoStockService.registrarMovimiento(
                    fincaProducto.getId(),
                    fincaProducto.getFinca().getId(),
                    fincaProducto.getProducto().getId(),
                    TipoMovimientoStock.DEVOLUCION,
                    cantidadTotal,
                    stockAnterior,
                    stockNuevo,
                    salida.getId(),
                    "salida",
                    "Devolución por eliminación de Salida " + salida.getNumero()
            );
        }

        // Soft delete: marcar salida como inactiva (los items se mantienen para historial)
        salida.setActivo(false);
        repositoryCommand.save(salida);
    }

    @Override
    public SalidaDto findById(UUID id) {
        Salida salida = repositoryQuery.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.BUSINESS_NOT_FOUND,
                        new ErrorField("id", "No se encontró la salida."))));

        SalidaDto dto = salida.toAggregate();
        dto.setItems(salida.getItems().stream()
                .map(ItemSalida::toAggregate)
                .collect(Collectors.toList()));
        return dto;
    }

    @Override
    public PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria) {
        // Construir especificación base con filtros del usuario
        GenericSpecificationsBuilder<Salida> specifications = new GenericSpecificationsBuilder<>(filterCriteria);

        // Agregar filtro de activos por defecto
        org.springframework.data.jpa.domain.Specification<Salida> activoSpec = (root, query, cb) -> cb.equal(root.get("activo"), true);
        org.springframework.data.jpa.domain.Specification<Salida> combinedSpec = org.springframework.data.jpa.domain.Specification.where(specifications).and(activoSpec);

        Page<Salida> data = repositoryQuery.findAll(combinedSpec, pageable);

        List<SalidaResponse> responses = data.getContent().stream()
                .map(s -> new SalidaResponse(
                        s.getId(),
                        s.getTipo(),
                        s.getDestino(),
                        s.getNumero(),
                        s.getFincaProductoId(),
                        s.getFincaProducto() != null && s.getFincaProducto().getFinca() != null ? s.getFincaProducto().getFinca().getCode() : null,
                        s.getFincaProducto() != null && s.getFincaProducto().getFinca() != null ? s.getFincaProducto().getFinca().getName() : null,
                        s.getFincaProducto() != null && s.getFincaProducto().getProducto() != null ? s.getFincaProducto().getProducto().getCode() : null,
                        s.getFincaProducto() != null && s.getFincaProducto().getProducto() != null ? s.getFincaProducto().getProducto().getName() : null,
                        s.getFincaProducto() != null ? s.getFincaProducto().getStock() : null,
                        s.getFecha(),
                        s.getObservaciones(),
                        s.getItems() != null ? s.getItems().stream().mapToInt(ItemSalida::getCantidad).sum() : 0
                ))
                .collect(Collectors.toList());

        return new PaginatedResponse(
                responses,
                data.getTotalPages(),
                data.getNumberOfElements(),
                data.getTotalElements(),
                data.getSize(),
                data.getNumber()
        );
    }

    @Override
    public String generarNumero(TipoSalida tipo) {
        String prefix = tipo == TipoSalida.VALE ? "VALE-" : "FAC-";
        Integer maxNum = repositoryQuery.findMaxNumeroByPrefix(prefix);
        int nextNum = (maxNum != null ? maxNum : 0) + 1;
        return prefix + String.format("%04d", nextNum);
    }

    private Double obtenerPrecioSegunDestino(FincaProducto fincaProducto, DestinoSalida destino) {
        if (fincaProducto.getProducto() == null) {
            return 0.0;
        }
        return switch (destino) {
            case TRABAJADORES -> fincaProducto.getProducto().getPriceTrabajador();
            case COMEDOR -> fincaProducto.getProducto().getPriceComedor();
            default -> fincaProducto.getProducto().getPrice();
        };
    }

    /**
     * RN-09: Determina el tipo de salida según el destino
     * - VENTA_ESTADO, POBLACION → FACTURA
     * - TRABAJADORES, COMEDOR, INSUMO, OTROS → VALE
     */
    private TipoSalida determinarTipoSegunDestino(DestinoSalida destino) {
        return switch (destino) {
            case VENTA_ESTADO, POBLACION -> TipoSalida.FACTURA;
            default -> TipoSalida.VALE;
        };
    }
}
