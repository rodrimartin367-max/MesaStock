CREATE TABLE categoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE proveedor (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    cif VARCHAR(9) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    email VARCHAR(120) UNIQUE
);

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
);

CREATE TABLE producto_proveedor (
    producto_id INT NOT NULL,
    proveedor_id INT NOT NULL,
    precio_compra DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (producto_id, proveedor_id),
    CONSTRAINT fk_producto_proveedor_producto FOREIGN KEY (producto_id)
        REFERENCES producto(id) ON DELETE CASCADE,
    CONSTRAINT fk_producto_proveedor_proveedor FOREIGN KEY (proveedor_id)
        REFERENCES proveedor(id) ON DELETE CASCADE
);

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
);

CREATE INDEX idx_producto_categoria ON producto(categoria_id);
CREATE INDEX idx_producto_proveedor_producto ON producto_proveedor(producto_id);
CREATE INDEX idx_producto_proveedor_proveedor ON producto_proveedor(proveedor_id);
CREATE INDEX idx_movimiento_producto ON movimiento_stock(producto_id);