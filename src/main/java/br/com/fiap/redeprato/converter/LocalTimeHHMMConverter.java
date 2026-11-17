package br.com.fiap.redeprato.converter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Maps Java {@link LocalTime} to Oracle {@code VARCHAR2(5)} in the format HH:mm. */
@Converter
public class LocalTimeHHMMConverter implements AttributeConverter<LocalTime, String> {

    public static final DateTimeFormatter HH_MM =
            DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

    @Override
    public String convertToDatabaseColumn(LocalTime value) {
        return value == null ? null : value.format(HH_MM);
    }

    @Override
    public LocalTime convertToEntityAttribute(String db) {
        if (db == null) {
            return null;
        }
        try {
            return LocalTime.parse(db.trim(), HH_MM);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Horário inválido no banco (esperado HH:MM): " + db, e);
        }
    }
}
