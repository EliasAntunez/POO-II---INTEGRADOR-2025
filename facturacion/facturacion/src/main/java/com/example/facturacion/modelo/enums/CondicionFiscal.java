package com.example.facturacion.modelo.enums;

public enum CondicionFiscal {   
    RESPONSABLE_INSCRIPTO ("Responsable Inscripto"),
    MONOTRIBUTISTA ("Monotributista"),
    EXENTO ("Exento"),
    NO_RESPONSABLE ("No Responsable");
    private final String descripcion;

    CondicionFiscal(String descripcion) {
        this.descripcion = descripcion;
    }
    public String getDescripcion() {
        return descripcion;
    }
}