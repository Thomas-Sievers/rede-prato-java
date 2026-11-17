package br.com.fiap.redeprato.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BooleanSNConverterTest {

    private final BooleanSNConverter converter = new BooleanSNConverter();

    @Test
    void trueBecomesS() {
        assertThat(converter.convertToDatabaseColumn(true)).isEqualTo("S");
    }

    @Test
    void falseBecomesN() {
        assertThat(converter.convertToDatabaseColumn(false)).isEqualTo("N");
    }

    @Test
    void nullStaysNullToDatabase() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void sBecomesTrue() {
        assertThat(converter.convertToEntityAttribute("S")).isTrue();
    }

    @Test
    void nBecomesFalse() {
        assertThat(converter.convertToEntityAttribute("N")).isFalse();
    }

    @Test
    void nullStaysNullToEntity() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void unknownValueIsRejected() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("X"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("X");
    }

    @Test
    void lowercaseIsRejected() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("s"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
