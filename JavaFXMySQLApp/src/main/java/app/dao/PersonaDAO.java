package app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import app.model.Persona;

import static java.sql.DriverManager.getConnection;

public class PersonaDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static final String SELECT_PERSONA_BY_ID =
            "SELECT id_persona, nombre, apellido, id_tipo_documento, numero_documento, " +
                    "id_direccion, telefono, email " + // Incluye salario si existe en esta tabla
                    "FROM Persona WHERE id_persona = ?";

    // Query para modificar la persona (asumo que se usa en tu controlador de empleados/usuarios)
    private static final String UPDATE_PERSONA =
            "UPDATE Persona SET nombre = ?, apellido = ?, id_tipo_documento = ?, numero_documento = ?, " +
                    "id_direccion = ?, telefono = ?, email = ? WHERE id_persona = ?";


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
        try (Connection conn = getConnection(URL, USER, PASSWORD);
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
        try (Connection conn = getConnection(URL, USER, PASSWORD);
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
        try (Connection conn = getConnection(URL, USER, PASSWORD);
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

    // Este método debe ser implementado en tu PersonaDAO.java
    public boolean verificarSiMailExisteParaOtro(String email, int idPersonaActual) {
        String sql = "SELECT COUNT(*) FROM Persona WHERE email = ? AND id_persona != ?";
        try (Connection conn = getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, idPersonaActual); // Excluye a la persona que estamos editando
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true; // Existe otra persona con ese email
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- MÉTODOS NECESARIOS PARA EL HISTORIAL DE ACTIVIDAD ---

    public Persona getPersonaById(int idPersona) {
        Persona persona = null;

        try (Connection conn = getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(SELECT_PERSONA_BY_ID)) {

            stmt.setInt(1, idPersona);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    persona = new Persona();

                    persona.setIdPersona(rs.getInt("id_persona"));
                    persona.setNombre(rs.getString("nombre"));
                    persona.setApellido(rs.getString("apellido"));
                    persona.setIdTipoDocumento(rs.getInt("id_tipo_documento"));
                    persona.setNumeroDocumento(rs.getString("numero_documento"));
                    persona.setIdDireccion(rs.getInt("id_direccion"));
                    persona.setTelefono(rs.getString("telefono"));
                    persona.setEmail(rs.getString("email"));

                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener persona por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return persona;
    }

    /**
     * Modifica los datos de una persona.
     * @param persona El objeto Persona con los datos actualizados.
     * @return true si la modificación fue exitosa.
     */
    public boolean modificarPersona(Persona persona) {
        String SQL = "UPDATE persona SET nombre = ?, apellido = ?, numero_documento = ?, " +
                "id_tipo_documento = ?, id_direccion = ? WHERE id_persona = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(SQL)) {

            ps.setString(1, persona.getNombre());
            ps.setString(2, persona.getApellido());
            ps.setString(3, persona.getNumeroDocumento());
            ps.setInt(4, persona.getIdTipoDocumento());
            ps.setInt(5, persona.getIdDireccion());
            ps.setInt(6, persona.getIdPersona());

            // La clave: executeUpdate() devuelve el número de filas afectadas.
            int filasAfectadas = ps.executeUpdate();

            // Retorna true SÓLO si se modificó al menos una fila (hubo un cambio real).
            if (filasAfectadas > 0) {
                System.out.println("DEBUG: Persona modificada (filas afectadas: " + filasAfectadas + ")");
                return true;
            } else {
                System.out.println("DEBUG: Modificación de Persona no ejecutada (Datos iguales).");
                return false; // Retorna false si no hubo cambios
            }

        } catch (SQLException e) {
            System.err.println("Error al modificar persona: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}