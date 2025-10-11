package app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Data Access Object para la tabla comprobantepago.
 * Se encarga de guardar la referencia al archivo PDF generado.
 * (Debes usar tu clase de utilidad para la conexión a la DB)
 */
public class ComprobantePagoDAO {

    // **NOTA IMPORTANTE:** Reemplaza este método con tu forma real de obtener la conexión a la base de datos.
    private Connection getConnection() throws SQLException {
        // Ejemplo: return TuUtilidadConexion.obtenerConexion();
        throw new UnsupportedOperationException("Implementa tu método de obtención de conexión.");
    }

    /**
     * Actualiza el campo 'archivo' (ruta del PDF) en la tabla 'comprobantepago'.
     * @param idPedido El ID del pedido asociado al comprobante.
     * @param rutaPDF La ruta absoluta donde se guardó el archivo PDF.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarRutaTicket(int idPedido, String rutaPDF) {
        // Asumiendo que el campo para guardar la ruta es 'archivo'
        String sql = "UPDATE comprobantepago SET archivo = ? WHERE id_pedido = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rutaPDF);
            ps.setInt(2, idPedido);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar la ruta del ticket para el pedido ID: " + idPedido);
            e.printStackTrace();
            return false;
        } catch (UnsupportedOperationException e) {
            System.err.println("ERROR DE CONEXIÓN: Por favor, implementa el método getConnection() en ComprobantePagoDAO.");
            return false;
        }
    }
}
