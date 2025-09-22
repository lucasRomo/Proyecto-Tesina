package app.dao;

import app.model.Cliente;
import java.sql.*;

import app.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClienteDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Método para insertar un cliente en la base de datos
    public boolean insertarCliente(Cliente cliente, Connection conn) throws SQLException {
        String sql = "INSERT INTO Cliente (id_persona, razon_social, persona_contacto, condiciones_pago, estado) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cliente.getIdPersona());
            stmt.setString(2, cliente.getRazonSocial());
            stmt.setString(3, cliente.getPersonaContacto());
            stmt.setString(4, cliente.getCondicionesPago());
            stmt.setString(5, cliente.getEstado());

            stmt.executeUpdate();
            return true;
        }
    }

    // Método para traer todos los clientes de la base de datos
    public ObservableList<Cliente> getAllClientes() {
        ObservableList<Cliente> clientes = FXCollections.observableArrayList();
        String sql = "SELECT p.*, c.* FROM Persona p JOIN Cliente c ON p.id_persona = c.id_persona";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Se crea un objeto Cliente con todos los datos
                Cliente cliente = new Cliente(
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getInt("id_tipo_documento"),
                        rs.getString("numero_documento"),
                        rs.getInt("id_direccion"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("razon_social"),
                        rs.getString("persona_contacto"),
                        rs.getString("condiciones_pago"),
                        rs.getString("estado")
                );
                cliente.setIdPersona(rs.getInt("id_persona"));
                cliente.setIdCliente(rs.getInt("id_cliente"));
                clientes.add(cliente);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener los clientes: " + e.getMessage());
            e.printStackTrace();
        }
        return clientes;
    }

    // Método para modificar los datos de un cliente en la base de datos (edición en línea)
    public boolean modificarCliente(Cliente cliente) {
        String sql = "UPDATE Cliente c " +
                "JOIN Persona p ON c.id_persona = p.id_persona " +
                "SET p.nombre = ?, p.apellido = ?, p.numero_documento = ?, p.telefono = ?, p.email = ?, " +
                "c.razon_social = ?, c.persona_contacto = ?, c.condiciones_pago = ? " +
                "WHERE c.id_cliente = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getApellido());
            pstmt.setString(3, cliente.getNumeroDocumento());
            pstmt.setString(4, cliente.getTelefono());
            pstmt.setString(5, cliente.getEmail());
            pstmt.setString(6, cliente.getRazonSocial());
            pstmt.setString(7, cliente.getPersonaContacto());
            pstmt.setString(8, cliente.getCondicionesPago());
            pstmt.setInt(9, cliente.getIdCliente());

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.out.println("Error al modificar el cliente: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Nuevo método para modificar solo el estado del cliente
    public boolean modificarEstadoCliente(int idCliente, String nuevoEstado) {
        String sql = "UPDATE Cliente SET estado = ? WHERE id_cliente = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, idCliente);

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.out.println("Error al modificar el estado del cliente: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}