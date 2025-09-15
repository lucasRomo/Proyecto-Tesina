package app.model;

import app.model.Pedido;
import app.util.DatabaseUtil;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    /**
     * Obtiene todos los pedidos de la base de datos.
     * @return una lista de objetos Pedido.
     */
    public List<Pedido> getAllPedidos() {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT id_pedido, fecha, estado, id_cliente, monto_total FROM Pedido";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int idPedido = rs.getInt("id_pedido");
                LocalDate fecha = rs.getDate("fecha").toLocalDate();
                String estado = rs.getString("estado");
                int idCliente = rs.getInt("id_cliente");
                double montoTotal = rs.getDouble("monto_total");
                pedidos.add(new Pedido(idPedido, fecha, estado, idCliente, montoTotal));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pedidos;
    }
}