package app.controller;

// Importaciones necesarias
import app.dao.PedidoDAO;
import app.model.Pedido;
import app.model.Cliente;
import app.model.Empleado;
import app.model.DetallePedido;
import app.dao.ClienteDAO;
import app.dao.EmpleadoDAO;
import app.dao.ComprobantePagoDAO;
import app.dao.DetallePedidoDAO;
import app.util.TicketPDFUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell; // Importación para TableCell
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la vista del historial de pedidos (solo estado "Retirado").
 */
public class VerHistorialPedidosController implements Initializable {

    // --- Constantes y Formateadores ---
    private static final String ESTADO_RETIRADO = "Retirado";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");


    // --- Campos FXML de Filtros ---
    @FXML
    private ComboBox<Cliente> clienteFilterComboBox;
    @FXML
    private ComboBox<Empleado> empleadoFilterComboBox;
    @FXML
    private ComboBox<String> tipoPagoFilterComboBox;

    // --- Campos FXML de la Tabla ---
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
    private TableColumn<Pedido, String> tipoPagoColumn;
    @FXML
    private TableColumn<Pedido, Double> montoTotalColumn;
    @FXML
    private TableColumn<Pedido, Double> montoEntregadoColumn;

    @FXML
    private TableColumn<Pedido, LocalDateTime> fechaEntregaEstimadaColumn; // Se usa para mostrar fechaFinalizacion

    @FXML
    private TableColumn<Pedido, String> instruccionesColumn;

    // Columna para el botón de Ticket
    @FXML
    private TableColumn<Pedido, Void> accionesColumn;

    // --- DAOs y Listas ---
    private PedidoDAO pedidoDAO;
    private ClienteDAO clienteDAO;
    private EmpleadoDAO empleadoDAO;
    private ComprobantePagoDAO comprobantePagoDAO;
    private DetallePedidoDAO detallePedidoDAO;

    private ObservableList<Pedido> pedidosRetiradosMaestra;
    private FilteredList<Pedido> pedidosFiltrados;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicializar DAOs
        pedidoDAO = new PedidoDAO();
        clienteDAO = new ClienteDAO();
        empleadoDAO = new EmpleadoDAO();
        comprobantePagoDAO = new ComprobantePagoDAO();
        detallePedidoDAO = new DetallePedidoDAO();

        // 1. Configurar las columnas de la tabla
        configurarColumnas();

        // 2. Cargar los datos maestros (solo Retirados) y configurar la lista filtrada
        cargarDatosMaestros();

        // 3. Inicializar los ComboBox de filtros con datos
        inicializarFiltros();

        // 4. Conectar los ComboBox a la función de filtrado
        conectarFiltros();

        // 5. Aplicar la lista filtrada a la tabla
        pedidosTable.setItems(pedidosFiltrados);
    }

    private void configurarColumnas() {
        idPedidoColumn.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        clienteColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        empleadoColumn.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tipoPagoColumn.setCellValueFactory(new PropertyValueFactory<>("tipoPago"));
        montoTotalColumn.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));
        montoEntregadoColumn.setCellValueFactory(new PropertyValueFactory<>("montoEntregado"));
        instruccionesColumn.setCellValueFactory(new PropertyValueFactory<>("instrucciones"));

        // Mapeamos a "fechaFinalizacion" ya que esta vista es solo para pedidos 'Retirado' (historial)
        fechaEntregaEstimadaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaFinalizacion"));

        // Configuramos la celda para formatear la fecha/hora
        fechaEntregaEstimadaColumn.setCellFactory(column -> new FormattedDateTableCell<>(DATE_FORMATTER));

        // Configurar la columna de Acciones para el botón Ticket
        agregarBotonTicketATabla();
    }

    /**
     * Configura la columna de acciones para mostrar un botón "Ticket" en cada fila.
     */
    private void agregarBotonTicketATabla() {
        // No necesita un PropertyValueFactory, solo actúa como un contenedor para el botón.
        accionesColumn.setCellValueFactory(param -> null);

        accionesColumn.setCellFactory(param -> new TableCell<Pedido, Void>() {
            private final Button btn = new Button("Ticket");

            {
                // Estilo del botón
                btn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-cursor: hand;");

                // Asignar el evento al botón
                btn.setOnAction((ActionEvent event) -> {
                    // Obtener el pedido de la fila actual
                    Pedido pedido = getTableView().getItems().get(getIndex());

                    // Llamar a la lógica de generación de PDF
                    generarTicketPDFParaPedido(pedido);
                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    /**
     * Lógica para generar el ticket PDF (reutilizada por el botón).
     * @param pedido El objeto Pedido para el cual se generará el ticket.
     */
    private void generarTicketPDFParaPedido(Pedido pedido) {

        // 1. Obtener los detalles del pedido
        List<DetallePedido> detalles = detallePedidoDAO.getDetallesPorPedido(pedido.getIdPedido());

        if (detalles.isEmpty()) {
            mostrarAlerta("Error", "No se encontraron detalles de productos para este pedido.", Alert.AlertType.ERROR);
            return;
        }

        // 2. Usar FileChooser para que el usuario elija dónde guardar
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Ticket de Pedido N° " + pedido.getIdPedido());

        // Configurar la extensión por defecto como PDF
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);

        // Sugerir un nombre de archivo
        String nombreSugerido = "Ticket_Pedido_" + pedido.getIdPedido() + ".pdf";
        fileChooser.setInitialFileName(nombreSugerido);

        // Obtener el Stage actual de la ventana
        Stage stage = (Stage) pedidosTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            String rutaGuardado = file.getAbsolutePath();
            try {
                // LLAMADA A LA UTILIDAD PDF
                TicketPDFUtil.generarTicket(pedido, detalles, rutaGuardado);

                mostrarAlerta("Éxito", "Ticket generado y guardado exitosamente en:\n" + rutaGuardado, Alert.AlertType.INFORMATION);

            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Error", "Ocurrió un error al generar o guardar el ticket: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Cancelado", "La generación del ticket PDF fue cancelada.", Alert.AlertType.INFORMATION);
        }
    }

    // --- Clase auxiliar FormattedDateTableCell (sin cambios, ya era correcta) ---
    /**
     * Clase auxiliar para formatear columnas de tipo LocalDateTime a String.
     */
    public static class FormattedDateTableCell<S, T extends LocalDateTime> extends javafx.scene.control.TableCell<S, T> {
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


    /**
     * Carga solo los pedidos con estado 'Retirado' usando el método optimizado del DAO.
     */
    private void cargarDatosMaestros() {
        System.out.println("Cargando historial de pedidos con estado: " + ESTADO_RETIRADO);

        // Uso del método getPedidosPorEstado()
        List<Pedido> listaRetirados = pedidoDAO.getPedidosPorEstado(ESTADO_RETIRADO);
        pedidosRetiradosMaestra = FXCollections.observableArrayList(listaRetirados);

        // Inicializar FilteredList con la lista maestra
        pedidosFiltrados = new FilteredList<>(pedidosRetiradosMaestra, p -> true); // Predicado inicial: mostrar todo
    }

    /**
     * Carga los datos de Cliente, Empleado y Tipos de Pago en los ComboBox.
     */
    private void inicializarFiltros() {
        // --- 1. Clientes ---
        ObservableList<Cliente> clientes = FXCollections.observableArrayList();
        clientes.add(null); // Opción para limpiar filtro (Mostrar Todos)
        clientes.addAll(clienteDAO.getAllClientes());

        clienteFilterComboBox.setItems(clientes);
        clienteFilterComboBox.setConverter(new StringConverter<Cliente>() {
            @Override
            public String toString(Cliente cliente) {
                return (cliente != null) ? cliente.getNombre() + " " + cliente.getApellido() : "Mostrar Todos los Clientes";
            }
            @Override
            public Cliente fromString(String string) { return null; }
        });


        // --- 2. Empleados ---
        ObservableList<Empleado> empleados = FXCollections.observableArrayList();
        empleados.add(null); // Opción para limpiar filtro (Mostrar Todos)
        empleados.addAll(empleadoDAO.getAllEmpleados());

        empleadoFilterComboBox.setItems(empleados);
        empleadoFilterComboBox.setConverter(new StringConverter<Empleado>() {
            @Override
            public String toString(Empleado empleado) {
                return (empleado != null) ? empleado.getNombre() + " " + empleado.getApellido() : "Mostrar Todos los Empleados";
            }
            @Override
            public Empleado fromString(String string) { return null; }
        });

        // --- 3. Tipos de Pago ---
        ObservableList<String> tiposPago = FXCollections.observableArrayList();
        tiposPago.add(null); // Opción para limpiar filtro (Mostrar Todos)

        // El DAO debe obtener los Tipos de Pago únicos de los pedidos
        tiposPago.addAll(pedidoDAO.getTiposPago());

        tipoPagoFilterComboBox.setItems(tiposPago);
        tipoPagoFilterComboBox.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String pago) {
                return (pago != null) ? pago : "Mostrar Todos los Tipos de Pago";
            }
            @Override
            public String fromString(String string) { return string; }
        });
    }

    /**
     * Conecta los listeners de los ComboBox para activar el filtro dinámico.
     */
    private void conectarFiltros() {
        if (clienteFilterComboBox != null) {
            clienteFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> actualizarFiltro());
        }
        if (empleadoFilterComboBox != null) {
            empleadoFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> actualizarFiltro());
        }
        if (tipoPagoFilterComboBox != null) {
            tipoPagoFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> actualizarFiltro());
        }
    }

    /**
     * Aplica los filtros seleccionados a la lista de pedidos (FilteredList).
     */
    private void actualizarFiltro() {
        pedidosFiltrados.setPredicate(pedido -> {
            // 1. Predicado Cliente
            Cliente clienteSeleccionado = clienteFilterComboBox.getSelectionModel().getSelectedItem();
            // Si hay un cliente seleccionado (no null) Y el ID del pedido no coincide, filtrar
            if (clienteSeleccionado != null && pedido.getIdCliente() != clienteSeleccionado.getIdCliente()) {
                return false;
            }

            // 2. Predicado Empleado
            Empleado empleadoSeleccionado = empleadoFilterComboBox.getSelectionModel().getSelectedItem();
            // Si hay un empleado seleccionado (no null) Y el ID del pedido no coincide, filtrar
            if (empleadoSeleccionado != null && pedido.getIdEmpleado() != empleadoSeleccionado.getIdEmpleado()) {
                return false;
            }

            // 3. Predicado Tipo de Pago
            String tipoPagoSeleccionado = tipoPagoFilterComboBox.getSelectionModel().getSelectedItem();

            // Si se seleccionó un tipo de pago (tipoPagoSeleccionado no es null)
            if (tipoPagoSeleccionado != null) {
                String pedidoTipoPago = pedido.getTipoPago();

                // Si el tipo de pago del pedido es nulo o vacío, no coincide con el seleccionado
                if (pedidoTipoPago == null || pedidoTipoPago.isEmpty()) {
                    return false;
                }

                // Si no coincide el tipo de pago (ignorando mayúsculas/minúsculas), filtrar
                if (!tipoPagoSeleccionado.equalsIgnoreCase(pedidoTipoPago)) {
                    return false;
                }
            }

            // Si pasa todos los filtros, mostrar el pedido
            return true;
        });

        if (pedidosFiltrados.isEmpty()) {
            System.out.println("No se encontraron resultados para los filtros aplicados.");
        }
    }

    /**
     * Limpia la selección en todos los ComboBox de filtro.
     */
    @FXML
    private void handleLimpiarFiltros(ActionEvent event) {
        if (clienteFilterComboBox != null) clienteFilterComboBox.getSelectionModel().clearSelection();
        if (empleadoFilterComboBox != null) empleadoFilterComboBox.getSelectionModel().clearSelection();
        if (tipoPagoFilterComboBox != null) tipoPagoFilterComboBox.getSelectionModel().clearSelection();
        // Los listeners se encargan de llamar a actualizarFiltro()
    }

    /**
     * Vuelve al menú principal de pedidos.
     * @param event El evento de acción.
     */
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
            // Llamada que requiere 3 parámetros (String, String, Alert.AlertType)
            mostrarAlerta("Error", "No se pudo volver al menú de pedidos. Verifique la ruta del FXML.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Muestra una ventana de alerta.
     * @param titulo Título de la alerta.
     * @param mensaje Contenido del mensaje.
     * @param tipo Tipo de alerta (INFORMATION, ERROR, WARNING, etc.).
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
