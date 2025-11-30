package com.example.facturacion.modelo.enums;

/**
 * Tipos de movimientos en cuenta corriente.
 */
public enum TipoMovimiento {
    CARGO("Cargo", "Cargo manual a la cuenta"),
    CREDITO("Crédito", "Crédito manual a favor del cliente"),
    FACTURA("Factura", "Cargo por factura emitida"),
    PAGO("Pago", "Abono por pago recibido"),
    ANULACION("Anulación", "Reversión por nota de crédito");

    private final String nombre;
    private final String descripcion;

    TipoMovimiento(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}