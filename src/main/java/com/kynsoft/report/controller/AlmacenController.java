package com.kynsoft.report.controller;

import com.kynsof.share.core.domain.request.PageableUtil;
import com.kynsof.share.core.domain.request.SearchRequest;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.bus.IMediator;
import com.kynsoft.report.applications.command.almacen.create.CreateAlmacenCommand;
import com.kynsoft.report.applications.command.almacen.create.CreateAlmacenMessage;
import com.kynsoft.report.applications.command.almacen.create.CreateAlmacenRequest;
import com.kynsoft.report.applications.command.almacen.delete.DeleteAlmacenCommand;
import com.kynsoft.report.applications.command.almacen.delete.DeleteAlmacenMessage;
import com.kynsoft.report.applications.command.almacen.update.UpdateAlmacenCommand;
import com.kynsoft.report.applications.command.almacen.update.UpdateAlmacenMessage;
import com.kynsoft.report.applications.command.almacen.update.UpdateAlmacenRequest;
import com.kynsoft.report.applications.query.almacen.getById.FindAlmacenByIdQuery;
import com.kynsoft.report.applications.query.almacen.search.GetSearchAlmacenQuery;
import com.kynsoft.report.applications.query.responseObject.AlmacenResponse;
import com.kynsoft.report.domain.services.IAlmacenService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/almacen")
public class AlmacenController {

    private final IMediator mediator;
    private final IAlmacenService almacenService;

    public AlmacenController(IMediator mediator, IAlmacenService almacenService) {
        this.mediator = mediator;
        this.almacenService = almacenService;
    }

    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody CreateAlmacenRequest request) {
        CreateAlmacenCommand createCommand = CreateAlmacenCommand.fromRequest(request);
        CreateAlmacenMessage response = mediator.send(createCommand);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlmacenResponse> findById(@PathVariable UUID id) {
        FindAlmacenByIdQuery query = new FindAlmacenByIdQuery(id);
        AlmacenResponse response = mediator.send(query);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody UpdateAlmacenRequest request) {
        UpdateAlmacenCommand updateCommand = UpdateAlmacenCommand.fromRequest(request, id);
        UpdateAlmacenMessage response = mediator.send(updateCommand);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        DeleteAlmacenCommand deleteCommand = new DeleteAlmacenCommand(id);
        DeleteAlmacenMessage response = mediator.send(deleteCommand);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse> search(@RequestBody SearchRequest request) {
        Pageable pageable = PageableUtil.createPageable(request);
        GetSearchAlmacenQuery query = new GetSearchAlmacenQuery(pageable, request.getFilter(), request.getQuery());
        PaginatedResponse data = mediator.send(query);
        return ResponseEntity.ok(data);
    }

    // Endpoints para manejo de productos

    @PostMapping("/{almacenId}/productos")
    public ResponseEntity<?> addProducto(@PathVariable UUID almacenId, @RequestBody AddProductoRequest request) {
        almacenService.addProducto(almacenId, request.getFincaProductoId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{almacenId}/productos/{fincaProductoId}")
    public ResponseEntity<?> removeProducto(@PathVariable UUID almacenId, @PathVariable UUID fincaProductoId) {
        almacenService.removeProducto(almacenId, fincaProductoId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{almacenId}/productos")
    public ResponseEntity<AlmacenResponse> getProductos(@PathVariable UUID almacenId) {
        FindAlmacenByIdQuery query = new FindAlmacenByIdQuery(almacenId);
        AlmacenResponse response = mediator.send(query);
        return ResponseEntity.ok(response);
    }

    // Request class para agregar producto
    @lombok.Getter
    @lombok.Setter
    public static class AddProductoRequest {
        private UUID fincaProductoId;
    }
}
