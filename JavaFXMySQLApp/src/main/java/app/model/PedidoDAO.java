package app.model;

import app.model.Pedido;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/proyectoTesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public boolean savePedido(Pedido pedido) {
        String sql = "INSERT INTO Pedido (id_cliente, fecha_creacion, fecha_entrega_estimada, fecha_finalizacion, estado, instrucciones, monto_total, monto_entregado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, pedido.getIdCliente());
            stmt.setTimestamp(2, Timestamp.valueOf(pedido.getFechaCreacion()));
            stmt.setTimestamp(3, pedido.getFechaEntregaEstimada() != null ? Timestamp.valueOf(pedido.getFechaEntregaEstimada()) : null);
            stmt.setTimestamp(4, pedido.getFechaFinalizacion() != null ? Timestamp.valueOf(pedido.getFechaFinalizacion()) : null);
            stmt.setString(5, pedido.getEstado());
            stmt.setString(6, pedido.getInstrucciones());
            stmt.setDouble(7, pedido.getMontoTotal());
            stmt.setDouble(8, pedido.getMontoEntregado());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        pedido.setIdPedido(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Pedido> getAllPedidos() {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT * FROM Pedido";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Pedido pedido = new Pedido(
                        rs.getInt("id_pedido"),
                        rs.getInt("id_cliente"),
                        rs.getTimestamp("fecha_creacion").toLocalDateTime(),
                        rs.getTimestamp("fecha_entrega_estimada") != null ? rs.getTimestamp("fecha_entrega_estimada").toLocalDateTime() : null,
                        rs.getTimestamp("fecha_finalizacion") != null ? rs.getTimestamp("fecha_finalizacion").toLocalDateTime() : null,
                        rs.getString("estado"),
                        rs.getString("instrucciones"),
                        rs.getDouble("monto_total"),
                        rs.getDouble("monto_entregado")
                );
                pedidos.add(pedido);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pedidos;
    }

    public boolean updatePedido(Pedido pedido) {
        String sql = "UPDATE Pedido SET id_cliente = ?, fecha_creacion = ?, fecha_entrega_estimada = ?, fecha_finalizacion = ?, estado = ?, instrucciones = ?, monto_total = ?, monto_entregado = ? WHERE id_pedido = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pedido.getIdCliente());
            stmt.setTimestamp(2, Timestamp.valueOf(pedido.getFechaCreacion()));
            stmt.setTimestamp(3, pedido.getFechaEntregaEstimada() != null ? Timestamp.valueOf(pedido.getFechaEntregaEstimada()) : null);
            stmt.setTimestamp(4, pedido.getFechaFinalizacion() != null ? Timestamp.valueOf(pedido.getFechaFinalizacion()) : null);
            stmt.setString(5, pedido.getEstado());
            stmt.setString(6, pedido.getInstrucciones());
            stmt.setDouble(7, pedido.getMontoTotal());
            stmt.setDouble(8, pedido.getMontoEntregado());
            stmt.setInt(9, pedido.getIdPedido());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}