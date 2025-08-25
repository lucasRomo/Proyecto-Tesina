package app.model.dao;

import app.model.Persona;
import java.sql.*;

public class PersonaDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public int insertarPersona(Persona persona, Connection conn) throws SQLException {
        String sql = "INSERT INTO Persona (nombre, apellido, id_tipo_documento, numero_documento, id_direccion, telefono, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int idGenerado = -1;

        // Usa la conexión proporcionada en lugar de crear una nueva
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, persona.getNombre());
            stmt.setString(2, persona.getApellido());
            stmt.setInt(3, persona.getIdTipoDocumento());
            stmt.setString(4, persona.getNumeroDocumento());
            stmt.setInt(5, persona.getIdDireccion());
            stmt.setString(6, persona.getTelefono());
            stmt.setString(7, persona.getEmail());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    }
                }
            }
        }
        return idGenerado;

}



    public boolean verificarSiDocumentoExiste(String numeroDocumento) {
        String sql = "SELECT COUNT(*) FROM Persona WHERE numero_documento = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroDocumento);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
