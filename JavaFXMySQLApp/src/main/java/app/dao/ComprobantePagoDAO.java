package app.dao;

import java.sql.Connection;
import java.sql.DriverManager; // Importación necesaria para getConnection
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

/**
 * Data Access Object para la tabla ComprobantePago.
 * Maneja la inserción inicial del pago y la actualización posterior del archivo.
 */
public class ComprobantePagoDAO {

    // ** 1. CONSTANTES DE CONEXIÓN **
    // Obtenidas del InsumoDAO (asegúrate de que esta configuración es correcta)
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // ** 2. IMPLEMENTACIÓN DE getConnection **
    private Connection getConnection() throws SQLException {
        // Ahora se utiliza el DriverManager para establecer la conexión real.
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * 1. Inserta un nuevo registro de comprobante de pago en la DB.
     * Este método se llama cuando el cliente realiza el pago.
     * @param idPedido El ID del pedido asociado.
     * @param idCliente El ID del cliente que realiza el pago.
     * @param tipoPago El método de pago (ej: "Transferencia").
     * @param montoPago El monto abonado.
     * @return El ID generado del nuevo comprobante o -1 en caso de error.
     */
    public int insertarComprobante(int idPedido, int idCliente, String tipoPago, double montoPago) {
        // El campo 'archivo' queda NULL inicialmente.
        String sql = "INSERT INTO ComprobantePago (id_pedido, id_cliente, tipo_pago, monto_pago, fecha_carga) " +
                "VALUES (?, ?, ?, ?, NOW())";
        int idGenerado = -1;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idPedido);
            ps.setInt(2, idCliente);
            ps.setString(3, tipoPago);
            ps.setDouble(4, montoPago);

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    }
                }
            }
            return idGenerado;

        } catch (SQLException e) {
            System.err.println("Error al insertar el comprobante inicial para el pedido ID: " + idPedido);
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 2. Actualiza el campo 'archivo' (ruta del PDF/JPG) para un comprobante existente.
     * Este es el método que guarda la ruta del archivo físico.
     * @param idPedido El ID del pedido asociado al comprobante.
     * @param rutaArchivo La ruta absoluta donde se guardó el archivo.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarRutaComprobante(int idPedido, String rutaArchivo) {
        // Usamos el ID del pedido para encontrar el comprobante que necesita el archivo.
        String sql = "UPDATE ComprobantePago SET archivo = ? WHERE id_pedido = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rutaArchivo);
            ps.setInt(2, idPedido);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar la ruta del comprobante para el pedido ID: " + idPedido);
            e.printStackTrace();
            return false;
        }
    }
}