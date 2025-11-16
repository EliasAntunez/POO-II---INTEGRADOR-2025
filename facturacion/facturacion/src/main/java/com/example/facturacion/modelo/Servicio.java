package com.example.facturacion.modelo;

import com.example.facturacion.modelo.enums.Alicuota;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
    * Modelo de entidad Servicio.
*/

// ## HU-04 Alta de Servicios
// **Descripción:** Como **Administrador** quiero **registrar nuevos servicios** para **tener sus datos en el sistema y el cliente pueda solicitar los mismos.**

// **Criterios de Aceptación:**
// - Datos necesarios y obligatorios para su registro:
//     - Nombre
//     - Descripción
//     - Precio
//     - Tipo de Alícuota (27%, 21%, 10.5%, 3.5%)
//     - Estado (Activo/Inactivo)
// - El sistema debe validar que los campos obligatorios sean validados antes del registro (no permitir campos vacíos)

// **Notas Técnicas:**
// - El sistema debe verificar que el nombre no se repita.
// - El sistema debe validar los tipos de datos (texto, número, etc)

@Entity
@Table(name = "servicio")
@Getter @Setter @NoArgsConstructor
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "nombre", unique = true, nullable = false, length = 100)
    private String nombre;
    @NotBlank
    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;
    @Column(name = "precio", nullable = false)
    private double precio;
    @Column(name = "alicuota", nullable = false)
    private Alicuota alicuota;
    @Column(name = "activo", nullable = false)
    private boolean activo;
}
