package app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

public class AsignarPedidoController {

    @FXML
    private ComboBox<String> empleadoComboBox;

    @FXML
    private void initialize() {
        // Ejemplo de cómo cargar datos en el ComboBox
        ObservableList<String> empleados = FXCollections.observableArrayList("Empleado 1", "Empleado 2", "Empleado 3");
        empleadoComboBox.setItems(empleados);
    }

    @FXML
    private void handleAsignar() {
        String selectedEmpleado = empleadoComboBox.getSelectionModel().getSelectedItem();
        if (selectedEmpleado != null) {
            mostrarAlerta("Asignación Exitosa", "El pedido ha sido asignado a " + selectedEmpleado, Alert.AlertType.INFORMATION);
            closeStage();
        } else {
            mostrarAlerta("Error de Asignación", "Por favor, seleccione un empleado.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelar() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) empleadoComboBox.getScene().getWindow();
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
