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
import app.util.TicketPDFUtil; // Clase de utilidad para generar PDF

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
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.awt.Desktop; // Importado para abrir el archivo

/**
 * Controlador para la vista del historial de pedidos.
 * Incluye pedidos con estado "Retirado" y "Cancelado" y permite filtrarlos.
 */
public class VerHistorialPedidosController implements Initializable {

    // --- Constantes y Formateadores ---
    private static final String ESTADO_RETIRADO = "Retirado";
    private static final String ESTADO_CANCELADO = "Cancelado";
    private static final String ESTADO_TODOS = "Mostrar Todos los Estados"; // Opción de filtro
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");


    // --- Campos FXML de Filtros ---
    @FXML
    private ComboBox<Cliente> clienteFilterComboBox;
    @FXML
    private ComboBox<Empleado> empleadoFilterComboBox;
    @FXML
    private ComboBox<String> tipoPagoFilterComboBox;

    // NUEVO CAMPO FXML: Filtro de Estado para el Historial
    @FXML
    private ComboBox<String> estadoHistorialFilterComboBox;

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
    @SuppressWarnings("unused")
    private TableColumn<Pedido, Double> montoEntregadoColumn;

    @FXML
    private TableColumn<Pedido, LocalDateTime> fechaEntregaEstimadaColumn;

    @FXML
    private TableColumn<Pedido, String> instruccionesColumn;

    // Columna para el botón de Ver Comprobante
    @FXML
    private TableColumn<Pedido, Void> comprobantePagoColumn;

    // Columna para el botón de Ticket (la columna a la que mapea el FXML)
    @FXML
    private TableColumn<Pedido, Void> accionesColumn;

    // --- DAOs y Listas ---
    private PedidoDAO pedidoDAO;
    private ClienteDAO clienteDAO;
    private EmpleadoDAO empleadoDAO;
    @SuppressWarnings("unused")
    private ComprobantePagoDAO comprobantePagoDAO;
    private DetallePedidoDAO detallePedidoDAO;

    private ObservableList<Pedido> pedidosHistorialMaestra; // Renombrada para mayor claridad
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

        // 2. Cargar los datos maestros (Retirados y Cancelados) y configurar la lista filtrada
        cargarDatosMaestros();

        // 3. Inicializar los ComboBox de filtros con datos, incluyendo el nuevo filtro de estado
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

        // Usamos fechaFinalizacion. Para Cancelados, este valor será nulo o se usará otra columna
        fechaEntregaEstimadaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaFinalizacion"));

        // Configuramos la celda para formatear la fecha/hora
        fechaEntregaEstimadaColumn.setCellFactory(column -> new FormattedDateTableCell<>(DATE_FORMATTER));

        // Configurar la columna de Acciones para el botón Ticket
        agregarBotonTicketATabla();

        // Columna para el botón Ver Comprobante
        configurarColumnaComprobanteView();

        // Aplicar la política de redimensionamiento
        pedidosTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // --- ASIGNACIÓN DE ANCHOS PREFERIDOS POR PORCENTAJE (SUMA 100.0%) ---
        idPedidoColumn.setPrefWidth(5.0);           // ID (5.0%)
        clienteColumn.setPrefWidth(11.0);           // Cliente (11.0%)
        empleadoColumn.setPrefWidth(11.0);          // Empleado (11.0%)
        estadoColumn.setPrefWidth(6.0);             // Estado (6.0%)
        tipoPagoColumn.setPrefWidth(6.0);           // Tipo Pago (6.0%)
        montoTotalColumn.setPrefWidth(7.5);         // Monto Total (7.5%)
        montoEntregadoColumn.setPrefWidth(8.0);     // Monto Entregado (8.0%)
        fechaEntregaEstimadaColumn.setPrefWidth(11.0); // Fecha Finalización (11.0%)
        instruccionesColumn.setPrefWidth(15.0);     // Instrucciones (15.0%)
        comprobantePagoColumn.setPrefWidth(9.5);    // Comprobante Pago (9.5%)
        accionesColumn.setPrefWidth(10.0);          // Ticket/Acciones (10.0%)
        // ------------------------------------------------------------------
    }

    /**
     * Configura la columna para incluir un botón de "Ver Comprobante" (solo vista).
     */
    private void configurarColumnaComprobanteView() {
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
                            handleVerComprobante(pedido);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Pedido pedido = getTableView().getItems().get(getIndex());

                            // Asume que la clase Pedido tiene un método getRutaComprobante()
                            String ruta = pedido.getRutaComprobante();

                            // Cambia el texto y estilo del botón basado en si ya hay un comprobante
                            if (ruta != null && !ruta.isEmpty()) {
                                btn.setText("Ver PDF");
                                btn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-cursor: hand;"); // Azul
                                btn.setDisable(false); // Habilitado para ver
                            } else {
                                btn.setText("No Disponible");
                                btn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;"); // Gris
                                btn.setDisable(true); // Deshabilitado si no hay ruta
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

    /**
     * Lógica para abrir el archivo PDF del comprobante de pago.
     */
    private void handleVerComprobante(Pedido pedido) {
        String ruta = pedido.getRutaComprobante();

        if (ruta == null || ruta.isEmpty()) {
            mostrarAlerta("Información", "Este pedido no tiene un comprobante de pago registrado.", Alert.AlertType.INFORMATION);
            return;
        }

        File comprobanteFile = new File(ruta);

        if (comprobanteFile.exists()) {
            try {
                // Abrir el PDF con el visor predeterminado del sistema operativo
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(comprobanteFile);
                } else {
                    mostrarAlerta("Advertencia", "La funcionalidad para abrir archivos no es compatible con su sistema operativo.", Alert.AlertType.WARNING);
                }
            } catch (IOException e) {
                mostrarAlerta("Error al Abrir", "No se pudo abrir el archivo PDF. Ruta: " + ruta + "\nError: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Archivo No Encontrado", "El archivo de comprobante no se encuentra en la ruta guardada:\n" + ruta, Alert.AlertType.ERROR);
        }
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

    // --- Clase auxiliar FormattedDateTableCell (sin cambios) ---
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
     * Carga los pedidos con estado 'Retirado' Y 'Cancelado'.
     */
    private void cargarDatosMaestros() {
        System.out.println("Cargando historial de pedidos con estado: " + ESTADO_RETIRADO + " y " + ESTADO_CANCELADO);

        List<Pedido> listaFinal = new ArrayList<>();

        // 1. Obtener Retirados
        List<Pedido> listaRetirados = pedidoDAO.getPedidosPorEstado(ESTADO_RETIRADO);
        if (listaRetirados != null) {
            listaFinal.addAll(listaRetirados);
        } else {
            System.err.println("Advertencia: No se pudo obtener la lista de pedidos Retirados.");
        }


        // 2. Obtener Cancelados
        List<Pedido> listaCancelados = pedidoDAO.getPedidosPorEstado(ESTADO_CANCELADO);
        if (listaCancelados != null) {
            listaFinal.addAll(listaCancelados);
        } else {
            System.err.println("Advertencia: No se pudo obtener la lista de pedidos Cancelados.");
        }


        pedidosHistorialMaestra = FXCollections.observableArrayList(listaFinal);

        // Inicializar FilteredList con la lista maestra
        pedidosFiltrados = new FilteredList<>(pedidosHistorialMaestra, p -> true); // Predicado inicial: mostrar todo
    }

    /**
     * Carga los datos de Cliente, Empleado, Tipos de Pago y el NUEVO filtro de Estado en los ComboBox.
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

        // --- 4. Filtro de Estado de Historial (NUEVO) ---
        ObservableList<String> estadosHistorial = FXCollections.observableArrayList(
                ESTADO_TODOS, ESTADO_RETIRADO, ESTADO_CANCELADO
        );
        estadoHistorialFilterComboBox.setItems(estadosHistorial);
        estadoHistorialFilterComboBox.setValue(ESTADO_TODOS); // Establecer 'Mostrar Todos' como valor inicial
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
        // CONEXIÓN DEL NUEVO FILTRO DE ESTADO
        if (estadoHistorialFilterComboBox != null) {
            estadoHistorialFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> actualizarFiltro());
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
            String tipoPagoSeleccionado = tipoPagoFilterComboBox.getSelectionModel().getSelectedItem();
            if (tipoPagoSeleccionado != null) {
                String pedidoTipoPago = pedido.getTipoPago();
                if (pedidoTipoPago == null || pedidoTipoPago.isEmpty() || !tipoPagoSeleccionado.equalsIgnoreCase(pedidoTipoPago)) {
                    return false;
                }
            }

            // 4. Predicado Estado de Historial (NUEVO)
            String estadoSeleccionado = estadoHistorialFilterComboBox.getSelectionModel().getSelectedItem();
            // Si el estado seleccionado NO es "Mostrar Todos los Estados", aplicamos el filtro
            if (estadoSeleccionado != null && !ESTADO_TODOS.equals(estadoSeleccionado)) {
                // Si el estado del pedido NO coincide con el estado seleccionado, filtrar
                if (!pedido.getEstado().equalsIgnoreCase(estadoSeleccionado)) {
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
        // Limpiar el nuevo filtro de estado, volviendo al valor por defecto
        if (estadoHistorialFilterComboBox != null) estadoHistorialFilterComboBox.setValue(ESTADO_TODOS);

        // Los listeners se encargan de llamar a actualizarFiltro()
    }

    // ===============================================
    // *** MÉTODO VOLVER CORREGIDO ***
    // ===============================================
    /**
     * Vuelve al menú principal de pedidos usando el patrón de navegación unificado.
     * @param event El evento de acción.
     */
    @FXML
    private void handleVolver(ActionEvent event) {
        try {
            // Usa el método unificado para cargar la escena en la ventana principal,
            // manteniendo el estado de maximización/tamaño de la aplicación.
            MenuController.loadScene(
                    (Node) event.getSource(),
                    "/PedidosPrimerMenu.fxml",
                    "Menú de Pedidos"
            );
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo volver al menú de pedidos. Verifique la ruta del FXML y la clase MenuController.", Alert.AlertType.ERROR);
        }
    }
    // ===============================================

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

    @FXML
    private void handleHelpButton() {
        // Creamos una nueva alerta de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuramos el título y los encabezados del mensaje
        alert.setTitle("Ayuda - Menu De Visualizacion de Pedidos Historial");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje (se actualiza la descripción de filtros)
        alert.setContentText("Este módulo permite la Visualizacion de Pedidos Finaliizados ('Retirado') y Pedidos CANCELADOS: \n"
                + "\n"
                + "1. Filtros: Utilice los ChoiceBox para filtrar por:\n"
                + "   - Estado: 'Retirado' o 'Cancelado'.\n"
                + "   - Nombre de Cliente.\n"
                + "   - Nombre de Empleado.\n"
                + "   - Tipo de Pago.\n"
                + "----------------------------------------------------------------------\n"
                + "2. Limpiar Filtros: Haga Click en el Boton para Limpiar todos Los Filtros Seleccionados Anteriormente.\n"
                + "----------------------------------------------------------------------\n"
                + "3. Ver PDF: Haga Click en el Siguiente Boton para ver el PDF del Comprobante de Pago Vinculado al ID del Pedido (Si no hay un Comprobante Vinculado se Mostrara No Disponible).\n"
                + "----------------------------------------------------------------------\n"
                + "4. Ticket: Haga Click en el Siguiente Boton para Generar y Guardar un PDF del Ticket del Pedido Finalizado Vinculado al ID del Pedido (Sólo para pedidos 'Retirado').\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();
    }

}
