package br.com.fiap.redeprato.exception;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

/** Turns every exception into the standard {@link ApiError} JSON with a Portuguese message. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    static final String MSG_DUPLICADO = "Registro duplicado: já existe um registro com esses dados.";
    static final String MSG_DEPENDENTES = "Não é possível excluir: existem registros ligados a este.";
    static final String MSG_RESTRICAO = "Dados inválidos: uma restrição do banco foi violada.";
    static final String MSG_INTERNO = "Erro interno. Tente novamente.";

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException e, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, e.getMessage(), req, null);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> business(BusinessRuleException e, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), req, null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException e, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, e.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e, HttpServletRequest req) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Dados inválidos.", req, fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException e, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "Requisição inválida: corpo da requisição mal formatado.", req, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> typeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "Requisição inválida: parâmetro '" + e.getName() + "' com valor inválido.", req, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> missingParam(MissingServletRequestParameterException e, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "Requisição inválida: parâmetro '" + e.getParameterName() + "' é obrigatório.", req, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integrity(DataIntegrityViolationException e, HttpServletRequest req) {
        String detail = fullMessage(e).toUpperCase(Locale.ROOT);
        log.warn("Data integrity violation on {}: {}", req.getRequestURI(), e.getMostSpecificCause().getMessage());
        if (detail.contains("ORA-00001") || detail.contains("UNIQUE") || detail.contains("23505")) {
            return build(HttpStatus.CONFLICT, MSG_DUPLICADO, req, null);
        }
        if (detail.contains("ORA-02292") || detail.contains("REFERENTIAL") || detail.contains("23503")) {
            return build(HttpStatus.CONFLICT, MSG_DEPENDENTES, req, null);
        }
        return build(HttpStatus.BAD_REQUEST, MSG_RESTRICAO, req, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> noResource(NoResourceFoundException e, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Recurso não encontrado.", req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> methodNotAllowed(HttpRequestMethodNotSupportedException e, HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido para este recurso.", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception e, HttpServletRequest req) {
        log.error("Unexpected error on {} {}", req.getMethod(), req.getRequestURI(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, MSG_INTERNO, req, null);
    }

    private static String fullMessage(Throwable t) {
        StringBuilder sb = new StringBuilder();
        for (Throwable c = t; c != null && sb.length() < 10_000; c = c.getCause()) {
            sb.append(c.getMessage()).append(' ');
            if (c.getCause() == c) {
                break;
            }
        }
        return sb.toString();
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req,
                                           Map<String, String> fields) {
        ApiError body = new ApiError(OffsetDateTime.now(clock).withNano(0), status.value(),
                status.getReasonPhrase(), message, req.getRequestURI(), fields);
        return ResponseEntity.status(status).body(body);
    }
}
