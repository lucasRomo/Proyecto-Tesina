package app.dao;

import app.model.Empleado;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Método de inserción existente (con manejo de transacción)
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

    // Método getAllEmpleados existente
    public List<Empleado> getAllEmpleados() {
        List<Empleado> empleados = new ArrayList<>();
        String sql = "SELECT e.id_empleado, e.fecha_contratacion, e.cargo, e.salario, e.estado, " +
                "e.id_persona, p.nombre, p.apellido " +
                "FROM Empleado e JOIN Persona p ON e.id_persona = p.id_persona " +
                "WHERE e.estado = 'Activo'";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
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

    // Método getEmpleadoById existente
    public Empleado getEmpleadoById(int id) {
        Empleado empleado = null;
        String sql = "SELECT e.id_empleado, e.fecha_contratacion, e.cargo, e.salario, e.estado, " +
                "e.id_persona, p.nombre, p.apellido FROM Empleado e JOIN Persona p ON e.id_persona = p.id_persona WHERE e.id_empleado = ?";
        // ... (resto del método) ...
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

    // 👇 MÉTODO NUEVO PARA EL FILTRO
    /**
     * Obtiene el ID del Empleado a partir de su nombre y apellido combinados.
     * @param nombreCompleto El nombre y apellido del empleado (e.g., "Juan Perez").
     * @return El ID del empleado, o 0 si no se encuentra.
     */
    public int getIdEmpleadoPorNombreCompleto(String nombreCompleto) {
        int idEmpleado = 0;
        String[] partes = nombreCompleto.split(" ", 2);
        if (partes.length < 2) {
            return 0;
        }
        String nombre = partes[0];
        String apellido = partes[1];

        String sql = "SELECT e.id_empleado " +
                "FROM Empleado e " +
                "JOIN Persona p ON e.id_persona = p.id_persona " +
                "WHERE p.nombre = ? AND p.apellido = ? AND e.estado = 'Activo'";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setString(2, apellido);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    idEmpleado = rs.getInt("id_empleado");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener ID de empleado por nombre completo: " + e.getMessage());
        }
        return idEmpleado;
    }
}