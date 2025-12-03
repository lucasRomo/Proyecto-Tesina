package app.dao;

import app.model.Producto;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object para la entidad Producto.
 * Gestiona la persistencia de los productos en la base de datos y provee datos estadísticos.
 * Maneja el id_categoria como un campo NULLable, donde 0 en Java significa NULL en SQL.
 */
public class ProductoDAO {

    // Constantes de conexión
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static final Logger LOGGER = Logger.getLogger(ProductoDAO.class.getName());

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
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Falta el driver de MySQL.", e);
        }
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
     * Obtiene un producto de la base de datos por su ID.
     * @param id ID del producto a buscar.
     * @return Objeto Producto si se encuentra, o null en caso contrario.
     */
    public Producto getProductoById(int id) {
        Producto producto = null;
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID + " = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    producto = mapResultSetToProducto(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener producto por ID: " + e.getMessage());
        }
        return producto;
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

            // La descripción puede ser nula/vacía
            String descripcion = producto.getDescripcion();
            if (descripcion == null || descripcion.trim().isEmpty()) {
                pstmt.setNull(2, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(2, descripcion);
            }

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
     * @param producto Objeto Producto con los nuevos datos.
     * @return true si la modificación **afectó alguna fila** (es decir, hubo un cambio real), false en caso contrario.
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

            // La descripción puede ser nula/vacía
            String descripcion = producto.getDescripcion();
            if (descripcion == null || descripcion.trim().isEmpty()) {
                pstmt.setNull(2, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(2, descripcion);
            }

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
            return affectedRows > 0; // True solo si MySQL reporta una fila afectada

        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica si un nombre de producto ya existe, excluyendo opcionalmente el producto actual.
     * @param nombre El nombre a verificar.
     * @param idProductoToExclude El ID del producto que se está editando (0 si es un nuevo registro).
     * @return true si el nombre ya está en uso por otro producto, false si es único.
     */
    public boolean isNombreProductoDuplicated(String nombre, int idProductoToExclude) {
        // Busca si existe un producto con el mismo nombre y cuyo ID no sea el que estamos editando
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME +
                " WHERE " + COL_NOMBRE + " = ? AND " + COL_ID + " != ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            pstmt.setInt(2, idProductoToExclude);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar duplicidad de nombre de producto: " + e.getMessage());
        }
        return false;
    }

    // ----------------------------------------------------------------------------------
    // MÉTODOS DE ESTADÍSTICAS (PARA InformesController) - CORREGIDOS
    // ----------------------------------------------------------------------------------

    /**
     * Obtiene la suma de unidades vendidas (cantidad) por categoría de producto,
     * filtrada por el rango de fechas de la FINALIZACIÓN del pedido.
     * Retorna un Map<Nombre Categoría, Total Unidades>.
     */
    public Map<String, Integer> getUnidadesVendidasPorCategoria(LocalDate inicio, LocalDate fin) {
        Map<String, Integer> data = new HashMap<>();

        // Consulta SQL CORRECTA: Suma las cantidades (dp.cantidad) de productos en pedidos finalizados.
        String sql = "SELECT c.nombre, SUM(dp.cantidad) AS total_unidades_vendidas " +
                "FROM DetallePedido dp " +
                "JOIN Producto p ON dp.id_producto = p.id_producto " +
                "LEFT JOIN Categoria c ON p.id_categoria = c.id_categoria " + // Usamos LEFT JOIN para incluir productos sin categoría
                "JOIN Pedido ped ON dp.id_pedido = ped.id_pedido " +
                "WHERE ped.fecha_finalizacion BETWEEN ? AND ? " +
                "GROUP BY c.nombre " +
                "HAVING SUM(dp.cantidad) > 0 " + // Asegura que solo se muestren categorías con ventas > 0
                "ORDER BY total_unidades_vendidas DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // inicio.atStartOfDay() y fin.atTime(23, 59, 59) para incluir el día completo
            stmt.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(fin.atTime(23, 59, 59)));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String nombreCategoria = rs.getString("nombre");
                    int totalUnidades = rs.getInt("total_unidades_vendidas");

                    // Si nombreCategoria es NULL (producto sin categoría), lo etiquetamos
                    if (nombreCategoria == null || nombreCategoria.trim().isEmpty()) {
                        nombreCategoria = "Sin Categoría";
                    }

                    // La cláusula HAVING ya filtra los 0, pero lo mantenemos para el nombre nulo
                    data.put(nombreCategoria, totalUnidades);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener unidades vendidas por categoría.", e);
            System.err.println("Error al obtener unidades vendidas por categoría: " + e.getMessage());
        }
        return data;
    }
}
