package es.restaurante.inventario.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;

public final class DataSourceProvider {

    private static final int TAMANO_MAXIMO_POOL = 10;

    private final DataSource dataSource;

    public DataSourceProvider() {
        this.dataSource = crearDesdeEntorno(System.getenv());
    }

    public DataSourceProvider(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "El DataSource no puede ser null");
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    private static HikariDataSource crearDesdeEntorno(Map<String, String> entorno) {
        String host = variableObligatoria(entorno, "DB_HOST");
        String puerto = variableObligatoria(entorno, "DB_PORT");
        String nombre = variableObligatoria(entorno, "DB_NAME");
        String usuario = variableObligatoria(entorno, "DB_USER");
        String contrasena = variableObligatoria(entorno, "DB_PASSWORD");

        try {
            int numeroPuerto = Integer.parseInt(puerto);
            if (numeroPuerto < 1 || numeroPuerto > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException excepcion) {
            throw new IllegalStateException("La variable de entorno DB_PORT debe ser un puerto entre 1 y 65535", excepcion);
        }

        HikariConfig configuracion = new HikariConfig();
        configuracion.setJdbcUrl("jdbc:mysql://" + host + ":" + puerto + "/" + nombre
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Madrid&characterEncoding=UTF-8");
        configuracion.setUsername(usuario);
        configuracion.setPassword(contrasena);
        configuracion.setDriverClassName("com.mysql.cj.jdbc.Driver");
        configuracion.setMaximumPoolSize(TAMANO_MAXIMO_POOL);
        configuracion.setConnectionTimeout(2_000);
        configuracion.setInitializationFailTimeout(-1);
        return new HikariDataSource(configuracion);
    }

    private static String variableObligatoria(Map<String, String> entorno, String nombre) {
        String valor = entorno.get(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno obligatoria " + nombre);
        }
        return nombre.equals("DB_PASSWORD") ? valor : valor.trim();
    }
}