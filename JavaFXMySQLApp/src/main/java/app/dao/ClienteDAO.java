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

    public Cliente getClienteById(int idCliente, Connection conn) throws SQLException {
        String sql = "SELECT p.*, c.* FROM Persona p JOIN Cliente c ON p.id_persona = c.id_persona WHERE c.id_cliente = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCliente);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
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
                    return cliente;
                }
            }
        }
        return null;
    }

    // Método para obtener un cliente por su ID
    public Cliente getClienteById(int idCliente) {
        String sql = "SELECT p.*, c.* FROM Persona p JOIN Cliente c ON p.id_persona = c.id_persona WHERE c.id_cliente = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCliente);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
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
                    return cliente;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener el cliente por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Retorna null si no se encuentra el cliente
    }

    // Método para modificar los datos de un cliente en la base de datos (edición en línea)
    public boolean modificarCliente(Cliente cliente) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Modificar la tabla Persona (CORRECCIÓN: incluye id_tipo_documento)
            String sqlPersona = "UPDATE Persona " +
                    "SET nombre = ?, apellido = ?, id_tipo_documento = ?, numero_documento = ?, telefono = ?, email = ? " +
                    "WHERE id_persona = ?";

            try (PreparedStatement pstmtPersona = conn.prepareStatement(sqlPersona)) {
                pstmtPersona.setString(1, cliente.getNombre());
                pstmtPersona.setString(2, cliente.getApellido());
                pstmtPersona.setInt(3, cliente.getIdTipoDocumento()); // <--- CORRECCIÓN CLAVE
                pstmtPersona.setString(4, cliente.getNumeroDocumento());
                pstmtPersona.setString(5, cliente.getTelefono());
                pstmtPersona.setString(6, cliente.getEmail());
                pstmtPersona.setInt(7, cliente.getIdPersona());

                if (pstmtPersona.executeUpdate() == 0) {
                    conn.rollback();
                    System.out.println("Error: No se encontró la persona para actualizar.");
                    return false;
                }
            }

            // 2. Modificar la tabla Cliente
            String sqlCliente = "UPDATE Cliente " +
                    "SET razon_social = ?, persona_contacto = ?, condiciones_pago = ? " +
                    "WHERE id_cliente = ?";

            try (PreparedStatement pstmtCliente = conn.prepareStatement(sqlCliente)) {
                pstmtCliente.setString(1, cliente.getRazonSocial());
                pstmtCliente.setString(2, cliente.getPersonaContacto());
                pstmtCliente.setString(3, cliente.getCondicionesPago());
                pstmtCliente.setInt(4, cliente.getIdCliente());

                if (pstmtCliente.executeUpdate() == 0) {
                    // Ya que la FK está en Cliente, si esto falla es porque el cliente fue borrado
                    // Pero la persona ya se actualizó, así que revertimos toda la operación.
                    conn.rollback();
                    System.out.println("Error: No se encontró el cliente para actualizar.");
                    return false;
                }
            }

            conn.commit(); // Confirmar la transacción
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); // Deshacer en caso de error
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("Error al modificar el cliente (transacción revertida): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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