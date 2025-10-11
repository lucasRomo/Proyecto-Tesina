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

    private static final String SELECT_ALL_REGISTROS =
            "SELECT ra.id_RegAct, ra.fecha_modificacion, ra.tabla_afectada, " +
                    "ra.columna_afectada, ra.id_registro_modificado, ra.dato_previo_modificacion, ra.dato_modificado, " +
                    "p.nombre, p.apellido, u.nombre_usuario AS nombre_usuario_login " +
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

    /**
     * Inserta un nuevo registro de actividad usando una conexión provista (para transacciones).
     * @param conn La conexión de la base de datos activa.
     * @throws SQLException Si ocurre un error SQL, se propaga al controlador para el rollback.
     */
    public boolean insertarRegistro(int idUsuarioResponsable, String tabla, String columna, int idRegistro, String valorAnterior, String valorNuevo, Connection conn) throws SQLException {
        // Verificar si ya existe un registro para este id_registro_modificado y columna
        String checkSql = "SELECT id_RegAct FROM RegistroActividad WHERE id_registro_modificado = ? AND columna_afectada = ? LIMIT 1";
        int existingIdRegAct = -1;
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, idRegistro);
            checkStmt.setString(2, columna);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    existingIdRegAct = rs.getInt("id_RegAct");
                }
            }
        }

        if (existingIdRegAct > 0) {
            // Si ya existe, actualiza el registro existente usando id_RegAct
            String updateSql = "UPDATE RegistroActividad SET dato_previo_modificacion = ?, dato_modificado = ?, fecha_modificacion = NOW() WHERE id_RegAct = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                System.out.println("Actualizando historial para id_RegAct = " + existingIdRegAct);
                updateStmt.setString(1, valorAnterior);
                updateStmt.setString(2, valorNuevo);
                updateStmt.setInt(3, existingIdRegAct);
                int rowsAffected = updateStmt.executeUpdate();
                System.out.println("Filas afectadas en historial (update): " + rowsAffected + " para id_RegAct = " + existingIdRegAct);
                return rowsAffected > 0;
            }
        } else {
            // Si no existe, inserta un nuevo registro
            String insertSql = "INSERT INTO RegistroActividad (id_usuario_responsable, tabla_afectada, columna_afectada, id_registro_modificado, dato_previo_modificacion, dato_modificado, fecha_modificacion) VALUES (?, ?, ?, ?, ?, ?, NOW())";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                System.out.println("Insertando historial para id_registro_modificado = " + idRegistro + ", columna = " + columna);
                insertStmt.setInt(1, idUsuarioResponsable);
                insertStmt.setString(2, tabla);
                insertStmt.setString(3, columna);
                insertStmt.setInt(4, idRegistro);
                insertStmt.setString(5, valorAnterior);
                insertStmt.setString(6, valorNuevo);
                int rowsAffected = insertStmt.executeUpdate();
                System.out.println("Filas afectadas en historial (insert): " + rowsAffected);
                return rowsAffected > 0;
            }
        }
    }
    // =========================================================================


    /**
     * Inserta un nuevo registro de actividad en la tabla RegistroActividad (Maneja su propia conexión).
     * Ahora reutiliza la lógica del método sobrecargado.
     */
    public void insertarRegistro(
            int idUsuarioResponsable,
            String tablaAfectada,
            String columnaAfectada,
            int idRegistroModificado,
            String datoPrevio,
            String datoModificado) {

        try (Connection con = getConnection()) {
            // Llama a la nueva versión, pasando la conexión que acaba de abrir
            insertarRegistro(idUsuarioResponsable, tablaAfectada, columnaAfectada, idRegistroModificado, datoPrevio, datoModificado, con);

        } catch (SQLException e) {
            System.err.println("❌ ERROR FATAL al insertar registro de actividad en la BD (sin transacción): " + e.getMessage());
            e.printStackTrace();
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
                    // Si no tiene persona asociada, usamos el nombre de usuario de login (ej: "admin")
                    nombreCompleto = nombreUsuarioLogin + " (Admin/Sistema)";
                } else {
                    nombreCompleto = "Usuario del Sistema Desconocido";
                }

                HistorialActividadTableView registro = new HistorialActividadTableView(
                        rs.getInt("id_RegAct"),
                        rs.getTimestamp("fecha_modificacion"),
                        nombreCompleto,
                        rs.getString("tabla_afectada"),
                        rs.getString("columna_afectada"),
                        rs.getInt("id_registro_modificado"),
                        rs.getString("dato_previo_modificacion"),
                        rs.getString("dato_modificado")
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