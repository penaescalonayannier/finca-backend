package com.kynsoft.report.infrastructure.entity;

import com.kynsoft.report.domain.dto.AlmacenDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "almacenes")
public class Almacen {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "inventario", unique = true, nullable = false, length = 50)
    private String inventario;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finca_id")
    private Finca finca;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "almacen_finca_producto",
        joinColumns = @JoinColumn(name = "almacen_id"),
        inverseJoinColumns = @JoinColumn(name = "finca_producto_id")
    )
    private List<FincaProducto> productos = new ArrayList<>();

    public Almacen(AlmacenDto dto) {
        this.id = dto.getId();
        this.nombre = dto.getNombre();
        this.inventario = dto.getInventario();
        this.activo = dto.getActivo() != null ? dto.getActivo() : true;
    }

    public AlmacenDto toAggregate() {
        return AlmacenDto.builder()
                .id(id)
                .nombre(nombre)
                .inventario(inventario)
                .activo(activo)
                .fincaId(finca != null ? finca.getId() : null)
                .fincaCode(finca != null ? finca.getCode() : null)
                .fincaName(finca != null ? finca.getName() : null)
                .build();
    }
}
