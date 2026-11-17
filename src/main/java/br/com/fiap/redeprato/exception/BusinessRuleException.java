package br.com.fiap.redeprato.exception;

/** A business rule was violated by the request data → HTTP 400. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
