package br.com.fiap.redeprato.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class LocalTimeHHMMConverterTest {

    private final LocalTimeHHMMConverter converter = new LocalTimeHHMMConverter();

    @Test
    void formatsAsHHmm() {
        assertThat(converter.convertToDatabaseColumn(LocalTime.of(9, 5))).isEqualTo("09:05");
    }

    @Test
    void dropsSeconds() {
        assertThat(converter.convertToDatabaseColumn(LocalTime.of(16, 0, 30))).isEqualTo("16:00");
    }

    @Test
    void nullToDatabaseIsNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void parsesHHmm() {
        assertThat(converter.convertToEntityAttribute("18:00")).isEqualTo(LocalTime.of(18, 0));
    }

    @Test
    void parsesBoundaries() {
        assertThat(converter.convertToEntityAttribute("00:00")).isEqualTo(LocalTime.MIDNIGHT);
        assertThat(converter.convertToEntityAttribute("23:59")).isEqualTo(LocalTime.of(23, 59));
    }

    @Test
    void nullToEntityIsNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void invalidHourIsRejected() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("24:00"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void wrongFormatIsRejected() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("9h"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HH:MM");
    }
}
