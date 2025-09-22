package app.controller;

import app.dao.PersonaDAO;
import app.model.TipoDocumento;
import app.dao.DireccionDAO;
import app.dao.TipoDocumentoDAO;
import app.model.Direccion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistroEmpleadoController {
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
        // Carga los tipos de documento desde la base de datos
        List<TipoDocumento> tipos = tipoDocumentoDAO.obtenerTodos();
        for (TipoDocumento tipo : tipos) {
            tipoDocumentoComboBox.getItems().add(tipo.getNombreTipo());
        }
    }

    @FXML
    public void handleSiguienteButton(ActionEvent event) {
        if (validarCampos()) {

            String tipoDocumentoSeleccionado = tipoDocumentoComboBox.getValue();
            String numeroDocumento = numeroDocumentoField.getText().trim();
            String email = emailField.getText().trim();
            PersonaDAO personaDAO = new PersonaDAO();

            if (tipoDocumentoSeleccionado == null) {
                mostrarAlerta("Advertencia", "Por favor, seleccione un tipo de documento.");
                return;
            }

            if (numeroDocumento.isEmpty()) {
                mostrarAlerta("Advertencia", "Por favor, ingrese el número de documento.");
                return;
            }

            if ("DNI".equals(tipoDocumentoSeleccionado)) {
                if (numeroDocumento.length() != 8) {
                    mostrarAlerta("Advertencia", "El DNI debe tener exactamente 8 caracteres.");
                    return;
                }
            } else if ("CUIT".equals(tipoDocumentoSeleccionado)) {
                if (numeroDocumento.length() != 11) {
                    mostrarAlerta("Advertencia", "El CUIT debe tener 11 caracteres.");
                    return;
                }
            } else if ("CUIL".equals(tipoDocumentoSeleccionado)) {
                if (numeroDocumento.length() != 11) {
                    mostrarAlerta("Advertencia", "El CUIL debe tener 11 caracteres.");
                    return;
                }
            } else if ("Pasaporte".equals(tipoDocumentoSeleccionado)) {
                if (numeroDocumento.length() < 6 || numeroDocumento.length() > 20) {
                    mostrarAlerta("Advertencia", "El Pasaporte debe tener entre 6 y 20 caracteres.");
                    return;
                }
            }

            if (personaDAO.verificarSiDocumentoExiste(numeroDocumento)) {
                mostrarAlerta("Error de Registro", "El número de documento que ingresó ya se encuentra registrado.");
                return; // Detiene la ejecución para no ir a la siguiente pantalla
            }
            if (personaDAO.verificarSiMailExiste(email)) {
                mostrarAlerta("Error de Registro", "El email que ingresó ya se encuentra registrado.");
                return; // Detiene la ejecución para no ir a la siguiente pantalla
            }

            // Fin verificaciones del DNI //

            if (codigoPostalField.getText().trim().length() != 4) {
                mostrarAlerta("Advertencia", "El código postal debe tener exactamente 4 caracteres.");
                return;
            }

            int longitudTelefono = telefonoField.getText().trim().length();
            if (longitudTelefono <= 6 || longitudTelefono >= 11) {
                mostrarAlerta("Advertencia", "El número de teléfono debe tener entre 7 y 11 dígitos.");
                return;
            }

            try {
                // 1. Insertar la dirección y obtener el ID
                Direccion nuevaDireccion = new Direccion(
                        calleField.getText(), numeroField.getText(), pisoField.getText(),
                        departamentoField.getText(), codigoPostalField.getText(),
                        ciudadField.getText(), provinciaField.getText(), paisField.getText()
                );
                int idDireccion = direccionDAO.insertarDireccion(nuevaDireccion);

                if (idDireccion != -1) {
                    // *** CAMBIO 1: OBTENER EL ID DEL TIPO DE DOCUMENTO ANTES DE PASARLO ***
                    int idTipoDocumento = tipoDocumentoDAO.obtenerIdPorNombre(tipoDocumentoComboBox.getValue());

                    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/registroEmpleadoDatosEspecificos.fxml")));
                    Parent root = loader.load();

                    EmpleadoController empleadoController = loader.getController();

                    // *** CAMBIO 2: PASAR EL ID EN LUGAR DEL NOMBRE ***
                    empleadoController.setDatosPersona(
                            nombreField.getText(),
                            apellidoField.getText(),
                            idTipoDocumento, // Ahora pasamos el ID
                            numeroDocumentoField.getText(),
                            idDireccion,
                            telefonoField.getText(),
                            emailField.getText()
                    );

                    // Obtener el 'Stage' (ventana) actual y mostrar la nueva escena
                    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root, app.MainApp.WINDOW_WIDTH, app.MainApp.WINDOW_HEIGHT));
                    stage.setTitle("Registro de Empleado");
                    stage.show();
                } else {
                    mostrarAlerta("Error", "No se pudo registrar la dirección. Intente de nuevo.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validarCampos() {
        // Validar que los campos personales no estén vacíos
        String regexNumeros = "\\d+";
        if (nombreField.getText().isEmpty() || apellidoField.getText().isEmpty() ||
                tipoDocumentoComboBox.getValue() == null || numeroDocumentoField.getText().isEmpty() ||
                emailField.getText().isEmpty() || telefonoField.getText().isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, complete todos los campos personales obligatorios.");
            return false;
        }

        if (!numeroField.getText().matches(regexNumeros)) {
            mostrarAlerta("Advertencia", "El campo 'Número' debe contener solo números.", Alert.AlertType.WARNING);
            return false;
        }

        // Validar que los campos de dirección no estén vacíos
        if (calleField.getText().isEmpty() || numeroField.getText().isEmpty() ||
                codigoPostalField.getText().isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, complete todos los campos de dirección obligatorios.");
            return false;
        }

        // Validar el formato del correo electrónico
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(emailField.getText());
        if (!matcher.matches()) {
            mostrarAlerta("Advertencia", "El formato del correo electrónico no es válido.");
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

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}