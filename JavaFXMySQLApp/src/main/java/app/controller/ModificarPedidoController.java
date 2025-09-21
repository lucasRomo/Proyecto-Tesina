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

public class ModificarPedidoController {

    @FXML
    private TextField idPedidoField;
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
    private Pedido pedido;
    private Stage stage;

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
        if (pedido != null) {
            idPedidoField.setText(String.valueOf(pedido.getIdPedido()));
            idClienteField.setText(String.valueOf(pedido.getIdCliente()));

            if (pedido.getFechaCreacion() != null) {
                fechaCreacionPicker.setValue(pedido.getFechaCreacion().toLocalDate());
            }
            if (pedido.getFechaEntregaEstimada() != null) {
                fechaEntregaEstimadaPicker.setValue(pedido.getFechaEntregaEstimada().toLocalDate());
            }
            if (pedido.getFechaFinalizacion() != null) {
                fechaFinalizacionPicker.setValue(pedido.getFechaFinalizacion().toLocalDate());
            }

            estadoField.setText(pedido.getEstado());
            instruccionesArea.setText(pedido.getInstrucciones());
            montoTotalField.setText(String.valueOf(pedido.getMontoTotal()));
            montoEntregadoField.setText(String.valueOf(pedido.getMontoEntregado()));
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleGuardar() {
        try {
            if (pedido != null) {
                pedido.setIdCliente(Integer.parseInt(idClienteField.getText()));
                pedido.setFechaCreacion(fechaCreacionPicker.getValue() != null ? fechaCreacionPicker.getValue().atStartOfDay() : null);
                pedido.setFechaEntregaEstimada(fechaEntregaEstimadaPicker.getValue() != null ? fechaEntregaEstimadaPicker.getValue().atStartOfDay() : null);
                pedido.setFechaFinalizacion(fechaFinalizacionPicker.getValue() != null ? fechaFinalizacionPicker.getValue().atStartOfDay() : null);
                pedido.setEstado(estadoField.getText());
                pedido.setInstrucciones(instruccionesArea.getText());
                pedido.setMontoTotal(Double.parseDouble(montoTotalField.getText()));
                pedido.setMontoEntregado(Double.parseDouble(montoEntregadoField.getText()));

                boolean exito = pedidoDAO.updatePedido(pedido);

                if (exito) {
                    mostrarAlerta("Éxito", "Pedido modificado exitosamente.", Alert.AlertType.INFORMATION);
                    closeStage();
                } else {
                    mostrarAlerta("Error", "No se pudo modificar el pedido.", Alert.AlertType.ERROR);
                }
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error de Formato", "Por favor, ingrese valores válidos.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Error", "Ocurrió un error al modificar el pedido.", Alert.AlertType.ERROR);
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