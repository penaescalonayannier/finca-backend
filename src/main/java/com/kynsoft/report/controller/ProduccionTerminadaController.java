package com.kynsoft.report.controller;

import com.kynsof.share.core.domain.request.PageableUtil;
import com.kynsof.share.core.domain.request.SearchRequest;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.bus.IMediator;
import com.kynsoft.report.applications.command.produccionterminada.create.CreateProduccionTerminadaCommand;
import com.kynsoft.report.applications.command.produccionterminada.create.CreateProduccionTerminadaMessage;
import com.kynsoft.report.applications.command.produccionterminada.create.CreateProduccionTerminadaRequest;
import com.kynsoft.report.applications.command.produccionterminada.delete.DeleteProduccionTerminadaCommand;
import com.kynsoft.report.applications.command.produccionterminada.delete.DeleteProduccionTerminadaMessage;
import com.kynsoft.report.applications.command.produccionterminada.update.UpdateProduccionTerminadaCommand;
import com.kynsoft.report.applications.command.produccionterminada.update.UpdateProduccionTerminadaMessage;
import com.kynsoft.report.applications.command.produccionterminada.update.UpdateProduccionTerminadaRequest;
import com.kynsoft.report.applications.query.produccionterminada.getall.GetAllProduccionTerminadaQuery;
import com.kynsoft.report.applications.query.produccionterminada.getbyid.FindProduccionTerminadaByIdQuery;
import com.kynsoft.report.applications.query.responseObject.ProduccionTerminadaResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/produccion-terminada")
public class ProduccionTerminadaController {

    private final IMediator mediator;

    public ProduccionTerminadaController(IMediator mediator) {
        this.mediator = mediator;
    }

    @PostMapping
    public ResponseEntity<CreateProduccionTerminadaMessage> create(
            @RequestBody CreateProduccionTerminadaRequest request) {
        CreateProduccionTerminadaCommand command = CreateProduccionTerminadaCommand.fromRequest(request);
        CreateProduccionTerminadaMessage response = mediator.send(command);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateProduccionTerminadaMessage> update(
            @PathVariable UUID id,
            @RequestBody UpdateProduccionTerminadaRequest request) {
        request.setId(id);
        UpdateProduccionTerminadaCommand command = UpdateProduccionTerminadaCommand.fromRequest(request);
        UpdateProduccionTerminadaMessage response = mediator.send(command);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteProduccionTerminadaMessage> delete(@PathVariable UUID id) {
        DeleteProduccionTerminadaCommand command = new DeleteProduccionTerminadaCommand(id);
        DeleteProduccionTerminadaMessage response = mediator.send(command);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduccionTerminadaResponse> findById(@PathVariable UUID id) {
        FindProduccionTerminadaByIdQuery query = new FindProduccionTerminadaByIdQuery(id);
        ProduccionTerminadaResponse response = mediator.send(query);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse> search(@RequestBody SearchRequest request) {
        Pageable pageable = PageableUtil.createPageable(request);

        GetAllProduccionTerminadaQuery query = new GetAllProduccionTerminadaQuery(
                pageable,
                request.getFilter(),
                request.getQuery()
        );

        PaginatedResponse response = mediator.send(query);
        return ResponseEntity.ok(response);
    }
}
