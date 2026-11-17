package br.com.serene;

import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
@RestControllerAdvice
public class ApiErrorHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<?> domain(ApiException ex) { return error(ex.status, ex.getMessage(), Map.of()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
        var errors = new TreeMap<String,String>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "Dados inválidos.", errors);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<?> malformed(Exception ex) { return error(HttpStatus.BAD_REQUEST, "JSON, identificador, data ou parâmetro inválido.", Map.of()); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> integrity(Exception ex) { return error(HttpStatus.CONFLICT, "Conflito: dado duplicado, vínculo existente ou sobreposição de horários.", Map.of()); }
    private ResponseEntity<?> error(HttpStatus status, String message, Map<String,String> errors) {
        return ResponseEntity.status(status).body(Map.of("status", status.value(), "message", message, "errors", errors));
    }
}
