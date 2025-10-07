package app.dao;

import app.model.Pedido;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types; // Importación necesaria para setNull
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para la entidad Pedido.
 * Maneja la persistencia de los objetos Pedido en la base de datos.
 */
public class PedidoDAO {

    // NOTA: Se mantienen las constantes de conexión a MySQL proporcionadas por el usuario.
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Método auxiliar para la conexión
    private Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // ----------------------------------------------------------------------------------
    // MÉTODOS EXISTENTES (ACTUALIZACIÓN de savePedido: Se quita fecha_finalizacion)
    // ----------------------------------------------------------------------------------

    public boolean savePedido(Pedido pedido, String tipoPago) {
        // Se ha quitado 'fecha_finalizacion' de la sentencia SQL para la creación inicial.
        String sqlPedido = "INSERT INTO Pedido (id_cliente, estado, fecha_creacion, fecha_entrega_estimada, instrucciones, monto_total, monto_entregado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlComprobante = "INSERT INTO ComprobantePago (id_pedido, id_cliente, tipo_pago, monto_pago, fecha_carga, estado_verificacion) " +
                "VALUES (?, ?, ?, ?, ?, 'Pendiente')";
        String sqlAsignacion = "INSERT INTO AsignacionPedido (id_pedido, id_empleado, fecha_asignacion) VALUES (?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmtPedido = null;
        PreparedStatement stmtAsignacion = null;
        PreparedStatement stmtComprobante = null;

        try {
            conn = obtenerConexion();
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar en la tabla Pedido
            stmtPedido = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS);

            stmtPedido.setInt(1, pedido.getIdCliente());
            stmtPedido.setString(2, pedido.getEstado());
            stmtPedido.setTimestamp(3, Timestamp.valueOf(pedido.getFechaCreacion()));

            // Manejo de fecha_entrega_estimada (sigue siendo opcional)
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
            } else {
                System.out.println("Advertencia: Pedido creado sin empleado asignado.");
            }

            // 3. Insertar en la tabla ComprobantePago
            if (pedido.getMontoEntregado() > 0 && tipoPago != null && !tipoPago.isEmpty()) {
                stmtComprobante = conn.prepareStatement(sqlComprobante);
                stmtComprobante.setInt(1, idPedidoGenerado);
                stmtComprobante.setInt(2, pedido.getIdCliente());
                stmtComprobante.setString(3, tipoPago);
                stmtComprobante.setDouble(4, pedido.getMontoEntregado());
                stmtComprobante.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

                int affectedRowsComprobante = stmtComprobante.executeUpdate();
                if (affectedRowsComprobante == 0) {
                    conn.rollback();
                    return false;
                }
            } else {
                System.out.println("Advertencia: No se creó ComprobantePago. Monto entregado es 0 o Tipo Pago vacío.");
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
                if (stmtComprobante != null) stmtComprobante.close();
                if (stmtAsignacion != null) stmtAsignacion.close();
                if (stmtPedido != null) stmtPedido.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

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

    /**
     * Obtiene una lista de todos los tipos de pago únicos registrados en los comprobantes.
     */
    public List<String> getTiposPago() {
        List<String> tiposPago = new ArrayList<>();
        String sql = "SELECT DISTINCT tipo_pago FROM ComprobantePago ORDER BY tipo_pago";

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tiposPago.add(rs.getString("tipo_pago"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return tiposPago;
    }


    // ----------------------------------------------------------------------------------
    // MÉTODO DE FILTRADO CLAVE: Actualizado para excluir pedidos 'Retirado'
    // ----------------------------------------------------------------------------------

    /**
     * Obtiene una lista de todos los pedidos ACTIVOS (estado <> 'Retirado'),
     * con la opción de filtrar por Empleado. Este método se usa en VerPedidosController.
     * @param idEmpleado El ID del empleado asignado. Si es 0 o negativo, trae todos.
     * @return Lista de objetos Pedido.
     */
    public List<Pedido> getPedidosPorEmpleado(int idEmpleado) {
        List<Pedido> pedidos = new ArrayList<>();

        String sql = "SELECT p.id_pedido, p.id_cliente, p.fecha_creacion, p.fecha_entrega_estimada, p.fecha_finalizacion, " +
                "p.estado, p.instrucciones, p.monto_total, p.monto_entregado, " +
                "cp.tipo_pago, " +
                "c.nombre AS nombre_cliente, c.apellido AS apellido_cliente, " +
                "ap.id_empleado, pr.nombre AS nombre_empleado, pr.apellido AS apellido_empleado " +
                "FROM Pedido p " +
                "LEFT JOIN Cliente cl ON p.id_cliente = cl.id_cliente " +
                "LEFT JOIN Persona c ON cl.id_persona = c.id_persona " +
                "LEFT JOIN AsignacionPedido ap ON p.id_pedido = ap.id_pedido " +
                "LEFT JOIN Empleado e ON ap.id_empleado = e.id_empleado " +
                "LEFT JOIN Persona pr ON e.id_persona = pr.id_persona " +
                "LEFT JOIN ComprobantePago cp ON p.id_pedido = cp.id_pedido " +
                // Cláusula WHERE OBLIGATORIA: Excluye pedidos Retirados
                "WHERE p.estado <> 'Retirado' " +
                // Cláusula AND CONDICIONAL: Filtra por empleado
                (idEmpleado > 0 ? "AND ap.id_empleado = ? " : "") +
                "GROUP BY p.id_pedido " +
                "ORDER BY p.id_pedido DESC";

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (idEmpleado > 0) {
                // Si hay filtro, se establece el parámetro
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
     * Obtiene una lista de pedidos filtrada por un estado específico.
     * Este método se usará en VerHistorialPedidosController.
     * @param estado El estado por el cual filtrar (ej. "Retirado").
     * @return Lista de objetos Pedido.
     */
    public List<Pedido> getPedidosPorEstado(String estado) {
        List<Pedido> pedidos = new ArrayList<>();

        String sql = "SELECT p.id_pedido, p.id_cliente, p.fecha_creacion, p.fecha_entrega_estimada, p.fecha_finalizacion, " +
                "p.estado, p.instrucciones, p.monto_total, p.monto_entregado, " +
                "cp.tipo_pago, " +
                "c.nombre AS nombre_cliente, c.apellido AS apellido_cliente, " +
                "ap.id_empleado, pr.nombre AS nombre_empleado, pr.apellido AS apellido_empleado " +
                "FROM Pedido p " +
                "LEFT JOIN Cliente cl ON p.id_cliente = cl.id_cliente " +
                "LEFT JOIN Persona c ON cl.id_persona = c.id_persona " +
                "LEFT JOIN AsignacionPedido ap ON p.id_pedido = ap.id_pedido " +
                "LEFT JOIN Empleado e ON ap.id_empleado = e.id_empleado " +
                "LEFT JOIN Persona pr ON e.id_persona = pr.id_persona " +
                "LEFT JOIN ComprobantePago cp ON p.id_pedido = cp.id_pedido " +
                "WHERE p.estado = ? " + // Condición CLAVE: filtrar por estado
                "GROUP BY p.id_pedido " +
                "ORDER BY p.fecha_finalizacion DESC"; // Ordenar por fecha de finalización descendente

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
     * Helper para mapear un ResultSet a un objeto Pedido.
     */
    private Pedido mapResultSetToPedido(ResultSet rs) throws SQLException {
        int idPedido = rs.getInt("id_pedido");
        int idCliente = rs.getInt("id_cliente");
        String nombreCliente = rs.getString("nombre_cliente") + " " + rs.getString("apellido_cliente");

        int idEmpleadoResultado = rs.getInt("id_empleado");
        String nombreEmpleado = "";
        if (rs.wasNull()) {
            idEmpleadoResultado = 0;
            nombreEmpleado = "Sin Asignar";
        } else {
            nombreEmpleado = rs.getString("nombre_empleado") + " " + rs.getString("apellido_empleado");
        }

        String estado = rs.getString("estado");
        String metodoPago = rs.getString("tipo_pago");
        if (metodoPago == null) {
            metodoPago = "N/A";
        }

        LocalDateTime fechaCreacion = rs.getTimestamp("fecha_creacion").toLocalDateTime();

        Timestamp fechaEntregaEstimadaTimestamp = rs.getTimestamp("fecha_entrega_estimada");
        LocalDateTime fechaEntregaEstimada = (fechaEntregaEstimadaTimestamp != null) ? fechaEntregaEstimadaTimestamp.toLocalDateTime() : null;

        Timestamp fechaFinalizacionTimestamp = rs.getTimestamp("fecha_finalizacion");
        // fecha_finalizacion debe poder ser nula si el pedido no ha sido retirado
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
                metodoPago,
                fechaCreacion,
                fechaEntregaEstimada,
                fechaFinalizacion,
                instrucciones,
                montoTotal,
                montoEntregado
        );
    }

    /**
     * Mantiene el método para compatibilidad, pero llama al nuevo método con filtro 0.
     */
    public List<Pedido> getAllPedidos() {
        // NOTA: Este método está obsoleto ya que getPedidosPorEmpleado(0) hace lo mismo
        // pero se mantiene por si alguna otra parte del código lo usa.
        return getPedidosPorEmpleado(0);
    }

    // ----------------------------------------------------------------------------------
    // MÉTODO DE MODIFICACIÓN EXISTENTE (Maneja la actualización de fecha_finalizacion)
    // ----------------------------------------------------------------------------------

    public boolean modificarPedido(Pedido pedido) {
        String sql = "UPDATE Pedido SET id_cliente = ?, estado = ?, fecha_entrega_estimada = ?, fecha_finalizacion = ?, instrucciones = ?, monto_total = ?, monto_entregado = ? WHERE id_pedido = ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pedido.getIdCliente());
            stmt.setString(2, pedido.getEstado());

            // Manejo de fecha_entrega_estimada (puede ser null)
            if (pedido.getFechaEntregaEstimada() != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(pedido.getFechaEntregaEstimada()));
            } else {
                stmt.setNull(3, java.sql.Types.TIMESTAMP);
            }

            // Manejo de fecha_finalizacion (puede ser null). Necesario para el estado "Retirado".
            if (pedido.getFechaFinalizacion() != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(pedido.getFechaFinalizacion()));
            } else {
                stmt.setNull(4, java.sql.Types.TIMESTAMP);
            }

            stmt.setString(5, pedido.getInstrucciones());
            stmt.setDouble(6, pedido.getMontoTotal());
            stmt.setDouble(7, pedido.getMontoEntregado());
            stmt.setInt(8, pedido.getIdPedido());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarPedidos(ObservableList<Pedido> pedidos) {
        // Este método aún no tiene implementación.
        return false;
    }
}