package com.example.facturacion.modelo.enums;

/**
 * Enumeración para las condiciones de pago de los clientes.
 * Define los plazos de pago disponibles.
 */
public enum CondicionPago {
    CONTADO("Contado", 0),
    CUENTA_CORRIENTE_30("Cuenta Corriente 30 días", 30),
    CUENTA_CORRIENTE_60("Cuenta Corriente 60 días", 60),
    CUENTA_CORRIENTE_90("Cuenta Corriente 90 días", 90);
    
    private final String descripcion;
    private final int dias;
    
    CondicionPago(String descripcion, int dias) {
        this.descripcion = descripcion;
        this.dias = dias;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public int getDias() {
        return dias;
    }
    
    @Override
    public String toString() {
        return descripcion;
    }
}
