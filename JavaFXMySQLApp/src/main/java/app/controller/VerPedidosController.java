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
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class VerPedidosController implements Initializable {

    // Formato para mostrar la fecha de creación (dd-MM-yyyy HH:mm)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML
    private TableView<Pedido> pedidosTable;
    @FXML
    private TableColumn<Pedido, Integer> idPedidoColumn;
    @FXML
    private TableColumn<Pedido, String> clienteColumn;
    @FXML
    private TableColumn<Pedido, String> empleadoColumn;
    @FXML
    private TableColumn<Pedido, String> estadoColumn;
    @FXML
    private TableColumn<Pedido, String> metodoPagoColumn; // AGREGADO para evitar error en FXML si se usa
    @FXML
    private TableColumn<Pedido, Double> montoTotalColumn;
    @FXML
    private TableColumn<Pedido, Double> montoEntregadoColumn;

    // Columna de Fecha de Creación
    @FXML
    private TableColumn<Pedido, LocalDateTime> fechaCreacionColumn;

    @FXML
    private TableColumn<Pedido, String> instruccionesColumn;

    // Columna para el botón de Ticket/Factura
    @FXML
    private TableColumn<Pedido, Void> ticketColumn;

    // ComboBox para filtrar por empleado
    @FXML
    private ComboBox<String> empleadoFilterComboBox;

    // ComboBox para filtrar por estado (si se requiere en el FXML)
    @FXML
    private ComboBox<String> estadoFilterComboBox;

    // ComboBox para filtrar por método de pago (si se requiere en el FXML)
    @FXML
    private ComboBox<String> metodoPagoFilterComboBox;

    private PedidoDAO pedidoDAO;
    private int idEmpleadoFiltro = 0; // 0 significa 'Todos los Empleados'

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pedidoDAO = new PedidoDAO();

        // Habilitar edición en la tabla completa
        pedidosTable.setEditable(true);

        // --- Configuración de Propiedades ---
        idPedidoColumn.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        clienteColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        empleadoColumn.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        // Nueva columna para método de pago, si se incluyó en FXML
        // Se asume que en el FXML existe una columna con fx:id="metodoPagoColumn"
        if (metodoPagoColumn != null) {
            metodoPagoColumn.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
            metodoPagoColumn.setEditable(false);
        }

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

            // Lógica para establecer la fecha de finalización al cambiar a 'Retirado'
            if (nuevoEstado.equalsIgnoreCase("Retirado")) {
                // Si se marca como Retirado, se le pone la fecha actual
                pedido.setFechaFinalizacion(LocalDateTime.now());
                mostrarAlerta("Éxito", "El pedido ha sido marcado como Retirado.", Alert.AlertType.INFORMATION); // CORRECCIÓN de firma
            } else if (pedido.getEstado() != null && pedido.getEstado().equalsIgnoreCase("Retirado") && !nuevoEstado.equalsIgnoreCase("Retirado")) {
                // Si se cambia de 'Retirado' a otro estado, limpiamos la fecha de finalización
                pedido.setFechaFinalizacion(null);
            }

            pedido.setEstado(nuevoEstado);
            guardarCambiosEnBD(pedido, "Estado");
        });

        // --- 2. Columna Ticket/Factura (Botón) ---
        configurarColumnaTicket();

        // --- 3 & 4. Columnas NUMÉRICAS (Double) ---
        montoTotalColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        montoTotalColumn.setOnEditCommit(event -> {
            if (event.getNewValue() != null && event.getNewValue() >= 0) {
                event.getRowValue().setMontoTotal(event.getNewValue());
                guardarCambiosEnBD(event.getRowValue(), "Monto Total");
            } else {
                mostrarAlerta("Advertencia", "El monto total debe ser un valor numérico positivo.", Alert.AlertType.WARNING); // CORRECCIÓN de firma
                pedidosTable.refresh();
            }
        });

        montoEntregadoColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        montoEntregadoColumn.setOnEditCommit(event -> {
            if (event.getNewValue() != null && event.getNewValue() >= 0) {
                event.getRowValue().setMontoEntregado(event.getNewValue());
                guardarCambiosEnBD(event.getRowValue(), "Monto Entregado");
            } else {
                mostrarAlerta("Advertencia", "El monto entregado debe ser un valor numérico positivo.", Alert.AlertType.WARNING); // CORRECCIÓN de firma
                pedidosTable.refresh();
            }
        });

        // --- 5. Columna INSTRUCCIONES (TextFieldTableCell) ---
        instruccionesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        instruccionesColumn.setOnEditCommit(event -> {
            event.getRowValue().setInstrucciones(event.getNewValue());
            guardarCambiosEnBD(event.getRowValue(), "Instrucciones");
        });

        // --- 6. Configuración del Filtro de Empleado ---
        cargarEmpleadosEnFiltro();
        empleadoFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            idEmpleadoFiltro = extractIdFromComboBox(newVal);
            cargarPedidos(); // Recarga los pedidos con el nuevo filtro
        });

        // --- 7. Configuración de Filtros Adicionales (Se asume que existen en FXML) ---
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


        // Carga inicial de pedidos (que ya filtra los 'Retirado' por defecto)
        cargarPedidos();
    }

    /**
     * Carga la lista de empleados para el ComboBox de filtro, corrigiendo el problema de visualización.
     */
    private void cargarEmpleadosEnFiltro() {
        try {
            List<String> listaEmpleados = pedidoDAO.getAllEmpleadosDisplay();
            // Añadir la opción para ver todos
            listaEmpleados.add(0, "0 - Todos los Empleados");
            empleadoFilterComboBox.setItems(FXCollections.observableArrayList(listaEmpleados));
            empleadoFilterComboBox.getSelectionModel().selectFirst();
        } catch (Exception e) {
            System.err.println("Error al cargar empleados para el filtro: " + e.getMessage());
            mostrarAlerta("Error de Carga", "No se pudieron cargar los empleados para el filtro.", Alert.AlertType.ERROR); // CORRECCIÓN de firma
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
            // El formato es "ID - Nombre"
            String idString = selectedItem.split(" - ")[0].trim();
            return Integer.parseInt(idString);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Si no se puede parsear, retorna 0 (Todos)
            return 0;
        }
    }


    /**
     * Configura la columna para incluir un botón "Ticket/Factura".
     */
    private void configurarColumnaTicket() {
        ticketColumn.setCellFactory(param -> new TableCell<Pedido, Void>() {
            private final Button btn = new Button("Ticket/Factura");
            private final HBox pane = new HBox(btn);

            {
                btn.setOnAction((ActionEvent event) -> {
                    // Obtiene el objeto Pedido de la fila actual
                    Pedido pedido = getTableView().getItems().get(getIndex());
                    handleGenerarTicket(pedido);
                });
                pane.setAlignment(Pos.CENTER);
                btn.getStyleClass().add("ticket-button"); // Estilo CSS opcional
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

    /**
     * Manejador del evento de clic en el botón Ticket/Factura.
     * Aquí iría la lógica para generar o cargar el PDF.
     * @param pedido El pedido para el cual generar el ticket.
     */
    private void handleGenerarTicket(Pedido pedido) {
        // CORRECCIÓN de firma en la llamada a mostrarAlerta
        mostrarAlerta("Ticket/Factura", "Generar Ticket para Pedido ID: " + pedido.getIdPedido(), Alert.AlertType.INFORMATION);
    }

    /**
     * Llama al DAO para sobrescribir los datos del pedido en la base de datos.
     * @param pedido El objeto Pedido actualizado.
     * @param campoEditado El nombre del campo que fue modificado.
     */
    private void guardarCambiosEnBD(Pedido pedido, String campoEditado) {
        boolean exito = pedidoDAO.modificarPedido(pedido);

        if (exito) {
            System.out.println("Pedido ID " + pedido.getIdPedido() + " actualizado. Campo modificado: " + campoEditado);

            // Si el estado es "Retirado", recargar la lista para que desaparezca
            if ("Retirado".equalsIgnoreCase(pedido.getEstado())) {
                cargarPedidos();
            } else {
                pedidosTable.refresh();
            }
        } else {
            // CORRECCIÓN de firma en la llamada a mostrarAlerta
            mostrarAlerta("Error de Guardado", "No se pudo actualizar el " + campoEditado + " del pedido ID " + pedido.getIdPedido() + " en la base de datos.", Alert.AlertType.ERROR);
            pedidosTable.refresh(); // Refresca para restaurar el valor antiguo en caso de error
        }
    }


    /**
     * Carga los pedidos activos (no Retirado), aplicando el filtro de Empleado si existe,
     * y los filtros de Estado y Método de Pago.
     */
    private void cargarPedidos() {
        // Obtiene la lista de pedidos del DAO (el DAO ya excluye los pedidos 'Retirado' y aplica el filtro de empleado)
        List<Pedido> listaCompleta = pedidoDAO.getPedidosPorEmpleado(idEmpleadoFiltro);

        // --- Aplicar filtros de UI adicionales ---
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
                        // El método de pago puede ser nulo o "N/A" si no hay comprobante
                        return metodoSeleccionado.equals(p.getMetodoPago());
                    }
                    return true;
                })
                .collect(Collectors.toList());

        pedidosTable.setItems(FXCollections.observableArrayList(listaFiltrada));
    }

    /**
     * Maneja el clic en el botón "Guardar Modificación".
     */
    @FXML
    private void handleGuardarCambios(ActionEvent event) {
        // Se asume que el guardado principal ocurre en onEditCommit, este es un guardado explícito.
        Pedido pedidoSeleccionado = pedidosTable.getSelectionModel().getSelectedItem();

        if (pedidoSeleccionado != null) {
            boolean exito = pedidoDAO.modificarPedido(pedidoSeleccionado);

            if (exito) {
                // CORRECCIÓN de firma en la llamada a mostrarAlerta
                mostrarAlerta("Éxito", "El Pedido ID " + pedidoSeleccionado.getIdPedido() + " ha sido modificado y guardado correctamente.", Alert.AlertType.INFORMATION);
                if ("Retirado".equalsIgnoreCase(pedidoSeleccionado.getEstado())) {
                    cargarPedidos(); // Recarga si se marcó como Retirado para que desaparezca
                } else {
                    pedidosTable.refresh();
                }
            } else {
                // CORRECCIÓN de firma en la llamada a mostrarAlerta
                mostrarAlerta("Error", "No se pudo modificar el pedido ID " + pedidoSeleccionado.getIdPedido() + " en la base de datos.", Alert.AlertType.ERROR);
            }
        } else {
            // CORRECCIÓN de firma en la llamada a mostrarAlerta
            mostrarAlerta("Advertencia", "Por favor, seleccione una fila antes de usar el botón 'Guardar Cambios'.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleVolver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PedidosPrimerMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root, 1800, 1000));
            stage.setTitle("Menú de Pedidos");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // CORRECCIÓN de firma en la llamada a mostrarAlerta
            mostrarAlerta("Error", "No se pudo volver al menú de pedidos.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Maneja el clic en el botón Limpiar Filtros.
     */
    @FXML
    private void handleLimpiarFiltros(ActionEvent event) {
        // Resetea todos los ComboBox a su primera opción ("Todos...")
        if (empleadoFilterComboBox != null) empleadoFilterComboBox.getSelectionModel().selectFirst();
        if (estadoFilterComboBox != null) estadoFilterComboBox.getSelectionModel().selectFirst();
        if (metodoPagoFilterComboBox != null) metodoPagoFilterComboBox.getSelectionModel().selectFirst();

        // El listener de valor del ComboBox de Empleado ya llama a cargarPedidos(), pero lo aseguramos
        idEmpleadoFiltro = 0;
        cargarPedidos();
    }

    /**
     * Muestra una alerta simple (CORREGIDA LA FIRMA para usar solo 3 parámetros).
     * @param titulo El título de la ventana.
     * @param mensaje El contenido del mensaje.
     * @param tipo El tipo de alerta (ERROR, INFORMATION, WARNING, etc.).
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null); // Eliminamos la cabecera ya que casi siempre se usa null.
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Clase auxiliar para formatear columnas de tipo LocalDateTime a String.
     */
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