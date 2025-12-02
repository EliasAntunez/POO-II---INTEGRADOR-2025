package com.example.facturacion.servicio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que encapsula los resultados de un proceso de facturación masiva.
 * Incluye estadísticas y detalles de cada operación.
 */
public class ResultadoFacturacionMasiva {
    
    private LocalDate fechaEmision;
    private LocalDate periodoInicio;
    private LocalDate periodoFin;
    
    private int exitosas = 0;
    private int fallidas = 0;
    private int omitidas = 0;
    
    private BigDecimal montoTotalFacturado = BigDecimal.ZERO;
    
    private List<DetalleFacturacion> detalles = new ArrayList<>();
    
    // Getters y Setters
    
    public LocalDate getFechaEmision() {
        return fechaEmision;
    }
    
    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
    
    public LocalDate getPeriodoInicio() {
        return periodoInicio;
    }
    
    public void setPeriodoInicio(LocalDate periodoInicio) {
        this.periodoInicio = periodoInicio;
    }
    
    public LocalDate getPeriodoFin() {
        return periodoFin;
    }
    
    public void setPeriodoFin(LocalDate periodoFin) {
        this.periodoFin = periodoFin;
    }
    
    public int getExitosas() {
        return exitosas;
    }
    
    public int getFallidas() {
        return fallidas;
    }
    
    public int getOmitidas() {
        return omitidas;
    }
    
    public BigDecimal getMontoTotalFacturado() {
        return montoTotalFacturado;
    }
    
    public List<DetalleFacturacion> getDetalles() {
        return detalles;
    }
    
    public void agregarExitoso(Long clienteId, String razonSocial, Long facturaId, BigDecimal monto) {
        exitosas++;
        montoTotalFacturado = montoTotalFacturado.add(monto);
        detalles.add(new DetalleFacturacion(clienteId, razonSocial, facturaId, "EXITOSA", null, monto));
    }
    
    public void agregarFallido(Long clienteId, String razonSocial, String motivo) {
        fallidas++;
        detalles.add(new DetalleFacturacion(clienteId, razonSocial, null, "FALLIDA", motivo, null));
    }
    
    public void agregarOmitido(Long clienteId, String razonSocial, String motivo) {
        omitidas++;
        detalles.add(new DetalleFacturacion(clienteId, razonSocial, null, "OMITIDA", motivo, null));
    }
    
    public int getTotal() {
        return exitosas + fallidas + omitidas;
    }
    
    /**
     * Clase interna que representa el detalle de cada cliente procesado.
     */
    public static class DetalleFacturacion {
        private final Long clienteId;
        private final String razonSocial;
        private final Long facturaId;
        private final String estado;
        private final String motivo;
        private final BigDecimal monto;
        
        public DetalleFacturacion(Long clienteId, String razonSocial, Long facturaId, 
                                 String estado, String motivo, BigDecimal monto) {
            this.clienteId = clienteId;
            this.razonSocial = razonSocial;
            this.facturaId = facturaId;
            this.estado = estado;
            this.motivo = motivo;
            this.monto = monto;
        }
        
        public Long getClienteId() {
            return clienteId;
        }
        
        public String getRazonSocial() {
            return razonSocial;
        }
        
        public Long getFacturaId() {
            return facturaId;
        }
        
        public String getEstado() {
            return estado;
        }
        
        public String getMotivo() {
            return motivo;
        }
        
        public BigDecimal getMonto() {
            return monto;
        }
    }
}
