package app.controller;

import app.MainApp;
import app.model.Direccion;
import app.model.TipoDocumento;
import app.model.dao.DireccionDAO;
import app.model.dao.TipoDocumentoDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class RegistroController {
    @FXML private TextField nombreField;
    @FXML private TextField apellidoField;
    @FXML private ComboBox<String> tipoDocumentoComboBox;
    @FXML private TextField numeroDocumentoField;
    @FXML private TextField emailField;
    @FXML private TextField telefonoField;
    @FXML private TextField calleField;
    @FXML private TextField numeroField;
    @FXML private TextField pisoField;
    @FXML private TextField departamentoField;
    @FXML private TextField codigoPostalField;
    @FXML private TextField ciudadField;
    @FXML private TextField provinciaField;
    @FXML private TextField paisField;

    private TipoDocumentoDAO tipoDocumentoDAO = new TipoDocumentoDAO();
    private DireccionDAO direccionDAO = new DireccionDAO();

    @FXML
    public void initialize() {
        TipoDocumentoDAO tipoDocumentoDAO = new TipoDocumentoDAO();
        List<TipoDocumento> tipos = tipoDocumentoDAO.obtenerTodos();
        for (TipoDocumento tipo : tipos) {
            tipoDocumentoComboBox.getItems().add(tipo.getNombreTipo());
        }
    }



    @FXML
    public void handleSiguienteButton(ActionEvent event) {
        if (validarCamposPersonales() && validarCamposDireccion()) {
            try {
                // Insertar dirección y obtener ID
                Direccion nuevaDireccion = new Direccion(
                        calleField.getText(), numeroField.getText(), pisoField.getText(),
                        departamentoField.getText(), codigoPostalField.getText(),
                        ciudadField.getText(), provinciaField.getText(), paisField.getText()
                );
                int idDireccion = direccionDAO.insertarDireccion(nuevaDireccion);

                if (idDireccion != -1) {
                    // Obtener ID de tipo de documento
                    int idTipoDocumento = tipoDocumentoDAO.obtenerIdPorNombre(tipoDocumentoComboBox.getValue());

                    // Cargar la siguiente vista y pasar datos
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/registroClienteDatosEspecificos.fxml"));
                    Parent root = loader.load();
                    ClienteController clienteController = loader.getController();

                    // Pasar datos a la siguiente vista
                    clienteController.setDatosPersona(
                            nombreField.getText(), apellidoField.getText(), idTipoDocumento,
                            numeroDocumentoField.getText(), idDireccion, telefonoField.getText(), emailField.getText()
                    );

                    Stage stage = (Stage)((Button)event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root, app.MainApp.WINDOW_WIDTH, app.MainApp.WINDOW_HEIGHT));
                    stage.setTitle("Datos del Cliente");
                    stage.show();
                } else {
                    mostrarAlerta("Error", "No se pudo registrar la dirección. Intente de nuevo.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validarCamposPersonales() {
        if (this.nombreField.getText().isEmpty() || this.apellidoField.getText().isEmpty() ||
                this.tipoDocumentoComboBox.getValue() == null || this.numeroDocumentoField.getText().isEmpty() ||
                this.emailField.getText().isEmpty() || this.telefonoField.getText().isEmpty()) {

            this.mostrarAlerta("Advertencia", "Por favor, complete todos los campos personales obligatorios.");
            return false;
        }

        if (!this.validarSoloLetras(this.nombreField.getText())) {
            this.mostrarAlerta("Advertencia", "El nombre no puede contener números. Por favor, ingrese caracteres válidos.");
            return false;
        }

        if (!this.validarSoloLetras(this.apellidoField.getText())) {
            this.mostrarAlerta("Advertencia", "El apellido no puede contener números. Por favor, ingrese caracteres válidos.");
            return false;
        }

        if (!this.validarSoloNumeros(this.telefonoField.getText())) {
            this.mostrarAlerta("Advertencia", "El teléfono solo puede contener números.");
            return false;

        }

        return true;
    }

    private boolean validarCamposDireccion() {
        if (calleField.getText().isEmpty() || numeroField.getText().isEmpty() ||
                codigoPostalField.getText().isEmpty() || ciudadField.getText().isEmpty() ||
                provinciaField.getText().isEmpty() || paisField.getText().isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, complete todos los campos de dirección obligatorios.");
            return false;
        }
        return true;
    }

    private boolean validarSoloLetras(String texto) {
        // La expresión regular [a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+ verifica que la cadena
        // solo contenga letras (mayúsculas y minúsculas), letras acentuadas, la ñ
        // y espacios en blanco.
        return texto.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+");
    }

    private boolean validarSoloNumeros(String texto) {
        // La expresión regular "\\d+" verifica que la cadena solo contenga dígitos (0-9).
        return texto.matches("\\d+");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}