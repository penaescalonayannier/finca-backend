package com.kynsoft.report.controller;

import com.kynsof.share.core.domain.request.PageableUtil;
import com.kynsof.share.core.domain.request.SearchRequest;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsoft.report.domain.dto.MovimientoStockDto;
import com.kynsoft.report.domain.services.IMovimientoStockService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movimiento-stock")
public class MovimientoStockController {

    private final IMovimientoStockService movimientoStockService;

    public MovimientoStockController(IMovimientoStockService movimientoStockService) {
        this.movimientoStockService = movimientoStockService;
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse> search(@RequestBody SearchRequest request) {
        Pageable pageable = PageableUtil.createPageable(request);
        PaginatedResponse response = movimientoStockService.search(pageable, request.getFilter());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/finca-producto/{fincaProductoId}")
    public ResponseEntity<List<MovimientoStockDto>> findByFincaProductoId(@PathVariable UUID fincaProductoId) {
        List<MovimientoStockDto> response = movimientoStockService.findByFincaProductoId(fincaProductoId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/finca/{fincaId}")
    public ResponseEntity<List<MovimientoStockDto>> findByFincaId(@PathVariable UUID fincaId) {
        List<MovimientoStockDto> response = movimientoStockService.findByFincaId(fincaId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<MovimientoStockDto>> findByProductoId(@PathVariable UUID productoId) {
        List<MovimientoStockDto> response = movimientoStockService.findByProductoId(productoId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/finca/{fincaId}/rango")
    public ResponseEntity<List<MovimientoStockDto>> findByFincaIdAndFechaBetween(
            @PathVariable UUID fincaId,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        LocalDateTime inicio = LocalDateTime.parse(fechaInicio);
        LocalDateTime fin = LocalDateTime.parse(fechaFin);
        List<MovimientoStockDto> response = movimientoStockService.findByFincaIdAndFechaBetween(fincaId, inicio, fin);
        return ResponseEntity.ok(response);
    }
}
