package com.example.facturacion.modelo.enums;

public enum TipoComprobante {
    FACTURA_A("A", "01", "Factura A"),
    NOTA_CREDITO_A("A", "03", "Nota de Crédito A"),
    FACTURA_B("B", "06", "Factura B"),
    NOTA_CREDITO_B("B", "08", "Nota de Crédito B");

    private final String letra;
    private final String codigo;
    private final String descripcion;

    TipoComprobante(String letra, String codigo, String descripcion) {
        this.letra = letra;
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getLetra() { return letra; }
    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }

    /**
     * Determina el tipo de FACTURA según la condición fiscal del cliente.
     * Asumiendo que la empresa emisora es RESPONSABLE INSCRIPTO.
     */
    public static TipoComprobante getTipoFactura(CondicionFiscal condicionCliente) {
        if (condicionCliente == CondicionFiscal.RESPONSABLE_INSCRIPTO) {
            return FACTURA_A;
        } else {
            return FACTURA_B;
        }
    }

    /**
     * Determina el tipo de NOTA DE CRÉDITO según la condición fiscal del cliente.
     */
    public static TipoComprobante getTipoNotaCredito(CondicionFiscal condicionCliente) {
        if (condicionCliente == CondicionFiscal.RESPONSABLE_INSCRIPTO) {
            return NOTA_CREDITO_A;
        } else {
            return NOTA_CREDITO_B;
        }
    }
}