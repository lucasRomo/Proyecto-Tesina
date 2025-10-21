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

    // =========================================================================
    // MODIFICACIÓN #1: Nueva sobrecarga para inserción en transacciones.
    // Se usa la conexión recibida por parámetro.
    // =========================================================================

    public boolean insertar(Usuario usuario, Connection conn) throws SQLException {
        String sql = "INSERT INTO Usuario (id_persona, nombre_usuario, contrasena) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuario.getIdPersona());
            stmt.setString(2, usuario.getUsuario());
            stmt.setString(3, usuario.getContrasenia());
            stmt.executeUpdate();
            return true;
        }
        // Nota: No se cierra conn aquí, se propaga la excepción si falla.
    }

    // Método original (mantengo el método de inserción sin conexión por si se usa fuera de transacción)
    public boolean insertar(Usuario usuario) throws SQLException {
        try (Connection conn = getConnection()) {
            return insertar(usuario, conn); // Reutilizo la lógica
        }
    }


    public ObservableList<UsuarioEmpleadoTableView> obtenerUsuariosEmpleados() throws SQLException {
        ObservableList<UsuarioEmpleadoTableView> listaUsuarios = FXCollections.observableArrayList();

        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.contrasena, p.nombre, p.apellido, p.numero_documento, e.salario, e.estado, p.id_persona, p.id_Direccion " +
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
                String numeroDocumento = rs.getString("numero_documento");
                double salario = rs.getDouble("salario");
                String estado = rs.getString("estado");
                int idPersona = rs.getInt("id_persona");
                int idDireccion = rs.getInt("id_Direccion");

                UsuarioEmpleadoTableView usuarioEmpleado = new UsuarioEmpleadoTableView(idUsuario, usuario, contrasena, nombre, apellido, numeroDocumento, salario, estado, idPersona, idDireccion);
                listaUsuarios.add(usuarioEmpleado);
            }
        }
        return listaUsuarios;
    }

    public boolean verificarUsuario(String Usuario, String Contrasenia) {
        String sql = "SELECT * FROM Usuario Where nombre_usuario = ? AND contrasena = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, Usuario);
            pstmt.setString(2, Contrasenia); // Manteniendo la comparación directa por solicitud

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

    /**
     * Modifica los datos principales del Usuario, Persona y Empleado asociado
     * dentro de una sola transacción (este método maneja su propia conexión).
     * @param usuario El objeto UsuarioEmpleadoTableView que contiene los datos a modificar.
     * @return true si todas las modificaciones fueron exitosas.
     */
    public boolean modificarUsuariosEmpleados(UsuarioEmpleadoTableView usuario) {
        // Este método NO debe ser llamado desde el controlador que usa la transacción.
        // Debe usarse un método que acepte la conexión.
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
            System.err.println("Error al modificar el usuario en la base de datos (Transacción completa).");
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // MODIFICACIÓN #2: Método de actualización de usuario para transacciones.
    // Acepta la conexión 'conn' para ser parte de la transacción del controlador.
    // ESTE ES EL MÉTODO QUE TU CONTROLADOR NECESITA (líneas 433, 562, etc.).
    // =========================================================================


    // Este método es usado en la transacción principal y en el cambio de estado.
    // Nota: El objeto Usuario model que le pasas tiene que tener el campo estado.
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
    // --------------------------------------------------------------------------------------------


    public String obtenerContrasenaPorUsuario(String nombreUsuario) {
        String contrasena = null;
        String sql = "SELECT contrasena FROM Usuario WHERE nombre_usuario = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombreUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    contrasena = rs.getString("contrasena");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener la contraseña del usuario: " + e.getMessage());
        }
        return contrasena;
    }

    public Usuario obtenerUsuarioPorId(int idUsuario, Connection conn) throws SQLException {
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.contrasena, u.id_persona, e.estado " +
                "FROM Usuario u " +
                "JOIN Empleado e ON u.id_persona = e.id_persona " +
                "WHERE u.id_usuario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario user = new Usuario();
                    user.setIdUsuario(rs.getInt("id_usuario"));
                    user.setUsuario(rs.getString("nombre_usuario"));
                    user.setContrasena(rs.getString("contrasena"));
                    user.setIdPersona(rs.getInt("id_persona"));
                    user.setEstado(rs.getString("estado")); // Obtenido de Empleado
                    return user;
                }
            }
        }
        return null;
    }

    // =========================================================================
    // MODIFICACIÓN CLAVE: Incluir JOIN con Empleado para obtener el estado.
    // =========================================================================
    public Usuario obtenerUsuarioPorCredenciales(String nombreUsuario, String contrasena) {

        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.contrasena, p.id_persona, p.id_direccion, p.id_tipo_persona, e.estado " +
                "FROM Usuario u " +
                "JOIN Persona p ON u.id_persona = p.id_persona " +
                "LEFT JOIN Empleado e ON u.id_persona = e.id_persona " + // LEFT JOIN para obtener el estado del empleado
                "WHERE u.nombre_usuario = ? AND u.contrasena = ?";

        Usuario usuarioEncontrado = null;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombreUsuario);
            pstmt.setString(2, contrasena);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int idTipoUsuario = rs.getInt("id_tipo_persona");
                    String estadoEmpleado = rs.getString("estado"); // Recuperamos el estado

                    usuarioEncontrado = new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre_usuario"),
                            rs.getString("contrasena"),
                            rs.getInt("id_persona"),
                            rs.getInt("id_direccion"),
                            idTipoUsuario
                    );

                    // Asignamos el estado al objeto Usuario
                    usuarioEncontrado.setEstado(estadoEmpleado);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuarioEncontrado;
    }
}