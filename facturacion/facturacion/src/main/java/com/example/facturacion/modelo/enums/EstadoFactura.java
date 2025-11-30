    package com.example.facturacion.modelo.enums;

    /**
     * Estados posibles de una factura.
     * HU-11: Registrar Pago Total
     */
    public enum EstadoFactura {
        PENDIENTE_PAGO("Pendiente de Pago"),
        PAGADA("Pagada"),
        PARCIALMENTE_PAGADA("Parcialmente Pagada"),
        ANULADA("Anulada");

        private final String descripcion;

        EstadoFactura(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }

        @Override
        public String toString() {
            return descripcion;
        }
        
        /**
         * Verifica si la factura puede recibir un pago.
         * Solo las facturas PENDIENTE_PAGO pueden pagarse.
         */
        public boolean puedePagarse() {
            return this == PENDIENTE_PAGO;
        }
    }