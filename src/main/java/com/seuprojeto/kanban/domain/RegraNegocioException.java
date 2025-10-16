// src/main/java/com/seuprojeto/kanban/domain/RegraNegocioException.java
package com.seuprojeto.kanban.domain;

public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String message) { super(message); }
    public RegraNegocioException(String message, Throwable cause) { super(message, cause); }
}
