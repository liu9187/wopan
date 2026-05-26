package cn.com.fanone.wopan.demo.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handle(Exception ex) {
        HttpStatus status = ex instanceof IllegalArgumentException
                ? HttpStatus.BAD_REQUEST
                : ex.getClass().isAnnotationPresent(org.springframework.web.bind.annotation.ResponseStatus.class)
                ? ex.getClass().getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value()
                : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(Map.of("error", ex.getMessage()));
    }
}
