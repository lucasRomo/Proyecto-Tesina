package app.controller;

import app.dao.PedidoDAO;
import app.model.Pedido;
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
    private int idEmpleadoFiltro = 0; // 0 significa 'Todos los Empleados'

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pedidosTable.setEditable(true);

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

            if (nuevoEstado.equalsIgnoreCase("Retirado")) {
                pedido.setFechaFinalizacion(LocalDateTime.now());
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
            if (event.getNewValue() != null && event.getNewValue() >= 0) {
                event.getRowValue().setMontoTotal(event.getNewValue());
                guardarCambiosEnBD(event.getRowValue(), "Monto Total");
            } else {
                mostrarAlerta("Advertencia", "El monto total debe ser un valor numérico positivo.", Alert.AlertType.WARNING);
                pedidosTable.refresh();
            }
        });

        montoEntregadoColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        montoEntregadoColumn.setOnEditCommit(event -> {
            if (event.getNewValue() != null && event.getNewValue() >= 0) {
                event.getRowValue().setMontoEntregado(event.getNewValue());
                guardarCambiosEnBD(event.getRowValue(), "Monto Entregado");
            } else {
                mostrarAlerta("Advertencia", "El monto entregado debe ser un valor numérico positivo.", Alert.AlertType.WARNING);
                pedidosTable.refresh();
            }
        });

        // --- 6. Columna INSTRUCCIONES (TextFieldTableCell) ---
        instruccionesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        instruccionesColumn.setOnEditCommit(event -> {
            event.getRowValue().setInstrucciones(event.getNewValue());
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

        if (pedido.getRutaComprobante() != null && !pedido.getRutaComprobante().isEmpty()) {
            File existingFile = new File(pedido.getRutaComprobante());
            if (existingFile.exists()) {
                try {
                    Desktop.getDesktop().open(existingFile);
                    System.out.println("Abriendo comprobante existente: " + pedido.getRutaComprobante());
                } catch (IOException e) {
                    mostrarAlerta("Error al Abrir", "No se pudo abrir el archivo PDF. Ruta: " + pedido.getRutaComprobante(), Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Advertencia", "La ruta guardada ya no contiene el archivo PDF. Proceda a subir uno nuevo.", Alert.AlertType.WARNING);
            }
        }

        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            String newPath = file.getAbsolutePath();
            pedido.setRutaComprobante(newPath);
            System.out.println("Comprobante subido para Pedido " + pedido.getIdPedido() + ". Ruta: " + newPath);
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


    private void guardarCambiosEnBD(Pedido pedido, String campoEditado) {
        boolean exito = pedidoDAO.modificarPedido(pedido);

        if (exito) {
            System.out.println("Pedido ID " + pedido.getIdPedido() + " actualizado. Campo modificado: " + campoEditado);

            if ("Retirado".equalsIgnoreCase(pedido.getEstado()) && !campoEditado.equalsIgnoreCase("Ruta Comprobante")) {
                cargarPedidos();
            } else {
                pedidosTable.refresh();
            }
        } else {
            mostrarAlerta("Error de Guardado", "No se pudo actualizar el " + campoEditado + " del pedido ID " + pedido.getIdPedido() + " en la base de datos.", Alert.AlertType.ERROR);
            pedidosTable.refresh();
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
            boolean exito = pedidoDAO.modificarPedido(pedidoSeleccionado);

            if (exito) {
                mostrarAlerta("Éxito", "El Pedido ID " + pedidoSeleccionado.getIdPedido() + " ha sido modificado y guardado correctamente.", Alert.AlertType.INFORMATION);
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

    // ===============================================
    // *** MÉTODO VOLVER CORREGIDO ***
    // ===============================================
    @FXML
    private void handleVolver(ActionEvent event) {
        try {
            // Usamos el método unificado para volver, eliminando la creación manual de Stage/Scene
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
    // ===============================================

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