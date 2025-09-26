package app.controller;

import app.model.Empleado;
import app.model.EmpleadoDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class EmpleadoController {

    @FXML private TextField nombreField;
    @FXML private TextField apellidoField;
    @FXML private DatePicker fechaContratacionPicker;
    @FXML private TextField cargoField;
    @FXML private TextField salarioField;
    @FXML private TextField idPersonaField;

    private EmpleadoDAO empleadoDAO;
    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void initialize() {
        empleadoDAO = new EmpleadoDAO();
    }

    @FXML
    private void handleGuardar() {
        try {
            String nombre = nombreField.getText();
            String apellido = apellidoField.getText();
            LocalDate fechaContratacion = fechaContratacionPicker.getValue();
            String cargo = cargoField.getText();
            double salario = Double.parseDouble(salarioField.getText());
            int idPersona = Integer.parseInt(idPersonaField.getText());

            // Crea el objeto Empleado usando el constructor con todos los datos
            Empleado nuevoEmpleado = new Empleado(fechaContratacion, cargo, salario, idPersona);
            nuevoEmpleado.setNombre(nombre);
            nuevoEmpleado.setApellido(apellido);

            boolean exito = empleadoDAO.insertarEmpleado(nuevoEmpleado);

            if (exito) {
                mostrarAlerta("Éxito", "Empleado guardado exitosamente.", Alert.AlertType.INFORMATION);
                if (dialogStage != null) {
                    dialogStage.close();
                }
            } else {
                mostrarAlerta("Error", "No se pudo guardar el empleado.", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Por favor, ingrese valores válidos para salario e ID de persona.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelar() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void setDatosPersona(String text, String text1, int idTipoDocumento, String text2, int idDireccion, String text3, String text4) {
    }
}