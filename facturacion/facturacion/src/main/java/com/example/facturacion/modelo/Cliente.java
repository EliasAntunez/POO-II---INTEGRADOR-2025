package com.example.facturacion.modelo;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.persistence.EnumType;
import com.example.facturacion.modelo.enums.CondicionFiscal;
//import jakarta.validation.constraints.NotNull;

//Lombok
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
    * Modelo de entidad Cliente.
*/

@Entity
@Table(name = "cliente")
@Getter @Setter @NoArgsConstructor
public class Cliente {
    /**
     * Identificador único del cliente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número de DNI del cliente.
     * Debe tener 7 u 8 dígitos.
     * No puede estar vacío.
     * Debe ser único.
     */
    @NotBlank
    @Pattern(regexp = "\\d{7,8}", message = "DNI debe tener 7 u 8 dígitos")
    @Column(name = "dni", unique = true, nullable = false, length = 8)
    private String dni;
    /**
     * Razón social del cliente.
     * No puede estar vacío.
     * Debe tener entre 2 y 255 caracteres.
    */
    @NotBlank
    @Size(max = 255)
    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    /**
     * Nombre del cliente.
     * Debe tener entre 2 y 100 caracteres.
     * No puede estar vacío.
    */
    @NotBlank
    @Size(max = 100)
    @Column(name = "nombre", nullable = false)
    private String nombre;

    /**
     * Apellido del cliente.
     * Debe tener entre 2 y 100 caracteres.
     * No puede estar vacío.
    */
    @NotBlank
    @Size(max = 100)
    @Column(name = "apellido", nullable = false)
    private String apellido;

    /**
     * CUIT del cliente.
     * Debe tener 11 dígitos.
     * No puede estar vacío.
     * Debe ser único.
    */
    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "CUIT debe tener 11 dígitos")
    @Column(name = "cuit", unique = true, nullable = false, length = 11)
    private String cuit;

    /**
     * Email del cliente.
     * No puede estar vacío.
     * Debe ser único.
    */
    @NotBlank
    @Email(message = "Email inválido")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    /**
     * Teléfono del cliente.
     * Debe tener entre 7 y 15 dígitos, puede incluir el prefijo '+'.
     * No puede estar vacío.
     * Debe ser único.
    */
    @NotBlank
    @Pattern(regexp = "^\\+?\\d{7,15}$", message = "Teléfono inválido")
    @Column(name = "telefono", unique = true, nullable = false)
    private String telefono;

    /**
     * Dirección del cliente.
     * No puede estar vacío.
     * Máximo 255 caracteres.
    */
    @NotBlank
    @Size(max = 255)
    @Column(name = "direccion", nullable = false)
    private String direccion;

    /**
     * Condición fiscal del cliente.
     * No puede ser nulo.
     */
    @NotNull
    @Column(name = "condicion_fiscal", nullable = false)
    @Enumerated(EnumType.STRING)
    private CondicionFiscal condicionFiscal;

    /**
     * Indica si el cliente está activo.
     */
    @Column(name = "activo", nullable = false)
    private boolean activo;
}