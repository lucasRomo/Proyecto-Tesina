package app.dao;

import app.controller.HistorialActividadTableView;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HistorialActividadDAO {

    // --- Configuración de Conexión ---
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    /**
     * Establece una conexión con la base de datos.
     *
     * @return Objeto Connection.
     * @throws SQLException Si ocurre un error de conexión.
     */
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: No se encontró el driver JDBC de MySQL.");
            throw new SQLException("Falta el driver JDBC", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // ✅ DESPUÉS:
    private static final String SELECT_ALL_REGISTROS =
            "SELECT ra.id_RegAct, ra.fecha_modificacion, ra.tabla_afectada, " +
                    "ra.columna_afectada, ra.id_registro_modificado, ra.dato_previo_modificacion, " +
                    "ra.dato_modificado, p.nombre, p.apellido, u.nombre_usuario AS nombre_usuario_login " +
                    "FROM RegistroActividad ra " +
                    "JOIN usuario u ON ra.id_usuario_responsable = u.id_usuario " +
                    "LEFT JOIN persona p ON u.id_persona = p.id_persona " +
                    "ORDER BY ra.fecha_modificacion DESC";

    // Consulta para insertar un nuevo registro de actividad.
    private static final String INSERT_REGISTRO =
            "INSERT INTO RegistroActividad (id_usuario_responsable, fecha_modificacion, tabla_afectada, " +
                    "id_registro_modificado, columna_afectada, dato_previo_modificacion, dato_modificado) " +
                    "VALUES (?, NOW(), ?, ?, ?, ?, ?)";

    // -------------------------------------------------------------------------
    // --- MÉTODOS DAO
    // -------------------------------------------------------------------------

    // =========================================================================
    // MODIFICACIÓN #1: Nueva sobrecarga para la transacción (Usa la conexión recibida)
    // =========================================================================


    public boolean insertarRegistro(int idUsuarioResponsable, String tabla, String columna,
                                    int idRegistro, String valorAnterior, String valorNuevo,
                                    Connection conn) throws SQLException {

        String insertSql = "INSERT INTO RegistroActividad (id_usuario_responsable, tabla_afectada, " +
                "columna_afectada, id_registro_modificado, dato_previo_modificacion, " +
                "dato_modificado) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setInt(1, idUsuarioResponsable);      // ← ID DEL USUARIO
            insertStmt.setString(2, tabla);
            insertStmt.setString(3, columna);
            insertStmt.setInt(4, idRegistro);
            insertStmt.setString(5, valorAnterior);
            insertStmt.setString(6, valorNuevo);
            return insertStmt.executeUpdate() > 0;
        }
    }

    public boolean insertarRegistro(int idUsuarioResponsable, String tabla, String columna,
                                    int idRegistro, String valorAnterior, String valorNuevo) {
        try (Connection con = getConnection()) {
            return insertarRegistro(idUsuarioResponsable, tabla, columna, idRegistro,
                    valorAnterior, valorNuevo, con);
        } catch (SQLException e) {
            System.err.println("❌ ERROR al insertar registro: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Obtiene todos los registros de actividad para la tabla de visualización.
     */
    public List<HistorialActividadTableView> obtenerTodosLosRegistros() {
        List<HistorialActividadTableView> registros = new ArrayList<>();

        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_REGISTROS)) {

            while (rs.next()) {
                String nombreCompleto;
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String nombreUsuarioLogin = rs.getString("nombre_usuario_login");

                // Lógica de fallback para el nombre del responsable
                if (nombre != null && apellido != null) {
                    nombreCompleto = nombre + " " + apellido;
                } else if (nombreUsuarioLogin != null && !nombreUsuarioLogin.trim().isEmpty()) {
                    nombreCompleto = nombreUsuarioLogin + " (Admin/Sistema)";
                } else {
                    nombreCompleto = "Usuario del Sistema Desconocido";
                }

                HistorialActividadTableView registro = new HistorialActividadTableView(
                        rs.getInt("id_RegAct"),                    // ← COLUMNA REAL
                        rs.getTimestamp("fecha_modificacion"),     // ← COLUMNA REAL
                        nombreCompleto,                            // ← DEL JOIN
                        rs.getString("tabla_afectada"),            // ← COLUMNA REAL
                        rs.getString("columna_afectada"),          // ← COLUMNA REAL
                        rs.getInt("id_registro_modificado"),       // ← COLUMNA REAL
                        rs.getString("dato_previo_modificacion"),  // ← COLUMNA REAL
                        rs.getString("dato_modificado")            // ← COLUMNA REAL
                );
                registros.add(registro);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener todos los registros de actividad: " + e.getMessage());
            e.printStackTrace();
        }
        return registros;
    }
}