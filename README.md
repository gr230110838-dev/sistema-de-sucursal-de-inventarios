# Sistema de sucursales e inventario

Aplicación de escritorio en Java Swing que administra los inventarios de las sucursales Autlán, El Grullo y Unión de Tula mediante bases de datos MySQL independientes.

## Objetivo

Permitir consultar, registrar, actualizar y vender productos, validar existencias y mantener la independencia del inventario de cada sucursal.

## Requisitos

- Java JDK 17 o superior.
- Apache Maven.
- MySQL 8 o superior.
- MySQL Workbench.

## Instalación

1. Ejecutar `sql/01_INSTALAR_MYSQL.sql` en Workbench.
2. Abrir la carpeta como proyecto Maven.
3. Ejecutar:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="mx.inventory360.Main"
```

4. Introducir host, puerto, usuario y contraseña de MySQL.

## Evidencias

Agregar capturas reales dentro de `evidencias/`:

- `01-bases.png`
- `02-autlan.png`
- `03-el-grullo.png`
- `04-registro.png`
- `05-validacion.png`
- `06-venta.png`
- `07-edicion.png`
- `08-historial.png`

## Conclusión

El proyecto integra Java Swing, JDBC y MySQL. Cada sucursal usa su propia base de datos y las ventas realizan una transacción que valida existencia, descuenta inventario y registra el movimiento.
