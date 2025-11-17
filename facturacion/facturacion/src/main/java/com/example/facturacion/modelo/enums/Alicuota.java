package com.example.facturacion.modelo.enums;

/**
 * Enum para los tipos de alícuota de IVA en Argentina.
 * HU-04: Define las alícuotas disponibles para los servicios.
 */
public enum Alicuota {
    ALICUOTA_27(27.0),
    ALICUOTA_21(21.0),
    ALICUOTA_10_5(10.5),
    ALICUOTA_3_5(3.5);

    private final double valor;

    Alicuota(double valor) {
        this.valor = valor;
    }

    public double getValor() {
        return valor;
    }
    
    @Override
    public String toString() {
        return valor + "%";
    }
}