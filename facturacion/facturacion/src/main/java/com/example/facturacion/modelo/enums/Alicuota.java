package com.example.facturacion.modelo.enums;

/**
 * Enum para los tipos de alícuota de IVA en Argentina.
 * Hay tres tasas, conocidas como alícuotas:
 * - General (21%): es la tasa estándar aplicable a la mayoría de bienes y servicios consumidos.
 * Afecta, por ejemplo, a electrodomésticos, servicios de restaurantes, ropa, calzado y hotelería.
 * - Reducido (10,5%): se aplica a ciertos bienes y servicios considerados esenciales, como productos de la canasta básica
 * de la alimentación tales como harina, leche, frutas y verduras, así como el transporte de pasajeros, los medicamentos y
 * la venta de algunos bienes de capital. Busca aliviar la carga impositiva en sectores más vulnerables.
 * - Aumentado (27%): esta tasa se aplica principalmente a servicios de telecomunicaciones, energía eléctrica y gas distribuido por redes.
 * Grava sectores que consumen más recursos o tienen mayores ingresos.
 * - Según la Ley, no pagarán IVA los siguientes servicios:
 *      - Transporte de Pasajeros (Urbano, interurbano, interprovincial y rural)
 *      - Educación (Colegios, Jardines, Universidades, entre otros)
 *      - Servicio de Salud Ambulatorio (Consultas médicas, odontológicas, psicólogos, psiquiatras, kinesiólogos, imagenología, entre otros)
 *
 * Fuente: AFIP - https://www.bbva.com/es/ar/salud-financiera/iva-en-argentina/
 */
public enum Alicuota {
    ALICUOTA_27(27.0, "Aumentada - Servicios especiales"),
    ALICUOTA_21(21.0, "General - Estándar"),
    ALICUOTA_10_5(10.5, "Reducida - Productos esenciales"),
    ALICUOTA_0(0.0, "Exento - No gravado");

    private final double valor;
    private final String descripcion;

    Alicuota(double valor, String descripcion) {
        this.valor = valor;
        this.descripcion = descripcion;
    }

    public double getValor() {
        return valor;
    }

    public String getDescripcion() {
        return descripcion;
    }
    
    @Override
    public String toString() {
        return valor + "%";
    }
}