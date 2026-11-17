package br.com.fiap.redeprato.bo;

import br.com.fiap.redeprato.exception.BusinessRuleException;

/** Small reusable validation helpers for the BOs. Messages are in Portuguese. */
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

    /** Phone: 10 or 11 digits (DDD + number). */
    public static void phone(String digits, String campo) {
        if (digits == null || digits.length() < 10 || digits.length() > 11) {
            throw new BusinessRuleException(campo + " deve ter 10 ou 11 dígitos (DDD + número).");
        }
    }
}
