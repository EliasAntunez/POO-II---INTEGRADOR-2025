package com.example.facturacion.modelo.enums;

/**
 * Medios de pago disponibles.
 */
public enum MedioPago {
    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia Bancaria"),
    CHEQUE("Cheque"),
    TARJETA_DEBITO("Tarjeta de Débito"),
    TARJETA_CREDITO("Tarjeta de Crédito"),
    MERCADO_PAGO("Mercado Pago"),
    OTRO("Otro");

    private final String descripcion;

    MedioPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}