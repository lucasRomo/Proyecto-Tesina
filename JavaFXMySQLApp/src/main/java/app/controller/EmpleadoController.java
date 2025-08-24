package app.controller;

import app.model.Persona;
import app.model.Usuario;
import app.model.dao.PersonaDAO;
import app.model.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class EmpleadoController {

    @FXML private Label nombrePersonaLabel;
    @FXML private TextField usuarioField;
    @FXML private TextField contraseniaField;
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";


    private PersonaDAO personaDAO = new PersonaDAO();
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    private Persona personaData; // Objeto para guardar los datos de la persona

    // Método para recibir los datos del controlador anterior
    public void setDatosPersona(String nombre, String apellido, int idTipoDocumento, String numeroDocumento, int idDireccion, String telefono, String email) {
        this.personaData = new Persona(nombre, apellido, idTipoDocumento, numeroDocumento, idDireccion, telefono, email);
        nombrePersonaLabel.setText(nombre + " " + apellido);
    }

    @FXML
    public void handleGuardarEmpleado() {
        if (validarCampos()) {
            // Ya no necesitas obtener el ID aquí porque ya lo recibiste
            // int idTipoDocumento = tipoDocumentoDAO.obtenerIdPorNombre(tipoDocumentoComboBox.getValue());

            int idPersona = personaDAO.insertarPersona(this.personaData);

            if (idPersona != -1) {
                // ... (el resto del código)
                Usuario nuevoUsuario = new Usuario(usuarioField.getText(), contraseniaField.getText());
                nuevoUsuario.setIdPersona(idPersona);

                if (usuarioDAO.insertar(nuevoUsuario)) {
                    mostrarAlerta("Éxito", "Empleado y Usuario registrados exitosamente.");
                    limpiarCampos();
                    Stage stage = (Stage) usuarioField.getScene().getWindow();
                    stage.close();
                } else {
                    mostrarAlerta("Error", "Error al registrar el usuario. Fallo en la tabla Usuario.");
                }
            } else {
                mostrarAlerta("Error", "Error al registrar la persona.");
            }
        }
    }

    private boolean validarCampos() {
        if (usuarioField.getText().isEmpty() || contraseniaField.getText().isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, complete los campos obligatorios del empleado.");
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void limpiarCampos() {
        usuarioField.clear();
        contraseniaField.clear();
    }
}