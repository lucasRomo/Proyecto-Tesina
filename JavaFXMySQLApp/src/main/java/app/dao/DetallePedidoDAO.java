package app.dao;

import app.model.DetallePedido;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Types;

/**
 * Clase DAO para manejar las operaciones CRUD relacionadas con la entidad DetallePedido.
 * Permite registrar detalles de pedidos como anotaciones simples, sin depender
 * de la tabla Producto, estableciendo id_producto a NULL.
 */
public class DetallePedidoDAO {

    // NOTA: Se mantienen las constantes de conexión a MySQL proporcionadas por el usuario.
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    /**
     * Método auxiliar para la conexión.
     */
    private Connection obtenerConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el driver JDBC de MySQL.");
            throw new SQLException("Falta el driver de MySQL.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Obtiene todos los detalles asociados a un ID de pedido específico.
     * @param idPedido El ID del pedido cuyos detalles se quieren obtener.
     * @return Una lista de objetos DetallePedido.
     */
    public List<DetallePedido> getDetallesPorPedido(int idPedido) {
        List<DetallePedido> detalles = new ArrayList<>();
        // Consulta adaptada para obtener 'descripcion' que mapea a 'nombreProducto' en el modelo
        String sql = "SELECT dp.id_detalle, dp.id_pedido, dp.id_producto, dp.descripcion, dp.cantidad, dp.precio_unitario, dp.subtotal " +
                "FROM DetallePedido dp " +
                "WHERE dp.id_pedido = ?";

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPedido);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int idDetallePedido = rs.getInt("id_detalle");
                    // id_producto se lee, pero su valor será 0 o NULL (se mantiene para el modelo)
                    int idProducto = rs.getInt("id_producto");
                    String descripcionProducto = rs.getString("descripcion");
                    int cantidad = rs.getInt("cantidad");
                    double precioUnitario = rs.getDouble("precio_unitario");
                    double subtotal = rs.getDouble("subtotal");

                    DetallePedido detalle = new DetallePedido(
                            idDetallePedido,
                            idPedido,
                            idProducto,
                            descripcionProducto, // Mapeado a nombreProducto
                            cantidad,
                            precioUnitario,
                            subtotal
                    );
                    detalles.add(detalle);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener detalles del pedido ID " + idPedido + ": " + e.getMessage());
            e.printStackTrace();
        }
        return detalles;
    }

    /**
     * Modifica la descripción (anotación), cantidad y el precio unitario de un DetallePedido existente.
     * @param detalle El objeto DetallePedido con los datos actualizados.
     * @return true si la modificación fue exitosa, false en caso contrario.
     */
    public boolean modificarDetallePedido(DetallePedido detalle) {
        // La consulta ahora incluye la actualización de la descripción.
        String sql = "UPDATE DetallePedido SET descripcion = ?, cantidad = ?, precio_unitario = ?, subtotal = (? * ?) " +
                "WHERE id_detalle = ?";

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 1. descripcion (anotación)
            stmt.setString(1, detalle.getNombreProducto());
            // 2. cantidad
            stmt.setInt(2, detalle.getCantidad());
            // 3. precio_unitario
            stmt.setDouble(3, detalle.getPrecioUnitario());
            // 4. y 5. Para el subtotal calculado en la BD
            stmt.setInt(4, detalle.getCantidad());
            stmt.setDouble(5, detalle.getPrecioUnitario());
            // 6. WHERE id_detalle
            stmt.setInt(6, detalle.getIdDetallePedido());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al modificar detalle de pedido ID " + detalle.getIdDetallePedido() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inserta un nuevo detalle de pedido como una anotación.
     * El campo id_producto se establece a NULL para asegurar que la entrada no dependa de la tabla Producto.
     * @param detalle El objeto DetallePedido a insertar.
     * @return true si la inserción fue exitosa.
     */
    public boolean saveDetallePedido(DetallePedido detalle) {
        String sql = "INSERT INTO DetallePedido (id_pedido, id_producto, descripcion, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, detalle.getIdPedido());
            // Se fuerza el id_producto a NULL, como fue solicitado.
            stmt.setNull(2, Types.INTEGER);

            stmt.setString(3, detalle.getNombreProducto()); // Usamos nombreProducto para la columna 'descripcion'
            stmt.setInt(4, detalle.getCantidad());
            stmt.setDouble(5, detalle.getPrecioUnitario());
            stmt.setDouble(6, detalle.getSubtotal());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar nuevo detalle de pedido (anotación): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}