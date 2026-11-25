package es.restaurante.inventario.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

@WebListener
public class AppContextListener implements ServletContextListener {

    public static final String DATA_SOURCE_ATTRIBUTE = "inventario.dataSource";

    private static final int MAXIMO_INTENTOS = 5;
    private static final long ESPERA_ENTRE_INTENTOS_MS = 3_000;

    private final DataSourceProvider dataSourceProvider;
    private DataSource dataSource;

    public AppContextListener() {
        this(new DataSourceProvider());
    }

    public AppContextListener(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = Objects.requireNonNull(dataSourceProvider);
    }

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        dataSource = dataSourceProvider.getDataSource();
        try {
            comprobarConexion(dataSource);
            evento.getServletContext().setAttribute(DATA_SOURCE_ATTRIBUTE, dataSource);
        } catch (SQLException excepcion) {
            cerrarPool();
            throw new IllegalStateException("No se pudo conectar con la base de datos tras " + MAXIMO_INTENTOS + " intentos", excepcion);
        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            cerrarPool();
            throw new IllegalStateException("Se interrumpió la inicialización de la conexión con la base de datos", excepcion);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        evento.getServletContext().removeAttribute(DATA_SOURCE_ATTRIBUTE);
        cerrarPool();
    }

    private static void comprobarConexion(DataSource dataSource) throws SQLException, InterruptedException {
        SQLException ultimoError = null;
        for (int intento = 1; intento <= MAXIMO_INTENTOS; intento++) {
            try (Connection conexion = dataSource.getConnection()) {
                if (conexion.isValid(2)) {
                    return;
                }
                ultimoError = new SQLException("La conexión obtenida no es válida");
            } catch (SQLException excepcion) {
                ultimoError = excepcion;
            }

            if (intento < MAXIMO_INTENTOS) {
                Thread.sleep(ESPERA_ENTRE_INTENTOS_MS);
            }
        }
        throw new SQLException("La base de datos no respondió", ultimoError);
    }

    private void cerrarPool() {
        if (dataSource instanceof HikariDataSource pool) {
            pool.close();
        }
    }
}