package app.controller;

import app.dao.DetallePedidoDAO;
import app.dao.PedidoDAO;
import app.model.DetallePedido;
import app.model.Pedido;
import app.util.TicketPDFUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

// Implementar la interfaz Initializable
public class DetallePedidoController implements Initializable {

    // --- Componentes FXML de Información y Totales ---
    @FXML private Label lblIdPedido;
    @FXML private Label lblCliente;
    @FXML private Label lblEstado;
    @FXML private Label lblEmpleado;
    @FXML private TextArea lblInstrucciones;
    @FXML private Label lblTotalPagar;
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private TextField txtMontoEntregado;
    @FXML private Label lblVuelto;

    // --- Componentes FXML de Adición Manual de Producto ---
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtPrecioUnitario;

    // --- Tabla de Detalles ---
    @FXML private TableView<DetallePedido> detallesTable;
    @FXML private TableColumn<DetallePedido, String> descripcionColumn;
    @FXML private TableColumn<DetallePedido, Integer> cantidadColumn;
    @FXML private TableColumn<DetallePedido, Double> precioUnitarioColumn;
    @FXML private TableColumn<DetallePedido, Double> subtotalColumn;
    @FXML private TableColumn<DetallePedido, Void> accionColumn;

    // --- Dependencias y Variables de Estado ---
    private Pedido pedidoActual;
    private PedidoDAO pedidoDAO = new PedidoDAO();
    private DetallePedidoDAO detalleDAO = new DetallePedidoDAO();
    private ObservableList<DetallePedido> detallesDelPedido = FXCollections.observableArrayList();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Inicializa el controlador. Configura la tabla, listeners y validaciones.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Configuración de columnas de la tabla
        descripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        cantidadColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        precioUnitarioColumn.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        // 2. Formato de Moneda para Subtotal y Precio Unitario, usando el CurrencyTableCell
        precioUnitarioColumn.setCellFactory(tc -> new CurrencyTableCell<>());
        subtotalColumn.setCellFactory(tc -> new CurrencyTableCell<>());

        // 3. Configurar la columna de Acción (Botón Eliminar)
        configurarColumnaAccion();

        // 4. Inicializar el ComboBox de Métodos de Pago
        cmbMetodoPago.setItems(FXCollections.observableArrayList(pedidoDAO.getTiposPago()));

        // 5. Agregar listeners para cálculos en tiempo real
        txtMontoEntregado.textProperty().addListener((obs, oldVal, newVal) -> calcularVuelto());

        // Asegurar que solo se ingresen números en Cantidad y Precio Unitario
        setupNumericValidation(txtCantidad);
        setupNumericValidation(txtPrecioUnitario);
        setupNumericValidation(txtMontoEntregado);
    }

    /**
     * Valida que el texto de un TextField sea solo numérico (con punto decimal opcional).
     * @param textField El campo de texto a validar.
     */
    private void setupNumericValidation(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Permite solo dígitos y un punto decimal
            if (!newValue.matches("\\d*([\\.]\\d*)?")) {
                // Si la nueva cadena no es un número válido, revertir al valor anterior
                textField.setText(oldValue);
            }
        });
    }

    /**
     * Configura la columna de acción con un botón para eliminar un detalle de la lista local.
     */
    private void configurarColumnaAccion() {
        accionColumn.setCellFactory(param -> new TableCell<DetallePedido, Void>() {
            private final Button btnEliminar = new Button("Eliminar");

            {
                btnEliminar.setOnAction((ActionEvent event) -> {
                    DetallePedido detalle = getTableView().getItems().get(getIndex());
                    handleEliminarDetalle(detalle);
                });
                btnEliminar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox pane = new HBox(btnEliminar);
                    pane.setStyle("-fx-alignment: center;");
                    setGraphic(pane);
                }
            }
        });
    }


    /**
     * Maneja la eliminación de un detalle de la lista local.
     * @param detalle El detalle a eliminar.
     */
    private void handleEliminarDetalle(DetallePedido detalle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("Eliminar Producto del Pedido");
        alert.setContentText("¿Está seguro de que desea eliminar el producto: " + detalle.getDescripcion() + "?\n\n(Esto no se guardará en la base de datos hasta que finalice el pedido)");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            detallesDelPedido.remove(detalle);
            recalcularTotalPedido();
        }
    }


    /**
     * Carga los datos de un pedido específico en la interfaz.
     * Este método es llamado desde VerPedidosController.
     * @param pedido El objeto Pedido seleccionado.
     */
    public void setPedido(Pedido pedido) {
        this.pedidoActual = pedido;

        // Cargar detalles del pedido desde el DAO (detalles preexistentes)
        List<DetallePedido> detallesExistentes = detalleDAO.getDetallesPorPedido(pedido.getIdPedido());
        detallesDelPedido.setAll(detallesExistentes);
        detallesTable.setItems(detallesDelPedido);

        // Rellenar etiquetas
        lblIdPedido.setText(String.valueOf(pedido.getIdPedido()));
        lblCliente.setText(pedido.getNombreCliente());
        lblEstado.setText(pedido.getEstado());
        lblEmpleado.setText(pedido.getNombreEmpleado());
        lblInstrucciones.setText(pedido.getInstrucciones());

        // Inicializar campos de pago/vuelto
        if (pedido.getMetodoPago() != null && !pedido.getMetodoPago().isEmpty()) {
            cmbMetodoPago.getSelectionModel().select(pedido.getMetodoPago());
        }

        // Si el monto entregado es 0.0, se establece el texto vacío para que no aparezca "0.00"
        if (pedido.getMontoEntregado() > 0.0) {
            txtMontoEntregado.setText(String.format("%.2f", pedido.getMontoEntregado()));
        } else {
            txtMontoEntregado.setText("");
        }

        // Recalcular el total basado en los detalles cargados y el monto total original de la BD
        recalcularTotalPedido();
        calcularVuelto();
    }

    /**
     * Agrega un nuevo detalle de producto a la lista local (anotación manual).
     */
    @FXML
    private void handleAgregarDetalle() {
        try {
            String descripcion = txtDescripcion.getText().trim();
            String cantidadText = txtCantidad.getText().replaceAll(",", ".").trim();
            String precioText = txtPrecioUnitario.getText().replaceAll(",", ".").trim();

            int cantidad = Integer.parseInt(cantidadText);
            double precioUnitario = Double.parseDouble(precioText);

            // 1. Validación
            if (descripcion.isEmpty() || cantidad <= 0 || precioUnitario < 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Datos Inválidos", "Asegúrese de ingresar una descripción, una cantidad válida (>0) y un precio unitario válido (>=0).");
                return;
            }

            // 2. Crear DetallePedido temporal
            DetallePedido nuevoDetalle = new DetallePedido();
            nuevoDetalle.setIdPedido(pedidoActual.getIdPedido());
            nuevoDetalle.setDescripcion(descripcion);
            nuevoDetalle.setCantidad(cantidad);
            nuevoDetalle.setPrecioUnitario(precioUnitario);
            nuevoDetalle.calcularSubtotal();

            // 3. Agregar a la lista local
            detallesDelPedido.add(nuevoDetalle);

            // 4. Limpiar campos y recalcular total
            txtDescripcion.clear();
            txtCantidad.clear();
            txtPrecioUnitario.clear();
            recalcularTotalPedido();

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Entrada", "Formato Inválido", "Por favor, ingrese valores numéricos válidos en Cantidad y Precio Unitario.");
        }
    }

    /**
     * Recalcula el Monto Total del Pedido sumando todos los subtotales de la lista local.
     * Actualiza el modelo Pedido y la etiqueta de la UI.
     */
    private void recalcularTotalPedido() {
        double nuevoTotal = detallesDelPedido.stream()
                .mapToDouble(DetallePedido::getSubtotal)
                .sum();

        // Actualizar el modelo del pedido con el nuevo monto total
        pedidoActual.setMontoTotal(nuevoTotal);

        // --- CORRECCIÓN CLAVE AQUÍ ---
        // Actualizar la UI del total: si es 0, mostrar solo '$ ', si no, formatear.
        if (nuevoTotal == 0.0) {
            lblTotalPagar.setText("$ ");
        } else {
            lblTotalPagar.setText(String.format("$ %.2f", nuevoTotal));
        }
        // --- FIN CORRECCIÓN ---

        calcularVuelto();
    }

    /**
     * Calcula el vuelto al cambiar el monto entregado y valida que sea numérico.
     */
    private void calcularVuelto() {
        double montoTotal = pedidoActual.getMontoTotal();
        double montoEntregado = 0.0;
        try {
            // Manejar comas y vacíos
            String cleanText = txtMontoEntregado.getText().replaceAll(",", ".").trim();
            if (!cleanText.isEmpty()) {
                montoEntregado = Double.parseDouble(cleanText);
            }
        } catch (NumberFormatException e) {
            // Se asume que el setupNumericValidation() maneja la mayoría de los errores.
        }

        double vuelto = montoEntregado - montoTotal;

        // Si el monto total es 0 y el monto entregado es 0 (o la caja de texto está vacía),
        // mostrar solo el signo de dinero.
        if (montoTotal == 0.0 && montoEntregado == 0.0) {
            lblVuelto.setText("$ ");
        } else {
            lblVuelto.setText(String.format("$ %.2f", vuelto));
        }

        // Estilo basado en si el vuelto es positivo o negativo
        if (vuelto < 0) {
            lblVuelto.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc3545;"); // Rojo (falta dinero)
        } else {
            lblVuelto.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;"); // Verde (vuelto)
        }
    }


    // --- Handlers de Botones (Finalizar y Ticket) ---

    /**
     * Finaliza el pedido (cambia el estado a "Retirado") y guarda la información de pago
     * y la lista *completa* de detalles en la base de datos.
     */
    @FXML
    private void finalizarPedido() {
        // 1. Validar campos de pago y detalles
        String metodoPago = cmbMetodoPago.getSelectionModel().getSelectedItem();
        if (metodoPago == null || metodoPago.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Método de Pago Requerido", "Por favor, seleccione el método de pago antes de finalizar.");
            return;
        }

        if (detallesDelPedido.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Detalles Faltantes", "El pedido debe tener al menos un producto para ser finalizado.");
            return;
        }

        double montoEntregado;
        try {
            String cleanText = txtMontoEntregado.getText().replaceAll(",", ".").trim();
            montoEntregado = Double.parseDouble(cleanText.isEmpty() ? "0.0" : cleanText);
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Validación", "Monto Inválido", "Ingrese un monto entregado numérico válido.");
            return;
        }

        double vuelto = montoEntregado - pedidoActual.getMontoTotal();
        // Permite finalización aunque el vuelto sea negativo, pero avisa.
        if (vuelto < 0) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Monto Insuficiente");
            confirmAlert.setHeaderText("Falta Dinero");
            confirmAlert.setContentText(String.format("El monto entregado ($ %.2f) es menor al total ($ %.2f). ¿Desea continuar de todas formas?", montoEntregado, pedidoActual.getMontoTotal()));
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        // 2. Actualizar el modelo del pedido
        pedidoActual.setEstado("Retirado");
        pedidoActual.setMetodoPago(metodoPago);
        pedidoActual.setMontoEntregado(montoEntregado);
        pedidoActual.setFechaFinalizacion(java.time.LocalDateTime.now());
        // El monto total ya fue actualizado por recalcularTotalPedido()

        // 3. Guardar en Base de Datos
        // Esta es la parte crítica: reemplazar todos los detalles
        if (detalleDAO.reemplazarDetalles(pedidoActual.getIdPedido(), detallesDelPedido)) {
            // b) Modificar el Pedido (estado, montos, pago)
            if (pedidoDAO.modificarPedido(pedidoActual)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Pedido Finalizado", "El pedido N° " + pedidoActual.getIdPedido() + " ha sido marcado como 'Retirado' y los detalles actualizados.");
                volverAlMenuPedidos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error de Base de Datos", "Se guardaron los detalles, pero no se pudo actualizar el estado/pago del pedido.");
            }
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error de Base de Datos", "No se pudieron actualizar los detalles del pedido en la base de datos.");
        }
    }

    /**
     * Genera el ticket (comprobante) del pedido actual en formato PDF, usando la lista local de detalles.
     */
    @FXML
    private void generarTicketPDF() {
        if (detallesDelPedido.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Generación de Ticket", "Detalles Faltantes", "No se puede generar un ticket sin productos.");
            return;
        }

        // Abrir diálogo de guardado
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Comprobante de Pedido");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        String nombreSugerido = "Pedido_" + pedidoActual.getIdPedido() + "_Comprobante.pdf";
        fileChooser.setInitialFileName(nombreSugerido);

        Stage stage = (Stage) detallesTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            String filePath = file.getAbsolutePath();
            try {
                // Se usa la lista local de detalles, que incluye las anotaciones manuales
                TicketPDFUtil.generarTicket(
                        pedidoActual,
                        detallesDelPedido,
                        filePath
                );
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Comprobante Generado",
                        "El ticket PDF se ha guardado exitosamente en:\n" + filePath);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error de Archivo",
                        "No se pudo crear el archivo PDF en la ruta especificada. Revise los permisos.");
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error de Generación",
                        "Ocurrió un error al generar el PDF: " + e.getMessage());
            }
        }
    }

    /**
     * Cierra la ventana actual y regresa al menú de pedidos.
     */
    @FXML
    private void volverAlMenuPedidos() {
        // Cierra la ventana modal actual
        Stage stage = (Stage) lblIdPedido.getScene().getWindow();
        stage.close();
    }

    /**
     * Muestra una alerta simple.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String cabecera, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecera);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Clase auxiliar para formatear la columna de precios como moneda, ocultando el '0.00' si el valor es 0.
     */
    private static class CurrencyTableCell<S> extends TableCell<S, Double> {
        @Override
        protected void updateItem(Double price, boolean empty) {
            super.updateItem(price, empty);
            if (empty || price == null) {
                setText(null);
            } else {
                // Si el precio es 0.0, lo muestra como "$ ", sino lo formatea con dos decimales.
                if (price == 0.0) {
                    setText("$ ");
                } else {
                    setText(String.format("$ %.2f", price));
                }
            }
        }
    }
}
