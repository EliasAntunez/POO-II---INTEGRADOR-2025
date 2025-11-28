package com.example.facturacion.modelo.enums;

/**
 * Estados posibles de un cliente.
 */
public enum EstadoCliente {
    ACTIVO("Activo", "Cliente activo y operativo"),
    SUSPENDIDO("Suspendido", "Cliente temporalmente suspendido"),
    DADO_DE_BAJA("Dado de Baja", "Cliente dado de baja (no operativo)");

    private final String nombre;
    private final String descripcion;

    EstadoCliente(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esActivo() {
        return this == ACTIVO;
    }
}