package com.kynsoft.report.controller;

import com.kynsof.share.core.domain.request.PageableUtil;
import com.kynsof.share.core.domain.request.SearchRequest;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.bus.IMediator;
import com.kynsoft.report.applications.command.salida.create.CreateSalidaCommand;
import com.kynsoft.report.applications.command.salida.create.CreateSalidaMessage;
import com.kynsoft.report.applications.command.salida.create.CreateSalidaRequest;
import com.kynsoft.report.applications.command.salida.delete.DeleteSalidaCommand;
import com.kynsoft.report.applications.command.salida.delete.DeleteSalidaMessage;
import com.kynsoft.report.applications.command.salida.update.UpdateSalidaCommand;
import com.kynsoft.report.applications.command.salida.update.UpdateSalidaMessage;
import com.kynsoft.report.applications.command.salida.update.UpdateSalidaRequest;
import com.kynsoft.report.applications.query.salida.getall.GetAllSalidaQuery;
import com.kynsoft.report.applications.query.salida.getbyid.FindSalidaByIdQuery;
import com.kynsoft.report.applications.query.responseObject.SalidaResponse;
import com.kynsoft.report.domain.dto.SalidaDto;
import com.kynsoft.report.domain.services.ISalidaService;
import com.kynsoft.report.infrastructure.services.FacturaPdfService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/salida")
public class SalidaController {

    private final IMediator mediator;
    private final ISalidaService salidaService;
    private final FacturaPdfService facturaPdfService;

    public SalidaController(IMediator mediator, ISalidaService salidaService, FacturaPdfService facturaPdfService) {
        this.mediator = mediator;
        this.salidaService = salidaService;
        this.facturaPdfService = facturaPdfService;
    }

    @PostMapping
    public ResponseEntity<CreateSalidaMessage> create(@RequestBody CreateSalidaRequest request) {
        CreateSalidaCommand command = CreateSalidaCommand.fromRequest(request);
        CreateSalidaMessage response = mediator.send(command);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateSalidaMessage> update(@PathVariable UUID id, @RequestBody UpdateSalidaRequest request) {
        request.setId(id);
        UpdateSalidaCommand command = UpdateSalidaCommand.fromRequest(request);
        UpdateSalidaMessage response = mediator.send(command);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteSalidaMessage> delete(@PathVariable UUID id) {
        DeleteSalidaCommand command = new DeleteSalidaCommand(id);
        DeleteSalidaMessage response = mediator.send(command);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalidaResponse> findById(@PathVariable UUID id) {
        FindSalidaByIdQuery query = new FindSalidaByIdQuery(id);
        SalidaResponse response = mediator.send(query);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse> search(@RequestBody SearchRequest request) {
        Pageable pageable = PageableUtil.createPageable(request);
        GetAllSalidaQuery query = new GetAllSalidaQuery(pageable, request.getFilter(), request.getQuery());
        PaginatedResponse response = mediator.send(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/factura")
    public ResponseEntity<byte[]> descargarFactura(@PathVariable UUID id) {
        try {
            SalidaDto salida = salidaService.findById(id);

            // Datos del suministrador (finca)
            String suministradorNombre = salida.getFincaName() != null ? salida.getFincaName() : "";
            String suministradorCodigo = salida.getFincaCode() != null ? salida.getFincaCode() : "";
            String suministradorDir = "";
            String suministradorMunicipio = "";

            byte[] pdfBytes = facturaPdfService.generarFactura(
                    salida,
                    suministradorNombre,
                    suministradorCodigo,
                    suministradorDir,
                    suministradorMunicipio
            );

            String tipoDoc = salida.getTipo().name().equals("VALE") ? "Vale" : "Factura";
            String filename = tipoDoc + "_" + salida.getNumero().replace("/", "-") + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
