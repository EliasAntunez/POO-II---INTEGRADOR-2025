package com.example.facturacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cliente_servicio")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClienteServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    @ToString.Exclude
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", nullable = false)
    @ToString.Exclude
    private Servicio servicio;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDate fechaAsignacion;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;

    /**
     * Precio personalizado para este cliente (si es null, usa el precio del servicio).
     */
    @Column(name = "precio", precision = 19, scale = 2)
    private BigDecimal precio;

    /**
     * Obtiene el precio aplicable (personalizado o del servicio).
     */
    public BigDecimal getPrecio() {
        return precio != null ? precio : (servicio != null ? servicio.getPrecio() : BigDecimal.ZERO);
    }

    @PrePersist
    protected void onCreate() {
        if (fechaAsignacion == null) {
            fechaAsignacion = LocalDate.now();
        }
    }

    // -----------------
    // Business (rich-model) methods
    // -----------------

    /**
     * Factory method to create a new assignment between client and service.
     * Performs basic validation and sets default values.
     */
    public static ClienteServicio asignarServicioACliente(Cliente cliente, Servicio servicio) {
        if (cliente == null) throw new IllegalArgumentException("Cliente no puede ser nulo");
        if (servicio == null) throw new IllegalArgumentException("Servicio no puede ser nulo");
        ClienteServicio cs = ClienteServicio.builder()
                .cliente(cliente)
                .servicio(servicio)
                .activo(true)
                .build();
        cs.ensureDefaults();
        return cs;
    }

    public void ensureDefaults(){
        if (this.fechaAsignacion == null) this.fechaAsignacion = LocalDate.now();
    }

    public void activar(){ this.activo = true; }

    public void desactivar(){ this.activo = false; }

    public void validarParaCrear(){
        if(this.cliente == null) throw new IllegalStateException("Cliente requerido");
        if(this.servicio == null) throw new IllegalStateException("Servicio requerido");
    }
}