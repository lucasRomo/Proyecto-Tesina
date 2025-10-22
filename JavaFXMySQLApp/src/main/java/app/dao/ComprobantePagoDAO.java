package app.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object para la tabla comprobantepago.
 * Se encarga de guardar la referencia al archivo PDF generado y de obtener estadísticas.
 */
public class ComprobantePagoDAO {

    // Constantes de conexión (asumiendo que son las mismas que usas en otros DAOs)
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Método auxiliar para la conexión
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el driver JDBC de MySQL.");
            throw new SQLException("Falta el driver de MySQL.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
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
        }
    }

    // ----------------------------------------------------------------------------------
    // NUEVO MÉTODO DE ESTADÍSTICAS - AJUSTADO PARA InformesController
    // ----------------------------------------------------------------------------------

    /**
     * Obtiene la distribución de ingresos agrupada por tipo de pago (Efectivo, Transferencia, etc.)
     * en un rango de fechas. Retorna un Map<Tipo de Pago, Monto Total>.
     * Método requerido por InformesController: getDistribucionPagosPorRango.
     */
    public Map<String, Double> getDistribucionPagosPorRango(LocalDate inicio, LocalDate fin) {
        // Retornamos un Map<String, Double> para la compatibilidad con InformesController
        Map<String, Double> data = new HashMap<>();

        String sql = "SELECT tipo_pago, COALESCE(SUM(monto_pago), 0) AS total_pago " +
                "FROM ComprobantePago WHERE fecha_carga BETWEEN ? AND ? " +
                "GROUP BY tipo_pago";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // inicio.atStartOfDay() y fin.atTime(23, 59, 59) para incluir el día completo
            stmt.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(fin.atTime(23, 59, 59)));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tipoPago = rs.getString("tipo_pago");
                    double totalPago = rs.getDouble("total_pago");

                    // Solo agregamos datos si el total es positivo
                    if (totalPago > 0) {
                        data.put(tipoPago, totalPago);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener distribución de pagos: " + e.getMessage());
            e.printStackTrace();
        }
        return data;
    }
}
