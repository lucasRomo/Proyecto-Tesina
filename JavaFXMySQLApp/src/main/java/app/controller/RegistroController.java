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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import app.model.dao.PersonaDAO;

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
    private PersonaDAO personaDAO = new PersonaDAO();

    @FXML
    public void initialize() {
        List<TipoDocumento> tipos = tipoDocumentoDAO.obtenerTodos();
        for (TipoDocumento tipo : tipos) {
            tipoDocumentoComboBox.getItems().add(tipo.getNombreTipo());
        }
    }

    @FXML
    public void handleSiguienteButton(ActionEvent event) {
        if (validarCamposPersonales() && validarCamposDireccion()) {

            String tipoDocumentoSeleccionado = tipoDocumentoComboBox.getValue();
            String numeroDocumento = numeroDocumentoField.getText().trim();

            // Validaciones de longitud del documento
            if ("DNI".equals(tipoDocumentoSeleccionado)) {
                if (numeroDocumento.length() != 8) {
                    mostrarAlerta("Advertencia", "El DNI debe tener exactamente 8 caracteres.");
                    return;
                }
            } else if ("CUIT".equals(tipoDocumentoSeleccionado) || "CUIL".equals(tipoDocumentoSeleccionado)) {
                if (numeroDocumento.length() != 11) {
                    mostrarAlerta("Advertencia", tipoDocumentoSeleccionado + " debe tener 11 caracteres.");
                    return;
                }
            } else if ("Pasaporte".equals(tipoDocumentoSeleccionado)) {
                if (numeroDocumento.length() < 6 || numeroDocumento.length() > 20) {
                    mostrarAlerta("Advertencia", "El Pasaporte debe tener entre 6 y 20 caracteres.");
                    return;
                }
            }

            // Validación de existencia de documento
            if (personaDAO.verificarSiDocumentoExiste(numeroDocumento)) {
                mostrarAlerta("Error de Registro", "El número de documento que ingresó ya se encuentra registrado.");
                return;
            }

            // Validaciones de formato de email, longitud de código postal y teléfono
            if (codigoPostalField.getText().trim().length() != 4) {
                mostrarAlerta("Advertencia", "El código postal debe tener exactamente 4 caracteres.");
                return;
            }

            int longitudTelefono = telefonoField.getText().trim().length();
            if (longitudTelefono <= 6 || longitudTelefono > 11) {
                mostrarAlerta("Advertencia", "El número de teléfono debe tener entre 7 y 11 dígitos.");
                return;
            }

            String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(emailField.getText());
            if (!matcher.matches()) {
                mostrarAlerta("Advertencia", "El formato del correo electrónico no es válido.");
                return;
            }

            // Si todas las validaciones pasan, se procede con la lógica de negocio
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
        return texto.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+");
    }

    private boolean validarSoloNumeros(String texto) {
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