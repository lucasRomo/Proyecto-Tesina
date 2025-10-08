package app.dao;

import app.model.HistorialActividadTableView;
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

    // -------------------------------------------------------------------------
    // --- CONSULTAS SQL
    // -------------------------------------------------------------------------

    // Consulta para obtener el historial completo.
    // Realiza un JOIN con Usuario y Persona para mostrar el nombre completo.
    private static final String SELECT_ALL_REGISTROS =
            "SELECT ra.id_RegAct, ra.fecha_modificacion, ra.tabla_afectada, " +
                    "ra.columna_afectada, ra.id_registro_modificado, ra.dato_previo_modificacion, ra.dato_modificado, " +
                    "p.nombre, p.apellido " +
                    "FROM RegistroActividad ra " +
                    "JOIN usuario u ON ra.id_usuario_responsable = u.id_usuario " +
                    "JOIN persona p ON u.id_persona = p.id_persona " +
                    "ORDER BY ra.fecha_modificacion DESC";

    // Consulta para insertar un nuevo registro de actividad.
    // CORREGIDO: Se elimina 'id_RegAct' (es autoincremental) y se añade 'fecha_modificacion'.
    private static final String INSERT_REGISTRO =
            "INSERT INTO RegistroActividad (id_usuario_responsable, fecha_modificacion, tabla_afectada, " +
                    "id_registro_modificado, columna_afectada, dato_previo_modificacion, dato_modificado) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)"; // 7 parámetros

    // -------------------------------------------------------------------------
    // --- MÉTODOS DAO
    // -------------------------------------------------------------------------

    /**
     * Inserta un nuevo registro de actividad en la tabla RegistroActividad.
     * Se debe llamar a este método inmediatamente después de un INSERT/UPDATE exitoso.
     * * @param idUsuario ID del usuario que realizó la acción.
     * @param tablaAfectada Nombre de la tabla modificada (ej: "Usuario").
     * @param columnaAfectada Nombre de la columna modificada (ej: "nombre").
     * @param idRegistroModificado ID del registro dentro de la tabla afectada.
     * @param datoPrevio Valor anterior del campo.
     * @param datoNuevo Valor nuevo del campo.
     * @return true si la inserción fue exitosa.
     */
    public boolean insertarRegistro(int idUsuario, String tablaAfectada, String columnaAfectada,
                                    int idRegistroModificado, String datoPrevio, String datoNuevo) {

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_REGISTRO)) {

            // Obtener la fecha y hora actual para el registro
            Timestamp now = new Timestamp(System.currentTimeMillis());

            // Mapeo de parámetros CORREGIDO (ahora incluye la fecha)
            ps.setInt(1, idUsuario);
            ps.setTimestamp(2, now); // Fecha de la modificación
            ps.setString(3, tablaAfectada);
            ps.setInt(4, idRegistroModificado);
            ps.setString(5, columnaAfectada);
            ps.setString(6, datoPrevio);
            ps.setString(7, datoNuevo);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al insertar registro de actividad: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene todos los registros de actividad para la tabla de visualización.
     * Incluye el nombre completo del usuario mediante un JOIN.
     * @return Una lista de objetos HistorialActividadTableView.
     */
    public List<HistorialActividadTableView> obtenerTodosLosRegistros() {
        List<HistorialActividadTableView> registros = new ArrayList<>();

        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_REGISTROS)) {

            while (rs.next()) {
                // Combina el nombre y el apellido para el campo del modelo
                String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");

                // Mapeo de los resultados al modelo HistorialActividadTableView
                HistorialActividadTableView registro = new HistorialActividadTableView( // Instancia el nuevo modelo
                        rs.getInt("id_RegAct"),
                        rs.getTimestamp("fecha_modificacion"),
                        nombreCompleto, // Nombre completo del empleado
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