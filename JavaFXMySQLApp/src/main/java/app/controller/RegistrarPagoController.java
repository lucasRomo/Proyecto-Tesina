package app.controller;

import app.model.PedidoDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

public class RegistrarPagoController {

    @FXML
    private TextField montoPagoField;
    @FXML
    private DatePicker fechaPagoPicker;
    @FXML
    private TextField metodoPagoField;

    private PedidoDAO pedidoDAO;

    @FXML
    private void initialize() {
        pedidoDAO = new PedidoDAO();
    }

    @FXML
    private void handleGuardar() {
        try {
            double monto = Double.parseDouble(montoPagoField.getText());
            LocalDate fecha = fechaPagoPicker.getValue();
            String metodo = metodoPagoField.getText();

            // Lógica para guardar el pago en la base de datos
            // Puedes crear una clase Pago y un DAO de Pago para esto

            mostrarAlerta("Éxito", "Pago registrado exitosamente.", Alert.AlertType.INFORMATION);
            closeStage();
        } catch (NumberFormatException e) {
            mostrarAlerta("Error de Formato", "Por favor, ingrese un valor numérico para el monto.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Error", "Ocurrió un error al registrar el pago.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelar() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) montoPagoField.getScene().getWindow();
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