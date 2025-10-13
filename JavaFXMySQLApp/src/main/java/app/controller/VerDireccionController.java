package app.controller;

import app.model.Direccion;
import app.dao.DireccionDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class VerDireccionController {

    @FXML private TextField calleTextField;
    @FXML private TextField numeroTextField;
    @FXML private TextField pisoTextField;
    @FXML private TextField deptoTextField;
    @FXML private TextField codigoPostalTextField;
    @FXML private TextField ciudadTextField;
    @FXML private TextField provinciaTextField;
    @FXML private TextField paisTextField;
    @FXML private Button guardarButton;
    @FXML private Button cancelarButton;

    private DireccionDAO direccionDAO = new DireccionDAO();
    private Direccion direccionActual;

    public void setDireccion(Direccion direccion) {
        this.direccionActual = direccion;
        if (direccion != null) {
            calleTextField.setText(direccion.getCalle());
            numeroTextField.setText(direccion.getNumero());
            pisoTextField.setText(direccion.getPiso());
            deptoTextField.setText(direccion.getDepartamento());
            codigoPostalTextField.setText(direccion.getCodigoPostal());
            ciudadTextField.setText(direccion.getCiudad());
            provinciaTextField.setText(direccion.getProvincia());
            paisTextField.setText(direccion.getPais());
        }
    }

    @FXML
    private void handleModificar() {
        calleTextField.setDisable(false);
        numeroTextField.setDisable(false);
        pisoTextField.setDisable(false);
        deptoTextField.setDisable(false);
        codigoPostalTextField.setDisable(false);
        ciudadTextField.setDisable(false);
        provinciaTextField.setDisable(false);
        paisTextField.setDisable(false);
    }

    @FXML
    private void handleGuardarCambios() {
        if (direccionActual != null) {
            if(validarCamposDireccion()){
                // Actualiza el objeto Dirección con los nuevos valores de los campos
                direccionActual.setCalle(calleTextField.getText().trim());
                direccionActual.setNumero(numeroTextField.getText().trim());
                direccionActual.setPiso(pisoTextField.getText().trim());
                direccionActual.setDepartamento(deptoTextField.getText().trim());
                direccionActual.setCodigoPostal(codigoPostalTextField.getText().trim());
                direccionActual.setCiudad(ciudadTextField.getText().trim());
                direccionActual.setProvincia(provinciaTextField.getText().trim());
                direccionActual.setPais(paisTextField.getText().trim());

                // Llama al método del DAO para actualizar la base de datos
                boolean exito = direccionDAO.modificarDireccion(direccionActual);

                if (exito) {
                    mostrarAlerta("Éxito", "Dirección modificada correctamente.", Alert.AlertType.INFORMATION);
                    Stage stage = (Stage) guardarButton.getScene().getWindow();
                    stage.close();
                } else {
                    mostrarAlerta("Error", "No se pudo modificar la dirección.", Alert.AlertType.ERROR);
                }
            }}
    }

    private boolean validarCamposDireccion() {
        // Expresión regular para verificar si una cadena contiene solo dígitos.
        String regexNumeros = "\\d+";
        // Expresión regular para verificar si una cadena contiene solo letras y espacios.
        // Se permiten letras, acentos y espacios.
        String regexLetras = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";

        String calle = calleTextField.getText().trim();
        String numero = numeroTextField.getText().trim();
        String cp = codigoPostalTextField.getText().trim();
        String ciudad = ciudadTextField.getText().trim();
        String provincia = provinciaTextField.getText().trim();


        // 1. Validación de campos obligatorios
        if (calle.isEmpty() || numero.isEmpty() || cp.isEmpty() || ciudad.isEmpty() || provincia.isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, complete Calle, Número, Código Postal, Ciudad y Provincia.", Alert.AlertType.WARNING);
            return false;
        }

        // 2. Validación de Calle (No debe ser solo números)
        // Usamos String.matches(regexNumeros) para verificar si *toda* la calle es solo números.
        if (calle.matches(regexNumeros)) {
            mostrarAlerta("Advertencia", "El campo 'Calle' debe contener al menos una letra.", Alert.AlertType.WARNING);
            return false;
        }

        // 3. Validación de Número (Solo números)
        if (!numero.matches(regexNumeros)) {
            mostrarAlerta("Advertencia", "El campo 'Número' debe contener solo números.", Alert.AlertType.WARNING);
            return false;
        }

        // 4. Validación de Código Postal (Solo números y 4 dígitos)
        if (!cp.matches(regexNumeros)) {
            mostrarAlerta("Advertencia", "El campo 'Código Postal' debe contener solo números.", Alert.AlertType.WARNING);
            return false;
        }
        if (cp.length() != 4) {
            mostrarAlerta("Advertencia", "El código postal debe tener exactamente 4 dígitos.", Alert.AlertType.WARNING);
            return false;
        }

        // 5. Validación de Ciudad (Solo letras y espacios)
        if (!ciudad.matches(regexLetras)) {
            mostrarAlerta("Advertencia", "El campo 'Ciudad' solo debe contener letras y espacios.", Alert.AlertType.WARNING);
            return false;
        }

        // 6. Validación de Provincia (Solo letras y espacios)
        if (!provincia.matches(regexLetras)) {
            mostrarAlerta("Advertencia", "El campo 'Provincia' solo debe contener letras y espacios.", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancelar() {
        Stage stage = (Stage) cancelarButton.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}