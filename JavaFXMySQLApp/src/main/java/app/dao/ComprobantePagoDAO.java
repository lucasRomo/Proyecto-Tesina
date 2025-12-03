package app.dao;

import java.sql.Connection;
import java.sql.DriverManager; // Importación necesaria para getConnection
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.sql.Statement;
import java.time.LocalDateTime;

/**
 * Data Access Object para la tabla ComprobantePago.
 * Maneja la inserción inicial del pago y la actualización posterior del archivo.
 * Data Access Object para la tabla comprobantepago.
 * Se encarga de guardar la referencia al archivo PDF generado y de obtener estadísticas.
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
     * 1. Actualiza el campo 'archivo' (ruta del PDF/JPG) para un comprobante existente.
     * Este es el método que guarda la ruta del archivo físico.
     * @param idPedido El ID del pedido asociado al comprobante.
     * @param rutaArchivo La ruta absoluta donde se guardó el archivo.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    // En ComprobantePagoDAO.java

    public boolean actualizarRutaComprobante(int idPedido, String rutaArchivo) {
        // Usamos el ID del pedido para encontrar el comprobante que necesita el archivo.
        String sql = "UPDATE ComprobantePago SET archivo = ? WHERE id_pedido = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rutaArchivo);
            ps.setInt(2, idPedido);

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas == 0) {
                // AÑADIDO: Muestra un mensaje si no se encontró el registro.
                System.err.println("ADVERTENCIA: No se encontró el registro de ComprobantePago para el pedido ID: " + idPedido + ". La actualización falló.");
            }

            return filasAfectadas > 0;

        } catch (SQLException e) {
            // AÑADIDO: La traza de error es crucial para saber si es un problema de SQL, conexión o Data Truncation.
            System.err.println("ERROR FATAL: Error al actualizar la ruta del comprobante para el pedido ID: " + idPedido);
            e.printStackTrace();
            return false;
        }
    }

    // ----------------------------------------------------------------------------------
    // NUEVO MÉTODO DE ESTADÍSTICAS - AJUSTADO PARA InformesController
    // ----------------------------------------------------------------------------------

    public Map<String, Integer> getConteoPagosPorRango(LocalDate inicio, LocalDate fin) {
        Map<String, Integer> conteo = new HashMap<>();

        // Consulta SQL con JOIN a Pedido y filtro por estado 'Retirado'
        String sql = "SELECT cp.tipo_pago, COUNT(cp.id_comprobante) AS transacciones " +
                "FROM ComprobantePago cp " +
                "JOIN Pedido p ON cp.id_pedido = p.id_pedido " + // <<-- JOIN CLAVE
                "WHERE cp.fecha_carga BETWEEN ? AND ? " +
                "AND p.estado = 'Retirado' " + // <<-- FILTRO POR PEDIDO COMPLETADO
                "GROUP BY cp.tipo_pago";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // inicio.atStartOfDay() y fin.atTime(23, 59, 59) para incluir el día completo
            stmt.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(fin.atTime(23, 59, 59)));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Solo si hay transacciones
                    conteo.put(rs.getString("tipo_pago"), rs.getInt("transacciones"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener distribución de pagos (conteo): " + e.getMessage());
            e.printStackTrace();
        }
        return conteo;
    }

    public Map<String, Double> getDistribucionPagosTotalCorrecto(LocalDate inicio, LocalDate fin) {
        Map<String, Double> data = new HashMap<>();

        // Consulta que usa una subconsulta (t1) agrupada por id_pedido.
        // Esto asocia el monto total del pedido (p.monto_entregado) a un UNICO tipo_pago por pedido,
        // eliminando la doble sumatoria.
        String sql = "SELECT t1.tipo_pago, SUM(p.monto_entregado) AS total_pago " +
                "FROM Pedido p " +
                "JOIN ( " +
                "    SELECT id_pedido, tipo_pago " +
                "    FROM ComprobantePago " +
                "    GROUP BY id_pedido " + // Agrupa los pagos por pedido para obtener un único tipo_pago por id_pedido
                ") AS t1 ON p.id_pedido = t1.id_pedido " +
                "WHERE p.fecha_finalizacion BETWEEN ? AND ? " + // Se usa la fecha de finalización del pedido para el rango de venta
                "AND p.estado = 'Retirado' " +
                "GROUP BY t1.tipo_pago";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // inicio.atStartOfDay() y fin.atTime(23, 59, 59) para incluir el día completo
            stmt.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(fin.atTime(23, 59, 59)));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tipoPago = rs.getString("tipo_pago");
                    double totalPago = rs.getDouble("total_pago");

                    if (totalPago > 0) {
                        data.put(tipoPago, totalPago);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener distribución de pagos (monto) con corrección: " + e.getMessage());
            e.printStackTrace();
        }
        return data;
    }

}
