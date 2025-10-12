package app.dao;

import app.model.Pedido;
import app.model.DetallePedido;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para la entidad Pedido.
 * Se ha ajustado para:
 * 1. ELIMINAR TODA REFERENCIA a 'metodo_pago' de la tabla Pedido.
 * 2. Obtener el 'tipo_pago' y la 'rutaComprobante' de la tabla 'ComprobantePago' mediante un JOIN en las consultas SELECT.
 */
public class PedidoDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Método auxiliar para la conexión
    private Connection obtenerConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el driver JDBC de MySQL.");
            throw new SQLException("Falta el driver de MySQL.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // ----------------------------------------------------------------------------------
    // MÉTODOS DE CREACIÓN Y MODIFICACIÓN (Mantener sin cambios)
    // ----------------------------------------------------------------------------------

    /**
     * Guarda un nuevo Pedido. La información de pago se maneja EXCLUSIVAMENTE
     * en la tabla ComprobantePago en otro proceso.
     * @param pedido El objeto Pedido a guardar.
     * @return true si la operación fue exitosa.
     */
    public boolean savePedido(Pedido pedido, String tipoPago) {
        // ELIMINADA la columna metodo_pago del INSERT de la tabla Pedido
        String sqlPedido = "INSERT INTO Pedido (id_cliente, estado, fecha_creacion, fecha_entrega_estimada, instrucciones, monto_total, monto_entregado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlAsignacion = "INSERT INTO AsignacionPedido (id_pedido, id_empleado, fecha_asignacion) VALUES (?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmtPedido = null;
        PreparedStatement stmtAsignacion = null;

        try {
            conn = obtenerConexion();
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar en la tabla Pedido
            stmtPedido = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS);

            stmtPedido.setInt(1, pedido.getIdCliente());
            stmtPedido.setString(2, pedido.getEstado());
            stmtPedido.setTimestamp(3, Timestamp.valueOf(pedido.getFechaCreacion()));

            // fecha_entrega_estimada
            if (pedido.getFechaEntregaEstimada() != null) {
                stmtPedido.setTimestamp(4, Timestamp.valueOf(pedido.getFechaEntregaEstimada()));
            } else {
                stmtPedido.setNull(4, java.sql.Types.TIMESTAMP);
            }

            stmtPedido.setString(5, pedido.getInstrucciones());
            stmtPedido.setDouble(6, pedido.getMontoTotal());
            stmtPedido.setDouble(7, pedido.getMontoEntregado());

            int affectedRowsPedido = stmtPedido.executeUpdate();

            if (affectedRowsPedido == 0) {
                conn.rollback();
                return false;
            }

            // Obtener el ID del pedido recién insertado
            int idPedidoGenerado = -1;
            try (ResultSet generatedKeys = stmtPedido.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    idPedidoGenerado = generatedKeys.getInt(1);
                    pedido.setIdPedido(idPedidoGenerado);
                } else {
                    conn.rollback();
                    throw new SQLException("Error al obtener el ID generado para el pedido.");
                }
            }

            // 2. Insertar en la tabla AsignacionPedido (solo si hay empleado asignado)
            if (pedido.getIdEmpleado() > 0) {
                stmtAsignacion = conn.prepareStatement(sqlAsignacion);
                stmtAsignacion.setInt(1, idPedidoGenerado);
                stmtAsignacion.setInt(2, pedido.getIdEmpleado());
                stmtAsignacion.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

                int affectedRowsAsignacion = stmtAsignacion.executeUpdate();
                if (affectedRowsAsignacion == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    System.err.println("Transaction is being rolled back due to: " + e.getMessage());
                    conn.rollback();
                } catch (SQLException excep) {
                    System.err.println("Error al hacer rollback: " + excep.getMessage());
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmtAsignacion != null) stmtAsignacion.close();
                if (stmtPedido != null) stmtPedido.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Modifica un pedido existente. ELIMINADO 'metodo_pago' del UPDATE.
     * @param pedido El objeto Pedido con los datos actualizados.
     * @return true si la modificación fue exitosa.
     */
    public boolean modificarPedido(Pedido pedido) {
        // ELIMINADO el campo 'metodo_pago'
        String sql = "UPDATE Pedido SET id_cliente = ?, estado = ?, fecha_entrega_estimada = ?, fecha_finalizacion = ?, instrucciones = ?, monto_total = ?, monto_entregado = ? WHERE id_pedido = ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pedido.getIdCliente());
            stmt.setString(2, pedido.getEstado());

            // 3: fecha_entrega_estimada (puede ser null)
            if (pedido.getFechaEntregaEstimada() != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(pedido.getFechaEntregaEstimada()));
            } else {
                stmt.setNull(3, java.sql.Types.TIMESTAMP);
            }

            // 4: fecha_finalizacion (puede ser null). Es clave para el estado "Retirado".
            if (pedido.getFechaFinalizacion() != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(pedido.getFechaFinalizacion()));
            } else {
                stmt.setNull(4, java.sql.Types.TIMESTAMP);
            }

            stmt.setString(5, pedido.getInstrucciones());
            stmt.setDouble(6, pedido.getMontoTotal());
            stmt.setDouble(7, pedido.getMontoEntregado());

            // 8: WHERE id_pedido
            stmt.setInt(8, pedido.getIdPedido()); // El índice se reduce por la eliminación de metodo_pago

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Modifica únicamente el monto total de un pedido existente.
     * @param idPedido El ID del pedido a modificar.
     * @param nuevoMonto El nuevo monto total del pedido.
     * @return true si la modificación fue exitosa.
     */
    public boolean modificarMontoTotal(int idPedido, double nuevoMonto) {
        String sql = "UPDATE Pedido SET monto_total = ? WHERE id_pedido = ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, nuevoMonto);
            stmt.setInt(2, idPedido);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error al modificar el monto total del pedido " + idPedido + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    // ----------------------------------------------------------------------------------
    // MÉTODOS DE CONSULTA
    // ----------------------------------------------------------------------------------

    /**
     * Obtiene una lista de todos los pedidos ACTIVOS (estado <> 'Retirado'),
     * haciendo JOIN con ComprobantePago para obtener el tipo_pago y la ruta.
     * @param idEmpleado El ID del empleado asignado. Si es 0 o negativo, trae todos.
     * @return Lista de objetos Pedido.
     */
    public List<Pedido> getPedidosPorEmpleado(int idEmpleado) {
        List<Pedido> pedidos = new ArrayList<>();

        String sql = "SELECT p.id_pedido, p.id_cliente, p.fecha_creacion, p.fecha_entrega_estimada, p.fecha_finalizacion, " +
                "p.estado, p.instrucciones, p.monto_total, p.monto_entregado, " +
                "cpr.tipo_pago, cpr.archivo, " + // <-- OBTENEMOS TIPO_PAGO Y EL ARCHIVO (RUTA)
                "clp.nombre AS nombre_cliente, clp.apellido AS apellido_cliente, " +
                "ap.id_empleado, emp.nombre AS nombre_empleado, emp.apellido AS apellido_empleado " +
                "FROM Pedido p " +
                "LEFT JOIN Cliente cl ON p.id_cliente = cl.id_cliente " +
                "LEFT JOIN Persona clp ON cl.id_persona = clp.id_persona " +
                "LEFT JOIN AsignacionPedido ap ON p.id_pedido = ap.id_pedido " +
                "LEFT JOIN Empleado e ON ap.id_empleado = e.id_empleado " +
                "LEFT JOIN Persona emp ON e.id_persona = emp.id_persona " +
                "LEFT JOIN ComprobantePago cpr ON p.id_pedido = cpr.id_pedido " + // <-- JOIN a ComprobantePago
                "WHERE p.estado <> 'Retirado' " +
                (idEmpleado > 0 ? "AND ap.id_empleado = ? " : "") +
                "GROUP BY p.id_pedido " +
                "ORDER BY p.id_pedido DESC";

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (idEmpleado > 0) {
                stmt.setInt(1, idEmpleado);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapResultSetToPedido(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pedidos;
    }

    /**
     * Obtiene una lista de pedidos filtrada por un estado específico,
     * haciendo JOIN con ComprobantePago para obtener el tipo_pago y la ruta.
     * @param estado El estado por el cual filtrar (ej. "Retirado").
     * @return Lista de objetos Pedido.
     */
    public List<Pedido> getPedidosPorEstado(String estado) {
        List<Pedido> pedidos = new ArrayList<>();

        String sql = "SELECT p.id_pedido, p.id_cliente, p.fecha_creacion, p.fecha_entrega_estimada, p.fecha_finalizacion, " +
                "p.estado, p.instrucciones, p.monto_total, p.monto_entregado, " +
                "cpr.tipo_pago, cpr.archivo, " + // <-- OBTENEMOS TIPO_PAGO Y EL ARCHIVO (RUTA)
                "clp.nombre AS nombre_cliente, clp.apellido AS apellido_cliente, " +
                "ap.id_empleado, emp.nombre AS nombre_empleado, emp.apellido AS apellido_empleado " +
                "FROM Pedido p " +
                "LEFT JOIN Cliente cl ON p.id_cliente = cl.id_cliente " +
                "LEFT JOIN Persona clp ON cl.id_persona = clp.id_persona " +
                "LEFT JOIN AsignacionPedido ap ON p.id_pedido = ap.id_pedido " +
                "LEFT JOIN Empleado e ON ap.id_empleado = e.id_empleado " +
                "LEFT JOIN Persona emp ON e.id_persona = emp.id_persona " +
                "LEFT JOIN ComprobantePago cpr ON p.id_pedido = cpr.id_pedido " + // <-- JOIN a ComprobantePago
                "WHERE p.estado = ? " +
                "GROUP BY p.id_pedido " +
                "ORDER BY p.fecha_finalizacion DESC";

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapResultSetToPedido(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pedidos;
    }

    /**
     * Obtiene todos los detalles (productos, cantidad, precio) de un pedido específico.
     * Mapea directamente las columnas de la tabla DetallePedido.
     * @param idPedido El ID del pedido a buscar.
     * @return Una lista de objetos DetallePedido.
     */
    public List<DetallePedido> getDetallesPorPedido(int idPedido) {
        List<DetallePedido> detalles = new ArrayList<>();
        // Consulta que trae todos los campos necesarios directamente de DetallePedido.
        // Se hace un JOIN con Producto para obtener el nombre real.
        String SQL = "SELECT dp.id_detalle, dp.id_pedido, dp.id_producto, dp.cantidad, dp.precio_unitario, dp.subtotal, " +
                "p.nombre AS descripcion_producto " +
                "FROM DetallePedido dp " +
                "JOIN Producto p ON dp.id_producto = p.id_producto " +
                "WHERE dp.id_pedido = ?";

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, idPedido);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DetallePedido detalle = new DetallePedido(
                            rs.getInt("id_detalle"),
                            rs.getInt("id_pedido"),
                            rs.getInt("id_producto"),
                            // Usamos el alias 'descripcion_producto' del JOIN
                            rs.getString("descripcion_producto"),
                            rs.getInt("cantidad"),
                            rs.getDouble("precio_unitario"),
                            rs.getDouble("subtotal")
                    );
                    detalles.add(detalle);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener detalles del pedido con ID " + idPedido + ": " + e.getMessage());
            e.printStackTrace();
        }

        return detalles;
    }


    /**
     * Helper para mapear un ResultSet a un objeto Pedido.
     * Ahora mapea el 'tipo_pago' y la 'rutaComprobante'.
     */
    private Pedido mapResultSetToPedido(ResultSet rs) throws SQLException {

        int idPedido = rs.getInt("id_pedido");
        int idCliente = rs.getInt("id_cliente");
        String nombreCliente = rs.getString("nombre_cliente") + " " + rs.getString("apellido_cliente");

        int idEmpleadoResultado = rs.getInt("id_empleado");
        String nombreEmpleado = "";
        if (rs.wasNull() || idEmpleadoResultado == 0) {
            idEmpleadoResultado = 0;
            nombreEmpleado = "Sin Asignar";
        } else {
            nombreEmpleado = rs.getString("nombre_empleado") + " " + rs.getString("apellido_empleado");
        }

        String estado = rs.getString("estado");

        // Extraer 'tipo_pago' del ResultSet (viene del JOIN a ComprobantePago)
        String tipoPago = rs.getString("tipo_pago");
        if (tipoPago == null || tipoPago.trim().isEmpty()) {
            tipoPago = "N/A";
        }

        // *******************************************************************
        // CAMBIO CLAVE: Extraer 'archivo' del ResultSet (la ruta)
        String rutaComprobante = rs.getString("archivo");
        // *******************************************************************

        LocalDateTime fechaCreacion = rs.getTimestamp("fecha_creacion").toLocalDateTime();

        Timestamp fechaEntregaEstimadaTimestamp = rs.getTimestamp("fecha_entrega_estimada");
        LocalDateTime fechaEntregaEstimada = (fechaEntregaEstimadaTimestamp != null) ? fechaEntregaEstimadaTimestamp.toLocalDateTime() : null;

        Timestamp fechaFinalizacionTimestamp = rs.getTimestamp("fecha_finalizacion");
        LocalDateTime fechaFinalizacion = (fechaFinalizacionTimestamp != null) ? fechaFinalizacionTimestamp.toLocalDateTime() : null;

        String instrucciones = rs.getString("instrucciones");
        double montoTotal = rs.getDouble("monto_total");
        double montoEntregado = rs.getDouble("monto_entregado");

        return new Pedido(
                idPedido,
                idCliente,
                nombreCliente,
                idEmpleadoResultado,
                nombreEmpleado,
                estado,
                tipoPago,
                fechaCreacion,
                fechaEntregaEstimada,
                fechaFinalizacion,
                instrucciones,
                montoTotal,
                montoEntregado,
                rutaComprobante // <--- Aquí se pasa la nueva ruta del comprobante
        );
    }

    /**
     * Obtiene una lista de todos los tipos de pago, ya sea desde la DB o una lista fija.
     */
    public List<String> getTiposPago() {
        // Lista de tipos de pago sugerida para el ComboBox del Comprobante
        List<String> tipos = new ArrayList<>();
        tipos.add("Efectivo");
        tipos.add("Tarjeta Débito");
        tipos.add("Tarjeta Crédito");
        tipos.add("Transferencia");
        return tipos;
    }

    // ----------------------------------------------------------------------------------
    // MÉTODOS DE UTILIDAD
    // ----------------------------------------------------------------------------------

    public List<String> getAllClientesDisplay() {
        // Lógica para obtener clientes. Se mantiene sin cambios.
        List<String> clientes = new ArrayList<>();
        String sql = "SELECT cl.id_cliente, p.nombre, p.apellido " +
                "FROM Cliente cl " +
                "JOIN Persona p ON cl.id_persona = p.id_persona " +
                "WHERE cl.estado = 'Activo'";

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int idCliente = rs.getInt("id_cliente");
                String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                clientes.add(idCliente + " - " + nombreCompleto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clientes;
    }

    public List<String> getAllEmpleadosDisplay() {
        // Lógica para obtener empleados. Se mantiene sin cambios.
        List<String> empleados = new ArrayList<>();
        String sql = "SELECT e.id_empleado, p.nombre, p.apellido " +
                "FROM Empleado e " +
                "JOIN Persona p ON e.id_persona = p.id_persona " +
                "WHERE e.estado = 'Activo'";

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int idEmpleado = rs.getInt("id_empleado");
                String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                empleados.add(idEmpleado + " - " + nombreCompleto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return empleados;
    }

    public List<Pedido> getAllPedidos() {
        return getPedidosPorEmpleado(0);
    }

    public boolean actualizarPedidos(ObservableList<Pedido> pedidos) {
        // Este método aún no tiene implementación.
        return false;
    }
}