package app.controller;

import app.dao.DetallePedidoDAO;
import app.dao.PedidoDAO;
import app.dao.ProductoDAO;
import app.model.DetallePedido;
import app.model.Pedido;
import app.model.Producto;
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

    // --- Componentes FXML de Adición de Producto (MODIFICADOS) ---
    // ESTO REEMPLAZA A txtDescripcion en la nueva lógica del FXML
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private TextField txtCantidad;
    // Se mantiene, pero se ignora en la lógica de agregar detalle, ya que el precio viene del Producto
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
    private ProductoDAO productoDAO = new ProductoDAO();
    private ObservableList<DetallePedido> detallesDelPedido = FXCollections.observableArrayList();
    private ObservableList<Producto> productosDisponibles = FXCollections.observableArrayList();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Configuración de columnas de la tabla
        descripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        cantidadColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        precioUnitarioColumn.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        // 2. Formato de Moneda
        precioUnitarioColumn.setCellFactory(tc -> new CurrencyTableCell<>());
        subtotalColumn.setCellFactory(tc -> new CurrencyTableCell<>());

        // 3. Configurar la columna de Acción
        configurarColumnaAccion();

        // 4. Inicializar el ComboBox de Métodos de Pago
        cmbMetodoPago.setItems(FXCollections.observableArrayList(pedidoDAO.getTiposPago()));

        // 5. Agregar listeners para cálculos en tiempo real
        txtMontoEntregado.textProperty().addListener((obs, oldVal, newVal) -> calcularVuelto());

        // Asegurar que solo se ingresen números
        setupNumericValidation(txtCantidad);
        setupNumericValidation(txtPrecioUnitario);
        setupNumericValidation(txtMontoEntregado);

        // 6. Cargar y Configurar el ComboBox de Productos
        cargarProductosDisponibles();
        configurarComboBoxProducto();
    }

    /**
     * Carga todos los productos desde la base de datos y los asigna al ComboBox.
     */
    private void cargarProductosDisponibles() {
        // Asumiendo que productoDAO.getAllProductos() devuelve List<Producto>
        productosDisponibles.setAll(productoDAO.getAllProductos());
        cmbProducto.setItems(productosDisponibles);
    }

    /**
     * Configura el CellFactory para mostrar el nombre del producto y su precio en el ComboBox.
     */
    private void configurarComboBoxProducto() {
        // Cómo se muestra el ítem en la lista desplegable
        cmbProducto.setCellFactory(lv -> new ListCell<Producto>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Mostrar Nombre y Precio del producto
                    setText(item.getNombreProducto() + " ($ " + String.format("%.2f", item.getPrecio()) + ")");
                }
            }
        });

        // Cómo se muestra el ítem seleccionado en el control (usa el mismo formato)
        cmbProducto.setButtonCell(cmbProducto.getCellFactory().call(null));

        // Listener para actualizar el campo de precio unitario (oculto)
        cmbProducto.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && txtPrecioUnitario != null) {
                // Esto es solo para mantener la coherencia si el campo fuera visible.
                txtPrecioUnitario.setText(String.format("%.2f", newVal.getPrecio()));
            } else if (txtPrecioUnitario != null) {
                txtPrecioUnitario.clear();
            }
        });
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
     */
    private void handleEliminarDetalle(DetallePedido detalle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("Eliminar Producto del Pedido");
        String mensaje = "¿Está seguro de que desea eliminar el producto: " + detalle.getDescripcion() + "?";
        if (detalle.getIdProducto() != 0) {
            mensaje += "\nID Producto: " + detalle.getIdProducto();
        }
        mensaje += "\n\n(Esto no se guardará en la base de datos hasta que finalice el pedido)";

        alert.setContentText(mensaje);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            detallesDelPedido.remove(detalle);
            recalcularTotalPedido();
        }
    }


    /**
     * Carga los datos de un pedido específico en la interfaz.
     */
    public void setPedido(Pedido pedido) {
        this.pedidoActual = pedido;

        List<DetallePedido> detallesExistentes = detalleDAO.getDetallesPorPedido(pedido.getIdPedido());
        detallesDelPedido.setAll(detallesExistentes);
        detallesTable.setItems(detallesDelPedido);

        lblIdPedido.setText(String.valueOf(pedido.getIdPedido()));
        lblCliente.setText(pedido.getNombreCliente());
        lblEstado.setText(pedido.getEstado());
        lblEmpleado.setText(pedido.getNombreEmpleado());
        lblInstrucciones.setText(pedido.getInstrucciones());

        if (pedido.getMetodoPago() != null && !pedido.getMetodoPago().isEmpty()) {
            cmbMetodoPago.getSelectionModel().select(pedido.getMetodoPago());
        }

        if (pedido.getMontoEntregado() > 0.0) {
            txtMontoEntregado.setText(String.format("%.2f", pedido.getMontoEntregado()));
        } else {
            txtMontoEntregado.setText("");
        }

        recalcularTotalPedido();
        calcularVuelto();
    }

    /**
     * Agrega un Producto real a la lista local de detalles, usando su ID y precio.
     */
    public void agregarProducto(Producto producto, int cantidad) {
        if (producto == null || cantidad <= 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Datos Inválidos", "Seleccione un producto y una cantidad válida.");
            return;
        }

        DetallePedido nuevoDetalle = new DetallePedido();
        nuevoDetalle.setIdPedido(pedidoActual.getIdPedido());
        nuevoDetalle.setIdProducto(producto.getIdProducto());
        nuevoDetalle.setDescripcion(producto.getNombreProducto());
        nuevoDetalle.setCantidad(cantidad);
        nuevoDetalle.setPrecioUnitario(producto.getPrecio());
        nuevoDetalle.calcularSubtotal();

        detallesDelPedido.add(nuevoDetalle);
        recalcularTotalPedido();
    }


    /**
     * **MÉTODO MODIFICADO (Ahora usa el ComboBox de Productos)**
     * Agrega un nuevo detalle de producto a la lista local usando el producto seleccionado.
     */
    @FXML
    private void handleAgregarDetalle() {
        Producto productoSeleccionado = cmbProducto.getSelectionModel().getSelectedItem();

        try {
            // Reemplaza comas por puntos y maneja el caso de cadena vacía (lo convierte a "0")
            String cantidadText = txtCantidad.getText().replaceAll(",", ".").trim();
            int cantidad = Integer.parseInt(cantidadText.isEmpty() ? "0" : cantidadText);

            // 1. Validación de ComboBox y Cantidad
            if (productoSeleccionado == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Selección Inválida", "Por favor, seleccione un producto de la lista.");
                return;
            }

            if (cantidad <= 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Cantidad Inválida", "La cantidad debe ser mayor a 0.");
                return;
            }

            // 2. Crear DetallePedido
            agregarProducto(productoSeleccionado, cantidad);

            // 3. Limpiar campos
            cmbProducto.getSelectionModel().clearSelection();
            txtCantidad.clear();

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Entrada", "Formato Inválido", "Por favor, ingrese un valor numérico válido en Cantidad.");
        }
    }

    /**
     * Recalcula el Monto Total del Pedido sumando todos los subtotales de la lista local.
     */
    private void recalcularTotalPedido() {
        double nuevoTotal = detallesDelPedido.stream()
                .mapToDouble(DetallePedido::getSubtotal)
                .sum();

        pedidoActual.setMontoTotal(nuevoTotal);

        if (nuevoTotal == 0.0) {
            lblTotalPagar.setText("$ ");
        } else {
            lblTotalPagar.setText(String.format("$ %.2f", nuevoTotal));
        }

        calcularVuelto();
    }

    /**
     * Calcula el vuelto al cambiar el monto entregado.
     */
    private void calcularVuelto() {
        double montoTotal = pedidoActual.getMontoTotal();
        double montoEntregado = 0.0;
        try {
            String cleanText = txtMontoEntregado.getText().replaceAll(",", ".").trim();
            if (!cleanText.isEmpty()) {
                montoEntregado = Double.parseDouble(cleanText);
            }
        } catch (NumberFormatException e) {
            // El validador ya debería haber capturado esto
        }

        double vuelto = montoEntregado - montoTotal;

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
        if (detalleDAO.reemplazarDetalles(pedidoActual.getIdPedido(), detallesDelPedido)) {
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
     * Genera el ticket (comprobante) del pedido actual en formato PDF.
     */
    @FXML
    private void generarTicketPDF() {
        if (detallesDelPedido.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Generación de Ticket", "Detalles Faltantes", "No se puede generar un ticket sin productos.");
            return;
        }

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
     * Clase auxiliar para formatear la columna de precios como moneda.
     */
    private static class CurrencyTableCell<S> extends TableCell<S, Double> {
        @Override
        protected void updateItem(Double price, boolean empty) {
            super.updateItem(price, empty);
            if (empty || price == null) {
                setText(null);
            } else {
                if (price == 0.0) {
                    setText("$ ");
                } else {
                    setText(String.format("$ %.2f", price));
                }
            }
        }
    }
}