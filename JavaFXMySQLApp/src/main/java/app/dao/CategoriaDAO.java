package app.dao;

import app.model.Categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Categoria.
 * Gestiona la persistencia de las categorías en la base de datos.
 */
public class CategoriaDAO {

    // Constantes de conexión (basadas en EmpleadoDAO)
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

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
        // El SELECT no tiene condición WHERE ya que asumimos que todas las categorías son válidas
        String sql = "SELECT id_categoria, nombre, descripcion FROM Categoria ORDER BY nombre";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Categoria categoria = new Categoria(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                );
                categorias.add(categoria);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener todas las categorías: " + e.getMessage());
        }
        return categorias;
    }

    /**
     * Busca una categoría por su ID.
     * @param id ID de la categoría a buscar.
     * @return Objeto Categoria o null si no se encuentra.
     */
    public Categoria getCategoriaById(int id) {
        String sql = "SELECT id_categoria, nombre, descripcion FROM Categoria WHERE id_categoria = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Categoria(
                            rs.getInt("id_categoria"),
                            rs.getString("nombre"),
                            rs.getString("descripcion")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener categoría por ID: " + e.getMessage());
        }
        return null;
    }
}