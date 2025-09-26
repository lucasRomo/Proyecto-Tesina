package app.dao;

import app.model.Empleado;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Archivo: app/dao/EmpleadoDAO.java

    // El método ahora acepta un objeto Connection para ser parte de la transacción.
    public boolean insertarEmpleado(Empleado empleado, Connection conn) throws SQLException {
        // Ya no se abre ni se cierra la conexión aquí.
        // Simplemente se usa la que se pasa como parámetro 'conn'.
        String sql = "INSERT INTO Empleado (fecha_contratacion, cargo, salario, estado, id_persona) VALUES (?, ?, ?, ?, ?)";

        // El 'try-with-resources' usa 'conn' que se pasa al método.
        // NOTA: Lanzar 'SQLException' es mejor para que el Controller maneje el 'rollback'.
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(empleado.getFechaContratacion()));
            stmt.setString(2, empleado.getCargo());
            stmt.setDouble(3, empleado.getSalario());
            stmt.setString(4, empleado.getEstado());
            stmt.setInt(5, empleado.getIdPersona());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
        // NOTA: Se elimina el bloque catch para propagar SQLException al Controller
        // y que este pueda hacer el rollback.
    }

// Si deseas mantener el método original de 1 argumento para otras operaciones,
// puedes renombrar este método a insertarEmpleadoConTransaccion, pero por simplicidad,
// lo ideal es modificar el que ya existe.

    public List<Empleado> getAllEmpleados() {
        List<Empleado> empleados = new ArrayList<>();
        // Incluir 'e.estado' en la selección
        String sql = "SELECT e.id_empleado, e.fecha_contratacion, e.cargo, e.salario, e.estado, " + // <-- Añadido el estado
                "e.id_persona, p.nombre, p.apellido " +
                "FROM Empleado e JOIN Persona p ON e.id_persona = p.id_persona " +
                "WHERE e.estado = 'Activo'"; // Opcional: filtrar solo activos
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Empleado empleado = new Empleado(
                        rs.getInt("id_empleado"),
                        rs.getDate("fecha_contratacion").toLocalDate(),
                        rs.getString("cargo"),
                        rs.getDouble("salario"),
                        rs.getString("estado"), // <-- Pasar el estado al constructor
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
        String sql = "SELECT e.id_empleado, e.fecha_contratacion, e.cargo, e.salario, e.estado, " + // <-- Añadido el estado
                "e.id_persona, p.nombre, p.apellido FROM Empleado e JOIN Persona p ON e.id_persona = p.id_persona WHERE e.id_empleado = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    empleado = new Empleado(
                            rs.getInt("id_empleado"),
                            rs.getDate("fecha_contratacion").toLocalDate(),
                            rs.getString("cargo"),
                            rs.getDouble("salario"),
                            rs.getString("estado"), // <-- Pasar el estado al constructor
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
}