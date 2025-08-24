package app.model;// Tu paquete DAO
import app.model.Usuario;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class UsuarioDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Método de inserción corregido para incluir el id_persona
    public boolean insertar(Usuario usuario) {
        // La consulta SQL debe usar el nombre correcto de la tabla: Usuario
        String sql = "INSERT INTO Usuario (id_persona, nombre_usuario, contrasena) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuario.getIdPersona());
            stmt.setString(2, usuario.getUsuario());
            stmt.setString(3, usuario.getContrasenia());

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verificarUsuario(String Usuario, String Contrasenia) {
        String contraseniaHasheada = Contrasenia;
        // La consulta debe usar el nombre de tabla correcto: Usuario
        String sql = "SELECT * FROM Usuario Where nombre_usuario = ? AND contrasena = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, Usuario);
            pstmt.setString(2, contraseniaHasheada);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
