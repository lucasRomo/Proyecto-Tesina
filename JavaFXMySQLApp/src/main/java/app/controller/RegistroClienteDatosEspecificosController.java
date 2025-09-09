package app.controller;

import app.model.Cliente;
import app.model.Persona;
import app.model.dao.ClienteDAO;
import app.model.dao.PersonaDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RegistroClienteDatosEspecificosController {

    @FXML private Label nombrePersonaLabel;
    @FXML private TextField razonSocialField;
    @FXML private TextField personaContactoField;
    @FXML private TextField condicionesPagoField;

    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private PersonaDAO personaDAO;
    private ClienteDAO clienteDAO;
    private Persona personaData;

    // Referencia al controlador de la ventana principal
    private ClienteController clienteController;

    public RegistroClienteDatosEspecificosController() {
        this.personaDAO = new PersonaDAO();
        this.clienteDAO = new ClienteDAO();
    }

    // Método setter para recibir la referencia
    public void setClienteController(ClienteController controller) {
        this.clienteController = controller;
    }

    public void setDatosPersona(String nombre, String apellido, int idTipoDocumento, String numeroDocumento, int idDireccion, String telefono, String email) {
        this.personaData = new Persona(nombre, apellido, idTipoDocumento, numeroDocumento, idDireccion, telefono, email);
        if (nombrePersonaLabel != null) {
            nombrePersonaLabel.setText(nombre + " " + apellido);
        }
    }

    @FXML
    public void handleGuardarCliente(ActionEvent event) {
        if (!validarCamposCliente()) {
            return;
        }

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false);

            int idPersona = personaDAO.insertarPersona(personaData, conn);

            if (idPersona != -1) {
                Cliente nuevoCliente = new Cliente(
                        personaData.getNombre(), personaData.getApellido(), personaData.getIdTipoDocumento(),
                        personaData.getNumeroDocumento(), personaData.getIdDireccion(), personaData.getTelefono(),
                        personaData.getEmail(), razonSocialField.getText(), personaContactoField.getText(),
                        condicionesPagoField.getText(), "Activo"
                );
                nuevoCliente.setIdPersona(idPersona);

                if (clienteDAO.insertarCliente(nuevoCliente, conn)) {
                    conn.commit();
                    mostrarAlerta("Éxito", "Cliente registrado exitosamente.", Alert.AlertType.INFORMATION);

                    // Llama al método del controlador principal para refrescar la tabla
                    if (clienteController != null) {
                        clienteController.refreshClientesTable();
                    }

                    Stage stage = (Stage)((Button)event.getSource()).getScene().getWindow();
                    stage.close();
                } else {
                    conn.rollback();
                    mostrarAlerta("Error", "Error al registrar el cliente. Fallo en la tabla Cliente.", Alert.AlertType.ERROR);
                }
            } else {
                conn.rollback();
                mostrarAlerta("Error", "Error al registrar el cliente. Fallo en la tabla Persona.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) { ex.printStackTrace(); }
            mostrarAlerta("Error", "Ocurrió un error en la base de datos. La operación fue cancelada.", Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) { e.printStackTrace(); }
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
}