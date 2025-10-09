package app.dao;

import app.model.DetallePedido;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            // Asegurarse de que el driver de MySQL está cargado
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
                    // id_producto puede ser 0 si es anotación (NULL en BD). Usamos getObject para verificar NULL.
                    Integer idProducto = (Integer) rs.getObject("id_producto");
                    String descripcionProducto = rs.getString("descripcion"); // Esta es la descripción manual o el nombre si es producto real
                    int cantidad = rs.getInt("cantidad");
                    double precioUnitario = rs.getDouble("precio_unitario");
                    double subtotal = rs.getDouble("subtotal");

                    DetallePedido detalle = new DetallePedido(
                            idDetallePedido,
                            idPedido,
                            idProducto != null ? idProducto : 0, // 0 si es NULL/anotación
                            descripcionProducto,
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
     * MODIFICACIÓN CLAVE: Elimina todos los detalles antiguos e inserta una nueva lista de detalles.
     * Esto asegura que las anotaciones manuales del controlador reemplacen o complementen
     * la lista anterior al finalizar el pedido.
     *
     * @param idPedido El ID del pedido a modificar.
     * @param nuevosDetalles La lista completa y actualizada de DetallePedido.
     * @return true si la operación (eliminación e inserción) fue exitosa.
     */
    public boolean reemplazarDetalles(int idPedido, List<DetallePedido> nuevosDetalles) {
        // Consultas SQL
        String sqlDelete = "DELETE FROM DetallePedido WHERE id_pedido = ?";
        String sqlInsert = "INSERT INTO DetallePedido (id_pedido, id_producto, descripcion, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = obtenerConexion();
            // Iniciar la transacción para asegurar atomicidad
            conn.setAutoCommit(false);

            // 1. Eliminar todos los detalles existentes para este pedido
            try (PreparedStatement deleteStmt = conn.prepareStatement(sqlDelete)) {
                deleteStmt.setInt(1, idPedido);
                deleteStmt.executeUpdate();
            }

            // 2. Insertar todos los nuevos detalles
            try (PreparedStatement insertStmt = conn.prepareStatement(sqlInsert)) {
                for (DetallePedido detalle : nuevosDetalles) {
                    insertStmt.setInt(1, idPedido);

                    // Si el idProducto es 0 (anotación manual), se establece a NULL en la BD.
                    if (detalle.getIdProducto() == 0) {
                        insertStmt.setNull(2, Types.INTEGER);
                    } else {
                        insertStmt.setInt(2, detalle.getIdProducto());
                    }

                    insertStmt.setString(3, detalle.getDescripcion()); // 'descripcion' es la anotación
                    insertStmt.setInt(4, detalle.getCantidad());
                    insertStmt.setDouble(5, detalle.getPrecioUnitario());
                    detalle.calcularSubtotal(); // Aseguramos el cálculo antes de guardar
                    insertStmt.setDouble(6, detalle.getSubtotal());

                    insertStmt.addBatch(); // Agregar a lote
                }
                insertStmt.executeBatch(); // Ejecutar todas las inserciones
            }

            conn.commit(); // Confirmar la transacción
            return true;

        } catch (SQLException e) {
            System.err.println("Error al reemplazar detalles del pedido ID " + idPedido + ": " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Deshacer en caso de error
                } catch (SQLException ex) {
                    System.err.println("Error en el rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    System.err.println("Error al cerrar la conexión: " + ex.getMessage());
                }
            }
        }
    }


    /**
     * Inserta un nuevo detalle de pedido (anotación o producto).
     * Nota: Este método ya no es el principal para el controlador, ahora se usa reemplazarDetalles.
     * Se mantiene por si se usa en otras partes del sistema.
     * @param detalle El objeto DetallePedido a insertar.
     * @return true si la inserción fue exitosa.
     */
    public boolean saveDetallePedido(DetallePedido detalle) {
        String sql = "INSERT INTO DetallePedido (id_pedido, id_producto, descripcion, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, detalle.getIdPedido());

            // Si el idProducto es 0 (anotación manual), se establece a NULL.
            if (detalle.getIdProducto() == 0) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, detalle.getIdProducto());
            }

            stmt.setString(3, detalle.getDescripcion()); // Usamos descripcion para la columna 'descripcion'
            stmt.setInt(4, detalle.getCantidad());
            stmt.setDouble(5, detalle.getPrecioUnitario());
            stmt.setDouble(6, detalle.getSubtotal());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar nuevo detalle de pedido: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un DetallePedido específico.
     * @param idDetalle El ID del detalle a eliminar.
     * @return true si la eliminación fue exitosa.
     */
    public boolean eliminarDetallePedido(int idDetalle) {
        String sql = "DELETE FROM DetallePedido WHERE id_detalle = ?";

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idDetalle);
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar detalle de pedido ID " + idDetalle + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
