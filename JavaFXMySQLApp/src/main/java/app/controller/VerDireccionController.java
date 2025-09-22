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
            direccionActual.setCalle(calleTextField.getText());
            direccionActual.setNumero(numeroTextField.getText());
            direccionActual.setPiso(pisoTextField.getText());
            direccionActual.setDepartamento(deptoTextField.getText());
            direccionActual.setCodigoPostal(codigoPostalTextField.getText());
            direccionActual.setCiudad(ciudadTextField.getText());
            direccionActual.setProvincia(provinciaTextField.getText());
            direccionActual.setPais(paisTextField.getText());

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

        if (calleTextField.getText().isEmpty() || numeroTextField.getText().isEmpty() ||
                codigoPostalTextField.getText().isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, complete todos los campos de dirección obligatorios.", Alert.AlertType.WARNING);
            return false;
        }
        // Nueva validación para el número: comprueba si la cadena solo contiene dígitos.
        else if (!numeroTextField.getText().matches(regexNumeros)) {
            mostrarAlerta("Advertencia", "El campo 'Número' debe contener solo números.", Alert.AlertType.WARNING);
            return false;
        }
        // Validación existente para el código postal.
        else if (codigoPostalTextField.getText().trim().length() != 4) {
            mostrarAlerta("Advertencia", "El código postal debe tener exactamente 4 caracteres.", Alert.AlertType.WARNING);
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