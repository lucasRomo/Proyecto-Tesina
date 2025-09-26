package app.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;

public class PedidoDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public boolean savePedido(Pedido pedido) {
        // SQL para insertar en la tabla Pedido (NO INCLUYE id_empleado)
        String sqlPedido = "INSERT INTO Pedido (id_cliente, estado, fecha_creacion, fecha_entrega_estimada, fecha_finalizacion, instrucciones, monto_total, monto_entregado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"; // 8 parámetros para la tabla Pedido

        Connection conn = null;
        PreparedStatement stmtPedido = null;
        PreparedStatement stmtAsignacion = null;

        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar en la tabla Pedido
            stmtPedido = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS); // Para obtener el ID autogenerado

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

            int affectedRowsPedido = stmtPedido.executeUpdate();

            if (affectedRowsPedido == 0) {
                conn.rollback(); // Si no se insertó el pedido, revertir la transacción
                return false;
            }

            // Obtener el ID del pedido recién insertado
            int idPedidoGenerado = -1;
            try (ResultSet generatedKeys = stmtPedido.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    idPedidoGenerado = generatedKeys.getInt(1);
                    pedido.setIdPedido(idPedidoGenerado); // Asignar el ID al objeto Pedido
                } else {
                    conn.rollback();
                    throw new SQLException("Error al obtener el ID generado para el pedido.");
                }
            }

            // 2. Insertar en la tabla AsignacionPedido (SIEMPRE que haya un id_empleado válido)
            if (pedido.getIdEmpleado() > 0) { // Asumimos que 0 o negativo no es un ID válido
                String sqlAsignacion = "INSERT INTO AsignacionPedido (id_pedido, id_empleado, fecha_asignacion) VALUES (?, ?, ?)";
                stmtAsignacion = conn.prepareStatement(sqlAsignacion);
                stmtAsignacion.setInt(1, idPedidoGenerado);
                stmtAsignacion.setInt(2, pedido.getIdEmpleado());
                stmtAsignacion.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now())); // Fecha actual de asignación

                int affectedRowsAsignacion = stmtAsignacion.executeUpdate();
                if (affectedRowsAsignacion == 0) {
                    conn.rollback(); // Si no se pudo asignar, revertir toda la transacción
                    return false;
                }
            } else {
                // Opcional: Si un pedido NO necesita un empleado asignado al crearse,
                // puedes decidir si esto es un error o simplemente no se crea la asignación.
                // Por ahora, si idEmpleado no es válido, no se crea la asignación pero el pedido SÍ se guarda.
                System.out.println("Advertencia: Pedido creado sin empleado asignado. idEmpleado: " + pedido.getIdEmpleado());
            }

            conn.commit(); // Confirmar la transacción si todo fue exitoso
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
            // Cerrar recursos en el bloque finally para asegurar que se cierren siempre
            try {
                if (stmtAsignacion != null) stmtAsignacion.close();
                if (stmtPedido != null) stmtPedido.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // --- getAllPedidos() MODIFICADO ---
    // Ahora hará un JOIN con AsignacionPedido y luego con Empleado/Persona
    // para obtener el nombre del empleado asignado.
    public List<Pedido> getAllPedidos() {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.id_pedido, p.id_cliente, p.fecha_creacion, p.fecha_entrega_estimada, p.fecha_finalizacion, " +
                "p.estado, p.instrucciones, p.monto_total, p.monto_entregado, " +
                "c.nombre AS nombre_cliente, c.apellido AS apellido_cliente, " + // Asumo que Cliente tiene nombre y apellido de Persona
                "e.id_empleado, pr.nombre AS nombre_empleado, pr.apellido AS apellido_empleado " +
                "FROM Pedido p " +
                "LEFT JOIN Cliente cl ON p.id_cliente = cl.id_cliente " +
                "LEFT JOIN Persona c ON cl.id_persona = c.id_persona " + // Obtener nombre/apellido del cliente
                "LEFT JOIN AsignacionPedido ap ON p.id_pedido = ap.id_pedido " + // Un pedido puede tener 0 o 1 asignación principal aquí
                "LEFT JOIN Empleado e ON ap.id_empleado = e.id_empleado " +
                "LEFT JOIN Persona pr ON e.id_persona = pr.id_persona"; // Obtener nombre/apellido del empleado

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int idPedido = rs.getInt("id_pedido");
                int idCliente = rs.getInt("id_cliente");
                String nombreCliente = rs.getString("nombre_cliente") + " " + rs.getString("apellido_cliente");

                // id_empleado puede ser 0 si no hay asignación
                int idEmpleado = rs.getInt("id_empleado");
                String nombreEmpleado = "";
                if (rs.wasNull()) { // Si el LEFT JOIN no encontró empleado asignado
                    idEmpleado = 0; // O un valor que indique "ninguno"
                    nombreEmpleado = "Sin Asignar";
                } else {
                    nombreEmpleado = rs.getString("nombre_empleado") + " " + rs.getString("apellido_empleado");
                }


                String estado = rs.getString("estado");
                LocalDateTime fechaCreacion = rs.getTimestamp("fecha_creacion").toLocalDateTime();
                Timestamp fechaEntregaEstimadaTimestamp = rs.getTimestamp("fecha_entrega_estimada");
                LocalDateTime fechaEntregaEstimada = (fechaEntregaEstimadaTimestamp != null) ? fechaEntregaEstimadaTimestamp.toLocalDateTime() : null;
                Timestamp fechaFinalizacionTimestamp = rs.getTimestamp("fecha_finalizacion");
                LocalDateTime fechaFinalizacion = (fechaFinalizacionTimestamp != null) ? fechaFinalizacionTimestamp.toLocalDateTime() : null;
                String instrucciones = rs.getString("instrucciones");
                double montoTotal = rs.getDouble("monto_total");
                double montoEntregado = rs.getDouble("monto_entregado");

                pedidos.add(new Pedido(idPedido, idCliente, nombreCliente, idEmpleado, nombreEmpleado, estado, fechaCreacion, fechaEntregaEstimada, fechaFinalizacion, instrucciones, montoTotal, montoEntregado));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pedidos;
    }

    // --- Otros métodos (updatePedido, deletePedido, etc.) también necesitarán revisión ---
    // Si cambias un pedido y un empleado, deberías actualizar la tabla AsignacionPedido
    // (insertar nuevo, borrar viejo, o modificar existente si es que un pedido solo tiene una asignación activa).
    // Por simplicidad, no los incluyo en este ejemplo, pero tenlo en cuenta.
    // Aquí está un ejemplo básico de cómo sería un update para Pedido, sin tocar AsignacionPedido:
    public boolean updatePedido(Pedido pedido) {
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
            stmt.setInt(8, pedido.getIdPedido());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // El método actualizarPedidos(ObservableList<Pedido> pedidos) también necesitaría
    // ser revisado para manejar AsignacionPedido si se cambia el empleado.
    // Por ahora, dejo la versión que solo actualiza la tabla Pedido.
    public boolean actualizarPedidos(ObservableList<Pedido> pedidos) {
        // ... (Tu implementación existente, que solo actualiza la tabla Pedido)
        // Deberías añadir lógica para AsignacionPedido aquí si los cambios de empleado se manejan así.
        return false; // Implementar la lógica de actualización para la lista de pedidos
    }
}