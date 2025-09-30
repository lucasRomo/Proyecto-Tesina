package app.controller;

import app.dao.PedidoDAO;
import app.model.Pedido;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class CrearPedidoController {

    // ComboBox existentes
    @FXML private ComboBox<String> clienteComboBox; // NOTA: Idealmente ComboBox<Cliente>
    @FXML private ComboBox<String> empleadoComboBox; // NOTA: Idealmente ComboBox<Empleado>
    @FXML private ComboBox<String> estadoComboBox;

    // NUEVO: ComboBox para el método de pago (Asegúrate de que el fx:id coincida con el FXML)
    @FXML private ComboBox<String> metodoPagoComboBox;

    // Otros campos
    @FXML private DatePicker fechaEntregaEstimadaPicker;
    @FXML private DatePicker fechaFinalizacionPicker;
    @FXML private TextField montoTotalField;
    @FXML private TextField montoEntregadoField;
    @FXML private TextArea instruccionesArea;

    private PedidoDAO pedidoDAO = new PedidoDAO();
    private Stage dialogStage;

    /**
     * Inicializa el controlador. Se llama automáticamente después de que se carga el FXML.
     */
    @FXML
    public void initialize() {
        // Inicializar ComboBox de Estado (ejemplo)
        estadoComboBox.setItems(FXCollections.observableArrayList(
                "Pendiente", "En Proceso", "Finalizado", "Entregado", "Cancelado"
        ));

        // NUEVO: Inicializar ComboBox de Método de Pago con las opciones requeridas
        metodoPagoComboBox.setItems(FXCollections.observableArrayList(
                "Transferencia", "Efectivo"
        ));

        // TODO: Cargar y rellenar clienteComboBox y empleadoComboBox desde el DAO
        // Estos son datos de ejemplo y deben ser reemplazados por tu lógica de carga real.
        clienteComboBox.setItems(FXCollections.observableArrayList("Cliente A", "Cliente B"));
        empleadoComboBox.setItems(FXCollections.observableArrayList("Empleado 1", "Empleado 2"));
    }

    /**
     * Establece el escenario (Stage) de este diálogo.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }


    /**
     * Maneja el evento de guardar el pedido, incluyendo el nuevo método de pago.
     */
    @FXML
    private void handleGuardar() {
        if (isInputValid()) {
            // NOTA: Reemplaza estos IDs estáticos con la lógica para obtener el ID real
            // de los objetos Cliente y Empleado seleccionados en los ComboBox.
            int idCliente = 1;
            int idEmpleado = 1;

            String estado = estadoComboBox.getSelectionModel().getSelectedItem();
            String metodoPago = metodoPagoComboBox.getSelectionModel().getSelectedItem(); // <-- VALOR DEL NUEVO CAMPO
            LocalDateTime fechaCreacion = LocalDateTime.now();

            LocalDateTime fechaEntregaEstimada = (fechaEntregaEstimadaPicker.getValue() != null)
                    ? fechaEntregaEstimadaPicker.getValue().atStartOfDay() : null;

            LocalDateTime fechaFinalizacion = (fechaFinalizacionPicker.getValue() != null)
                    ? fechaFinalizacionPicker.getValue().atStartOfDay() : null;

            String instrucciones = instruccionesArea.getText();
            double montoTotal = Double.parseDouble(montoTotalField.getText());
            double montoEntregado = Double.parseDouble(montoEntregadoField.getText());

            // Usamos el constructor actualizado del modelo Pedido
            Pedido nuevoPedido = new Pedido(
                    idCliente,
                    idEmpleado,
                    fechaCreacion,
                    fechaEntregaEstimada,
                    fechaFinalizacion,
                    estado,
                    metodoPago, // <-- Se pasa el nuevo método de pago
                    instrucciones,
                    montoTotal,
                    montoEntregado
            );

            if (pedidoDAO.savePedido(nuevoPedido)) {
                mostrarAlerta("Éxito", "Pedido guardado", "El nuevo pedido se ha guardado exitosamente.", Alert.AlertType.INFORMATION);
                dialogStage.close();
            } else {
                mostrarAlerta("Error", "Error al guardar", "No se pudo guardar el pedido en la base de datos.", Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Valida la entrada del usuario, incluyendo el nuevo campo de pago.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (clienteComboBox.getSelectionModel().isEmpty()) {
            errorMessage += "Debes seleccionar un cliente.\n";
        }
        if (estadoComboBox.getSelectionModel().isEmpty()) {
            errorMessage += "Debes seleccionar un estado para el pedido.\n";
        }
        if (metodoPagoComboBox.getSelectionModel().isEmpty()) { // <-- VALIDACIÓN DEL NUEVO CAMPO
            errorMessage += "Debes seleccionar un método de pago.\n";
        }

        // Validación de montos
        if (montoTotalField.getText() == null || montoTotalField.getText().isEmpty()) {
            errorMessage += "El monto total no puede estar vacío.\n";
        } else {
            try {
                if (Double.parseDouble(montoTotalField.getText()) < 0) {
                    errorMessage += "El monto total debe ser un número positivo.\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "El monto total debe ser un número válido.\n";
            }
        }

        // Validación básica de montoEntregado (si aplica)
        if (montoEntregadoField.getText() == null || montoEntregadoField.getText().isEmpty()) {
            // Opcional: Podrías permitir que esté vacío si el monto entregado es 0.
        } else {
            try {
                if (Double.parseDouble(montoEntregadoField.getText()) < 0) {
                    errorMessage += "El monto entregado debe ser un número positivo o cero.\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "El monto entregado debe ser un número válido.\n";
            }
        }


        if (errorMessage.isEmpty()) {
            return true;
        } else {
            mostrarAlerta("Campos Inválidos", "Por favor, corrige los campos", errorMessage, Alert.AlertType.ERROR);
            return false;
        }
    }


    /**
     * Maneja el evento de cancelar y cierra la ventana.
     */
    @FXML
    private void handleCancelar() {
        // Obtener el Stage de cualquier componente FXML
        if (dialogStage != null) {
            dialogStage.close();
        } else {
            // Esto se usa en caso de que el dialogStage no se haya inyectado correctamente
            ((Stage) clienteComboBox.getScene().getWindow()).close();
        }
    }

    /**
     * Método auxiliar para mostrar alertas.
     */
    private void mostrarAlerta(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}