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
        // Se añade p.numero_documento a la consulta SELECT (ya corregido en el paso anterior)
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

                // NOTA: Asegúrate de que UsuarioEmpleadoTableView tenga el constructor actualizado.
                UsuarioEmpleadoTableView usuarioEmpleado = new UsuarioEmpleadoTableView(idUsuario, usuario, contrasena, nombre, apellido, numeroDocumento, salario, estado, idPersona, idDireccion);
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

    /**
     * Modifica los datos principales del Usuario, Persona y Empleado asociado
     * dentro de una sola transacción. (Mantiene la funcionalidad transaccional completa)
     * * @param usuario El objeto UsuarioEmpleadoTableView que contiene los datos a modificar.
     * @return true si todas las modificaciones fueron exitosas.
     */
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
            System.err.println("Error al modificar el usuario en la base de datos (Transacción completa).");
            e.printStackTrace();
            return false;
        }
    }

    // --- CORRECCIÓN CLAVE: Método reintroducido para resolver el error 'cannot find symbol' ---
    // Este método solo actualiza la tabla Usuario (username y contraseña).
    private static final String UPDATE_USUARIO_SOLO =
            "UPDATE Usuario SET nombre_usuario = ?, contrasena = ? WHERE id_usuario = ?";

    public boolean modificarUsuariosEmpleados(Usuario usuarioView) {
        boolean exito = false;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_USUARIO_SOLO)) {

            ps.setString(1, usuarioView.getUsuario());
            ps.setString(2, usuarioView.getContrasenia());
            // No se incluye el campo 'estado' aquí.
            ps.setInt(3, usuarioView.getIdUsuario());

            if (ps.executeUpdate() > 0) {
                exito = true;
            }

        } catch (SQLException e) {
            System.err.println("Error al modificar datos del Usuario (simple): " + e.getMessage());
            e.printStackTrace();
        }
        return exito;
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

    public Usuario obtenerUsuarioPorId(int idUsuario) throws SQLException {
        // Solo necesitamos los campos de la tabla Usuario para la comparación de credenciales
        String sql = "SELECT id_usuario, nombre_usuario, contrasena FROM Usuario WHERE id_usuario = ?";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario user = new Usuario();
                    user.setIdUsuario(rs.getInt("id_usuario"));
                    user.setUsuario(rs.getString("nombre_usuario"));
                    user.setContrasena(rs.getString("contrasena"));

                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por ID: " + e.getMessage());
            throw e; // Relanza la excepción para que el controlador la maneje
        }
        return null;
    }

    public Usuario obtenerUsuarioPorCredenciales(String nombreUsuario, String contrasena) {

        // CORRECCIÓN FINAL: Usamos p.id_tipo_persona según el esquema DDL proporcionado.
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.contrasena, p.id_persona, p.id_direccion, p.id_tipo_persona " +
                "FROM Usuario u " +
                "JOIN Persona p ON u.id_persona = p.id_persona " +
                "WHERE u.nombre_usuario = ? AND u.contrasena = ?";

        Usuario usuarioEncontrado = null;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombreUsuario);
            pstmt.setString(2, contrasena);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Recuperamos el ID de Tipo de Persona (el rol)
                    int idTipoUsuario = rs.getInt("id_tipo_persona");

                    // Creamos el objeto Usuario con todos los datos
                    usuarioEncontrado = new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre_usuario"),
                            rs.getString("contrasena"),
                            rs.getInt("id_persona"),
                            rs.getInt("id_direccion"),
                            idTipoUsuario
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuarioEncontrado;
    }
}
