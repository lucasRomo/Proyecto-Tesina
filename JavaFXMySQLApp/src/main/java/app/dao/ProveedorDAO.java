package app.dao;

import app.model.Proveedor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class ProveedorDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public ObservableList<Proveedor> getAllProveedores() {
        ObservableList<Proveedor> proveedores = FXCollections.observableArrayList();
        String sql = "SELECT p.*, tp.descripcion AS tipo_descripcion FROM Proveedor p JOIN TipoProveedor tp ON p.id_tipo_proveedor = tp.id_tipo_proveedor";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                proveedores.add(new Proveedor(
                        rs.getInt("id_proveedor"),
                        rs.getString("nombre"),
                        rs.getString("contacto"),
                        rs.getString("mail"),
                        rs.getString("estado"),
                        rs.getInt("id_direccion"),
                        rs.getInt("id_tipo_proveedor"),
                        rs.getString("tipo_descripcion") // <-- ¡Agregamos esto!
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener los proveedores: " + e.getMessage());
        }
        return proveedores;
    }
    // Nuevo método para obtener proveedores por tipo de proveedor
    public ObservableList<Proveedor> getProveedoresByTipo(int idTipoProveedor) {
        ObservableList<Proveedor> proveedores = FXCollections.observableArrayList();
        String query = "SELECT * FROM Proveedor WHERE id_tipo_proveedor = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idTipoProveedor);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    proveedores.add(new Proveedor(
                            rs.getInt("id_proveedor"),
                            rs.getString("nombre"),
                            rs.getString("contacto"),
                            rs.getString("mail"),
                            rs.getString("estado"),
                            rs.getInt("id_direccion"),
                            rs.getInt("id_tipo_proveedor")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener proveedores por tipo: " + e.getMessage());
        }
        return proveedores;
    }

    public boolean modificarEstadoProveedor(int idProveedor, String nuevoEstado) {
        String sql = "UPDATE Proveedor SET estado = ? WHERE id_proveedor = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, idProveedor);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error al modificar el estado del proveedor: " + e.getMessage());
            return false;
        }
    }

    // Nuevo método para modificar todos los campos de un proveedor
    public boolean modificarProveedor(Proveedor proveedor) {
        String sql = "UPDATE Proveedor SET nombre = ?, contacto = ?, mail = ?, estado = ?, id_tipo_proveedor = ? WHERE id_proveedor = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, proveedor.getNombre());
            stmt.setString(2, proveedor.getContacto());
            stmt.setString(3, proveedor.getMail());
            stmt.setString(4, proveedor.getEstado());
            stmt.setInt(5, proveedor.getIdTipoProveedor());
            stmt.setInt(6, proveedor.getIdProveedor());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error al modificar el proveedor: " + e.getMessage());
            return false;
        }
    }

    public boolean registrarProveedor(Proveedor proveedor, Connection conn) throws SQLException {
        String sql = "INSERT INTO Proveedor (nombre, contacto, mail, estado, id_direccion, id_tipo_proveedor) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, proveedor.getNombre());
            stmt.setString(2, proveedor.getContacto());
            stmt.setString(3, proveedor.getMail());
            stmt.setString(4, proveedor.getEstado());
            stmt.setInt(5, proveedor.getIdDireccion());
            stmt.setInt(6, proveedor.getIdTipoProveedor());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}