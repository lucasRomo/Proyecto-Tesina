package app.dao;

import app.controller.ComprobantesAdminTableView; // Usamos el modelo ComprobanteData que creamos anteriormente
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * Data Access Object (DAO) para la entidad ComprobantePago.
 * Proporciona métodos para interactuar con la tabla ComprobantePago y las tablas relacionadas.
 */
public class ComprobanteDAO {

    // Configuración de la conexión a la BD
    private static final String URL = "jdbc:mysql://localhost:3306/proyectoTesina";
    private static final String USER = "root";
    // *** IMPORTANTE: Reemplaza "" con tu contraseña real de MySQL si tienes una. ***
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
     * Obtiene todos los comprobantes de pago de la base de datos, incluyendo
     * la razón social o el nombre y apellido del cliente para la visualización y búsqueda.
     * @return Una ObservableList de objetos ComprobanteData.
     */
    public ObservableList<ComprobantesAdminTableView> getAllComprobantes() {
        ObservableList<ComprobantesAdminTableView> listaComprobantes = FXCollections.observableArrayList();

        // Consulta SQL para obtener todos los datos del comprobante más el nombre/razón social del cliente.
        // Utilizamos COALESCE para mostrar la razón social si existe, de lo contrario, el nombre completo de la persona.
        String sql = """
            SELECT
                CP.id_comprobante,
                CP.id_pedido,
                CP.id_cliente,
                CP.archivo,
                CP.tipo_pago,
                CP.monto_pago,
                CP.fecha_carga,
                CP.fecha_verificacion,
                CP.estado_verificacion,
                TRIM(CONCAT_WS(' - ',
                                        c.razon_social,
                                        CONCAT(p.nombre, ' ', p.apellido)
                                    )) AS nombre_completo_cliente
            FROM
                ComprobantePago CP
            INNER JOIN
                Cliente C ON CP.id_cliente = C.id_cliente
            INNER JOIN
                Persona P ON C.id_persona = P.id_persona
            ORDER BY
                CP.fecha_carga DESC;
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                long idComprobante = rs.getLong("id_comprobante");
                long idPedido = rs.getLong("id_pedido");
                String tipoPago = rs.getString("tipo_pago");
                double montoPago = rs.getDouble("monto_pago");

                // Conversión de SQL Timestamp a Java LocalDateTime
                Timestamp tsCarga = rs.getTimestamp("fecha_carga");
                LocalDateTime fechaCarga = tsCarga != null ? tsCarga.toLocalDateTime() : null;

                Timestamp tsVerificacion = rs.getTimestamp("fecha_verificacion");
                LocalDateTime fechaVerificacion = tsVerificacion != null ? tsVerificacion.toLocalDateTime() : null;

                String estadoVerificacion = rs.getString("estado_verificacion");
                String nombreCompletoCliente = rs.getString("nombre_completo_cliente");

                ComprobantesAdminTableView comprobante = new ComprobantesAdminTableView(
                        idComprobante,
                        idPedido,
                        tipoPago,
                        montoPago,
                        fechaCarga,
                        fechaVerificacion,
                        estadoVerificacion,
                        nombreCompletoCliente
                );
                listaComprobantes.add(comprobante);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener la lista de comprobantes: " + e.getMessage());
            e.printStackTrace();
        }
        return listaComprobantes;
    }

    /**
     * Actualiza el estado de verificación y la fecha de verificación de un comprobante.
     * @param idComprobante ID del comprobante a actualizar.
     * @param nuevoEstado El nuevo estado de verificación (e.g., "Verificado", "Rechazado").
     * @return true si la actualización fue exitosa.
     */
    public boolean actualizarEstadoVerificacion(long idComprobante, String nuevoEstado) {
        // La fecha de verificación se establece en el momento actual de la actualización
        LocalDateTime ahora = LocalDateTime.now();

        String SQL = "UPDATE ComprobantePago SET estado_verificacion = ?, fecha_verificacion = ? WHERE id_comprobante = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, nuevoEstado);
            stmt.setTimestamp(2, Timestamp.valueOf(ahora));
            stmt.setLong(3, idComprobante);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar el estado de verificación del comprobante #" + idComprobante + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}