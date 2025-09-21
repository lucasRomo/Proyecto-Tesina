package app.controller;

import app.model.Pedido;
import app.model.PedidoDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class CrearPedidoController {

    @FXML
    private TextField idClienteField;
    @FXML
    private DatePicker fechaCreacionPicker;
    @FXML
    private DatePicker fechaEntregaEstimadaPicker;
    @FXML
    private DatePicker fechaFinalizacionPicker;
    @FXML
    private TextField estadoField;
    @FXML
    private TextArea instruccionesArea;
    @FXML
    private TextField montoTotalField;
    @FXML
    private TextField montoEntregadoField;

    private PedidoDAO pedidoDAO = new PedidoDAO();
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleGuardar() {
        try {
            int idCliente = Integer.parseInt(idClienteField.getText());
            LocalDateTime fechaCreacion = fechaCreacionPicker.getValue() != null ? fechaCreacionPicker.getValue().atStartOfDay() : null;
            LocalDateTime fechaEntregaEstimada = fechaEntregaEstimadaPicker.getValue() != null ? fechaEntregaEstimadaPicker.getValue().atStartOfDay() : null;
            LocalDateTime fechaFinalizacion = fechaFinalizacionPicker.getValue() != null ? fechaFinalizacionPicker.getValue().atStartOfDay() : null;
            String estado = estadoField.getText();
            String instrucciones = instruccionesArea.getText();
            double montoTotal = Double.parseDouble(montoTotalField.getText());
            double montoEntregado = Double.parseDouble(montoEntregadoField.getText());

            Pedido nuevoPedido = new Pedido(idCliente, fechaCreacion, fechaEntregaEstimada, fechaFinalizacion, estado, instrucciones, montoTotal, montoEntregado);
            boolean exito = pedidoDAO.savePedido(nuevoPedido);

            if (exito) {
                mostrarAlerta("Éxito", "Pedido creado exitosamente.", Alert.AlertType.INFORMATION);
                closeStage();
            } else {
                mostrarAlerta("Error", "No se pudo crear el pedido.", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error de Formato", "Por favor, ingrese valores válidos en los campos numéricos.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Error", "Ocurrió un error al guardar el pedido.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelar() {
        closeStage();
    }

    private void closeStage() {
        if (stage != null) {
            stage.close();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}