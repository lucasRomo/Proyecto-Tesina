package app.dao;

import app.model.Categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object para la entidad Categoria.
 * Gestiona la persistencia de las categorías en la base de datos.
 */
public class CategoriaDAO {

    // Constantes de conexión
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String TABLE_NAME = "Categoria";
    private static final String COL_ID = "id_categoria";
    private static final String COL_NOMBRE = "nombre";
    private static final String COL_DESCRIPCION = "descripcion";

    /**
     * Método auxiliar para obtener la conexión a la base de datos.
     * @return Objeto Connection.
     * @throws SQLException Si ocurre un error de SQL.
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Obtiene una lista de todas las categorías de la base de datos.
     * @return Lista de objetos Categoria.
     */
    public List<Categoria> getAllCategorias() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT " + COL_ID + ", " + COL_NOMBRE + ", " + COL_DESCRIPCION +
                " FROM " + TABLE_NAME + " ORDER BY " + COL_NOMBRE;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Categoria categoria = new Categoria(
                        rs.getInt(COL_ID),
                        rs.getString(COL_NOMBRE),
                        rs.getString(COL_DESCRIPCION)
                );
                categorias.add(categoria);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener todas las categorías: " + e.getMessage());
        }
        return categorias;
    }

    /**
     * Obtiene un mapa de ID de Categoría a Nombre.
     * Es crucial para mostrar los nombres en la TableView y para el filtro.
     * @return Map<Integer, String> donde la clave es el ID y el valor es el nombre.
     */
    public Map<Integer, String> getCategoriaNamesMap() {
        Map<Integer, String> categoryMap = new HashMap<>();
        String sql = "SELECT " + COL_ID + ", " + COL_NOMBRE + " FROM " + TABLE_NAME;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categoryMap.put(rs.getInt(COL_ID), rs.getString(COL_NOMBRE));
            }

            // Agregar la opción para productos SIN CATEGORÍA (ID 0 en el modelo)
            categoryMap.put(0, "Sin Categoría");

        } catch (SQLException e) {
            System.err.println("Error al obtener el mapa de nombres de categorías: " + e.getMessage());
        }
        return categoryMap;
    }

    /**
     * Inserta una nueva categoría en la base de datos.
     * @param categoria Objeto Categoria a guardar.
     * @return true si la inserción fue exitosa, false en caso contrario.
     */
    public boolean saveCategoria(Categoria categoria) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" + COL_NOMBRE + ", " + COL_DESCRIPCION + ") VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, categoria.getNombre());

            String descripcion = categoria.getDescripcion();
            if (descripcion == null || descripcion.trim().isEmpty()) {
                pstmt.setNull(2, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(2, descripcion);
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Obtener el ID generado y actualizar el objeto Categoria
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        categoria.setIdCategoria(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            // No mostrar el StackTrace completo, solo el error.
            System.err.println("Error al guardar categoría: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica si ya existe una categoría con el nombre especificado.
     * @param nombre El nombre de la categoría a verificar.
     * @return true si el nombre ya está registrado, false en caso contrario.
     */
    public boolean isNombreCategoriaDuplicated(String nombre) {
        // Ignoramos mayúsculas/minúsculas para la unicidad (idealmente usando una función LOWER en SQL)
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + COL_NOMBRE + " = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Si el conteo es mayor a 0, significa que ya existe una categoría con ese nombre.
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar duplicidad de nombre de categoría: " + e.getMessage());
            // En caso de error de DB, asumimos que puede haber duplicado para evitar registros corruptos.
            return true;
        }
        return false;
    }
}