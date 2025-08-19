package app.controller;

import app.model.Cliente;
import app.model.Persona;
import app.model.dao.ClienteDAO;
import app.model.dao.PersonaDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class ClienteController {

    @FXML private Label nombrePersonaLabel;
    @FXML private TextField razonSocialField;
    @FXML private TextField personaContactoField;
    @FXML private TextField condicionesPagoField;

    private PersonaDAO personaDAO = new PersonaDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();
    private Persona personaData; // Objeto para guardar los datos de la persona del paso anterior

    // Método para recibir los datos del controlador anterior
    public void setDatosPersona(String nombre, String apellido, int idTipoDocumento, String numeroDocumento, int idDireccion, String telefono, String email) {
        this.personaData = new Persona(nombre, apellido, idTipoDocumento, numeroDocumento, idDireccion, telefono, email);
        nombrePersonaLabel.setText(nombre + " " + apellido);
    }

    @FXML
    public void handleGuardarCliente() {
        if (validarCamposCliente()) {
            // 1. Validar si el documento ya existe
            if (personaDAO.existeNumeroDocumento(personaData.getNumeroDocumento())) {
                mostrarAlerta("Error", "El número de documento ya está registrado.");
                return;
            }

            // 2. Insertar en la tabla Persona
            int idPersona = personaDAO.insertarPersona(personaData);

            if (idPersona != -1) {
                // 3. Si la inserción en Persona fue exitosa, insertar en Cliente
                Cliente nuevoCliente = new Cliente(
                        personaData.getNombre(), personaData.getApellido(), personaData.getIdTipoDocumento(),
                        personaData.getNumeroDocumento(), personaData.getIdDireccion(), personaData.getTelefono(),
                        personaData.getEmail(), razonSocialField.getText(), personaContactoField.getText(), condicionesPagoField.getText()
                );

                nuevoCliente.setIdPersona(idPersona);

                if (clienteDAO.insertarCliente(nuevoCliente)) {
                    mostrarAlerta("Éxito", "Cliente registrado exitosamente.");
                    limpiarCampos();
                } else {
                    mostrarAlerta("Error", "Error al registrar el cliente. Fallo en la tabla Cliente.");
                }
            } else {
                mostrarAlerta("Error", "Error al registrar el cliente. Fallo en la tabla Persona.");
            }
        }
    }

    private boolean validarCamposCliente() {
        if (personaContactoField.getText().isEmpty() || condicionesPagoField.getText().isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, complete los campos obligatorios del cliente.");
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
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