package app.controller;

import app.dao.ClienteDAO; // Necesaria para clientes
import app.dao.EmpleadoDAO; // Necesaria para empleados
import app.dao.PedidoDAO;
import app.model.Cliente; // Modelo Cliente
import app.model.Empleado; // Modelo Empleado
import app.model.Pedido;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList; // Importación CLAVE para el filtrado dinámico
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox; // Importación CLAVE para filtros
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class VerHistorialPedidosController implements Initializable {

    // NUEVOS CAMPOS FXML: ComboBox para filtros
    @FXML
    private ComboBox<Cliente> clienteFilterComboBox;
    @FXML
    private ComboBox<Empleado> empleadoFilterComboBox;
    @FXML
    private ComboBox<String> metodoPagoFilterComboBox;

    // Campos FXML de la tabla (existentes)
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
    @FXML
    private TableColumn<Pedido, String> fechaEntregaEstimadaColumn;
    @FXML
    private TableColumn<Pedido, String> instruccionesColumn;

    private PedidoDAO pedidoDAO;
    private ClienteDAO clienteDAO; // Asumimos existencia
    private EmpleadoDAO empleadoDAO; // Asumimos existencia
    private static final String ESTADO_RETIRADO = "Retirado";

    // Listas de datos
    private ObservableList<Pedido> pedidosRetiradosMaestra;
    private FilteredList<Pedido> pedidosFiltrados;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicializar DAOs
        pedidoDAO = new PedidoDAO();
        // **ATENCIÓN:** Asegúrate de que ClienteDAO y EmpleadoDAO existan y estén en tu proyecto.
        clienteDAO = new ClienteDAO();
        empleadoDAO = new EmpleadoDAO();

        // 1. Configurar las columnas de la tabla (lógica existente)
        idPedidoColumn.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        clienteColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        empleadoColumn.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        metodoPagoColumn.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        montoTotalColumn.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));
        montoEntregadoColumn.setCellValueFactory(new PropertyValueFactory<>("montoEntregado"));
        fechaEntregaEstimadaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaEntregaEstimada"));
        instruccionesColumn.setCellValueFactory(new PropertyValueFactory<>("instrucciones"));

        // 2. Cargar los datos maestros y configurar la lista filtrada
        cargarDatosMaestros();

        // 3. Inicializar los ComboBox de filtros
        inicializarFiltros();

        // 4. Conectar los ComboBox a la función de filtrado
        // Cada vez que cambia la selección, se llama a actualizarFiltro()
        if (clienteFilterComboBox != null) {
            clienteFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> actualizarFiltro());
        }
        if (empleadoFilterComboBox != null) {
            empleadoFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> actualizarFiltro());
        }
        if (metodoPagoFilterComboBox != null) {
            metodoPagoFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> actualizarFiltro());
        }

        // 5. Aplicar la lista filtrada a la tabla
        pedidosTable.setItems(pedidosFiltrados);
    }

    /**
     * Carga todos los pedidos y los filtra para obtener solo los 'Retirados'.
     */
    private void cargarDatosMaestros() {
        System.out.println("Cargando historial de pedidos con estado: " + ESTADO_RETIRADO);

        // Carga todos los pedidos y filtra en memoria para obtener solo los 'Retirados'.
        ObservableList<Pedido> todosPedidos = FXCollections.observableArrayList(pedidoDAO.getAllPedidos());
        pedidosRetiradosMaestra = todosPedidos.filtered(
                pedido -> ESTADO_RETIRADO.equalsIgnoreCase(pedido.getEstado())
        );

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
        // ATENCIÓN: Se asume que getAllClientes() existe y devuelve una List<Cliente>
        clientes.addAll(clienteDAO.getAllClientes());

        clienteFilterComboBox.setItems(clientes);
        clienteFilterComboBox.setConverter(new javafx.util.StringConverter<Cliente>() {
            @Override
            public String toString(Cliente cliente) {
                // Muestra Nombre Apellido para el ComboBox
                return (cliente != null) ? cliente.getNombre() + " " + cliente.getApellido() : "Mostrar Todos los Clientes";
            }
            @Override
            public Cliente fromString(String string) { return null; }
        });


        // --- 2. Empleados ---
        ObservableList<Empleado> empleados = FXCollections.observableArrayList();
        empleados.add(null); // Opción para limpiar filtro
        // ATENCIÓN: Se asume que getAllEmpleados() existe y devuelve una List<Empleado>
        empleados.addAll(empleadoDAO.getAllEmpleados());

        empleadoFilterComboBox.setItems(empleados);
        empleadoFilterComboBox.setConverter(new javafx.util.StringConverter<Empleado>() {
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

        // **CORRECCIÓN:** Manejamos la excepción si getTiposPago() no existe para evitar el error de compilación.
        try {
            List<String> tipos = pedidoDAO.getTiposPago();
            if (tipos != null) {
                metodosPago.addAll(tipos);
            }
        } catch (AbstractMethodError | NoSuchMethodError e) {
            // Esto se ejecuta si el método no existe en el DAO (el error que ves).
            System.err.println("Advertencia: El método getTiposPago() no se encontró en PedidoDAO. Usando datos de ejemplo.");
            metodosPago.addAll(FXCollections.observableArrayList("Efectivo", "Tarjeta", "Transferencia"));
        }

        metodoPagoFilterComboBox.setItems(metodosPago);
        metodoPagoFilterComboBox.setConverter(new javafx.util.StringConverter<String>() {
            @Override
            public String toString(String pago) {
                return (pago != null) ? pago : "Mostrar Todos los Tipos de Pago";
            }
            @Override
            public String fromString(String string) { return string; }
        });
    }

    /**
     * Aplica los filtros seleccionados a la lista de pedidos.
     */
    private void actualizarFiltro() {
        pedidosFiltrados.setPredicate(pedido -> {
            // 1. Predicado Cliente
            Cliente clienteSeleccionado = clienteFilterComboBox.getSelectionModel().getSelectedItem();
            // Si hay un cliente seleccionado (no nulo) y el idPedido NO coincide con el idCliente seleccionado, retorna false
            if (clienteSeleccionado != null && pedido.getIdCliente() != clienteSeleccionado.getIdCliente()) {
                return false;
            }

            // 2. Predicado Empleado
            Empleado empleadoSeleccionado = empleadoFilterComboBox.getSelectionModel().getSelectedItem();
            // Si hay un empleado seleccionado (no nulo) y el idEmpleado NO coincide con el idEmpleado seleccionado, retorna false
            if (empleadoSeleccionado != null && pedido.getIdEmpleado() != empleadoSeleccionado.getIdEmpleado()) {
                return false;
            }

            // 3. Predicado Tipo de Pago
            String metodoPagoSeleccionado = metodoPagoFilterComboBox.getSelectionModel().getSelectedItem();
            // Si hay un método de pago seleccionado (no nulo) y NO coincide con el metodoPago del pedido, retorna false
            // **ATENCIÓN:** Esto requiere que el modelo Pedido tenga el método getMetodoPago().
            if (metodoPagoSeleccionado != null) {
                try {
                    if (!metodoPagoSeleccionado.equalsIgnoreCase(pedido.getMetodoPago())) {
                        return false;
                    }
                } catch (AbstractMethodError | NoSuchMethodError e) {
                    // Si getMetodoPago() no existe en el modelo Pedido, ignoramos el filtro de pago
                    System.err.println("Advertencia: El método getMetodoPago() no se encontró en el modelo Pedido. Ignorando filtro de pago.");
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
     * Este método resuelve el error "Cannot resolve symbol 'handleLimpiarFiltros'".
     */
    @FXML
    private void handleLimpiarFiltros(ActionEvent event) {
        if (clienteFilterComboBox != null) clienteFilterComboBox.getSelectionModel().clearSelection();
        if (empleadoFilterComboBox != null) empleadoFilterComboBox.getSelectionModel().clearSelection();
        if (metodoPagoFilterComboBox != null) metodoPagoFilterComboBox.getSelectionModel().clearSelection();
        // Los listeners ya se encargan de llamar a actualizarFiltro() automáticamente.
    }


    /**
     * Vuelve al menú principal de pedidos.
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
}
