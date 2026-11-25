package es.restaurante.inventario.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ValidacionException extends RuntimeException {

    private final Map<String, String> errores;

    public ValidacionException(String mensaje, Map<String, String> errores) {
        super(mensaje);
        this.errores = Collections.unmodifiableMap(new LinkedHashMap<>(errores));
    }

    public ValidacionException(Map<String, String> errores) {
        this("Hay errores de validación", errores);
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}