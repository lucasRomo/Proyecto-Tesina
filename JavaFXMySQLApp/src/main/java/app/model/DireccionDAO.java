package app.model.dao;

import app.model.Direccion;
import java.sql.*;

public class DireccionDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public int insertarDireccion(Direccion direccion) {
        String sql = "INSERT INTO Direccion (calle, numero, piso, departamento, codigo_postal, ciudad, provincia, pais) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int idGenerado = -1;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, direccion.getCalle());
            stmt.setString(2, direccion.getNumero());
            stmt.setString(3, direccion.getPiso());
            stmt.setString(4, direccion.getDepartamento());
            stmt.setString(5, direccion.getCodigoPostal());
            stmt.setString(6, direccion.getCiudad());
            stmt.setString(7, direccion.getProvincia());
            stmt.setString(8, direccion.getPais());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    idGenerado = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idGenerado;
    }
}