package br.com.fiap.redeprato.service;

import br.com.fiap.redeprato.exception.BusinessRuleException;

/** BO layer (business rules): small reusable validation helpers for the services. Messages are in Portuguese. */
public final class Checks {

    private Checks() {
    }

    /** Trims; blank becomes null. */
    public static String clean(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    /** Removes every non-digit character; blank becomes null. */
    public static String digits(String value) {
        if (value == null) {
            return null;
        }
        String d = value.replaceAll("\\D", "");
        return d.isEmpty() ? null : d;
    }

    /** Required text: not blank and at most {@code max} characters. */
    public static void required(String value, String campo, int max) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(campo + " é obrigatório.");
        }
        maxLength(value, campo, max);
    }

    /** Same as {@link #required} for feminine field names ("Cidade é obrigatória."). */
    public static void requiredF(String value, String campo, int max) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(campo + " é obrigatória.");
        }
        maxLength(value, campo, max);
    }

    /** Required non-text value. */
    public static void notNull(Object value, String message) {
        if (value == null) {
            throw new BusinessRuleException(message);
        }
    }

    /** Optional text: when present, at most {@code max} characters. */
    public static void maxLength(String value, String campo, int max) {
        if (value != null && value.length() > max) {
            throw new BusinessRuleException(campo + " deve ter no máximo " + max + " caracteres.");
        }
    }

    /** Number of decimal places ignoring trailing zeros (8.50 → 1). */
    public static int decimals(java.math.BigDecimal v) {
        return Math.max(0, v.stripTrailingZeros().scale());
    }

    /** Money value NUMBER(10,2): at most 2 decimals and 99 999 999,99. */
    public static void money(java.math.BigDecimal v, String campo) {
        if (v == null) {
            return;
        }
        if (decimals(v) > 2) {
            throw new BusinessRuleException(campo + " deve ter no máximo 2 casas decimais.");
        }
        if (v.compareTo(new java.math.BigDecimal("99999999.99")) > 0) {
            throw new BusinessRuleException(campo + " deve ser no máximo 99999999,99.");
        }
    }

    /** Phone: 10 or 11 digits (DDD + number). */
    public static void phone(String digits, String campo) {
        if (digits == null || digits.length() < 10 || digits.length() > 11) {
            throw new BusinessRuleException(campo + " deve ter 10 ou 11 dígitos (DDD + número).");
        }
    }
}
