package app.dao;

import app.model.Insumo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InsumoDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public ObservableList<Insumo> getAllInsumos() {
        ObservableList<Insumo> insumos = FXCollections.observableArrayList();
        String query = "SELECT * FROM Insumo";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                insumos.add(new Insumo(
                        rs.getInt("id_insumo"),
                        rs.getString("nombre_insumo"),
                        rs.getString("descripcion"),
                        rs.getInt("stock_minimo"),
                        rs.getInt("stock_actual"),
                        rs.getString("estado"),
                        rs.getInt("id_tipo_proveedor")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los insumos: " + e.getMessage());
        }
        return insumos;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }


    public Insumo getInsumoById(int idInsumo, Connection conn) throws SQLException {
        String query = "SELECT * FROM Insumo WHERE id_insumo = ?";

        // Usamos la conexión que nos llega por parámetro (conn)
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idInsumo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Insumo(
                            rs.getInt("id_insumo"),
                            rs.getString("nombre_insumo"),
                            rs.getString("descripcion"),
                            rs.getInt("stock_minimo"),
                            rs.getInt("stock_actual"),
                            rs.getString("estado"),
                            rs.getInt("id_tipo_proveedor")
                    );
                }
            }
        }
        return null; // Retorna null si no se encuentra o hay error.
    }


    public boolean modificarInsumo(Insumo insumo) {
        String query = "UPDATE Insumo SET nombre_insumo = ?, descripcion = ?, stock_minimo = ?, stock_actual = ?, id_tipo_proveedor = ? WHERE id_insumo = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, insumo.getNombreInsumo());
            pstmt.setString(2, insumo.getDescripcion());
            pstmt.setInt(3, insumo.getStockMinimo());
            pstmt.setInt(4, insumo.getStockActual());
            pstmt.setInt(5, insumo.getIdTipoProveedor());
            pstmt.setInt(6, insumo.getIdInsumo());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error al modificar insumo: " + e.getMessage());
            return false;
        }
    }

    public boolean modificarEstadoInsumo(int idInsumo, String nuevoEstado) {
        String query = "UPDATE Insumo SET estado = ? WHERE id_insumo = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, idInsumo);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error al modificar el estado del insumo: " + e.getMessage());
            return false;
        }
    }

    public boolean insertarInsumo(Insumo insumo) {
        String query = "INSERT INTO Insumo (nombre_insumo, descripcion, stock_minimo, stock_actual, estado, id_tipo_proveedor) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, insumo.getNombreInsumo());
            pstmt.setString(2, insumo.getDescripcion());
            pstmt.setInt(3, insumo.getStockMinimo());
            pstmt.setInt(4, insumo.getStockActual());
            pstmt.setString(5, insumo.getEstado());
            pstmt.setInt(6, insumo.getIdTipoProveedor());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar insumo: " + e.getMessage());
            return false;
        }
    }

    // Puedes agregar más métodos como insertarInsumo o eliminarInsumo si los necesitas.
}