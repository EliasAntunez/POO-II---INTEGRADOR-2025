package com.example.facturacion.modelo;

import java.math.BigDecimal;

import com.example.facturacion.modelo.enums.Alicuota;
import com.example.facturacion.modelo.enums.ConvertidorAlicuota;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modelo de entidad Servicio.
 *
 * HU-04: Alta de Servicios
 * HU-05: Modificación de Servicios
 * HU-06: Baja de Servicios (lógica)
 */
@Entity
@Table(name = "servicio")
@Getter @Setter @NoArgsConstructor
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre", unique = true, nullable = false, length = 100)
    private String nombre;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;
    
    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    @Column(name = "precio", nullable = false)
    private BigDecimal precio;
    
    @NotNull(message = "La alícuota es obligatoria")
    @Convert(converter = ConvertidorAlicuota.class)
    @Column(name = "alicuota", nullable = false)
    private Alicuota alicuota;
    
    @Column(name = "activo", nullable = false)
    private boolean activo = true;  // Por defecto activo
}