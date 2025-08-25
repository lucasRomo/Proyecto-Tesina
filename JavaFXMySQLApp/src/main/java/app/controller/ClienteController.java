package app.controller;

import app.model.Cliente;
import app.model.Persona;
import app.model.dao.ClienteDAO;
import app.model.dao.PersonaDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ClienteController {

    @FXML private Label nombrePersonaLabel;
    @FXML private TextField razonSocialField;
    @FXML private TextField personaContactoField;
    @FXML private TextField condicionesPagoField;

    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private PersonaDAO personaDAO;
    private ClienteDAO clienteDAO;

    private Persona personaData; // Objeto para guardar los datos de la persona del paso anterior

    // Constructor para inicializar los DAOs, una práctica recomendada
    public ClienteController() {
        this.personaDAO = new PersonaDAO();
        this.clienteDAO = new ClienteDAO();
    }

    // Método para recibir los datos del controlador anterior
    public void setDatosPersona(String nombre, String apellido, int idTipoDocumento, String numeroDocumento, int idDireccion, String telefono, String email) {
        this.personaData = new Persona(nombre, apellido, idTipoDocumento, numeroDocumento, idDireccion, telefono, email);
        nombrePersonaLabel.setText(nombre + " " + apellido);
    }

    @FXML
    public void handleGuardarCliente(ActionEvent event) {
        if (!validarCamposCliente()) {
            return;
        }

        // Se elimina la validación del documento duplicado de aquí y se sugiere moverla
        // al controlador del primer formulario (RegistroClienteController).

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false); // Inicia la transacción

            // 1. Insertar en la tabla Persona
            // Nota: Se asume que insertarPersona en PersonaDAO ahora acepta una conexión.
            int idPersona = personaDAO.insertarPersona(personaData, conn);

            if (idPersona != -1) {
                // 2. Si la inserción en Persona fue exitosa, insertar en Cliente
                Cliente nuevoCliente = new Cliente(
                        personaData.getNombre(), personaData.getApellido(), personaData.getIdTipoDocumento(),
                        personaData.getNumeroDocumento(), personaData.getIdDireccion(), personaData.getTelefono(),
                        personaData.getEmail(), razonSocialField.getText(), personaContactoField.getText(), condicionesPagoField.getText()
                );
                nuevoCliente.setIdPersona(idPersona);

                // Nota: Se asume que insertarCliente en ClienteDAO ahora acepta una conexión.
                if (clienteDAO.insertarCliente(nuevoCliente, conn)) {
                    conn.commit(); // Confirma la transacción
                    mostrarAlerta("Éxito", "Cliente registrado exitosamente.", Alert.AlertType.INFORMATION);
                    limpiarCampos();
                    Stage stage = (Stage)((Button)event.getSource()).getScene().getWindow();
                    stage.close();

                } else {
                    conn.rollback(); // Deshace todo si falla la inserción de Cliente
                    mostrarAlerta("Error", "Error al registrar el cliente. Fallo en la tabla Cliente.", Alert.AlertType.ERROR);
                }
            } else {
                conn.rollback(); // Deshace la inserción de la persona si ya estaba insertada
                mostrarAlerta("Error", "Error al registrar el cliente. Fallo en la tabla Persona.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); // Deshace la transacción en caso de excepción
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            mostrarAlerta("Error", "Ocurrió un error en la base de datos. La operación fue cancelada.", Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close(); // Cierra la conexión
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validarCamposCliente() {
        if (razonSocialField.getText().isEmpty() || personaContactoField.getText().isEmpty() ||
                condicionesPagoField.getText().isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, complete todos los campos obligatorios del cliente.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void limpiarCampos() {
        razonSocialField.clear();
        personaContactoField.clear();
        condicionesPagoField.clear();
    }
}
