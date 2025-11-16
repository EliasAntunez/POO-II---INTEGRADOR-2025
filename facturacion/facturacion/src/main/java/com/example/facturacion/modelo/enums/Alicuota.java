package com.example.facturacion.modelo.enums;

public enum Alicuota {
    ALICUOTA_27(27),
    ALICUOTA_21(21),
    ALICUOTA_10_5(10.5),
    ALICUOTA_3_5(3.5);

    private final double valor;

    Alicuota(double valor) {
        this.valor = valor;
    }

    public double getValor() {
        return valor;
    }
}
