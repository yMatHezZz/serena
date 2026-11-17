package br.com.serene;

import org.springframework.http.HttpStatus;
public class ApiException extends RuntimeException {
    public final HttpStatus status;
    private ApiException(HttpStatus status, String message) { super(message); this.status = status; }
    public static ApiException notFound(String message) { return new ApiException(HttpStatus.NOT_FOUND, message); }
    public static ApiException conflict(String message) { return new ApiException(HttpStatus.CONFLICT, message); }
    public static ApiException badRequest(String message) { return new ApiException(HttpStatus.BAD_REQUEST, message); }
}
