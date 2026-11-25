package es.restaurante.inventario.config;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class DataSourceProviderTest {

    @Test
    void permiteInyectarUnDataSourceExterno() {
        DataSource externo = mock(DataSource.class);

        DataSourceProvider proveedor = new DataSourceProvider(externo);

        assertSame(externo, proveedor.getDataSource());
    }
}