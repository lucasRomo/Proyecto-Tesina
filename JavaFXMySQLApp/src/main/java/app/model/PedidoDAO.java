package app.model;

import app.model.Pedido;
import app.model.Cliente;
import app.model.Empleado;
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
        String sql = "INSERT INTO Pedido (id_cliente, id_empleado, estado, fecha_creacion, fecha_entrega_estimada, instrucciones, monto_total, monto_entregado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pedido.getIdCliente());
            stmt.setInt(2, pedido.getIdEmpleado());
            stmt.setString(3, pedido.getEstado());
            stmt.setTimestamp(4, Timestamp.valueOf(pedido.getFechaCreacion()));
            stmt.setTimestamp(5, Timestamp.valueOf(pedido.getFechaEntregaEstimada()));
            stmt.setString(6, pedido.getInstrucciones());
            stmt.setDouble(7, pedido.getMontoTotal());
            stmt.setDouble(8, pedido.getMontoEntregado());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Pedido> getAllPedidos() {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre AS nombre_cliente, e.nombre AS nombre_empleado " +
                "FROM Pedido p " +
                "LEFT JOIN Cliente c ON p.id_cliente = c.id_cliente " +
                "LEFT JOIN Empleado e ON p.id_empleado = e.id_empleado";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int idPedido = rs.getInt("id_pedido");
                int idCliente = rs.getInt("id_cliente");
                String nombreCliente = rs.getString("nombre_cliente");
                int idEmpleado = rs.getInt("id_empleado");
                String nombreEmpleado = rs.getString("nombre_empleado");
                String estado = rs.getString("estado");
                LocalDateTime fechaCreacion = rs.getTimestamp("fecha_creacion").toLocalDateTime();
                LocalDateTime fechaEntregaEstimada = rs.getTimestamp("fecha_entrega_estimada").toLocalDateTime();
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

    // Nuevo método para actualizar un solo pedido
    public boolean updatePedido(Pedido pedido) {
        String sql = "UPDATE Pedido SET id_cliente = ?, id_empleado = ?, estado = ?, fecha_entrega_estimada = ?, instrucciones = ?, monto_total = ?, monto_entregado = ? WHERE id_pedido = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pedido.getIdCliente());
            stmt.setInt(2, pedido.getIdEmpleado());
            stmt.setString(3, pedido.getEstado());
            stmt.setTimestamp(4, Timestamp.valueOf(pedido.getFechaEntregaEstimada()));
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

    // Nuevo método para actualizar una lista de pedidos
    public boolean actualizarPedidos(ObservableList<Pedido> pedidos) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false); // Inicia la transacción
            String sql = "UPDATE Pedido SET id_cliente = ?, id_empleado = ?, estado = ?, fecha_entrega_estimada = ?, instrucciones = ?, monto_total = ?, monto_entregado = ? WHERE id_pedido = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Pedido pedido : pedidos) {
                    stmt.setInt(1, pedido.getIdCliente());
                    stmt.setInt(2, pedido.getIdEmpleado());
                    stmt.setString(3, pedido.getEstado());
                    stmt.setTimestamp(4, Timestamp.valueOf(pedido.getFechaEntregaEstimada()));
                    stmt.setString(5, pedido.getInstrucciones());
                    stmt.setDouble(6, pedido.getMontoTotal());
                    stmt.setDouble(7, pedido.getMontoEntregado());
                    stmt.setInt(8, pedido.getIdPedido());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                conn.commit(); // Confirma la transacción
                return true;
            } catch (SQLException e) {
                conn.rollback(); // Revierte si hay un error
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
