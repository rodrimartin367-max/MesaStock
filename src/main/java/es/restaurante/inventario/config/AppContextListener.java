package es.restaurante.inventario.config;

import com.zaxxer.hikari.HikariDataSource;
import es.restaurante.inventario.dao.CategoriaDao;
import es.restaurante.inventario.dao.MovimientoDao;
import es.restaurante.inventario.dao.ProductoDao;
import es.restaurante.inventario.dao.ProductoProveedorDao;
import es.restaurante.inventario.dao.ProveedorDao;
import es.restaurante.inventario.dao.jdbc.CategoriaDaoJdbc;
import es.restaurante.inventario.dao.jdbc.MovimientoDaoJdbc;
import es.restaurante.inventario.dao.jdbc.ProductoDaoJdbc;
import es.restaurante.inventario.dao.jdbc.ProductoProveedorDaoJdbc;
import es.restaurante.inventario.dao.jdbc.ProveedorDaoJdbc;
import es.restaurante.inventario.service.CategoriaService;
import es.restaurante.inventario.service.DashboardService;
import es.restaurante.inventario.service.MovimientoService;
import es.restaurante.inventario.service.ProductoService;
import es.restaurante.inventario.service.ProveedorService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

@WebListener
public class AppContextListener implements ServletContextListener {

    public static final String DATA_SOURCE_ATTRIBUTE = "inventario.dataSource";
    public static final String CATEGORIA_SERVICE_ATTRIBUTE = "inventario.categoriaService";
    public static final String PROVEEDOR_SERVICE_ATTRIBUTE = "inventario.proveedorService";
    public static final String PRODUCTO_SERVICE_ATTRIBUTE = "inventario.productoService";
    public static final String MOVIMIENTO_SERVICE_ATTRIBUTE = "inventario.movimientoService";
    public static final String DASHBOARD_SERVICE_ATTRIBUTE = "inventario.dashboardService";

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
            publicarComponentes(evento.getServletContext());
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
        ServletContext contexto = evento.getServletContext();
        contexto.removeAttribute(DATA_SOURCE_ATTRIBUTE);
        contexto.removeAttribute(CATEGORIA_SERVICE_ATTRIBUTE);
        contexto.removeAttribute(PROVEEDOR_SERVICE_ATTRIBUTE);
        contexto.removeAttribute(PRODUCTO_SERVICE_ATTRIBUTE);
        contexto.removeAttribute(MOVIMIENTO_SERVICE_ATTRIBUTE);
        contexto.removeAttribute(DASHBOARD_SERVICE_ATTRIBUTE);
        cerrarPool();
    }

    private void publicarComponentes(ServletContext contexto) {
        CategoriaDao categoriaDao = new CategoriaDaoJdbc(dataSource);
        ProveedorDao proveedorDao = new ProveedorDaoJdbc(dataSource);
        ProductoDao productoDao = new ProductoDaoJdbc(dataSource);
        ProductoProveedorDao productoProveedorDao = new ProductoProveedorDaoJdbc(dataSource);
        MovimientoDao movimientoDao = new MovimientoDaoJdbc(dataSource);

        contexto.setAttribute(DATA_SOURCE_ATTRIBUTE, dataSource);
        contexto.setAttribute(CATEGORIA_SERVICE_ATTRIBUTE, new CategoriaService(categoriaDao));
        contexto.setAttribute(PROVEEDOR_SERVICE_ATTRIBUTE, new ProveedorService(proveedorDao));
        contexto.setAttribute(PRODUCTO_SERVICE_ATTRIBUTE,
                new ProductoService(productoDao, categoriaDao, proveedorDao, productoProveedorDao));
        contexto.setAttribute(MOVIMIENTO_SERVICE_ATTRIBUTE,
                new MovimientoService(dataSource, productoDao, movimientoDao));
        contexto.setAttribute(DASHBOARD_SERVICE_ATTRIBUTE,
                new DashboardService(productoDao, categoriaDao, proveedorDao));
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