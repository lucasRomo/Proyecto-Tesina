package app.controller;

import app.dao.PedidoDAO;
import app.model.Pedido;
// AGREGADO: Importaciones requeridas para Auditoría y Transacción
import app.dao.HistorialActividadDAO;
import app.controller.SessionManager; // Necesario para la auditoría (asumiendo que existe)
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
// FIN AGREGADO

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.awt.Desktop;

public class VerPedidosController implements Initializable {

    // CONFIGURACIÓN DE CONEXIÓN (Debe coincidir con la de otros controllers)
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    // FIN CONFIGURACIÓN

    // Formato para mostrar la fecha de creación (dd-MM-yyyy HH:mm)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML private TableView<Pedido> pedidosTable;
    @FXML private TableColumn<Pedido, Integer> idPedidoColumn;
    @FXML private TableColumn<Pedido, String> clienteColumn;
    @FXML private TableColumn<Pedido, String> empleadoColumn;
    @FXML private TableColumn<Pedido, String> estadoColumn;
    @FXML private TableColumn<Pedido, Double> montoTotalColumn;
    @FXML private TableColumn<Pedido, Double> montoEntregadoColumn;
    @FXML private TableColumn<Pedido, LocalDateTime> fechaCreacionColumn;
    @FXML private TableColumn<Pedido, String> instruccionesColumn;
    @FXML private TableColumn<Pedido, Void> ticketColumn;
    @FXML private TableColumn<Pedido, Void> comprobantePagoColumn;

    @FXML private ComboBox<String> empleadoFilterComboBox;
    @FXML private ComboBox<String> estadoFilterComboBox;
    @FXML private ComboBox<String> metodoPagoFilterComboBox;

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    // AGREGADO: DAO de historial
    private final HistorialActividadDAO historialDAO = new HistorialActividadDAO();
    // CLAVE: Variable para guardar el estado original antes de la edición de CELDA
    private Pedido pedidoOriginal;

    private int idEmpleadoFiltro = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pedidosTable.setEditable(true);

        // --- AGREGADO: Listener para capturar la copia original ---
        // Al seleccionar una fila, se guarda su estado original para la auditoría.
        pedidosTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                this.pedidoOriginal = crearCopiaPedido(newVal);
            }
        });
        // --- FIN AGREGADO ---

        // --- Configuración de Propiedades ---
        idPedidoColumn.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        clienteColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        empleadoColumn.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        montoTotalColumn.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));
        montoEntregadoColumn.setCellValueFactory(new PropertyValueFactory<>("montoEntregado"));
        instruccionesColumn.setCellValueFactory(new PropertyValueFactory<>("instrucciones"));

        // Configurar la columna de Creación con el tipo LocalDateTime y formato personalizado
        fechaCreacionColumn.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        fechaCreacionColumn.setCellFactory(column -> new FormattedDateTableCell<>(DATE_FORMATTER));
        fechaCreacionColumn.setEditable(false);

        // Deshabilitar edición en columnas que no deben cambiar
        clienteColumn.setEditable(false);
        empleadoColumn.setEditable(false);

        // --- 1. Columna ESTADO (ComboBoxTableCell) ---
        ObservableList<String> estados = FXCollections.observableArrayList(
                "Pendiente", "En Proceso", "Finalizado", "Entregado", "Cancelado", "Retirado"
        );
        estadoColumn.setCellFactory(ComboBoxTableCell.forTableColumn(estados));
        estadoColumn.setOnEditCommit(event -> {
            Pedido pedido = event.getRowValue();
            String nuevoEstado = event.getNewValue();
            String estadoAnterior = event.getOldValue();

            if (estadoAnterior.equals(nuevoEstado)) { return; }

            if (nuevoEstado.equalsIgnoreCase("Retirado")) {
                pedido.setFechaFinalizacion(LocalDateTime.now());
                // ALERTA ESPECÍFICA MANTENIDA: Esta alerta es para confirmar una acción importante (finalización/retiro)
                mostrarAlerta("Éxito", "El pedido ha sido marcado como Retirado.", Alert.AlertType.INFORMATION);
            } else if (pedido.getEstado() != null && pedido.getEstado().equalsIgnoreCase("Retirado") && !nuevoEstado.equalsIgnoreCase("Retirado")) {
                pedido.setFechaFinalizacion(null);
            }

            pedido.setEstado(nuevoEstado);
            guardarCambiosEnBD(pedido, "Estado");
        });

        // --- 2. Columna Ticket/Factura (Botón) ---
        configurarColumnaTicket();

        // --- 3. Columna Comprobante Pago (Botón) ---
        configurarColumnaComprobante();

        // --- 4 & 5. Columnas NUMÉRICAS (Double) ---
        montoTotalColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        montoTotalColumn.setOnEditCommit(event -> {
            Double nuevoValor = event.getNewValue();
            if (nuevoValor != null && nuevoValor >= 0) {
                if (event.getOldValue().equals(nuevoValor)) { return; }
                event.getRowValue().setMontoTotal(nuevoValor);
                guardarCambiosEnBD(event.getRowValue(), "Monto Total");
            } else {
                mostrarAlerta("Advertencia", "El monto total debe ser un valor numérico positivo.", Alert.AlertType.WARNING);
                pedidosTable.refresh();
            }
        });

        montoEntregadoColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        montoEntregadoColumn.setOnEditCommit(event -> {
            Double nuevoValor = event.getNewValue();
            if (nuevoValor != null && nuevoValor >= 0) {
                if (event.getOldValue().equals(nuevoValor)) { return; }
                event.getRowValue().setMontoEntregado(nuevoValor);
                guardarCambiosEnBD(event.getRowValue(), "Monto Entregado");
            } else {
                mostrarAlerta("Advertencia", "El monto entregado debe ser un valor numérico positivo.", Alert.AlertType.WARNING);
                pedidosTable.refresh();
            }
        });

        // --- 6. Columna INSTRUCCIONES (TextFieldTableCell) ---
        instruccionesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        instruccionesColumn.setOnEditCommit(event -> {
            String nuevoValor = event.getNewValue();
            String valorOriginal = event.getOldValue();
            if (valorOriginal.equals(nuevoValor)) { return; }
            event.getRowValue().setInstrucciones(nuevoValor);
            guardarCambiosEnBD(event.getRowValue(), "Instrucciones");
        });

        // --- 7. Configuración del Filtro de Empleado ---
        cargarEmpleadosEnFiltro();
        empleadoFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            idEmpleadoFiltro = extractIdFromComboBox(newVal);
            cargarPedidos();
        });

        // --- 8. Configuración de Filtros Adicionales ---
        if (estadoFilterComboBox != null) {
            estadoFilterComboBox.setItems(FXCollections.observableArrayList(estados));
            estadoFilterComboBox.getItems().add(0, "Todos los Estados");
            estadoFilterComboBox.getSelectionModel().selectFirst();
            estadoFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> cargarPedidos());
        }

        if (metodoPagoFilterComboBox != null) {
            List<String> tiposPago = pedidoDAO.getTiposPago();
            tiposPago.add(0, "Todos los Métodos");
            metodoPagoFilterComboBox.setItems(FXCollections.observableArrayList(tiposPago));
            metodoPagoFilterComboBox.getSelectionModel().selectFirst();
            metodoPagoFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> cargarPedidos());
        }

        // Carga inicial de pedidos
        cargarPedidos();
    }

    // =========================================================================
    // UTILIDADES DE AUDITORÍA Y CONTROL
    // =========================================================================

    /**
     * Crea una copia profunda del Pedido, esencial para la auditoría.
     * Utiliza el CONSTRUCTOR COMPLETO de 14 parámetros de Pedido.java.
     */
    private Pedido crearCopiaPedido(Pedido original) {
        if (original == null) return null;

        // ** CLAVE CORREGIDA: Usar el Constructor Completo de 14 parámetros **
        // El orden DEBE coincidir con el constructor de Pedido.java
        Pedido copia = new Pedido(
                original.getIdPedido(),             // 1. idPedido
                original.getIdCliente(),            // 2. idCliente
                original.getNombreCliente(),        // 3. nombreCliente
                original.getIdEmpleado(),           // 4. idEmpleado
                original.getNombreEmpleado(),       // 5. nombreEmpleado
                original.getEstado(),               // 6. estado
                original.getTipoPago(),             // 7. tipoPago
                original.getFechaCreacion(),        // 8. fechaCreacion
                original.getFechaEntregaEstimada(), // 9. fechaEntregaEstimada
                original.getFechaFinalizacion(),    // 10. fechaFinalizacion
                original.getInstrucciones(),        // 11. instrucciones
                original.getMontoTotal(),           // 12. montoTotal
                original.getMontoEntregado(),       // 13. montoEntregado
                original.getRutaComprobante()       // 14. RutaComprobante
        );

        return copia;
    }

    /**
     * Registra el cambio en Historial. Utiliza la conexión transaccional. No hace Commit.
     */
    private void auditarCambio(Connection conn, Pedido pedidoActual, String columna, Object valorOriginal, Object valorNuevo) throws SQLException {
        String originalStr = (valorOriginal != null) ? valorOriginal.toString() : "";
        String nuevoStr = (valorNuevo != null) ? valorNuevo.toString() : "";

        // Asumiendo que SessionManager existe y proporciona el ID de usuario
        boolean exitoRegistro = historialDAO.insertarRegistro(
                SessionManager.getInstance().getLoggedInUserId(),
                "Pedido",
                columna,
                pedidoActual.getIdPedido(),
                originalStr,
                nuevoStr,
                conn
        );

        if (!exitoRegistro) {
            // Si falla el registro, lanzamos una excepción para provocar el ROLLBACK
            throw new SQLException("Fallo al registrar la actividad para la columna: " + columna);
        }
    }

    // =========================================================================
    // LÓGICA DE PERSISTENCIA Y AUDITORÍA (POR CELDA)
    // =========================================================================

    private void guardarCambiosEnBD(Pedido pedidoActual, String campoEditado) {

        // 1. Obtener la versión original del pedido antes de la edición.
        Pedido original = this.pedidoOriginal;

        if (original == null || pedidoActual.getIdPedido() != original.getIdPedido()) {
            mostrarAlerta("Error", "No se pudo iniciar la auditoría. Seleccione la fila nuevamente.", Alert.AlertType.ERROR);
            pedidosTable.refresh();
            return;
        }

        Connection conn = null;
        Object valorOriginal = null;
        Object valorNuevo = null;
        boolean huboCambioParaAuditar = false;

        try {
            // 2. Iniciar Transacción (SOLO para HistorialActividadDAO)
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false);

            // --- 3. IDENTIFICAR LOS CAMBIOS Y AUDITAR ---
            switch (campoEditado) {
                case "Estado":
                    valorOriginal = original.getEstado();
                    valorNuevo = pedidoActual.getEstado();
                    if (!valorOriginal.equals(valorNuevo)) { auditarCambio(conn, pedidoActual, "estado", valorOriginal, valorNuevo); huboCambioParaAuditar = true; }
                    break;
                case "Monto Total":
                    valorOriginal = original.getMontoTotal();
                    valorNuevo = pedidoActual.getMontoTotal();
                    if (Double.compare((double)valorOriginal, (double)valorNuevo) != 0) { auditarCambio(conn, pedidoActual, "montoTotal", valorOriginal, valorNuevo); huboCambioParaAuditar = true; }
                    break;
                case "Monto Entregado":
                    valorOriginal = original.getMontoEntregado();
                    valorNuevo = pedidoActual.getMontoEntregado();
                    if (Double.compare((double)valorOriginal, (double)valorNuevo) != 0) { auditarCambio(conn, pedidoActual, "montoEntregado", valorOriginal, valorNuevo); huboCambioParaAuditar = true; }
                    break;
                case "Instrucciones":
                    valorOriginal = original.getInstrucciones();
                    valorNuevo = pedidoActual.getInstrucciones();
                    if (!String.valueOf(valorOriginal).equals(String.valueOf(valorNuevo))) { auditarCambio(conn, pedidoActual, "instrucciones", valorOriginal, valorNuevo); huboCambioParaAuditar = true; }
                    break;
                case "Ruta Comprobante":
                    valorOriginal = original.getRutaComprobante();
                    valorNuevo = pedidoActual.getRutaComprobante();
                    if (!String.valueOf(valorOriginal).equals(String.valueOf(valorNuevo))) { auditarCambio(conn, pedidoActual, "RutaComprobante", valorOriginal, valorNuevo); huboCambioParaAuditar = true; }
                    break;
            }

            if (!huboCambioParaAuditar) {
                conn.rollback();
                return;
            }

            // --- 4. PERSISTIR CAMBIOS EN EL DAO ---
            boolean exitoActualizacion = pedidoDAO.modificarPedido(pedidoActual);

            if (exitoActualizacion) {
                conn.commit(); // Confirma el registro de actividad
                // <<--- ALERTA DE ÉXITO ELIMINADA AQUÍ para guardado silencioso --->>

                // Actualizar la copia original para futuras ediciones en la misma fila
                this.pedidoOriginal = crearCopiaPedido(pedidoActual);

                if ("Retirado".equalsIgnoreCase(pedidoActual.getEstado()) && !campoEditado.equalsIgnoreCase("Ruta Comprobante")) {
                    cargarPedidos();
                } else {
                    pedidosTable.refresh();
                }
            } else {
                conn.rollback(); // Deshace la auditoría si el DAO falla
                mostrarAlerta("Error de Guardado", "No se pudo actualizar el pedido en la BD. ROLLBACK de historial realizado.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { /* Ignorar */ }
            mostrarAlerta("Error de BD", "Ocurrió un error de base de datos durante la auditoría: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException closeEx) { /* Ignorar */ }
            pedidosTable.refresh();
        }
    }


    // =========================================================================
    // MÉTODOS EXISTENTES (sin cambios críticos)
    // =========================================================================

    private void cargarEmpleadosEnFiltro() {
        try {
            List<String> listaEmpleados = pedidoDAO.getAllEmpleadosDisplay();
            listaEmpleados.add(0, "0 - Todos los Empleados");
            empleadoFilterComboBox.setItems(FXCollections.observableArrayList(listaEmpleados));
            empleadoFilterComboBox.getSelectionModel().selectFirst();
        } catch (Exception e) {
            System.err.println("Error al cargar empleados para el filtro: " + e.getMessage());
            mostrarAlerta("Error de Carga", "No se pudieron cargar los empleados para el filtro.", Alert.AlertType.ERROR);
        }
    }

    private int extractIdFromComboBox(String selectedItem) {
        if (selectedItem == null || selectedItem.isEmpty()) {
            return 0;
        }
        try {
            String idString = selectedItem.split(" - ")[0].trim();
            return Integer.parseInt(idString);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    private void configurarColumnaTicket() {
        ticketColumn.setCellFactory(param -> new TableCell<Pedido, Void>() {
            private final Button btn = new Button("Detallar/Ticket");
            private final HBox pane = new HBox(btn);

            {
                btn.setOnAction((ActionEvent event) -> {
                    Pedido pedido = getTableView().getItems().get(getIndex());
                    handleGenerarTicket(pedido, (Stage) ((Node) event.getSource()).getScene().getWindow());
                });
                pane.setAlignment(Pos.CENTER);
                btn.getStyleClass().add("ticket-button");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void configurarColumnaComprobante() {
        Callback<TableColumn<Pedido, Void>, TableCell<Pedido, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Pedido, Void> call(final TableColumn<Pedido, Void> param) {
                final TableCell<Pedido, Void> cell = new TableCell<>() {

                    private final Button btn = new Button();
                    private final HBox pane = new HBox(btn);

                    {
                        btn.setMinWidth(110);
                        pane.setAlignment(Pos.CENTER);

                        btn.setOnAction(event -> {
                            Pedido pedido = getTableView().getItems().get(getIndex());
                            handleSubirComprobante(pedido);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Pedido pedido = getTableView().getItems().get(getIndex());
                            String ruta = pedido.getRutaComprobante();

                            if (ruta != null && !ruta.isEmpty()) {
                                btn.setText("VER/Cambiar PDF");
                                btn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-cursor: hand;");
                            } else {
                                btn.setText("Subir Comprobante");
                                btn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand;");
                            }
                            setGraphic(pane);
                        }
                    }
                };
                return cell;
            }
        };

        comprobantePagoColumn.setCellFactory(cellFactory);
    }

    private void handleSubirComprobante(Pedido pedido) {
        Stage stage = (Stage) pedidosTable.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Comprobante de Pago (PDF)");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);

        boolean archivoExistenteAbierto = false;
        if (pedido.getRutaComprobante() != null && !pedido.getRutaComprobante().isEmpty()) {
            File existingFile = new File(pedido.getRutaComprobante());
            if (existingFile.exists()) {
                try {
                    Desktop.getDesktop().open(existingFile);
                    System.out.println("Abriendo comprobante existente: " + pedido.getRutaComprobante());
                    archivoExistenteAbierto = true;
                } catch (IOException e) {
                    mostrarAlerta("Error al Abrir", "No se pudo abrir el archivo PDF. Ruta: " + pedido.getRutaComprobante(), Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Advertencia", "La ruta guardada ya no contiene el archivo PDF. Proceda a subir uno nuevo.", Alert.AlertType.WARNING);
            }
        }

        if (archivoExistenteAbierto && pedido.getRutaComprobante() != null && !pedido.getRutaComprobante().isEmpty()) {
            // Si hay archivo y se abrió, el usuario decidirá si quiere cambiarlo o solo verlo.
        }

        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            String newPath = file.getAbsolutePath();

            if (newPath.equals(pedido.getRutaComprobante())) {
                pedidosTable.refresh();
                return;
            }

            // La edición del modelo ocurre aquí
            pedido.setRutaComprobante(newPath);
            System.out.println("Comprobante subido para Pedido " + pedido.getIdPedido() + ". Ruta: " + newPath);

            // Llamada que ahora incluye auditoría
            guardarCambiosEnBD(pedido, "Ruta Comprobante");
            pedidosTable.refresh();
        }
    }


    private void handleGenerarTicket(Pedido pedido, Stage ownerStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detallePedidoView.fxml"));
            Parent root = loader.load();

            DetallePedidoController controller = loader.getController();
            controller.setPedido(pedido);

            Stage stage = new Stage();
            stage.setTitle("Detalle de Compra y Generación de Ticket - Pedido ID: " + pedido.getIdPedido());
            stage.setScene(new Scene(root, 1000, 700));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(ownerStage);
            stage.centerOnScreen();
            stage.showAndWait();

            cargarPedidos();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la ventana de Detalle de Pedido.\nVerifique que 'detallePedidoView.fxml' existe en el classpath.\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarPedidos() {
        List<Pedido> listaCompleta = pedidoDAO.getPedidosPorEmpleado(idEmpleadoFiltro);

        List<Pedido> listaFiltrada = listaCompleta.stream()
                .filter(p -> {
                    String estadoSeleccionado = estadoFilterComboBox != null ? estadoFilterComboBox.getSelectionModel().getSelectedItem() : null;
                    if (estadoSeleccionado != null && !estadoSeleccionado.equals("Todos los Estados")) {
                        return p.getEstado().equals(estadoSeleccionado);
                    }
                    return true;
                })
                .filter(p -> {
                    String metodoSeleccionado = metodoPagoFilterComboBox != null ? metodoPagoFilterComboBox.getSelectionModel().getSelectedItem() : null;
                    if (metodoSeleccionado != null && !metodoSeleccionado.equals("Todos los Métodos")) {
                        return metodoSeleccionado.equals(p.getTipoPago());
                    }
                    return true;
                })
                .collect(Collectors.toList());

        pedidosTable.setItems(FXCollections.observableArrayList(listaFiltrada));
    }

    @FXML
    private void handleGuardarCambios(ActionEvent event) {
        Pedido pedidoSeleccionado = pedidosTable.getSelectionModel().getSelectedItem();

        if (pedidoSeleccionado != null) {
            // Este botón debe usarse principalmente si la edición de celda no se usó (ej. edición externa).
            boolean exito = pedidoDAO.modificarPedido(pedidoSeleccionado);

            if (exito) {
                mostrarAlerta("Éxito", "El Pedido ha sido modificado y guardado correctamente.", Alert.AlertType.INFORMATION);
                if ("Retirado".equalsIgnoreCase(pedidoSeleccionado.getEstado())) {
                    cargarPedidos();
                } else {
                    pedidosTable.refresh();
                }
            } else {
                mostrarAlerta("Error", "No se pudo modificar el pedido ID " + pedidoSeleccionado.getIdPedido() + " en la base de datos.", Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Advertencia", "Por favor, seleccione una fila antes de usar el botón 'Guardar Cambios'.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleLimpiarFiltros(ActionEvent event) {
        if (empleadoFilterComboBox != null) empleadoFilterComboBox.getSelectionModel().selectFirst();
        if (estadoFilterComboBox != null) estadoFilterComboBox.getSelectionModel().selectFirst();
        if (metodoPagoFilterComboBox != null) metodoPagoFilterComboBox.getSelectionModel().selectFirst();

        idEmpleadoFiltro = 0;
        cargarPedidos();
    }

    @FXML
    private void handleVolver(ActionEvent event) {
        try {
            MenuController.loadScene(
                    (Node) event.getSource(),
                    "/PedidosPrimerMenu.fxml",
                    "Menú de Pedidos"
            );
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo volver al menú de pedidos.", Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static class FormattedDateTableCell<S, T extends LocalDateTime> extends TableCell<S, T> {
        private final DateTimeFormatter formatter;

        public FormattedDateTableCell(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(formatter.format(item));
            }
        }
    }
}