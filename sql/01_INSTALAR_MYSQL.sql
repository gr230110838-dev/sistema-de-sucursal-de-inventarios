CREATE DATABASE IF NOT EXISTS inventory_autlan CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS inventory_el_grullo CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS inventory_union_de_tula CHARACTER SET utf8mb4;

CREATE TABLE IF NOT EXISTS inventory_autlan.productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    existencia INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS inventory_autlan.ventas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    producto_id INT NOT NULL,
    codigo_producto VARCHAR(30) NOT NULL,
    nombre_producto VARCHAR(120) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (producto_id) REFERENCES inventory_autlan.productos(id)
);

CREATE TABLE IF NOT EXISTS inventory_el_grullo.productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    existencia INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS inventory_el_grullo.ventas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    producto_id INT NOT NULL,
    codigo_producto VARCHAR(30) NOT NULL,
    nombre_producto VARCHAR(120) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (producto_id) REFERENCES inventory_el_grullo.productos(id)
);

CREATE TABLE IF NOT EXISTS inventory_union_de_tula.productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    existencia INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS inventory_union_de_tula.ventas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    producto_id INT NOT NULL,
    codigo_producto VARCHAR(30) NOT NULL,
    nombre_producto VARCHAR(120) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (producto_id) REFERENCES inventory_union_de_tula.productos(id)
);

INSERT IGNORE INTO inventory_autlan.productos(codigo,nombre,precio,existencia) VALUES
('P001','Laptop',12500.00,3),
('P002','Teclado',450.00,10);

INSERT IGNORE INTO inventory_el_grullo.productos(codigo,nombre,precio,existencia) VALUES
('P001','Laptop',12500.00,5),
('P004','Monitor',3200.00,8);

INSERT IGNORE INTO inventory_union_de_tula.productos(codigo,nombre,precio,existencia) VALUES
('P001','Laptop',12500.00,8),
('P006','Memoria USB',180.00,4);
