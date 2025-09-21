package app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import app.model.Persona;

public class PersonaDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Método para insertar una persona en la base de datos
    public int insertarPersona(Persona persona, Connection conn) throws SQLException {
        String sql = "INSERT INTO Persona (nombre, apellido, id_tipo_documento, numero_documento, id_direccion, telefono, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int idGenerado = -1;
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, persona.getNombre());
            stmt.setString(2, persona.getApellido());
            stmt.setInt(3, persona.getIdTipoDocumento());
            stmt.setString(4, persona.getNumeroDocumento());
            stmt.setInt(5, persona.getIdDireccion());
            stmt.setString(6, persona.getTelefono());
            stmt.setString(7, persona.getEmail());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    }
                }
            }
        }
        return idGenerado;
    }

    // Método para verificar si un número de documento ya existe
    public boolean verificarSiDocumentoExiste(String numeroDocumento) {
        String sql = "SELECT COUNT(*) FROM Persona WHERE numero_documento = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, numeroDocumento);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean verificarSiMailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM Persona WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // NUEVO MÉTODO (CORREGIDO) PARA VERIFICAR LA EXISTENCIA DE UN DOCUMENTO, EXCLUYENDO A UNA PERSONA
    public boolean verificarSiDocumentoExiste(String numeroDocumento, int idPersonaExcluir) {
        String sql = "SELECT COUNT(*) FROM Persona WHERE numero_documento = ? AND id_persona != ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, numeroDocumento);
            stmt.setInt(2, idPersonaExcluir);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}