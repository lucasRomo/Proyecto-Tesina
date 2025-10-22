package app.dao;

import app.model.Empleado;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Empleado.
 * Implementa las operaciones CRUD y maneja la lógica de negocio que
 * afecta tanto a la tabla Empleado como a la tabla Persona (transaccional).
 */
public class EmpleadoDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // CONSULTAS SQL
    private static final String SELECT_BASE =
            "SELECT e.id_empleado, e.fecha_contratacion, e.cargo, e.salario, e.estado, " +
                    "e.id_persona, p.nombre, p.apellido " +
                    "FROM Empleado e JOIN Persona p ON e.id_persona = p.id_persona";

    // 1. Actualiza los datos de la persona
    private static final String UPDATE_PERSONA =
            "UPDATE Persona SET nombre = ?, apellido = ? WHERE id_persona = ?";

    // 2. Actualiza los datos del empleado
    private static final String UPDATE_EMPLEADO =
            "UPDATE Empleado SET fecha_contratacion = ?, cargo = ?, salario = ?, estado = ? WHERE id_empleado = ?";

    // 3. Actualiza el estado del usuario (asumiendo que la tabla usuario se relaciona con id_persona o id_empleado)
    // Usaremos una consulta de ejemplo que asume relación a través de Empleado (debe ajustarse al esquema real de Usuario)
    private static final String UPDATE_USUARIO_ESTADO =
            "UPDATE Usuario u JOIN Empleado e ON u.id_empleado = e.id_empleado " + // AJUSTAR ESTA CLÁUSULA JOIN SEGÚN TU ESQUEMA REAL
                    "SET u.estado = ? WHERE e.id_empleado = ?";

    private static final String UPDATE_EMPLEADO_ESTADO_SIMPLE =
            "UPDATE Empleado SET estado = ? WHERE id_empleado = ?";


    /**
     * Obtiene la conexión a la base de datos y carga el driver.
     * @return Objeto Connection.
     * @throws SQLException Si ocurre un error de SQL o al cargar el driver.
     */
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver JDBC de MySQL no encontrado.");
            throw new SQLException("Falta el driver de MySQL.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Mapea un ResultSet a un objeto Empleado (incluyendo nombre y apellido de Persona).
     */
    private Empleado mapResultSetToEmpleado(ResultSet rs) throws SQLException {
        // Asegúrate que tu modelo Empleado tiene un constructor que acepta todos estos campos.
        return new Empleado(
                rs.getInt("id_empleado"),
                rs.getDate("fecha_contratacion").toLocalDate(),
                rs.getString("cargo"),
                rs.getDouble("salario"),
                rs.getString("estado"),
                rs.getInt("id_persona"),
                rs.getString("nombre"), // Columna de la tabla Persona
                rs.getString("apellido") // Columna de la tabla Persona
        );
    }

    // ----------------------------------------------------------------------
    // OPERACIONES CRÍTICAS (TRANSACCIONALES)
    // ----------------------------------------------------------------------

    /**
     * Inserta un nuevo registro de Empleado.
     * Debe ser llamada dentro de una transacción que maneje la inserción de Persona previamente.
     * @param empleado El objeto Empleado a guardar.
     * @param conn La conexión activa para la transacción.
     * @return true si la inserción fue exitosa.
     * @throws SQLException Si falla alguna operación SQL.
     */
    public boolean insertarEmpleado(Empleado empleado, Connection conn) throws SQLException {
        String sql = "INSERT INTO Empleado (fecha_contratacion, cargo, salario, estado, id_persona) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, java.sql.Date.valueOf(empleado.getFechaContratacion()));
            stmt.setString(2, empleado.getCargo());
            stmt.setDouble(3, empleado.getSalario());
            stmt.setString(4, empleado.getEstado());
            stmt.setInt(5, empleado.getIdPersona());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                // Obtener el ID generado y actualizar el objeto Empleado
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        empleado.setIdEmpleado(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

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

        // 1. Actualizar tabla Persona (nombre y apellido)
        try (PreparedStatement psPersona = conn.prepareStatement(UPDATE_PERSONA)) {
            psPersona.setString(1, empleado.getNombre());
            psPersona.setString(2, empleado.getApellido());
            psPersona.setInt(3, empleado.getIdPersona());
            personaRowsAffected = psPersona.executeUpdate();
        }

        // 2. Actualizar tabla Empleado (fecha_contratacion, cargo, salario, estado)
        try (PreparedStatement psEmpleado = conn.prepareStatement(UPDATE_EMPLEADO)) {
            psEmpleado.setDate(1, java.sql.Date.valueOf(empleado.getFechaContratacion()));
            psEmpleado.setString(2, empleado.getCargo());
            psEmpleado.setDouble(3, empleado.getSalario());
            psEmpleado.setString(4, empleado.getEstado());
            psEmpleado.setInt(5, empleado.getIdEmpleado());
            empleadoRowsAffected = psEmpleado.executeUpdate();
        }

        // 3. Opcional: Actualizar el estado en la tabla Usuario si está activo/inactivo (Descomentar si es necesario)
        /*
        try (PreparedStatement psUsuario = conn.prepareStatement(UPDATE_USUARIO_ESTADO)) {
            // Se asume que el estado del usuario es el mismo que el del empleado ("Activo"/"Inactivo")
            psUsuario.setString(1, empleado.getEstado());
            psUsuario.setInt(2, empleado.getIdEmpleado()); // Usamos id_empleado en la cláusula WHERE de la JOIN
            psUsuario.executeUpdate(); // No validamos el affectedRows aquí porque el usuario puede no existir
        }
        */

        // Devolvemos true si se actualizaron las filas necesarias (Persona y Empleado).
        return personaRowsAffected > 0 && empleadoRowsAffected > 0;
    }


    // ----------------------------------------------------------------------
    // OPERACIONES SIMPLES (LECTURA Y ESTADO)
    // ----------------------------------------------------------------------

    /**
     * Obtiene una lista de todos los empleados (Activos e Inactivos).
     * Nota: Este método se reutiliza para cargar el ChoiceBox en InformesController.
     */
    public List<Empleado> getAllEmpleados() {
        List<Empleado> empleados = new ArrayList<>();
        // Solo mostramos empleados que estén activos en el ChoiceBox de informes
        String sql = SELECT_BASE + " WHERE e.estado = 'Activo' ORDER BY p.apellido, p.nombre";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                empleados.add(mapResultSetToEmpleado(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener todos los empleados: " + e.getMessage());
        }
        return empleados;
    }

    /**
     * Obtiene un empleado por su ID de Persona.
     * @param idPersona ID de la persona asociada al empleado.
     * @param conn Conexión activa (puede ser null si no es parte de una transacción).
     * @return El objeto Empleado o null.
     */
    public Empleado obtenerEmpleadoPorId(int idPersona, Connection conn) throws SQLException {
        boolean closeConn = (conn == null);
        Connection currentConn = closeConn ? getConnection() : conn;

        String sql = SELECT_BASE + " WHERE e.id_persona = ?";

        try (PreparedStatement stmt = currentConn.prepareStatement(sql)) {
            stmt.setInt(1, idPersona);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmpleado(rs);
                }
            }
        } finally {
            if (closeConn && currentConn != null) {
                currentConn.close();
            }
        }
        return null;
    }

    /**
     * Modifica el estado del empleado (y opcionalmente del usuario asociado)
     * @param idEmpleado ID del empleado a modificar.
     * @param nuevoEstado El nuevo estado (e.g., "Activo", "Inactivo").
     * @return true si el empleado fue actualizado.
     */
    public boolean modificarEstado(int idEmpleado, String nuevoEstado) {
        boolean empleadoUpdated = false;

        // La operación de cambio de estado afecta a Empleado y potencialmente a Usuario.
        // Debe ser transaccional.
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false); // Iniciar transacción

            // 1. Actualizar estado en tabla Empleado
            try (PreparedStatement psEmpleado = con.prepareStatement(UPDATE_EMPLEADO_ESTADO_SIMPLE)) {
                psEmpleado.setString(1, nuevoEstado);
                psEmpleado.setInt(2, idEmpleado);
                empleadoUpdated = psEmpleado.executeUpdate() > 0;
            }

            // 2. Opcional: Actualizar estado en tabla Usuario (si aplica)
            /*
            try (PreparedStatement psUsuario = con.prepareStatement(UPDATE_USUARIO_ESTADO)) {
                psUsuario.setString(1, nuevoEstado);
                psUsuario.setInt(2, idEmpleado); // Usamos id_empleado
                psUsuario.executeUpdate();
            }
            */

            con.commit(); // Confirmar la transacción

        } catch (SQLException e) {
            System.err.println("Error al modificar el estado del empleado en la DB. Intentando rollback.");
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error durante el rollback: " + ex.getMessage());
                }
            }
            e.printStackTrace();
            empleadoUpdated = false;

        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar la conexión: " + e.getMessage());
                }
            }
        }
        return empleadoUpdated;
    }
}
