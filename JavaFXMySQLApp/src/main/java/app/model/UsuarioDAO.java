package app.model;
import app.controller.HasheadorController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class UsuarioDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/usuarios";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static boolean insertar(Usuario usuario) {
        String sql = "INSERT INTO Usuarios (Usuario, Contrasenia) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getUsuario());
            String contraseniaHasheada = HasheadorController.hashPassword(usuario.getContrasenia());
            stmt.setString(2, contraseniaHasheada);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verificarUsuario(String Usuario, String Contrasenia) {
        String contraseniaHasheada = HasheadorController.hashPassword(Contrasenia);
        String sql = "SELECT * FROM usuarios Where Usuario = ? AND Contrasenia = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, Usuario);
            pstmt.setString(2, contraseniaHasheada);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

            // Hasta Aca //

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
