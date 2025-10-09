package app.dao;

import app.model.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Producto.
 * Gestiona la persistencia de los productos en la base de datos.
 * Maneja el id_categoria como un campo NULLable, donde 0 en Java significa NULL en SQL.
 */
public class ProductoDAO {

    // Constantes de conexión
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Nombre de la tabla y columnas
    private static final String TABLE_NAME = "producto";
    private static final String COL_ID = "id_producto";
    private static final String COL_NOMBRE = "nombre_producto";
    private static final String COL_DESCRIPCION = "descripcion";
    private static final String COL_PRECIO = "precio";
    private static final String COL_STOCK = "stock";
    private static final String COL_CATEGORIA = "id_categoria"; // Campo NULLable

    /**
     * Método auxiliar para obtener la conexión a la base de datos.
     * @return Objeto Connection.
     * @throws SQLException Si ocurre un error de SQL.
     */
    private Connection getConnection() throws SQLException {
        // Asegúrate de tener el driver JDBC cargado en tu proyecto
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Mapea un ResultSet a un objeto Producto.
     * @param rs ResultSet con los datos del producto.
     * @return Objeto Producto.
     * @throws SQLException Si ocurre un error de SQL.
     */
    private Producto mapResultSetToProducto(ResultSet rs) throws SQLException {
        // Si id_categoria es NULL en la DB, getInt devuelve 0, que usamos en el modelo
        return new Producto(
                rs.getInt(COL_ID),
                rs.getString(COL_NOMBRE),
                rs.getString(COL_DESCRIPCION),
                rs.getDouble(COL_PRECIO),
                rs.getInt(COL_STOCK),
                rs.getInt(COL_CATEGORIA)
        );
    }

    /**
     * Obtiene una lista de todos los productos de la base de datos.
     * @return Lista de objetos Producto.
     */
    public List<Producto> getAllProductos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                productos.add(mapResultSetToProducto(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener todos los productos: " + e.getMessage());
        }
        return productos;
    }

    /**
     * Inserta un nuevo producto en la base de datos.
     * **IMPORTANTE:** Si idCategoria es 0 en el modelo, se insertará NULL en la columna de la DB.
     * @param producto Objeto Producto a guardar.
     * @return true si la inserción fue exitosa, false en caso contrario.
     */
    public boolean saveProducto(Producto producto) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                COL_NOMBRE + ", " + COL_DESCRIPCION + ", " +
                COL_PRECIO + ", " + COL_STOCK + ", " + COL_CATEGORIA +
                ") VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, producto.getNombreProducto());
            pstmt.setString(2, producto.getDescripcion());
            pstmt.setDouble(3, producto.getPrecio());
            pstmt.setInt(4, producto.getStock());

            // 5. Manejo de id_categoria opcional (NULLable)
            if (producto.getIdCategoria() == 0) {
                // Si es 0 (nulo/sin asignar en Java), enviamos NULL a la base de datos
                pstmt.setNull(5, java.sql.Types.INTEGER);
            } else {
                // Si es cualquier otro valor, enviamos el ID real
                pstmt.setInt(5, producto.getIdCategoria());
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Obtener el ID generado y actualizar el objeto Producto
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        producto.setIdProducto(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar producto: " + e.getMessage());
        }
        return false;
    }

    /**
     * Modifica un producto existente en la base de datos.
     * **IMPORTANTE:** Si idCategoria es 0 en el modelo, se actualizará a NULL en la columna de la DB.
     * @param producto Objeto Producto con los nuevos datos.
     * @return true si la modificación fue exitosa, false en caso contrario.
     */
    public boolean updateProducto(Producto producto) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                COL_NOMBRE + " = ?, " +
                COL_DESCRIPCION + " = ?, " +
                COL_PRECIO + " = ?, " +
                COL_STOCK + " = ?, " +
                COL_CATEGORIA + " = ? " +
                "WHERE " + COL_ID + " = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, producto.getNombreProducto());
            pstmt.setString(2, producto.getDescripcion());
            pstmt.setDouble(3, producto.getPrecio());
            pstmt.setInt(4, producto.getStock());

            // 5. Manejo de id_categoria opcional (NULLable)
            if (producto.getIdCategoria() == 0) {
                // Si es 0 (nulo/sin asignar en Java), enviamos NULL a la base de datos
                pstmt.setNull(5, java.sql.Types.INTEGER);
            } else {
                // Si es cualquier otro valor, enviamos el ID real
                pstmt.setInt(5, producto.getIdCategoria());
            }

            pstmt.setInt(6, producto.getIdProducto()); // Cláusula WHERE

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
        }
        return false;
    }

    /**
     * Elimina un producto de la base de datos por su ID.
     * @param id ID del producto a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public boolean deleteProducto(int id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + COL_ID + " = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
        }
        return false;
    }
}
