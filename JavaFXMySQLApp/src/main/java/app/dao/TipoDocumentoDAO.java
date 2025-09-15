package app.dao;

import app.model.TipoDocumento;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipoDocumentoDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public List<TipoDocumento> obtenerTodos() {
        List<TipoDocumento> tipos = new ArrayList<>();
        String sql = "SELECT id_tipo_documento, nombre_tipo FROM TipoDocumento";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tipos.add(new TipoDocumento(rs.getInt("id_tipo_documento"), rs.getString("nombre_tipo")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tipos;
    }

    public int obtenerIdPorNombre(String nombre) {
        String sql = "SELECT id_tipo_documento FROM TipoDocumento WHERE nombre_tipo = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_tipo_documento");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}