package app.controller;

import app.dao.DetallePedidoDAO;
import app.dao.PedidoDAO;
import app.model.DetallePedido;
import app.model.Pedido;
import app.util.TicketPDFUtil; // Necesitas este import
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser; // Importante para guardar el archivo
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DetallePedidoController {

    // --- Componentes FXML del Detalle del Pedido ---
    @FXML
    private Label lblIdPedido;
    @FXML
    private Label lblCliente;
    @FXML
    private Label lblEstado;
    @FXML
    private Label lblFechaCreacion;
    @FXML
    private Label lblFechaEntrega;
    @FXML
    private Label lblEmpleado;
    @FXML
    private Label lblInstrucciones;
    @FXML
    private TableView<DetallePedido> detallesTable;
    @FXML
    private TableColumn<DetallePedido, String> descripcionColumn;
    @FXML
    private TableColumn<DetallePedido, Integer> cantidadColumn;
    @FXML
    private TableColumn<DetallePedido, Double> precioUnitarioColumn;
    @FXML
    private TableColumn<DetallePedido, Double> subtotalColumn;
    @FXML
    private Label lblTotalPagar;
    @FXML
    private ComboBox<String> cmbMetodoPago; // Asumo que tienes un ComboBox para el método de pago
    @FXML
    private TextField txtMontoEntregado;
    @FXML
    private Label lblVuelto;

    // --- Dependencias y Variables de Estado ---
    private Pedido pedidoActual;
    private PedidoDAO pedidoDAO = new PedidoDAO();
    private DetallePedidoDAO detalleDAO = new DetallePedidoDAO();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // --- Método de Inicialización (Llamado después de que el FXML carga) ---
    @FXML
    public void initialize() {
        // Inicialización de columnas de la tabla
        descripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        cantidadColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        precioUnitarioColumn.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        // Asegurar que las columnas numéricas se alineen a la derecha y establecer el formato de moneda.
        precioUnitarioColumn.setCellFactory(tc -> new TableCell<DetallePedido, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$ %.2f", price));
                }
            }
        });
        subtotalColumn.setCellFactory(tc -> new TableCell<DetallePedido, Double>() {
            @Override
            protected void updateItem(Double subtotal, boolean empty) {
                super.updateItem(subtotal, empty);
                if (empty || subtotal == null) {
                    setText(null);
                } else {
                    setText(String.format("$ %.2f", subtotal));
                }
            }
        });

        // Inicializar el ComboBox de Métodos de Pago
        // Nota: Asegúrate de que PedidoDAO tiene el método getTiposPago() implementado
        cmbMetodoPago.setItems(FXCollections.observableArrayList(pedidoDAO.getTiposPago()));

        // Agregar listener al campo de monto entregado para calcular el vuelto
        txtMontoEntregado.textProperty().addListener((obs, oldVal, newVal) -> calcularVuelto());
    }

    /**
     * Carga los datos de un pedido específico en la interfaz.
     * @param pedido El objeto Pedido seleccionado.
     */
    public void setPedido(Pedido pedido) {
        this.pedidoActual = pedido;

        // Cargar detalles del pedido
        // Nota: Asegúrate de que DetallePedidoDAO tiene el método getDetallesPorPedido()
        List<DetallePedido> detalles = detalleDAO.getDetallesPorPedido(pedido.getIdPedido());
        detallesTable.setItems(FXCollections.observableArrayList(detalles));

        // Rellenar etiquetas
        lblIdPedido.setText(String.valueOf(pedido.getIdPedido()));
        lblCliente.setText(pedido.getNombreCliente());
        lblEstado.setText(pedido.getEstado());
        lblFechaCreacion.setText(pedido.getFechaCreacion().format(formatter));

        String fechaEntregaStr = pedido.getFechaEntregaEstimada() != null ?
                pedido.getFechaEntregaEstimada().format(formatter) : "N/A";
        lblFechaEntrega.setText(fechaEntregaStr);

        lblEmpleado.setText(pedido.getNombreEmpleado());
        lblInstrucciones.setText(pedido.getInstrucciones());
        lblTotalPagar.setText(String.format("$ %.2f", pedido.getMontoTotal()));

        // Cargar y seleccionar método de pago y monto entregado si ya existen
        if (pedido.getMetodoPago() != null && !pedido.getMetodoPago().isEmpty()) {
            cmbMetodoPago.getSelectionModel().select(pedido.getMetodoPago());
        }
        // Usar String.valueOf para evitar errores si el monto es nulo o 0.0
        txtMontoEntregado.setText(String.format("%.2f", pedido.getMontoEntregado()));

        calcularVuelto();
    }

    /**
     * Calcula el vuelto al cambiar el monto entregado.
     */
    private void calcularVuelto() {
        try {
            double montoTotal = pedidoActual.getMontoTotal();
            String montoEntregadoText = txtMontoEntregado.getText().replaceAll("[^\\d.]", ""); // Limpia el texto

            if (montoEntregadoText.isEmpty()) {
                lblVuelto.setText("$ 0.00");
                return;
            }

            double montoEntregado = Double.parseDouble(montoEntregadoText);
            double vuelto = montoEntregado - montoTotal;

            lblVuelto.setText(String.format("$ %.2f", vuelto));

        } catch (NumberFormatException e) {
            // Manejar entrada no válida (ej. doble punto decimal)
            lblVuelto.setText("Error");
        }
    }


    // --- Handlers de Botones ---

    /**
     * Finaliza el pedido (cambia el estado a "Retirado") y guarda la información de pago.
     * (Corregido: Eliminada la referencia a 'event' en la documentación)
     */
    @FXML
    private void finalizarPedido() {
        // 1. Validar campos
        String metodoPago = cmbMetodoPago.getSelectionModel().getSelectedItem();
        if (metodoPago == null || metodoPago.isEmpty() || metodoPago.equals("N/A")) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Método de Pago Requerido", "Por favor, seleccione el método de pago antes de finalizar.");
            return;
        }

        double montoEntregado;
        try {
            // Reemplazar comas por puntos y limpiar caracteres no numéricos
            String cleanText = txtMontoEntregado.getText().replaceAll(",", ".").replaceAll("[^\\d.]", "");
            montoEntregado = Double.parseDouble(cleanText);
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Validación", "Monto Inválido", "Ingrese un monto entregado numérico válido.");
            return;
        }

        double vuelto = montoEntregado - pedidoActual.getMontoTotal();
        if (vuelto < 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Monto Insuficiente", "El monto entregado es menor al monto total del pedido. Vuelto: " + String.format("$ %.2f", vuelto));
            return;
        }

        // 2. Actualizar el modelo del pedido
        pedidoActual.setEstado("Retirado");
        pedidoActual.setMetodoPago(metodoPago);
        pedidoActual.setMontoEntregado(montoEntregado);
        pedidoActual.setFechaFinalizacion(java.time.LocalDateTime.now()); // Establecer la fecha de finalización

        // 3. Llamar al DAO para modificar el pedido
        // Nota: Asegúrate de que PedidoDAO tiene el método modificarPedido(Pedido pedido)
        if (pedidoDAO.modificarPedido(pedidoActual)) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Pedido Finalizado", "El pedido N° " + pedidoActual.getIdPedido() + " ha sido marcado como 'Retirado'.");

            // Opcional: Redirigir o actualizar la vista principal
            volverAlMenuPedidos();

        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error de Base de Datos", "No se pudo actualizar el pedido en la base de datos.");
        }
    }

    /**
     * Genera el ticket (comprobante) del pedido actual en formato PDF.
     * ESTE ES EL MÉTODO CORREGIDO para manejar 3 argumentos.
     */
    @FXML
    private void generarTicketPDF() {
        // 1. Abrir diálogo para que el usuario elija dónde guardar el archivo
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Comprobante de Pedido");

        // Configurar la extensión por defecto como PDF
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);

        // Sugerir un nombre de archivo
        String nombreSugerido = "Pedido_" + pedidoActual.getIdPedido() + "_Comprobante.pdf";
        fileChooser.setInitialFileName(nombreSugerido);

        // Obtener la Stage actual para el diálogo (Mejor práctica en JavaFX)
        Stage stage = (Stage) detallesTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            // Obtener la ruta absoluta del archivo
            String filePath = file.getAbsolutePath();

            try {
                // 2. Llamada CORREGIDA: Se pasan los 3 argumentos (Pedido, Detalles, Ruta)
                // Nota: Asegúrate de que TicketPDFUtil.generarTicket(Pedido, List<DetallePedido>, String) existe
                TicketPDFUtil.generarTicket(
                        pedidoActual,
                        detallesTable.getItems(),
                        filePath
                );

                // 3. Mostrar confirmación
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Comprobante Generado",
                        "El ticket PDF se ha guardado exitosamente en:\n" + filePath);

            } catch (FileNotFoundException e) {
                // Error si no se tienen permisos de escritura
                e.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error de Archivo",
                        "No se pudo crear el archivo PDF en la ruta especificada. Revise los permisos.");
            } catch (Exception e) {
                // Cualquier otro error de generación (ej. iText no configurado)
                e.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error de Generación",
                        "Ocurrió un error al generar el PDF. Asegúrese de tener iText 7 configurado.");
            }
        } else {
            // El usuario canceló
            mostrarAlerta(Alert.AlertType.WARNING, "Cancelado", "Operación Cancelada",
                    "La generación del comprobante PDF fue cancelada por el usuario.");
        }
    }

    /**
     * Cierra la ventana actual y regresa al menú de pedidos.
     */
    @FXML
    private void volverAlMenuPedidos() {
        try {
            Stage stage = (Stage) lblIdPedido.getScene().getWindow();
            // Asumo que la siguiente escena es la de ListaPedidosController
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/view/ListaPedidosView.fxml"));
            Parent root = loader.load();

            // Si quieres actualizar algo en la vista de lista:
            // ListaPedidosController controller = loader.getController();
            // controller.cargarPedidosActivos();

            stage.setScene(new Scene(root, 1366, 768));
            stage.setTitle("Gestión de Pedidos");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error de Navegación", "No se pudo cargar la vista principal de pedidos.");
        }
    }

    /**
     * Muestra una alerta simple de JavaFX.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String encabezado, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}