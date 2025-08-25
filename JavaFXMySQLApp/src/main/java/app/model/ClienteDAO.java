package app.model.dao;

import app.model.Cliente;
import java.sql.*;

public class ClienteDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public boolean insertarCliente(Cliente cliente, Connection conn) throws SQLException {
        String sql = "INSERT INTO Cliente (id_persona, razon_social, persona_contacto, condiciones_pago) VALUES (?, ?, ?, ?)";

        // Usa la conexión 'conn' que se pasa como parámetro
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cliente.getIdPersona());
            stmt.setString(2, cliente.getRazonSocial());
            stmt.setString(3, cliente.getPersonaContacto());
            stmt.setString(4, cliente.getCondicionesPago());

            stmt.executeUpdate();
            return true;
        }
    }
}
