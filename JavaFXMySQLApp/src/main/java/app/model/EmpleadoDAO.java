package app.model;

import app.model.Empleado;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EmpleadoDAO {

    // Método para insertar un empleado en la base de datos
    public boolean insertarEmpleado(Empleado empleado, Connection conn) throws SQLException {
        String sql = "INSERT INTO Empleado (fecha_contratacion, cargo, salario, id_persona) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(empleado.getFechaContratacion()));
            stmt.setString(2, empleado.getCargo());
            stmt.setDouble(3, empleado.getSalario());
            stmt.setInt(4, empleado.getIdPersona());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }
}