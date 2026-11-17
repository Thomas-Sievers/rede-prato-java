package br.com.fiap.redeprato.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Maps Java {@code boolean} to Oracle {@code CHAR(1)} 'S'/'N'. */
@Converter
public class BooleanSNConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? "S" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String db) {
        if (db == null) {
            return null;
        }
        return switch (db.trim()) {
            case "S" -> Boolean.TRUE;
            case "N" -> Boolean.FALSE;
            default -> throw new IllegalArgumentException("Valor S/N inválido no banco: " + db);
        };
    }
}
