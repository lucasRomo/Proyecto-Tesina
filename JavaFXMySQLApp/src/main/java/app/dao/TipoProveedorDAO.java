package app.dao;

import app.model.TipoProveedor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class TipoProveedorDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public ObservableList<TipoProveedor> getAllTipoProveedores() throws SQLException {
        ObservableList<TipoProveedor> tipos = FXCollections.observableArrayList();
        String sql = "SELECT * FROM TipoProveedor";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tipos.add(new TipoProveedor(
                        rs.getInt("id_tipo_proveedor"),
                        rs.getString("descripcion")
                ));
            }
        }
        return tipos;
    }

    /**
     * Inserta un nuevo tipo de proveedor en la base de datos.
     * @param tipo El objeto TipoProveedor a insertar.
     * @return true si la inserción fue exitosa, false en caso contrario.
     */
    public boolean insertarTipoProveedor(TipoProveedor tipo) throws SQLException {
        String sql = "INSERT INTO TipoProveedor (descripcion) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tipo.getDescripcion());
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        tipo.setId(rs.getInt(1)); // Asigna el ID generado al objeto
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Obtiene un TipoProveedor por su descripción.
     * @param descripcion La descripción del tipo de proveedor a buscar.
     * @return El objeto TipoProveedor si se encuentra, o null en caso contrario.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public TipoProveedor getTipoProveedorByDescription(String descripcion) throws SQLException {
        String sql = "SELECT * FROM TipoProveedor WHERE descripcion = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, descripcion);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new TipoProveedor(
                            rs.getInt("id_tipo_proveedor"),
                            rs.getString("descripcion")
                    );
                }
            }
        }
        return null; // Retorna null si no se encuentra el tipo
    }

    public TipoProveedor getTipoProveedorById(int id) throws SQLException {
        String sql = "SELECT * FROM TipoProveedor WHERE id_tipo_proveedor = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new TipoProveedor(
                            rs.getInt("id_tipo_proveedor"),
                            rs.getString("descripcion")
                    );
                }
            }
        }
        return null; // Retorna null si no se encuentra el tipo
    }
}