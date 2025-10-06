package app.dao;

import app.controller.FacturasAdminTableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * Clase de acceso a datos para la entidad Factura.
 * Proporciona métodos para interactuar con la tabla Factura.
 */
public class FacturaDAO {
    // Configuración de la conexión a la BD
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    /**
     * Establece la conexión con la base de datos MySQL.
     * @return Objeto Connection.
     * @throws SQLException Si ocurre un error de conexión a la base de datos.
     */
    private Connection getConnection() throws SQLException {
        // Ajuste de zona horaria para compatibilidad entre Java y MySQL
        return DriverManager.getConnection(URL + "?serverTimezone=America/Argentina/Buenos_Aires", USER, PASSWORD);
    }

    /**
     * Obtiene todas las facturas de la base de datos, incluyendo el nombre del cliente.
     * @return Una ObservableList de objetos FacturasAdminTableView.
     */
    public ObservableList<FacturasAdminTableView> obtenerFacturasAdmin() {
        ObservableList<FacturasAdminTableView> listaFacturas = FXCollections.observableArrayList();

        // Consulta SQL MEJORADA: Incluye JOINs para obtener el nombre del cliente.
        // Asumo que tienes tablas Cliente y Persona para obtener el nombre/razón social.
        String sql = """
            SELECT
                f.id_factura,
                f.id_pedido,
                f.id_cliente,
                f.numero_factura,
                f.fecha_emision,
                f.monto_total,
                f.estado_pago,
                TRIM(CONCAT_WS(' - ',\s
                                        c.razon_social,
                                        CONCAT(p.nombre, ' ', p.apellido)
                                    )) AS nombre_cliente
            FROM
                Factura f
            INNER JOIN
                Cliente c ON f.id_cliente = c.id_cliente
            INNER JOIN
                Persona p ON c.id_persona = p.id_persona
            ORDER BY
                f.fecha_emision DESC;
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int idFactura = rs.getInt("id_factura");
                int idPedido = rs.getInt("id_pedido");
                String numeroFactura = rs.getString("numero_factura");

                // Conversión de Timestamp a LocalDateTime
                Timestamp ts = rs.getTimestamp("fecha_emision");
                LocalDateTime fechaEmision = ts != null ? ts.toLocalDateTime() : null;

                double montoTotal = rs.getDouble("monto_total");
                String estadoPago = rs.getString("estado_pago");

                // NUEVO: Obtener el nombre del cliente
                String nombreCliente = rs.getString("nombre_cliente");

                // Llama al constructor AHORA DE 8 PARÁMETROS del modelo de vista
                FacturasAdminTableView factura = new FacturasAdminTableView(
                        idFactura,
                        idPedido,
                        nombreCliente,
                        numeroFactura,
                        fechaEmision,
                        montoTotal,
                        estadoPago
                );
                listaFacturas.add(factura);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener la lista de facturas para la vista de administración: " + e.getMessage());
            e.printStackTrace();
        }
        return listaFacturas;
    }
}