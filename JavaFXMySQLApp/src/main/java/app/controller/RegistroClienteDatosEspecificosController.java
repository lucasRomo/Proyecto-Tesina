package app.controller;

import app.model.Cliente;
import app.model.Persona;
import app.dao.ClienteDAO;
import app.dao.PersonaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox; // CLAVE: Importar ChoiceBox
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RegistroClienteDatosEspecificosController {

    @FXML private Label nombrePersonaLabel;

    // CLAVE: Reemplazamos TextField por ChoiceBox
    @FXML private ChoiceBox<String> razonSocialChoiceBox;

    @FXML private TextField personaContactoField;
    @FXML private TextField condicionesPagoField;

    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private PersonaDAO personaDAO;
    private ClienteDAO clienteDAO;
    private Persona personaData;

    private ClienteController clienteController;
    private RegistroController registroController;

    public RegistroClienteDatosEspecificosController() {
        this.personaDAO = new PersonaDAO();
        this.clienteDAO = new ClienteDAO();
    }

    // CLAVE: Método para inicializar el ChoiceBox
    @FXML
    public void initialize() {
        ObservableList<String> opcionesRazonSocial = FXCollections.observableArrayList(
                "Responsable Inscripto", "Monotributista", "Persona"
        );
        razonSocialChoiceBox.setItems(opcionesRazonSocial);
        // Opcional: Establecer un valor por defecto
        // razonSocialChoiceBox.getSelectionModel().selectFirst();
    }

    // Métodos setter (sin cambios)
    public void setClienteController(ClienteController controller) {
        this.clienteController = controller;
    }

    public void setRegistroController(RegistroController controller) {
        this.registroController = controller;
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

        // Obtener el valor seleccionado del ChoiceBox
        String razonSocialSeleccionada = razonSocialChoiceBox.getValue();

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false);

            personaData.setIdTipoPersona(1);

            int idPersona = personaDAO.insertarPersona(personaData, conn);

            if (idPersona != -1) {
                Cliente nuevoCliente = new Cliente(
                        personaData.getNombre(), personaData.getApellido(), personaData.getIdTipoDocumento(),
                        personaData.getNumeroDocumento(), personaData.getIdDireccion(), personaData.getTelefono(),
                        personaData.getEmail(),
                        // CLAVE: Usamos la razón social seleccionada
                        razonSocialSeleccionada,
                        personaContactoField.getText(),
                        condicionesPagoField.getText(), "Activo"
                );
                nuevoCliente.setIdPersona(idPersona);

                if (clienteDAO.insertarCliente(nuevoCliente, conn)) {
                    conn.commit();
                    mostrarAlerta("Éxito", "Cliente registrado exitosamente.", Alert.AlertType.INFORMATION);

                    if (clienteController != null) {
                        clienteController.refreshClientesTable();
                    }

                    Stage stageActual = (Stage)((Button)event.getSource()).getScene().getWindow();
                    stageActual.close();

                    if (registroController != null) {
                        registroController.cerrarVentana();
                    }

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

    @FXML
    public void handleCancelarRegistro(ActionEvent event) {
        Stage stageActual = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stageActual.close();

        if (registroController != null) {
            registroController.cerrarVentana();
        }
    }

    private boolean validarCamposCliente() {
        // CLAVE: Validar que se haya seleccionado una Razón Social
        if (razonSocialChoiceBox.getValue() == null) {
            mostrarAlerta("Advertencia", "Por favor, seleccione una Razón Social.", Alert.AlertType.WARNING);
            return false;
        }

        // Validar otros campos
        if (personaContactoField.getText().isEmpty() || condicionesPagoField.getText().isEmpty()) {
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