SET NAMES utf8mb4;

CREATE TABLE categoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
) CHARACTER SET=utf8mb4 ENGINE=InnoDB;

CREATE TABLE proveedor (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    cif VARCHAR(9) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    email VARCHAR(120) UNIQUE
) CHARACTER SET=utf8mb4 ENGINE=InnoDB;

CREATE TABLE producto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    categoria_id INT NOT NULL,
    unidad_medida VARCHAR(10) NOT NULL,
    stock_actual DECIMAL(10,2) NOT NULL DEFAULT 0,
    stock_minimo DECIMAL(10,2) NOT NULL DEFAULT 0,
    precio_unitario DECIMAL(10,2) NOT NULL,
    fecha_actualizacion DATETIME NOT NULL,
    CONSTRAINT chk_producto_unidad_medida CHECK (unidad_medida IN ('KG', 'L', 'UD')),
    CONSTRAINT chk_producto_stock_actual CHECK (stock_actual >= 0),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id)
        REFERENCES categoria(id) ON DELETE RESTRICT ON UPDATE CASCADE
) CHARACTER SET=utf8mb4 ENGINE=InnoDB;

CREATE TABLE producto_proveedor (
    producto_id INT NOT NULL,
    proveedor_id INT NOT NULL,
    precio_compra DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (producto_id, proveedor_id),
    CONSTRAINT fk_producto_proveedor_producto FOREIGN KEY (producto_id)
        REFERENCES producto(id) ON DELETE CASCADE,
    CONSTRAINT fk_producto_proveedor_proveedor FOREIGN KEY (proveedor_id)
        REFERENCES proveedor(id) ON DELETE CASCADE
) CHARACTER SET=utf8mb4 ENGINE=InnoDB;

CREATE TABLE movimiento_stock (
    id INT AUTO_INCREMENT PRIMARY KEY,
    producto_id INT NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nota VARCHAR(255),
    CONSTRAINT chk_movimiento_tipo CHECK (tipo IN ('ENTRADA', 'SALIDA', 'MERMA')),
    CONSTRAINT chk_movimiento_cantidad CHECK (cantidad > 0),
    CONSTRAINT fk_movimiento_producto FOREIGN KEY (producto_id)
        REFERENCES producto(id) ON DELETE CASCADE
) CHARACTER SET=utf8mb4 ENGINE=InnoDB;

CREATE INDEX idx_producto_categoria ON producto(categoria_id);
CREATE INDEX idx_producto_proveedor_producto ON producto_proveedor(producto_id);
CREATE INDEX idx_producto_proveedor_proveedor ON producto_proveedor(proveedor_id);
CREATE INDEX idx_movimiento_producto ON movimiento_stock(producto_id);

INSERT INTO categoria (id, nombre, descripcion) VALUES
    (1, 'Carnes', 'Carnes frescas para cocina'),
    (2, 'Pescados', 'Pescados y productos del mar'),
    (3, 'Verduras y frutas', 'Productos frescos de huerta y fruta'),
    (4, 'Bebidas', 'Bebidas frías y de servicio'),
    (5, 'Limpieza', 'Productos y material de limpieza');

INSERT INTO proveedor (id, nombre, cif, telefono, email) VALUES
    (1, 'Distribuciones La Huerta', 'B1234567X', '+34 910 123 456', 'pedidos@lahuerta.es'),
    (2, 'Carnes del Mercado', 'A7654321Z', '911 234 567', 'ventas@carnesmercado.es'),
    (3, 'Pescados Costa Azul', 'J1234567A', '+34 912 345 678', 'comercial@costaazul.es'),
    (4, 'Suministros Hosteleros Norte', 'B9876543C', '913 456 789', 'pedidos@hostelerosnorte.es');

INSERT INTO producto (id, nombre, categoria_id, unidad_medida, stock_actual, stock_minimo, precio_unitario, fecha_actualizacion) VALUES
    (1, 'Bistec de ternera', 1, 'KG', 12.00, 15.00, 18.50, CURRENT_TIMESTAMP),
    (2, 'Pollo entero', 1, 'KG', 8.00, 10.00, 6.20, CURRENT_TIMESTAMP),
    (3, 'Cordero', 1, 'KG', 14.00, 8.00, 16.90, CURRENT_TIMESTAMP),
    (4, 'Salmón fresco', 2, 'KG', 1.50, 3.00, 21.00, CURRENT_TIMESTAMP),
    (5, 'Merluza', 2, 'KG', 7.00, 8.00, 13.75, CURRENT_TIMESTAMP),
    (6, 'Tomate', 3, 'KG', 9.00, 10.00, 2.40, CURRENT_TIMESTAMP),
    (7, 'Patata', 3, 'KG', 30.00, 20.00, 1.20, CURRENT_TIMESTAMP),
    (8, 'Lechuga', 3, 'UD', 2.00, 4.00, 1.10, CURRENT_TIMESTAMP),
    (9, 'Manzana', 3, 'KG', 12.00, 8.00, 2.10, CURRENT_TIMESTAMP),
    (10, 'Agua mineral', 4, 'L', 120.00, 50.00, 0.45, CURRENT_TIMESTAMP),
    (11, 'Vino tinto de la casa', 4, 'L', 5.00, 6.00, 4.80, CURRENT_TIMESTAMP),
    (12, 'Zumo de naranja', 4, 'L', 2.00, 3.00, 2.30, CURRENT_TIMESTAMP),
    (13, 'Detergente lavavajillas', 5, 'L', 5.00, 6.00, 3.90, CURRENT_TIMESTAMP),
    (14, 'Desinfectante de superficies', 5, 'L', 2.00, 3.00, 4.25, CURRENT_TIMESTAMP),
    (15, 'Guantes desechables', 5, 'UD', 4.00, 5.00, 0.08, CURRENT_TIMESTAMP);

INSERT INTO producto_proveedor (producto_id, proveedor_id, precio_compra) VALUES
    (1, 2, 15.20),
    (2, 2, 4.80),
    (3, 2, 13.50),
    (4, 3, 17.90),
    (5, 3, 11.40),
    (6, 1, 1.70),
    (7, 1, 0.80),
    (8, 1, 0.75),
    (9, 1, 1.45),
    (10, 4, 0.28),
    (11, 4, 3.20),
    (12, 4, 1.45),
    (13, 4, 2.65),
    (14, 4, 3.10),
    (15, 4, 0.05),
    (6, 4, 1.85),
    (9, 4, 1.60);

INSERT INTO movimiento_stock (id, producto_id, tipo, cantidad, fecha, nota) VALUES
    (1, 1, 'ENTRADA', 20.00, '2026-11-20 08:00:00', 'Reposición semanal'),
    (2, 1, 'SALIDA', 8.00, '2026-11-21 13:15:00', 'Servicio de comidas'),
    (3, 2, 'ENTRADA', 18.00, '2026-11-20 08:10:00', 'Entrega del proveedor'),
    (4, 2, 'SALIDA', 10.00, '2026-11-21 13:20:00', 'Preparación de menú'),
    (5, 3, 'ENTRADA', 14.00, '2026-11-20 08:20:00', 'Entrega del proveedor'),
    (6, 4, 'ENTRADA', 5.00, '2026-11-20 08:30:00', 'Pescado fresco'),
    (7, 4, 'MERMA', 0.50, '2026-11-22 09:00:00', 'Retirada por deterioro'),
    (8, 5, 'ENTRADA', 12.00, '2026-11-20 08:40:00', 'Entrega del proveedor'),
    (9, 5, 'SALIDA', 5.00, '2026-11-21 13:30:00', 'Servicio de comidas'),
    (10, 6, 'ENTRADA', 16.00, '2026-11-20 09:00:00', 'Compra de mercado'),
    (11, 6, 'SALIDA', 7.00, '2026-11-21 13:35:00', 'Preparación de ensaladas'),
    (12, 7, 'ENTRADA', 40.00, '2026-11-20 09:10:00', 'Compra de mercado'),
    (13, 8, 'ENTRADA', 10.00, '2026-11-20 09:20:00', 'Compra de mercado'),
    (14, 8, 'MERMA', 2.00, '2026-11-22 09:15:00', 'Hojas dañadas'),
    (15, 9, 'ENTRADA', 20.00, '2026-11-20 09:30:00', 'Compra de fruta'),
    (16, 10, 'ENTRADA', 120.00, '2026-11-20 10:00:00', 'Reposición de bebidas'),
    (17, 11, 'ENTRADA', 24.00, '2026-11-20 10:10:00', 'Reposición de bebidas'),
    (18, 12, 'ENTRADA', 18.00, '2026-11-20 10:20:00', 'Reposición de bebidas'),
    (19, 13, 'ENTRADA', 12.00, '2026-11-20 10:30:00', 'Reposición de almacén'),
    (20, 15, 'ENTRADA', 10.00, '2026-11-20 10:40:00', 'Reposición de material');