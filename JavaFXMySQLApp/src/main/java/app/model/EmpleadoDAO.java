package app.model;

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

    public boolean insertarEmpleado(Empleado empleado) {
        String sql = "INSERT INTO Empleado (fecha_contratacion, cargo, salario, id_persona) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(empleado.getFechaContratacion()));
            stmt.setString(2, empleado.getCargo());
            stmt.setDouble(3, empleado.getSalario());
            stmt.setInt(4, empleado.getIdPersona());
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Error al insertar empleado: " + e.getMessage());
            return false;
        }
    }

    public List<Empleado> getAllEmpleados() {
        List<Empleado> empleados = new ArrayList<>();
        String sql = "SELECT e.*, p.nombre, p.apellido FROM Empleado e JOIN Persona p ON e.id_persona = p.id_persona";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Empleado empleado = new Empleado(
                        rs.getInt("id_empleado"),
                        rs.getDate("fecha_contratacion").toLocalDate(),
                        rs.getString("cargo"),
                        rs.getDouble("salario"),
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
        String sql = "SELECT e.*, p.nombre, p.apellido FROM Empleado e JOIN Persona p ON e.id_persona = p.id_persona WHERE e.id_empleado = ?";
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