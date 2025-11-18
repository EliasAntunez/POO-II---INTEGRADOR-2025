package com.example.facturacion.modelo.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ConvertidorAlicuota implements AttributeConverter<Alicuota, Double> {

    @Override
    public Double convertToDatabaseColumn(Alicuota attribute) {
        return attribute == null ? null : attribute.getValor();
    }

    @Override
    public Alicuota convertToEntityAttribute(Double dbData) {
        if (dbData == null) return null;
        for (Alicuota a : Alicuota.values()) {
            if (Double.compare(a.getValor(), dbData) == 0) {
                return a;
            }
        }
        throw new IllegalArgumentException("Valor de alícuota desconocido: " + dbData);
    }
}