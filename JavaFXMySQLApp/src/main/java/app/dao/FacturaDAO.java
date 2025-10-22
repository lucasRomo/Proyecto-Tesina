package app.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacturaDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Método auxiliar para la conexión
    private Connection obtenerConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el driver JDBC de MySQL.");
            throw new SQLException("Falta el driver de MySQL.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // ----------------------------------------------------------------------------------
    // 1. MÉTODOS DE MÉTRICAS CLAVE (lblTotalVentas) - RENOMBRADO A *PorRango*
    // ----------------------------------------------------------------------------------

    /**
     * Obtiene el monto total de ventas (Facturas) emitidas en un rango de fechas.
     * Método requerido por InformesController: getTotalVentasPorRango.
     */
    public double getTotalVentasPorRango(LocalDate inicio, LocalDate fin) {
        String sql = "SELECT COALESCE(SUM(monto_total), 0) AS total_ventas " +
                "FROM Factura WHERE fecha_emision BETWEEN ? AND ?";
        double total = 0.0;
        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // inicio.atStartOfDay() y fin.atTime(23, 59, 59) para incluir el día completo
            stmt.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(fin.atTime(23, 59, 59)));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("total_ventas");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener total de ventas: " + e.getMessage());
            // Manejo de excepciones adecuado
        }
        return total;
    }

    /**
     * Obtiene el número total de facturas emitidas en un rango de fechas.
     * Método requerido por InformesController: getTotalFacturasPorRango.
     */
    public int getTotalFacturasPorRango(LocalDate inicio, LocalDate fin) {
        String sql = "SELECT COUNT(id_factura) AS count_facturas " +
                "FROM Factura WHERE fecha_emision BETWEEN ? AND ?";
        int count = 0;
        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(fin.atTime(23, 59, 59)));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt("count_facturas");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener conteo de facturas: " + e.getMessage());
            // Manejo de excepciones adecuado
        }
        return count;
    }

    // ----------------------------------------------------------------------------------
    // 2. MÉTODO DE GRÁFICO DE TENDENCIA (lineChartVentas) - AJUSTADO PARA RETORNAR Map
    // ----------------------------------------------------------------------------------

    /**
     * Obtiene las ventas diarias (Facturas) para el LineChart.
     * Retorna un Map<LocalDate, Double> (Fecha, Monto).
     * Método requerido por InformesController: getVentasDiariasPorRango.
     */
    public Map<LocalDate, Double> getVentasDiariasPorRango(LocalDate inicio, LocalDate fin) {
        // Utilizamos HashMap para el retorno
        Map<LocalDate, Double> data = new HashMap<>();
        String sql = "SELECT DATE(fecha_emision) AS dia, SUM(monto_total) AS monto " +
                "FROM Factura WHERE fecha_emision BETWEEN ? AND ? " +
                "GROUP BY dia ORDER BY dia";

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(fin.atTime(23, 59, 59)));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Mapeamos directamente al HashMap
                    data.put(
                            rs.getDate("dia").toLocalDate(),
                            rs.getDouble("monto")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas diarias: " + e.getMessage());
            // Manejo de excepciones adecuado
        }
        return data;
    }

    // ----------------------------------------------------------------------------------
    // 3. MÉTODO DE MÉTRICA POR EMPLEADO (Mantenido y Renombrado)
    // ----------------------------------------------------------------------------------

    /**
     * Obtiene el conteo de facturas asociadas a un Empleado en un rango de fechas.
     * La relación se establece a través de Pedido y AsignacionPedido.
     * * NOTA: Se RENOMBRA a 'contarFacturasPorEmpleado' para coincidir con el Controller.
     */
    public int contarFacturasPorEmpleado(int idEmpleado, LocalDate inicio, LocalDate fin) {
        String sql = "SELECT COUNT(f.id_factura) AS count_facturas " +
                "FROM Factura f " +
                "JOIN Pedido p ON f.id_pedido = p.id_pedido " +
                "JOIN AsignacionPedido ap ON p.id_pedido = ap.id_pedido " +
                "WHERE ap.id_empleado = ? AND f.fecha_emision BETWEEN ? AND ?";
        int count = 0;
        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmpleado);
            stmt.setTimestamp(2, Timestamp.valueOf(inicio.atStartOfDay()));
            stmt.setTimestamp(3, Timestamp.valueOf(fin.atTime(23, 59, 59)));

            // Log para debug (opcional, pero útil)
            System.out.println("DEBUG FacturaDAO: Ejecutando consulta para empleado ID: " + idEmpleado +
                    " entre " + inicio + " y " + fin);


            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt("count_facturas");
                    System.out.println("DEBUG FacturaDAO: Conteo de facturas: " + count);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener facturas por empleado: " + e.getMessage());
            // Manejo de excepciones adecuado
        }
        return count;
    }
}
