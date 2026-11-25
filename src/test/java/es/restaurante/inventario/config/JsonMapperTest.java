package es.restaurante.inventario.config;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonMapperTest {

    @Test
    void serializaFechaComoTextoIso() throws Exception {
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 25, 19, 1);

        String json = JsonMapper.getInstance().writeValueAsString(fecha);

        assertEquals("\"2026-11-25T19:01:00\"", json);
    }
}