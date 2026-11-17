package br.com.fiap.redeprato.exception;

import java.time.OffsetDateTime;
import java.util.Map;

/** JSON body returned for every error. */
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fields) {
}
