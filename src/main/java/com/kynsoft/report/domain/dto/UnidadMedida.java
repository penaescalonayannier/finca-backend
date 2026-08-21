package com.kynsoft.report.domain.dto;

public enum UnidadMedida {
    KG("Kilogramo", "Peso"),
    G("Gramo", "Peso"),
    LB("Libra", "Peso"),
    QQ("Quintal", "Peso"),
    L("Litro", "Volumen"),
    ML("Mililitro", "Volumen"),
    GAL("Galón", "Volumen"),
    UND("Unidad", "Cantidad"),
    DOC("Docena", "Cantidad"),
    SACO("Saco", "Cantidad"),
    CAJA("Caja", "Cantidad"),
    M("Metro", "Longitud"),
    CM("Centímetro", "Longitud");

    private final String nombre;
    private final String categoria;

    UnidadMedida(String nombre, String categoria) {
        this.nombre = nombre;
        this.categoria = categoria;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }
}
