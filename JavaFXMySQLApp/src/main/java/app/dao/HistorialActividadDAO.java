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
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Consulta para obtener el historial completo.
    private static final String SELECT_ALL_REGISTROS =
            "SELECT ra.id_RegAct, ra.fecha_modificacion, ra.tabla_afectada, " +
                    "ra.columna_afectada, ra.id_registro_modificado, ra.dato_previo_modificacion, ra.dato_modificado, " +
                    "p.nombre, p.apellido " +
                    "FROM RegistroActividad ra " +
                    "JOIN usuario u ON ra.id_usuario_responsable = u.id_usuario " +
                    "JOIN persona p ON u.id_persona = p.id_persona " +
                    "ORDER BY ra.fecha_modificacion DESC";

    // Consulta para insertar un nuevo registro de actividad.
    private static final String INSERT_REGISTRO =
            "INSERT INTO RegistroActividad (id_usuario_responsable, fecha_modificacion, tabla_afectada, " +
                    "id_registro_modificado, columna_afectada, dato_previo_modificacion, dato_modificado) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)"; // 7 parámetros

    // -------------------------------------------------------------------------
    // --- MÉTODOS DAO
    // -------------------------------------------------------------------------

    /**
     * Inserta un nuevo registro de actividad en la tabla RegistroActividad.
     */
    public void insertarRegistro(
            int idUsuarioResponsable,
            String tablaAfectada,
            String columnaAfectada,
            int idRegistroModificado,
            String datoPrevio,
            String datoModificado) {

        // El try-with-resources asegura que el PreparedStatement se cierre automáticamente
        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(INSERT_REGISTRO)) {

            pstmt.setInt(1, idUsuarioResponsable);
            pstmt.setString(2, tablaAfectada);
            pstmt.setString(3, columnaAfectada);
            pstmt.setInt(4, idRegistroModificado);
            pstmt.setString(5, datoPrevio);
            pstmt.setString(6, datoModificado);

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("✅ Éxito: Registro de actividad insertado correctamente.");
            } else {
                System.err.println("❌ Advertencia: La inserción del registro de actividad no afectó ninguna fila.");
            }

        } catch (SQLException e) {
            // MUY IMPORTANTE: Imprimir la excepción completa si la inserción falla
            System.err.println("❌ ERROR FATAL al insertar registro de actividad en la BD: " + e.getMessage());
            e.printStackTrace();
            // Aquí puedes ver si hay un problema de clave foránea, tipo de dato, etc.
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
                String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");

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