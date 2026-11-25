package es.restaurante.inventario.service;

import es.restaurante.inventario.exception.ValidacionException;

import java.util.Map;

final class ValidacionesServicio {

    private ValidacionesServicio() {
    }

    static String texto(Map<String, String> errores, String campo, String valor, int longitudMaxima,
                        boolean obligatorio) {
        if (valor == null) {
            if (obligatorio) {
                errores.put(campo, "Es obligatorio.");
            }
            return null;
        }

        String normalizado = valor.trim();
        if (normalizado.isEmpty()) {
            if (obligatorio) {
                errores.put(campo, "Es obligatorio.");
            }
            return null;
        }
        if (normalizado.length() > longitudMaxima) {
            errores.put(campo, "No puede superar los " + longitudMaxima + " caracteres.");
        }
        return normalizado;
    }

    static void lanzarSiHayErrores(Map<String, String> errores) {
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }
}