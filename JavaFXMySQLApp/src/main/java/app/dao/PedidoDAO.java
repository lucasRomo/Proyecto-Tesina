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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para la entidad Pedido.
 * Maneja la persistencia de los objetos Pedido en la base de datos.
 */
public class PedidoDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    /**
     * Guarda un nuevo pedido en la tabla Pedido y, si aplica, la asignación en AsignacionPedido.
     * @param pedido El objeto Pedido a guardar.
     * @return true si el pedido y la asignación (si la hay) se guardaron exitosamente, false en caso contrario.
     */
    public boolean savePedido(Pedido pedido) {
        String sqlPedido = "INSERT INTO Pedido (id_cliente, estado, fecha_creacion, fecha_entrega_estimada, fecha_finalizacion, instrucciones, monto_total, monto_entregado, metodo_pago) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmtPedido = null;
        PreparedStatement stmtAsignacion = null;

        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar en la tabla Pedido
            stmtPedido = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS);

            stmtPedido.setInt(1, pedido.getIdCliente());
            stmtPedido.setString(2, pedido.getEstado());
            stmtPedido.setTimestamp(3, Timestamp.valueOf(pedido.getFechaCreacion()));

            // Manejo de fecha_entrega_estimada (puede ser null en la DB)
            if (pedido.getFechaEntregaEstimada() != null) {
                stmtPedido.setTimestamp(4, Timestamp.valueOf(pedido.getFechaEntregaEstimada()));
            } else {
                stmtPedido.setNull(4, java.sql.Types.TIMESTAMP);
            }

            // Manejo de fecha_finalizacion (puede ser null en la DB)
            if (pedido.getFechaFinalizacion() != null) {
                stmtPedido.setTimestamp(5, Timestamp.valueOf(pedido.getFechaFinalizacion()));
            } else {
                stmtPedido.setNull(5, java.sql.Types.TIMESTAMP);
            }

            stmtPedido.setString(6, pedido.getInstrucciones());
            stmtPedido.setDouble(7, pedido.getMontoTotal());
            stmtPedido.setDouble(8, pedido.getMontoEntregado());
            stmtPedido.setString(9, pedido.getMetodoPago());

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

            // 2. Insertar en la tabla AsignacionPedido (SIEMPRE que haya un id_empleado válido)
            if (pedido.getIdEmpleado() > 0) {
                String sqlAsignacion = "INSERT INTO AsignacionPedido (id_pedido, id_empleado, fecha_asignacion) VALUES (?, ?, ?)";
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
                System.out.println("Advertencia: Pedido creado sin empleado asignado. idEmpleado: " + pedido.getIdEmpleado());
            }

            conn.commit(); // Confirmar la transacción
            return true;

        } catch (SQLException e) {
            // Manejo de errores y rollback
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
            // Cerrar recursos
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
     * Recupera todos los pedidos de la base de datos, incluyendo datos del cliente y empleado asignado.
     * @return Una lista de objetos Pedido.
     */
    public List<Pedido> getAllPedidos() {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.id_pedido, p.id_cliente, p.fecha_creacion, p.fecha_entrega_estimada, p.fecha_finalizacion, " +
                "p.estado, p.instrucciones, p.monto_total, p.monto_entregado, p.metodo_pago, " +
                "c.nombre AS nombre_cliente, c.apellido AS apellido_cliente, " +
                "e.id_empleado, pr.nombre AS nombre_empleado, pr.apellido AS apellido_empleado " +
                "FROM Pedido p " +
                "LEFT JOIN Cliente cl ON p.id_cliente = cl.id_cliente " +
                "LEFT JOIN Persona c ON cl.id_persona = c.id_persona " +
                "LEFT JOIN AsignacionPedido ap ON p.id_pedido = ap.id_pedido " +
                "LEFT JOIN Empleado e ON ap.id_empleado = e.id_empleado " +
                "LEFT JOIN Persona pr ON e.id_persona = pr.id_persona";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int idPedido = rs.getInt("id_pedido");
                int idCliente = rs.getInt("id_cliente");
                String nombreCliente = rs.getString("nombre_cliente") + " " + rs.getString("apellido_cliente");

                int idEmpleado = rs.getInt("id_empleado");
                String nombreEmpleado = "";
                if (rs.wasNull()) {
                    idEmpleado = 0;
                    nombreEmpleado = "Sin Asignar";
                } else {
                    nombreEmpleado = rs.getString("nombre_empleado") + " " + rs.getString("apellido_empleado");
                }

                String estado = rs.getString("estado");
                String metodoPago = rs.getString("metodo_pago");

                LocalDateTime fechaCreacion = rs.getTimestamp("fecha_creacion").toLocalDateTime();
                Timestamp fechaEntregaEstimadaTimestamp = rs.getTimestamp("fecha_entrega_estimada");
                LocalDateTime fechaEntregaEstimada = (fechaEntregaEstimadaTimestamp != null) ? fechaEntregaEstimadaTimestamp.toLocalDateTime() : null;
                Timestamp fechaFinalizacionTimestamp = rs.getTimestamp("fecha_finalizacion");
                LocalDateTime fechaFinalizacion = (fechaFinalizacionTimestamp != null) ? fechaFinalizacionTimestamp.toLocalDateTime() : null;

                String instrucciones = rs.getString("instrucciones");
                double montoTotal = rs.getDouble("monto_total");
                double montoEntregado = rs.getDouble("monto_entregado");

                pedidos.add(new Pedido(
                        idPedido,
                        idCliente,
                        nombreCliente,
                        idEmpleado,
                        nombreEmpleado,
                        estado,
                        metodoPago,
                        fechaCreacion,
                        fechaEntregaEstimada,
                        fechaFinalizacion,
                        instrucciones,
                        montoTotal,
                        montoEntregado
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pedidos;
    }

    /**
     * RENOMBRADO: Antes era updatePedido.
     * Actualiza los datos de un pedido en la base de datos (sobrescribe los anteriores).
     * @param pedido El objeto Pedido con los datos actualizados.
     * @return true si el pedido fue modificado exitosamente, false en caso contrario.
     */
    public boolean modificarPedido(Pedido pedido) {
        String sql = "UPDATE Pedido SET id_cliente = ?, estado = ?, fecha_entrega_estimada = ?, fecha_finalizacion = ?, instrucciones = ?, monto_total = ?, monto_entregado = ?, metodo_pago = ? WHERE id_pedido = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pedido.getIdCliente());
            stmt.setString(2, pedido.getEstado());

            // Manejo de fecha_entrega_estimada (puede ser null)
            if (pedido.getFechaEntregaEstimada() != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(pedido.getFechaEntregaEstimada()));
            } else {
                stmt.setNull(3, java.sql.Types.TIMESTAMP);
            }

            // Manejo de fecha_finalizacion (puede ser null)
            if (pedido.getFechaFinalizacion() != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(pedido.getFechaFinalizacion()));
            } else {
                stmt.setNull(4, java.sql.Types.TIMESTAMP);
            }

            stmt.setString(5, pedido.getInstrucciones());
            stmt.setDouble(6, pedido.getMontoTotal());
            stmt.setDouble(7, pedido.getMontoEntregado());

            // Método de pago
            stmt.setString(8, pedido.getMetodoPago());

            stmt.setInt(9, pedido.getIdPedido()); // Cláusula WHERE

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Nota: El método actualizarPedidos no es necesario si utilizas modificarPedido()
    // en el evento onEditCommit de cada celda, que es el enfoque implementado en el Controller.
    public boolean actualizarPedidos(ObservableList<Pedido> pedidos) {
        // Esta implementación solo devuelve false, es mejor usar modificarPedido()
        // en el controlador para guardar fila por fila, como ya lo hicimos.
        return false;
    }
}