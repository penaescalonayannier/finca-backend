package com.kynsoft.report.controller;

import com.kynsof.share.core.domain.request.PageableUtil;
import com.kynsof.share.core.domain.request.SearchRequest;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.bus.IMediator;
import com.kynsoft.report.applications.command.deudaTrabajador.create.CreateDeudaTrabajadorCommand;
import com.kynsoft.report.applications.command.deudaTrabajador.create.CreateDeudaTrabajadorMessage;
import com.kynsoft.report.applications.command.deudaTrabajador.create.CreateDeudaTrabajadorRequest;
import com.kynsoft.report.applications.command.deudaTrabajador.delete.DeleteDeudaTrabajadorCommand;
import com.kynsoft.report.applications.command.deudaTrabajador.delete.DeleteDeudaTrabajadorMessage;
import com.kynsoft.report.applications.command.deudaTrabajador.update.UpdateDeudaTrabajadorCommand;
import com.kynsoft.report.applications.command.deudaTrabajador.update.UpdateDeudaTrabajadorMessage;
import com.kynsoft.report.applications.command.deudaTrabajador.update.UpdateDeudaTrabajadorRequest;
import com.kynsoft.report.applications.query.deudaTrabajador.getById.FindDeudaTrabajadorByIdQuery;
import com.kynsoft.report.applications.query.deudaTrabajador.search.GetSearchDeudaTrabajadorQuery;
import com.kynsoft.report.applications.query.responseObject.DeudaTrabajadorResponse;
import com.kynsoft.report.domain.dto.DeudaTrabajadorDetalleDto;
import com.kynsoft.report.domain.dto.PagoDeudaDto;
import com.kynsoft.report.domain.services.IDeudaTrabajadorDetalleService;
import com.kynsoft.report.domain.services.IPagoDeudaService;
import com.kynsoft.report.applications.command.pagoDeuda.RegistrarPagoRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/deuda-trabajador")
public class DeudaTrabajadorController {

    private final IMediator mediator;
    private final IDeudaTrabajadorDetalleService detalleService;
    private final IPagoDeudaService pagoDeudaService;

    public DeudaTrabajadorController(IMediator mediator,
                                      IDeudaTrabajadorDetalleService detalleService,
                                      IPagoDeudaService pagoDeudaService) {
        this.mediator = mediator;
        this.detalleService = detalleService;
        this.pagoDeudaService = pagoDeudaService;
    }

    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody CreateDeudaTrabajadorRequest request) {
        CreateDeudaTrabajadorCommand createCommand = CreateDeudaTrabajadorCommand.fromRequest(request);
        CreateDeudaTrabajadorMessage response = mediator.send(createCommand);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeudaTrabajadorResponse> findById(@PathVariable UUID id) {
        FindDeudaTrabajadorByIdQuery query = new FindDeudaTrabajadorByIdQuery(id);
        DeudaTrabajadorResponse response = mediator.send(query);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody UpdateDeudaTrabajadorRequest request) {
        request.setId(id);
        UpdateDeudaTrabajadorCommand updateCommand = UpdateDeudaTrabajadorCommand.fromRequest(request);
        UpdateDeudaTrabajadorMessage response = mediator.send(updateCommand);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        DeleteDeudaTrabajadorCommand deleteCommand = new DeleteDeudaTrabajadorCommand(id);
        DeleteDeudaTrabajadorMessage response = mediator.send(deleteCommand);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse> search(@RequestBody SearchRequest request) {
        Pageable pageable = PageableUtil.createPageable(request);
        GetSearchDeudaTrabajadorQuery query = new GetSearchDeudaTrabajadorQuery(pageable, request.getFilter());
        PaginatedResponse data = mediator.send(query);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/detalles/{trabajadorId}")
    public ResponseEntity<List<DeudaTrabajadorDetalleDto>> getDetalles(@PathVariable UUID trabajadorId) {
        List<DeudaTrabajadorDetalleDto> detalles = detalleService.findByTrabajadorId(trabajadorId);
        return ResponseEntity.ok(detalles);
    }

    @PostMapping("/pago")
    public ResponseEntity<?> registrarPago(@RequestBody RegistrarPagoRequest request) {
        PagoDeudaDto dto = PagoDeudaDto.builder()
                .trabajadorId(request.getTrabajadorId())
                .monto(request.getMonto())
                .formaPago(request.getFormaPago())
                .referenciaBancaria(request.getReferenciaBancaria())
                .build();
        UUID pagoId = pagoDeudaService.registrarPago(dto);
        return ResponseEntity.ok(java.util.Map.of("id", pagoId, "message", "Pago registrado exitosamente"));
    }

    @GetMapping("/pagos/{trabajadorId}")
    public ResponseEntity<List<PagoDeudaDto>> getPagos(@PathVariable UUID trabajadorId) {
        List<PagoDeudaDto> pagos = pagoDeudaService.findByTrabajadorId(trabajadorId);
        return ResponseEntity.ok(pagos);
    }

    @PostMapping("/historial/search")
    public ResponseEntity<PaginatedResponse> searchHistorial(@RequestBody SearchRequest request) {
        Pageable pageable = PageableUtil.createPageable(request);
        PaginatedResponse data = detalleService.search(pageable, request.getFilter());
        return ResponseEntity.ok(data);
    }
}
