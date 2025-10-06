package app.controller;

import app.dao.PedidoDAO;
import app.model.Pedido;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List; // Importar List

public class CrearPedidoController {

    // ComboBox existentes
    @FXML private ComboBox<String> clienteComboBox;
    @FXML private ComboBox<String> empleadoComboBox;
    @FXML private ComboBox<String> estadoComboBox;
    @FXML private ComboBox<String> metodoPagoComboBox; // ComboBox para el tipo_pago en ComprobantePago

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
        // Inicializar ComboBox de Estado
        estadoComboBox.setItems(FXCollections.observableArrayList(
                "Pendiente", "En Proceso", "Finalizado", "Entregado", "Cancelado"
        ));

        // Inicializar ComboBox de Método de Pago con las opciones requeridas para ComprobantePago
        metodoPagoComboBox.setItems(FXCollections.observableArrayList(
                "Transferencia", "Efectivo", "Tarjeta"
        ));

        // Cargar Clientes
        try {
            List<String> listaClientes = pedidoDAO.getAllClientesDisplay();
            clienteComboBox.setItems(FXCollections.observableArrayList(listaClientes));
        } catch (Exception e) {
            System.err.println("Error al cargar clientes: " + e.getMessage());
            mostrarAlerta("Error de BD", "No se pudieron cargar los clientes.", "Verifica la conexión a la base de datos.", Alert.AlertType.ERROR);
        }

        // Cargar Empleados
        try {
            List<String> listaEmpleados = pedidoDAO.getAllEmpleadosDisplay();
            listaEmpleados.add(0, "0 - Sin Asignar");
            empleadoComboBox.setItems(FXCollections.observableArrayList(listaEmpleados));
        } catch (Exception e) {
            System.err.println("Error al cargar empleados: " + e.getMessage());
        }
    }

    /**
     * Establece el escenario (Stage) de este diálogo (usado solo si se abre como modal).
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }


    /**
     * Maneja el evento de guardar el pedido y regresa al menú de pedidos.
     */
    @FXML
    private void handleGuardar(ActionEvent event) {
        if (isInputValid()) {

            int idCliente = extractIdFromComboBox(clienteComboBox.getSelectionModel().getSelectedItem());
            String empleadoSeleccionado = empleadoComboBox.getSelectionModel().getSelectedItem();
            int idEmpleado = (empleadoSeleccionado != null) ? extractIdFromComboBox(empleadoSeleccionado) : 0;

            String estado = estadoComboBox.getSelectionModel().getSelectedItem();
            // Obtener el tipo de pago para pasarlo al DAO para ComprobantePago
            String tipoPago = metodoPagoComboBox.getSelectionModel().getSelectedItem();
            LocalDateTime fechaCreacion = LocalDateTime.now();

            LocalDateTime fechaEntregaEstimada = (fechaEntregaEstimadaPicker.getValue() != null)
                    ? fechaEntregaEstimadaPicker.getValue().atStartOfDay() : null;

            LocalDateTime fechaFinalizacion = (fechaFinalizacionPicker.getValue() != null)
                    ? fechaFinalizacionPicker.getValue().atStartOfDay() : null;

            String instrucciones = instruccionesArea.getText();
            double montoTotal = Double.parseDouble(montoTotalField.getText());
            double montoEntregado = (montoEntregadoField.getText() != null && !montoEntregadoField.getText().isEmpty())
                    ? Double.parseDouble(montoEntregadoField.getText())
                    : 0.0;


            // Constructor de Pedido MODIFICADO: ya no incluye metodoPago
            Pedido nuevoPedido = new Pedido(
                    idCliente,
                    idEmpleado,
                    fechaCreacion,
                    fechaEntregaEstimada,
                    fechaFinalizacion,
                    estado,
                    instrucciones,
                    montoTotal,
                    montoEntregado
            );

            // LLAMADA AL DAO MODIFICADA: Se pasa el tipoPago como segundo argumento
            if (pedidoDAO.savePedido(nuevoPedido, tipoPago)) {
                mostrarAlerta("Éxito", "Pedido guardado", "El nuevo pedido se ha guardado exitosamente.", Alert.AlertType.INFORMATION);
                volverAlMenuPedidos(event);
            } else {
                mostrarAlerta("Error", "Error al guardar", "No se pudo guardar el pedido ni su comprobante de pago en la base de datos.", Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Método auxiliar para extraer el ID de un String con formato "ID - Nombre".
     */
    private int extractIdFromComboBox(String selectedItem) {
        if (selectedItem == null || selectedItem.isEmpty()) {
            return 0;
        }
        try {
            String idString = selectedItem.split(" - ")[0].trim();
            return Integer.parseInt(idString);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Error al parsear ID desde ComboBox: " + selectedItem);
            mostrarAlerta("Error de Formato", "ID Inválido", "El formato de selección de Cliente/Empleado es incorrecto. Contacte a soporte.", Alert.AlertType.ERROR);
            return 0;
        }
    }


    /**
     * Valida la entrada del usuario.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (clienteComboBox.getSelectionModel().isEmpty()) {
            errorMessage += "Debes seleccionar un cliente.\n";
        }
        if (estadoComboBox.getSelectionModel().isEmpty()) {
            errorMessage += "Debes seleccionar un estado para el pedido.\n";
        }
        if (metodoPagoComboBox.getSelectionModel().isEmpty()) {
            errorMessage += "Debes seleccionar un método de pago.\n";
        }

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

        if (montoEntregadoField.getText() != null && !montoEntregadoField.getText().isEmpty()) {
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
     * Maneja el evento de cancelar y vuelve al menú de pedidos anterior.
     */
    @FXML
    private void handleCancelar(ActionEvent event) {
        volverAlMenuPedidos(event);
    }

    /**
     * Lógica compartida para volver a la vista del menú de Pedidos.
     */
    private void volverAlMenuPedidos(ActionEvent event) {
        try {
            // Carga la vista del menú de pedidos
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pedidosPrimerMenu.fxml"));
            Parent root = loader.load();

            // Obtiene el Stage actual (la ventana)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Configura la nueva escena
            stage.setScene(new Scene(root, 1800, 1000));
            stage.setTitle("Menú de Pedidos");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Carga", "No se pudo volver al menú de pedidos.", "Hubo un error al cargar 'pedidosPrimerMenu.fxml'.", Alert.AlertType.ERROR);
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
