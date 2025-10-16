package com.seuprojeto.kanban.web;
import com.seuprojeto.kanban.service.RegraNegocioException; import org.springframework.http.*; import org.springframework.validation.FieldError; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import java.util.*;
@ControllerAdvice public class ApiExceptionHandler {
  @ExceptionHandler(NoSuchElementException.class) public ResponseEntity<?> notFound(NoSuchElementException ex){ return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage())); }
  @ExceptionHandler(RegraNegocioException.class) public ResponseEntity<?> business(RegraNegocioException ex){ return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("message", ex.getMessage())); }
  @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<?> validation(MethodArgumentNotValidException ex){
    Map<String,Object> body=new LinkedHashMap<>(); body.put("message","Erro de validação"); Map<String,String> fields=new LinkedHashMap<>();
    for(var err: ex.getBindingResult().getAllErrors()){ String field = err instanceof FieldError fe ? fe.getField() : err.getObjectName(); fields.put(field, err.getDefaultMessage()); }
    body.put("fields",fields); return ResponseEntity.badRequest().body(body);
  }
}
