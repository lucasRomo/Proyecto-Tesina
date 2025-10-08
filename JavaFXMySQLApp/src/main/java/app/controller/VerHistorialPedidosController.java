package app.controller;

// Importaciones necesarias
import app.dao.PedidoDAO;
import app.model.Pedido;
import app.model.Cliente;
import app.model.Empleado;
import app.model.DetallePedido; // NUEVA IMPORTACIÓN
import app.dao.ClienteDAO;
import app.dao.EmpleadoDAO;
import app.dao.ComprobantePagoDAO; // NUEVA IMPORTACIÓN
import app.util.TicketPDFUtil; // NUEVA IMPORTACIÓN

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

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
    private ComboBox<String> metodoPagoFilterComboBox;

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
    private TableColumn<Pedido, String> metodoPagoColumn;
    @FXML
    private TableColumn<Pedido, Double> montoTotalColumn;
    @FXML
    private TableColumn<Pedido, Double> montoEntregadoColumn;

    // Mapea a fechaFinalizacion
    @FXML
    private TableColumn<Pedido, LocalDateTime> fechaEntregaEstimadaColumn;

    @FXML
    private TableColumn<Pedido, String> instruccionesColumn;

    // --- DAOs y Listas ---
    private PedidoDAO pedidoDAO;
    private ClienteDAO clienteDAO;
    private EmpleadoDAO empleadoDAO;
    private ComprobantePagoDAO comprobantePagoDAO; // NUEVO DAO

    private ObservableList<Pedido> pedidosRetiradosMaestra;
    private FilteredList<Pedido> pedidosFiltrados;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicializar DAOs
        pedidoDAO = new PedidoDAO();
        clienteDAO = new ClienteDAO();
        empleadoDAO = new EmpleadoDAO();
        comprobantePagoDAO = new ComprobantePagoDAO(); // INICIALIZACIÓN NUEVA

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
        metodoPagoColumn.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        montoTotalColumn.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));
        montoEntregadoColumn.setCellValueFactory(new PropertyValueFactory<>("montoEntregado"));
        instruccionesColumn.setCellValueFactory(new PropertyValueFactory<>("instrucciones"));

        // Mapeamos a "fechaFinalizacion" para el historial
        fechaEntregaEstimadaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaFinalizacion"));

        // Configuramos la celda para formatear la fecha/hora
        fechaEntregaEstimadaColumn.setCellFactory(column -> new FormattedDateTableCell<>(DATE_FORMATTER));
    }

    /**
     * Clase auxiliar para formatear columnas de tipo LocalDateTime a String.
     * Esto maneja el formateo de fecha/hora en la columna de la tabla.
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
        clientes.add(null); // Opción para limpiar filtro
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
        empleados.add(null); // Opción para limpiar filtro
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

        // --- 3. Métodos de Pago ---
        ObservableList<String> metodosPago = FXCollections.observableArrayList();
        metodosPago.add(null); // Opción para limpiar filtro

        // **IMPORTANTE: Debes asegurarte de que este método exista en tu PedidoDAO real.**
        metodosPago.addAll(pedidoDAO.getTiposPago());

        metodoPagoFilterComboBox.setItems(metodosPago);
        metodoPagoFilterComboBox.setConverter(new StringConverter<String>() {
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
        if (metodoPagoFilterComboBox != null) {
            metodoPagoFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> actualizarFiltro());
        }
    }

    /**
     * Aplica los filtros seleccionados a la lista de pedidos (FilteredList).
     */
    private void actualizarFiltro() {
        pedidosFiltrados.setPredicate(pedido -> {
            // 1. Predicado Cliente
            Cliente clienteSeleccionado = clienteFilterComboBox.getSelectionModel().getSelectedItem();
            if (clienteSeleccionado != null && pedido.getIdCliente() != clienteSeleccionado.getIdCliente()) {
                return false;
            }

            // 2. Predicado Empleado
            Empleado empleadoSeleccionado = empleadoFilterComboBox.getSelectionModel().getSelectedItem();
            if (empleadoSeleccionado != null && pedido.getIdEmpleado() != empleadoSeleccionado.getIdEmpleado()) {
                return false;
            }

            // 3. Predicado Tipo de Pago
            String metodoPagoSeleccionado = metodoPagoFilterComboBox.getSelectionModel().getSelectedItem();
            // Si el método de pago del pedido es nulo o vacío, lo tratamos como "N/A"
            String pedidoMetodoPago = (pedido.getMetodoPago() != null && !pedido.getMetodoPago().isEmpty()) ? pedido.getMetodoPago() : "N/A";

            if (metodoPagoSeleccionado != null) {
                // Compara el método de pago seleccionado con el del pedido (ignorando mayúsculas/minúsculas)
                if (!metodoPagoSeleccionado.equalsIgnoreCase(pedidoMetodoPago)) {
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
        if (metodoPagoFilterComboBox != null) metodoPagoFilterComboBox.getSelectionModel().clearSelection();
        // Los listeners se encargan de llamar a actualizarFiltro()
    }

    // ----------------------------------------------------------------------------------
    // NUEVO MÉTODO: Generar Ticket PDF
    // ----------------------------------------------------------------------------------

    /**
     * Maneja la generación del ticket PDF para el pedido seleccionado.
     * Este método debería estar conectado a un botón "Generar Ticket" en el FXML.
     */
    @FXML
    private void handleGenerarTicketPDF(ActionEvent event) {
        Pedido pedidoSeleccionado = pedidosTable.getSelectionModel().getSelectedItem();

        if (pedidoSeleccionado == null) {
            mostrarAlerta("Advertencia", "Por favor, seleccione un pedido de la tabla para generar el ticket.", Alert.AlertType.WARNING);
            return;
        }

        // 1. Obtener los detalles del pedido usando el DAO
        // ASUMIMOS que PedidoDAO tiene este método:
        List<DetallePedido> detalles = pedidoDAO.getDetallesPorPedido(pedidoSeleccionado.getIdPedido());

        if (detalles.isEmpty()) {
            mostrarAlerta("Error", "No se encontraron detalles de productos para este pedido.", Alert.AlertType.ERROR);
            return;
        }

        // 2. Generar el PDF
        String nombreArchivo = "Ticket_Pedido_" + pedidoSeleccionado.getIdPedido() + ".pdf";
        // Guarda el archivo en el Escritorio del usuario (cambiar según la necesidad)
        String rutaGuardado = System.getProperty("user.home") + "/Desktop/" + nombreArchivo;

        try {
            // Llamar a la utilidad estática para generar el PDF
            TicketPDFUtil.generarPDF(pedidoSeleccionado, detalles, rutaGuardado);

            // 3. Actualizar la base de datos con la ruta del archivo
            // ASUMIMOS que ComprobantePagoDAO tiene este método:
            boolean rutaActualizada = comprobantePagoDAO.actualizarRutaTicket(
                    pedidoSeleccionado.getIdPedido(), rutaGuardado
            );

            if (rutaActualizada) {
                mostrarAlerta("Éxito", "Ticket generado y guardado en:\n" + rutaGuardado + "\n\nLa ruta ha sido registrada en la base de datos.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Advertencia", "Ticket generado y guardado en:\n" + rutaGuardado + "\n\nAVISO: No se pudo actualizar la ruta del archivo en la base de datos (ComprobantePago).", Alert.AlertType.WARNING);
            }

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Ocurrió un error al generar o guardar el ticket: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Ocurrió un error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    /**
     * Vuelve al menú principal de pedidos.
     */
    @FXML
    private void handleVolver(ActionEvent event) {
        try {
            // NOTA: Asegúrate de que "/PedidosPrimerMenu.fxml" sea la ruta correcta
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
            mostrarAlerta("Error", "No se pudo volver al menú de pedidos.", Alert.AlertType.ERROR);
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
