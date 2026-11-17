package br.com.fiap.redeprato.exception;

/** Resource not found → HTTP 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    /** Builds the standard message, e.g. {@code "Lote 42 não encontrado."}. */
    public static NotFoundException of(String recurso, Object id) {
        return new NotFoundException(recurso + " " + id + " não encontrado.");
    }

    /** Feminine form, e.g. {@code "Reserva 7 não encontrada."}. */
    public static NotFoundException ofFem(String recurso, Object id) {
        return new NotFoundException(recurso + " " + id + " não encontrada.");
    }
}
