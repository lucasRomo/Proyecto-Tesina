package app.dao;

import app.controller.UsuarioEmpleadoTableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class UsuarioDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Método de inserción corregido para incluir el id_persona
    public boolean insertar(Usuario usuario, Connection conn) throws SQLException {
        String sql = "INSERT INTO Usuario (id_persona, nombre_usuario, contrasena) VALUES (?, ?, ?)";

        // Usa la conexión 'conn' que se pasa como parámetro
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuario.getIdPersona());
            stmt.setString(2, usuario.getUsuario());
            stmt.setString(3, usuario.getContrasenia());
            stmt.executeUpdate();
            return true;
        }

    }


    public ObservableList<UsuarioEmpleadoTableView> obtenerUsuariosEmpleados() throws SQLException {
        ObservableList<UsuarioEmpleadoTableView> listaUsuarios = FXCollections.observableArrayList();
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.contrasena, p.nombre, p.apellido, e.salario, e.estado, p.id_persona " +
                "FROM Usuario u " +
                "JOIN Empleado e ON u.id_persona = e.id_persona " +
                "JOIN Persona p ON e.id_persona = p.id_persona";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int idUsuario = rs.getInt("id_usuario");
                String usuario = rs.getString("nombre_usuario");
                String contrasena = rs.getString("contrasena");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                double salario = rs.getDouble("salario");
                String estado = rs.getString("estado");
                int idPersona = rs.getInt("id_persona");

                UsuarioEmpleadoTableView usuarioEmpleado = new UsuarioEmpleadoTableView(idUsuario, usuario, contrasena, nombre, apellido, salario, estado, idPersona);
                listaUsuarios.add(usuarioEmpleado);
            }
        }
        return listaUsuarios;
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

    public boolean verificarSiUsuarioExiste(String Usuario) {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE nombre_usuario = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, Usuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Si el conteo es mayor que 0, significa que el usuario ya existe.
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar si el usuario existe:");
            e.printStackTrace();
        }
        return false;
    }

    public boolean modificarUsuariosEmpleados(UsuarioEmpleadoTableView usuario) {
        // La lógica para la actualización de múltiples tablas debe ser una transacción
        String sqlUpdateUsuario = "UPDATE Usuario SET nombre_usuario = ?, contrasena = ? WHERE id_usuario = ?";
        String sqlUpdatePersona = "UPDATE Persona SET nombre = ?, apellido = ? WHERE id_persona = ?";
        String sqlUpdateEmpleado = "UPDATE Empleado SET salario = ?, estado = ? WHERE id_persona = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Inicia la transacción

            // 1. Actualizar Usuario
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateUsuario)) {
                stmt.setString(1, usuario.getUsuario());
                stmt.setString(2, usuario.getContrasena());
                stmt.setInt(3, usuario.getIdUsuario());
                stmt.executeUpdate();
            }

            // 2. Actualizar Persona
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdatePersona)) {
                stmt.setString(1, usuario.getNombre());
                stmt.setString(2, usuario.getApellido());
                stmt.setInt(3, usuario.getIdPersona());
                stmt.executeUpdate();
            }

            // 3. Actualizar Empleado
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateEmpleado)) {
                stmt.setDouble(1, usuario.getSalario());
                stmt.setString(2, usuario.getEstado());
                stmt.setInt(3, usuario.getIdPersona());
                stmt.executeUpdate();
            }

            conn.commit(); // Confirma la transacción
            return true;
        } catch (SQLException e) {
            System.err.println("Error al modificar el usuario en la base de datos.");
            e.printStackTrace();
            return false;
        }
    }
}