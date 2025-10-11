package app.dao;

import app.model.Empleado;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.DriverManager.getConnection;

public class EmpleadoDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // CONSULTAS SQL
    // 1. Actualiza los datos de la persona
    private static final String UPDATE_PERSONA =
            "UPDATE Persona SET nombre = ?, apellido = ? WHERE id_persona = ?";
    // 2. Actualiza los datos del empleado
    private static final String UPDATE_EMPLEADO =
            "UPDATE Empleado SET fecha_contratacion = ?, cargo = ?, salario = ? WHERE id_empleado = ?";
    // 3. Actualiza el estado del usuario (usando el ID de persona para encontrar el usuario)
    // NOTA: Asumimos que la tabla 'usuario' tiene un id_persona para encontrarlo.
    // Si tu tabla 'usuario' se relaciona directamente con el id_empleado, ajusta la consulta
    // y la variable a usar en el PreparedStatement. Aquí usamos id_persona:
    private static final String UPDATE_USUARIO_ESTADO =
            "UPDATE usuario u JOIN Empleado e ON u.id_usuario = e.id_usuario_relacionado " + // AJUSTAR ESTA CLÁUSULA JOIN SEGÚN TU ESQUEMA REAL
                    "SET u.estado = ? WHERE u.id_persona = ?"; // Usamos id_persona si la tabla Usuario tiene ese campo.

    // Si la tabla usuario NO tiene id_persona, y solo tiene id_usuario:
    // private static final String UPDATE_USUARIO_ESTADO_BY_EMPLEADO_ID =
    //        "UPDATE usuario u JOIN Empleado e ON ... SET u.estado = ? WHERE e.id_empleado = ?";

    private static final String UPDATE_EMPLEADO_ESTADO_SIMPLE =
            "UPDATE Empleado SET estado = ? WHERE id_empleado = ?";


    private Connection getConnection() throws SQLException {
        // Implementación real para obtener tu conexión
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // --- MÉTODOS EXISTENTES (AJUSTADOS CON getConnection) ---

    // El método ahora acepta un objeto Connection para ser parte de la transacción.
    public boolean insertarEmpleado(Empleado empleado, Connection conn) throws SQLException {
        String sql = "INSERT INTO Empleado (fecha_contratacion, cargo, salario, estado, id_persona) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(empleado.getFechaContratacion()));
            stmt.setString(2, empleado.getCargo());
            stmt.setDouble(3, empleado.getSalario());
            stmt.setString(4, empleado.getEstado());
            stmt.setInt(5, empleado.getIdPersona());
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public List<Empleado> getAllEmpleados() {
        List<Empleado> empleados = new ArrayList<>();
        String sql = "SELECT e.id_empleado, e.fecha_contratacion, e.cargo, e.salario, e.estado, " +
                "e.id_persona, p.nombre, p.apellido " +
                "FROM Empleado e JOIN Persona p ON e.id_persona = p.id_persona " +
                "WHERE e.estado = 'Activo'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Empleado empleado = new Empleado(
                        rs.getInt("id_empleado"),
                        rs.getDate("fecha_contratacion").toLocalDate(),
                        rs.getString("cargo"),
                        rs.getDouble("salario"),
                        rs.getString("estado"),
                        rs.getInt("id_persona"),
                        rs.getString("nombre"),
                        rs.getString("apellido")
                );
                empleados.add(empleado);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener todos los empleados: " + e.getMessage());
        }
        return empleados;
    }

    public Empleado getEmpleadoById(int id) {
        Empleado empleado = null;
        String sql = "SELECT e.id_empleado, e.fecha_contratacion, e.cargo, e.salario, e.estado, " +
                "e.id_persona, p.nombre, p.apellido FROM Empleado e JOIN Persona p ON e.id_persona = p.id_persona WHERE e.id_empleado = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    empleado = new Empleado(
                            rs.getInt("id_empleado"),
                            rs.getDate("fecha_contratacion").toLocalDate(),
                            rs.getString("cargo"),
                            rs.getDouble("salario"),
                            rs.getString("estado"),
                            rs.getInt("id_persona"),
                            rs.getString("nombre"),
                            rs.getString("apellido")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener empleado por ID: " + e.getMessage());
        }
        return empleado;
    }


    // El método `modificar` original (solo estado de usuario) fue renombrado y simplificado
    // para manejar solo la actualización del campo 'estado' en la tabla Empleado,
    // ya que la modificación del estado sí funcionó en tu prueba.
    public boolean modificarEstado(int idEmpleado, String nuevoEstado) {
        boolean rowUpdated = false;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_EMPLEADO_ESTADO_SIMPLE)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idEmpleado);

            rowUpdated = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al modificar el estado del empleado en la DB.");
            e.printStackTrace();
        }
        return rowUpdated;
    }


    // -------------------------------------------------------------------------
    // --- NUEVO MÉTODO CRÍTICO: ACTUALIZACIÓN COMPLETA TRANSACCIONAL ---
    // -------------------------------------------------------------------------

    /**
     * Realiza la actualización de los datos de Empleado y Persona dentro de una transacción.
     * @param empleado El objeto Empleado con los datos actualizados.
     * @param conn La conexión activa para la transacción.
     * @return true si todas las actualizaciones fueron exitosas.
     * @throws SQLException Si falla alguna operación SQL.
     */
    public boolean actualizarEmpleadoCompleto(Empleado empleado, Connection conn) throws SQLException {
        int personaRowsAffected = 0;
        int empleadoRowsAffected = 0;
        // int usuarioRowsAffected = 0; // Usar si modificas el estado en la tabla Usuario aquí

        // 1. Actualizar tabla Persona (nombre y apellido)
        try (PreparedStatement psPersona = conn.prepareStatement(UPDATE_PERSONA)) {
            // Esto asegura que el nombre y apellido siempre se envíen,
            // evitando el error "apellido no puede ser nulo".
            psPersona.setString(1, empleado.getNombre());
            psPersona.setString(2, empleado.getApellido());
            psPersona.setInt(3, empleado.getIdPersona());
            personaRowsAffected = psPersona.executeUpdate();
        }

        // 2. Actualizar tabla Empleado (salario, cargo, fecha)
        try (PreparedStatement psEmpleado = conn.prepareStatement(UPDATE_EMPLEADO)) {
            psEmpleado.setDate(1, java.sql.Date.valueOf(empleado.getFechaContratacion()));
            psEmpleado.setString(2, empleado.getCargo());
            psEmpleado.setDouble(3, empleado.getSalario());
            psEmpleado.setInt(4, empleado.getIdEmpleado());
            empleadoRowsAffected = psEmpleado.executeUpdate();
        }

        // 3. Actualizar la tabla Usuario (Si cambias el estado junto con los demás datos)
        /*
        try (PreparedStatement psUsuario = conn.prepareStatement(UPDATE_USUARIO_ESTADO)) {
            psUsuario.setString(1, empleado.getEstado());
            psUsuario.setInt(2, empleado.getIdPersona()); // O el ID del usuario/empleado, según tu esquema
            usuarioRowsAffected = psUsuario.executeUpdate();
        }
        */

        // Devolvemos true si se actualizaron las filas necesarias.
        // Asumimos que la actualización de Empleado y Persona es obligatoria.
        return personaRowsAffected > 0 && empleadoRowsAffected > 0; // && (usuarioRowsAffected > 0);
    }
}
