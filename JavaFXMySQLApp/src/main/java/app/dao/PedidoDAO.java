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
     * Guarda un nuevo pedido en la tabla Pedido, la asignación en AsignacionPedido y el tipo de pago en ComprobantePago.
     * @param pedido El objeto Pedido a guardar.
     * @param tipoPago El tipo de pago seleccionado (por ejemplo, "Transferencia", "Efectivo").
     * @return true si todas las inserciones fueron exitosas, false en caso contrario.
     */
    public boolean savePedido(Pedido pedido, String tipoPago) {
        // SQL para Pedido (SIN la columna metodo_pago)
        String sqlPedido = "INSERT INTO Pedido (id_cliente, estado, fecha_creacion, fecha_entrega_estimada, fecha_finalizacion, instrucciones, monto_total, monto_entregado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // SQL para ComprobantePago
        // Se asume que el 'monto_entregado' es el primer 'monto_pago'
        String sqlComprobante = "INSERT INTO ComprobantePago (id_pedido, id_cliente, tipo_pago, monto_pago, fecha_carga, estado_verificacion) " +
                "VALUES (?, ?, ?, ?, ?, 'Pendiente')";


        Connection conn = null;
        PreparedStatement stmtPedido = null;
        PreparedStatement stmtAsignacion = null;
        PreparedStatement stmtComprobante = null;

        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar en la tabla Pedido
            stmtPedido = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS);

            stmtPedido.setInt(1, pedido.getIdCliente());
            stmtPedido.setString(2, pedido.getEstado());
            stmtPedido.setTimestamp(3, Timestamp.valueOf(pedido.getFechaCreacion()));

            // Manejo de fechas (puede ser null en la DB)
            if (pedido.getFechaEntregaEstimada() != null) {
                stmtPedido.setTimestamp(4, Timestamp.valueOf(pedido.getFechaEntregaEstimada()));
            } else {
                stmtPedido.setNull(4, java.sql.Types.TIMESTAMP);
            }

            if (pedido.getFechaFinalizacion() != null) {
                stmtPedido.setTimestamp(5, Timestamp.valueOf(pedido.getFechaFinalizacion()));
            } else {
                stmtPedido.setNull(5, java.sql.Types.TIMESTAMP);
            }

            stmtPedido.setString(6, pedido.getInstrucciones());
            stmtPedido.setDouble(7, pedido.getMontoTotal());
            stmtPedido.setDouble(8, pedido.getMontoEntregado());
            // Se quitó la línea para metodo_pago

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
                if (stmtComprobante != null) stmtComprobante.close();
                if (stmtAsignacion != null) stmtAsignacion.close();
                if (stmtPedido != null) stmtPedido.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Obtiene una lista de String con el formato "ID - Nombre Apellido" de los Clientes.
     */
    public List<String> getAllClientesDisplay() {
        List<String> clientes = new ArrayList<>();
        // Seleccionamos el ID del cliente, nombre y apellido de la persona
        String sql = "SELECT cl.id_cliente, p.nombre, p.apellido " +
                "FROM Cliente cl " +
                "JOIN Persona p ON cl.id_persona = p.id_persona " +
                "WHERE cl.estado = 'Activo'"; // Opcional: solo clientes activos

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
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

    /**
     * Obtiene una lista de String con el formato "ID - Nombre Apellido" de los Empleados.
     */
    public List<String> getAllEmpleadosDisplay() {
        List<String> empleados = new ArrayList<>();
        // Seleccionamos el ID del empleado, nombre y apellido de la persona
        String sql = "SELECT e.id_empleado, p.nombre, p.apellido " +
                "FROM Empleado e " +
                "JOIN Persona p ON e.id_persona = p.id_persona " +
                "WHERE e.estado = 'Activo'"; // Opcional: solo empleados activos

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
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
     * Recupera todos los pedidos de la base de datos.
     * NOTA: La columna 'metodo_pago' se ha eliminado del SQL.
     */
    public List<Pedido> getAllPedidos() {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.id_pedido, p.id_cliente, p.fecha_creacion, p.fecha_entrega_estimada, p.fecha_finalizacion, " +
                "p.estado, p.instrucciones, p.monto_total, p.monto_entregado, " +
                "c.nombre AS nombre_cliente, c.apellido AS apellido_cliente, " +
                "e.id_empleado, pr.nombre AS nombre_empleado, pr.apellido AS apellido_empleado " +
                // Se podría añadir un JOIN a ComprobantePago si se quiere mostrar el tipo de pago inicial
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
                String metodoPagoPlaceholder = ""; // Valor de relleno para el constructor

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
                        metodoPagoPlaceholder,
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
     * Actualiza los datos de un pedido en la base de datos (sobrescribe los anteriores).
     * NOTA: Se ha ELIMINADO la actualización del campo 'metodo_pago'.
     */
    public boolean modificarPedido(Pedido pedido) {
        String sql = "UPDATE Pedido SET id_cliente = ?, estado = ?, fecha_entrega_estimada = ?, fecha_finalizacion = ?, instrucciones = ?, monto_total = ?, monto_entregado = ? WHERE id_pedido = ?";
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

            // El índice 8 es ahora la cláusula WHERE
            stmt.setInt(8, pedido.getIdPedido());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarPedidos(ObservableList<Pedido> pedidos) {
        return false;
    }
}