package app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import app.model.Persona;

public class PersonaDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static final String SELECT_PERSONA_BY_ID =
            "SELECT id_persona, nombre, apellido, id_tipo_documento, numero_documento, " +
                    "id_direccion, telefono, email " +
                    "FROM Persona WHERE id_persona = ?";

    private static final String UPDATE_PERSONA =
            "UPDATE Persona SET nombre = ?, apellido = ?, id_tipo_documento = ?, numero_documento = ?, " +
                    "id_direccion = ?, telefono = ?, email = ? WHERE id_persona = ?";

    private static final String UPDATE_DOCUMENTO =
            "UPDATE Persona SET id_tipo_documento = ?, numero_documento = ? WHERE id_persona = ?";

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: No se encontró el driver JDBC de MySQL.");
            throw new SQLException("Falta el driver JDBC", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

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

    // Método para verificar si un número de documento ya existe (solo número)
    public boolean verificarSiDocumentoExiste(String numeroDocumento) {
        String sql = "SELECT COUNT(*) FROM Persona WHERE numero_documento = ?";
        try (Connection conn = getConnection();
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
        try (Connection conn = getConnection();
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

    public boolean verificarSiDocumentoExiste(String numeroDocumento, int idPersonaExcluir) {
        String sql = "SELECT COUNT(*) FROM Persona WHERE numero_documento = ? AND id_persona != ?";
        try (Connection conn = getConnection();
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

    // =========================================================================
    // NUEVO MÉTODO DE VALIDACIÓN: Incluye tipo de documento y excluye a la persona actual
    // Este método es el que usará EdicionDocumentoController
    // =========================================================================
    /**
     * Verifica si la combinación de Tipo y Número de Documento ya existe para otra persona.
     */
    public boolean verificarSiDocumentoExisteParaOtro(int idTipoDocumento, String numeroDocumento, int idPersonaExcluir) {
        String sql = "SELECT COUNT(*) FROM Persona WHERE id_tipo_documento = ? AND numero_documento = ? AND id_persona != ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTipoDocumento);
            stmt.setString(2, numeroDocumento);
            stmt.setInt(3, idPersonaExcluir);
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
    // =========================================================================

    public boolean verificarSiMailExisteParaOtro(String email, int idPersonaActual) {
        String sql = "SELECT COUNT(*) FROM Persona WHERE email = ? AND id_persona != ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, idPersonaActual);
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

    public Persona obtenerPersonaPorId(int idPersona, Connection conn) throws SQLException {
        String sql = "SELECT nombre, apellido FROM Persona WHERE id_persona = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPersona);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Persona persona = new Persona();
                    persona.setIdPersona(idPersona);
                    persona.setNombre(rs.getString("nombre"));
                    persona.setApellido(rs.getString("apellido"));
                    return persona;
                }
            }
        }
        return null;
    }

    public boolean modificarPersona(Persona persona, Connection conn) throws SQLException {
        String SQL = UPDATE_PERSONA;

        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, persona.getNombre());
            ps.setString(2, persona.getApellido());
            ps.setInt(3, persona.getIdTipoDocumento());
            ps.setString(4, persona.getNumeroDocumento());
            ps.setInt(5, persona.getIdDireccion());
            ps.setString(6, persona.getTelefono());
            ps.setString(7, persona.getEmail());
            ps.setInt(8, persona.getIdPersona());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("DEBUG: Persona modificada (filas afectadas: " + filasAfectadas + ")");
                return true;
            } else {
                System.out.println("DEBUG: Modificación de Persona no ejecutada (Datos iguales o ID no encontrado).");
                return false;
            }
        }
    }

    public boolean modificarPersona(Persona persona) {
        try (Connection con = getConnection()) {
            return modificarPersona(persona, con);
        } catch (SQLException e) {
            System.err.println("Error al modificar persona: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // NUEVO MÉTODO DE MODIFICACIÓN: Modifica solo Tipo y Número de Documento
    // Este método es el que usará EdicionDocumentoController
    // =========================================================================
    /**
     * Actualiza el tipo y número de documento para una persona específica.
     * @param idPersona ID de la persona a modificar.
     * @param idTipoDocumento Nuevo ID de tipo de documento.
     * @param numeroDocumento Nuevo número de documento.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean modificarDocumento(int idPersona, int idTipoDocumento, String numeroDocumento) {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_DOCUMENTO)) {

            ps.setInt(1, idTipoDocumento);
            ps.setString(2, numeroDocumento);
            ps.setInt(3, idPersona);

            int filasAfectadas = ps.executeUpdate();

            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al modificar el documento de la persona: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // =========================================================================
}