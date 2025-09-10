package app.model;

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

    public Direccion obtenerPorId(int id) {
        String sql = "SELECT * FROM Direccion WHERE id_direccion = ?";
        Direccion direccion = null;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    direccion = new Direccion();
                    direccion.setIdDireccion(rs.getInt("id_direccion"));
                    direccion.setCalle(rs.getString("calle"));
                    direccion.setNumero(rs.getString("numero"));
                    direccion.setPiso(rs.getString("piso"));
                    direccion.setDepartamento(rs.getString("departamento"));
                    direccion.setCodigoPostal(rs.getString("codigo_postal"));
                    direccion.setCiudad(rs.getString("ciudad"));
                    direccion.setProvincia(rs.getString("provincia"));
                    direccion.setPais(rs.getString("pais"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return direccion;
    }

    public boolean modificarDireccion(Direccion direccion) {
        // Asegúrate de que el nombre de la columna aquí coincida con tu base de datos
        String sql = "UPDATE Direccion SET calle = ?, numero = ?, piso = ?, departamento = ?, codigo_postal = ?, ciudad = ?, provincia = ?, pais = ? WHERE id_direccion = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, direccion.getCalle());
            stmt.setString(2, direccion.getNumero());
            stmt.setString(3, direccion.getPiso());
            stmt.setString(4, direccion.getDepartamento());
            stmt.setString(5, direccion.getCodigoPostal());
            stmt.setString(6, direccion.getCiudad());
            stmt.setString(7, direccion.getProvincia());
            stmt.setString(8, direccion.getPais());
            // Asegúrate de que el nombre de la columna aquí coincida con tu base de datos
            stmt.setInt(9, direccion.getIdDireccion());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }}
}