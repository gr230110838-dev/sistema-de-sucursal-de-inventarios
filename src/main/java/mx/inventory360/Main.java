package mx.inventory360;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

public class Main extends JFrame {
    private final String host;
    private final String port;
    private final String user;
    private final String password;

    private final JComboBox<String> sucursal = new JComboBox<>(
            new String[]{"Autlán", "El Grullo", "Unión de Tula"}
    );

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Código", "Producto", "Precio", "Existencia"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);

    public Main(String host, String port, String user, String password) {
        super("Sistema de inventario por sucursal");
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;

        construirInterfaz();
        cargarProductos();
    }

    private void construirInterfaz() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel encabezado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        encabezado.add(new JLabel("Selecciona sucursal:"));
        encabezado.add(sucursal);

        JButton actualizar = new JButton("Actualizar");
        encabezado.add(actualizar);
        add(encabezado, BorderLayout.NORTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(26);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton nuevo = new JButton("Registrar producto");
        JButton editar = new JButton("Editar producto");
        JButton validar = new JButton("Validar existencia");
        JButton vender = new JButton("Vender");

        JPanel botones = new JPanel(new FlowLayout());
        botones.add(nuevo);
        botones.add(editar);
        botones.add(validar);
        botones.add(vender);
        add(botones, BorderLayout.SOUTH);

        sucursal.addActionListener(e -> cargarProductos());
        actualizar.addActionListener(e -> cargarProductos());
        nuevo.addActionListener(e -> mostrarFormulario(false));
        editar.addActionListener(e -> mostrarFormulario(true));
        validar.addActionListener(e -> validarExistencia());
        vender.addActionListener(e -> venderProducto());
    }

    private String baseSeleccionada() {
        return switch (sucursal.getSelectedIndex()) {
            case 1 -> "inventory_el_grullo";
            case 2 -> "inventory_union_de_tula";
            default -> "inventory_autlan";
        };
    }

    private Connection conectar() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/"
                + baseSeleccionada() + "?serverTimezone=UTC";
        return DriverManager.getConnection(url, user, password);
    }

    private void cargarProductos() {
        modelo.setRowCount(0);
        String sql = "SELECT id, codigo, nombre, precio, existencia FROM productos ORDER BY nombre";

        try (Connection conexion = conectar();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {

            while (resultado.next()) {
                modelo.addRow(new Object[]{
                        resultado.getInt("id"),
                        resultado.getString("codigo"),
                        resultado.getString("nombre"),
                        resultado.getBigDecimal("precio"),
                        resultado.getInt("existencia")
                });
            }
        } catch (SQLException e) {
            mostrarError("No se pudieron consultar los productos.", e);
        }
    }

    private void mostrarFormulario(boolean editar) {
        int fila = tabla.getSelectedRow();
        if (editar && fila < 0) {
            mensaje("Selecciona un producto de la tabla para editar.");
            return;
        }

        JTextField codigo = new JTextField();
        JTextField nombre = new JTextField();
        JTextField precio = new JTextField();
        JTextField existencia = new JTextField();

        if (editar) {
            codigo.setText(modelo.getValueAt(fila, 1).toString());
            nombre.setText(modelo.getValueAt(fila, 2).toString());
            precio.setText(modelo.getValueAt(fila, 3).toString());
            existencia.setText(modelo.getValueAt(fila, 4).toString());
        } else {
            existencia.setText("0");
        }

        JPanel formulario = new JPanel(new GridLayout(4, 2, 8, 8));
        formulario.add(new JLabel("Código:"));
        formulario.add(codigo);
        formulario.add(new JLabel("Nombre:"));
        formulario.add(nombre);
        formulario.add(new JLabel("Precio:"));
        formulario.add(precio);
        formulario.add(new JLabel("Existencia:"));
        formulario.add(existencia);

        int opcion = JOptionPane.showConfirmDialog(
                this, formulario,
                editar ? "Editar producto" : "Registrar producto",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (opcion != JOptionPane.OK_OPTION) return;

        try {
            String cod = codigo.getText().trim();
            String nom = nombre.getText().trim();
            BigDecimal pre = new BigDecimal(precio.getText().trim());
            int stock = Integer.parseInt(existencia.getText().trim());

            if (cod.isEmpty() || nom.isEmpty()) {
                throw new IllegalArgumentException("Código y nombre son obligatorios.");
            }
            if (pre.compareTo(BigDecimal.ZERO) <= 0 || stock < 0) {
                throw new IllegalArgumentException("Precio positivo y existencia no negativa.");
            }

            if (editar) {
                int id = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
                actualizarProducto(id, cod, nom, pre, stock);
            } else {
                registrarProducto(cod, nom, pre, stock);
            }

            cargarProductos();
            mensaje(editar ? "Producto actualizado." : "Producto registrado.");
        } catch (NumberFormatException e) {
            mensaje("Precio y existencia deben ser numéricos.");
        } catch (IllegalArgumentException | SQLException e) {
            mostrarError("No se pudo guardar el producto.", e);
        }
    }

    private void registrarProducto(String codigo, String nombre,
                                   BigDecimal precio, int existencia)
            throws SQLException {
        String sql = "INSERT INTO productos(codigo,nombre,precio,existencia) VALUES(?,?,?,?)";
        try (Connection conexion = conectar();
             PreparedStatement s = conexion.prepareStatement(sql)) {
            s.setString(1, codigo);
            s.setString(2, nombre);
            s.setBigDecimal(3, precio);
            s.setInt(4, existencia);
            s.executeUpdate();
        }
    }

    private void actualizarProducto(int id, String codigo, String nombre,
                                    BigDecimal precio, int existencia)
            throws SQLException {
        String sql = "UPDATE productos SET codigo=?,nombre=?,precio=?,existencia=? WHERE id=?";
        try (Connection conexion = conectar();
             PreparedStatement s = conexion.prepareStatement(sql)) {
            s.setString(1, codigo);
            s.setString(2, nombre);
            s.setBigDecimal(3, precio);
            s.setInt(4, existencia);
            s.setInt(5, id);
            s.executeUpdate();
        }
    }

    private int idSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mensaje("Selecciona un producto de la tabla.");
            return -1;
        }
        return Integer.parseInt(modelo.getValueAt(fila, 0).toString());
    }

    private void validarExistencia() {
        int id = idSeleccionado();
        if (id < 0) return;

        String entrada = JOptionPane.showInputDialog(this, "Cantidad a validar:");
        if (entrada == null) return;

        try {
            int cantidad = Integer.parseInt(entrada.trim());
            if (cantidad <= 0) throw new NumberFormatException();

            try (Connection conexion = conectar();
                 PreparedStatement s = conexion.prepareStatement(
                         "SELECT existencia FROM productos WHERE id=?")) {
                s.setInt(1, id);
                try (ResultSet r = s.executeQuery()) {
                    if (r.next()) {
                        int stock = r.getInt(1);
                        mensaje(cantidad <= stock
                                ? "Existencia suficiente. Disponibles: " + stock
                                : "Existencia insuficiente. Disponibles: " + stock);
                    }
                }
            }
        } catch (NumberFormatException e) {
            mensaje("Escribe una cantidad entera mayor que cero.");
        } catch (SQLException e) {
            mostrarError("No se pudo validar la existencia.", e);
        }
    }

    private void venderProducto() {
        int fila = tabla.getSelectedRow();
        int id = idSeleccionado();
        if (id < 0) return;

        String entrada = JOptionPane.showInputDialog(this, "Cantidad a vender:");
        if (entrada == null) return;

        try {
            int cantidad = Integer.parseInt(entrada.trim());
            if (cantidad <= 0) throw new NumberFormatException();

            String nombre = modelo.getValueAt(fila, 2).toString();
            String codigo = modelo.getValueAt(fila, 1).toString();
            registrarVenta(id, codigo, nombre, cantidad);
            cargarProductos();
            mensaje("Venta registrada correctamente.");
        } catch (NumberFormatException e) {
            mensaje("La cantidad debe ser un entero mayor que cero.");
        } catch (SQLException | IllegalArgumentException e) {
            mostrarError("No se pudo realizar la venta.", e);
        }
    }

    private void registrarVenta(int id, String codigo, String nombre, int cantidad)
            throws SQLException {
        try (Connection conexion = conectar()) {
            conexion.setAutoCommit(false);
            try {
                BigDecimal precio;
                int stock;

                try (PreparedStatement s = conexion.prepareStatement(
                        "SELECT precio, existencia FROM productos WHERE id=? FOR UPDATE")) {
                    s.setInt(1, id);
                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) throw new SQLException("Producto no encontrado.");
                        precio = r.getBigDecimal("precio");
                        stock = r.getInt("existencia");
                    }
                }

                if (cantidad > stock) {
                    throw new IllegalArgumentException(
                            "Existencia insuficiente. Disponibles: " + stock);
                }

                BigDecimal total = precio.multiply(BigDecimal.valueOf(cantidad));

                try (PreparedStatement s = conexion.prepareStatement(
                        "UPDATE productos SET existencia=existencia-? WHERE id=?")) {
                    s.setInt(1, cantidad);
                    s.setInt(2, id);
                    s.executeUpdate();
                }

                try (PreparedStatement s = conexion.prepareStatement(
                        "INSERT INTO ventas(producto_id,codigo_producto,nombre_producto,cantidad,precio_unitario,total) VALUES(?,?,?,?,?,?)")) {
                    s.setInt(1, id);
                    s.setString(2, codigo);
                    s.setString(3, nombre);
                    s.setInt(4, cantidad);
                    s.setBigDecimal(5, precio);
                    s.setBigDecimal(6, total);
                    s.executeUpdate();
                }

                conexion.commit();
            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    private void mensaje(String texto) {
        JOptionPane.showMessageDialog(this, texto);
    }

    private void mostrarError(String texto, Exception e) {
        JOptionPane.showMessageDialog(this,
                texto + "\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JTextField host = new JTextField("localhost");
            JTextField puerto = new JTextField("3306");
            JTextField usuario = new JTextField("root");
            JPasswordField clave = new JPasswordField();

            JPanel formulario = new JPanel(new GridLayout(4, 2, 8, 8));
            formulario.add(new JLabel("Servidor:"));
            formulario.add(host);
            formulario.add(new JLabel("Puerto:"));
            formulario.add(puerto);
            formulario.add(new JLabel("Usuario MySQL:"));
            formulario.add(usuario);
            formulario.add(new JLabel("Contraseña:"));
            formulario.add(clave);

            int opcion = JOptionPane.showConfirmDialog(
                    null, formulario, "Conexión a MySQL",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (opcion == JOptionPane.OK_OPTION) {
                new Main(host.getText().trim(), puerto.getText().trim(),
                        usuario.getText().trim(), new String(clave.getPassword()))
                        .setVisible(true);
            }
        });
    }
}
