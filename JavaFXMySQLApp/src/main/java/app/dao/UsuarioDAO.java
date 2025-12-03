package app.dao;

import app.controller.UsuarioEmpleadoTableView;
import app.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class UsuarioDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: No se encontró el driver JDBC de MySQL.");
            throw new SQLException("Falta el driver JDBC", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public boolean insertar(Usuario usuario, Connection conn) throws SQLException {
        // Asumo que el campo 'id_tipo_persona' se maneja al insertar la Persona
        String sql = "INSERT INTO Usuario (id_persona, nombre_usuario, contrasena) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuario.getIdPersona());
            stmt.setString(2, usuario.getUsuario());
            stmt.setString(3, usuario.getContrasenia());
            stmt.executeUpdate();
            return true;
        }
    }

    public boolean insertar(Usuario usuario) throws SQLException {
        try (Connection conn = getConnection()) {
            return insertar(usuario, conn);
        }
    }

    // =========================================================================
    // CORRECCIÓN #1: Obtener p.id_tipo_persona para la vista de tabla.
    // =========================================================================
    public ObservableList<UsuarioEmpleadoTableView> obtenerUsuariosEmpleados() throws SQLException {
        ObservableList<UsuarioEmpleadoTableView> listaUsuarios = FXCollections.observableArrayList();

        // Se obtiene p.id_tipo_persona
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.contrasena, p.id_tipo_persona, p.nombre, p.apellido, p.numero_documento, e.salario, e.estado, p.id_persona, p.id_Direccion " +
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
                // Se lee id_tipo_persona de la columna 'id_tipo_persona'
                int idTipoUsuario = rs.getInt("id_tipo_persona");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String numeroDocumento = rs.getString("numero_documento");
                double salario = rs.getDouble("salario");
                String estado = rs.getString("estado");
                int idPersona = rs.getInt("id_persona");
                int idDireccion = rs.getInt("id_Direccion");

                // Se pasa idTipoUsuario al constructor de UsuarioEmpleadoTableView
                UsuarioEmpleadoTableView usuarioEmpleado = new UsuarioEmpleadoTableView(idUsuario, usuario, contrasena, nombre, apellido, numeroDocumento, salario, estado, idPersona, idDireccion, idTipoUsuario);
                listaUsuarios.add(usuarioEmpleado);
            }
        }
        return listaUsuarios;
    }


    public boolean verificarSiUsuarioExiste(String Usuario) {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE nombre_usuario = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, Usuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar si el usuario existe:");
            e.printStackTrace();
        }
        return false;
    }

    // Método de actualización de usuario para transacciones. (SIN CAMBIOS)
    public boolean modificarUsuariosEmpleados(Usuario usuario, Connection conn) throws SQLException {
        if (usuario == null || usuario.getIdUsuario() <= 0) {
            return false;
        }

        // Obtener id_persona desde Usuario
        String getPersonaSql = "SELECT id_persona FROM Usuario WHERE id_usuario = ?";
        int idPersona = -1;
        try (PreparedStatement getStmt = conn.prepareStatement(getPersonaSql)) {
            getStmt.setInt(1, usuario.getIdUsuario());
            try (ResultSet rs = getStmt.executeQuery()) {
                if (rs.next()) {
                    idPersona = rs.getInt("id_persona");
                } else {
                    return false;
                }
            }
        }

        // Actualizar estado en la tabla Empleado
        String updateSql = "UPDATE Empleado SET estado = ? WHERE id_persona = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setString(1, usuario.getEstado());
            stmt.setInt(2, idPersona);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // =========================================================================
    // CORRECCIÓN #2: Obtener p.id_tipo_persona para el historial.
    // =========================================================================
    public Usuario obtenerUsuarioPorId(int idUsuario, Connection conn) throws SQLException {
        // Se obtiene p.id_tipo_persona
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.contrasena, p.id_tipo_persona, u.id_persona, e.estado " +
                "FROM Usuario u " +
                "JOIN Empleado e ON u.id_persona = e.id_persona " +
                "JOIN Persona p ON u.id_persona = p.id_persona " + // Se añade JOIN a Persona
                "WHERE u.id_usuario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario user = new Usuario();
                    user.setIdUsuario(rs.getInt("id_usuario"));
                    user.setUsuario(rs.getString("nombre_usuario"));
                    user.setContrasena(rs.getString("contrasena"));
                    // Se lee id_tipo_persona
                    user.setIdTipoUsuario(rs.getInt("id_tipo_persona"));
                    user.setIdPersona(rs.getInt("id_persona"));
                    user.setEstado(rs.getString("estado"));
                    return user;
                }
            }
        }
        return null;
    }



    // =========================================================================
    // CORRECCIÓN #3: Obtener p.id_tipo_persona para inicio de sesión.
    // (Este era el método que causaba el error original)
    // =========================================================================
    public Usuario obtenerUsuarioPorCredenciales(String nombreUsuario, String contrasena) {

        // Se selecciona p.id_tipo_persona
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.contrasena, p.id_tipo_persona, p.id_persona, p.id_direccion, e.estado " +
                "FROM Usuario u " +
                "JOIN Persona p ON u.id_persona = p.id_persona " +
                "LEFT JOIN Empleado e ON u.id_persona = e.id_persona " +
                "WHERE u.nombre_usuario = ? AND u.contrasena = ?";

        Usuario usuarioEncontrado = null;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombreUsuario);
            pstmt.setString(2, contrasena);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Se lee id_tipo_persona
                    int idTipoUsuario = rs.getInt("id_tipo_persona");
                    String estadoEmpleado = rs.getString("estado");

                    usuarioEncontrado = new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre_usuario"),
                            rs.getString("contrasena"),
                            rs.getInt("id_persona"),
                            rs.getInt("id_direccion"),
                            idTipoUsuario
                    );

                    usuarioEncontrado.setEstado(estadoEmpleado);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuarioEncontrado;
    }
}