package com.kynsoft.report.applications.query.responseObject;

import com.kynsof.share.core.domain.bus.query.IResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ProduccionTerminadaResponse implements IResponse {
    private UUID id;
    private UUID fincaId;
    private String fincaCode;
    private String fincaName;
    private UUID productoId;
    private String productoCode;
    private String productoName;
    private LocalDateTime fecha;
    private Integer cantidadTerminada;
    private UUID trabajadorEntregaId;
    private String trabajadorEntregaNombre;
    private UUID trabajadorRecibeId;
    private String trabajadorRecibeNombre;
    private String observaciones;
}
