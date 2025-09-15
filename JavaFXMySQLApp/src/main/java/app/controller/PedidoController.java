package app.controller;

import app.model.Pedido;
import app.model.dao.PedidoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.List;

public class PedidosController {

    @FXML private TableView<Pedido> pedidosTableView;
    @FXML private TableColumn<Pedido, Integer> idPedidoColumn;
    @FXML private TableColumn<Pedido, LocalDate> fechaColumn;
    @FXML private TableColumn<Pedido, String> estadoColumn;
    @FXML private TableColumn<Pedido, Integer> idClienteColumn;
    @FXML private TableColumn<Pedido, Double> montoTotalColumn;
    @FXML private TextField filterField;

    private PedidoDAO pedidoDAO;
    private ObservableList<Pedido> masterData;

    @FXML
    private void initialize() {
        pedidoDAO = new PedidoDAO();

        // Configurar las columnas de la tabla para mapear a las propiedades del modelo Pedido
        idPedidoColumn.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        idClienteColumn.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        montoTotalColumn.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));

        // Cargar los datos desde la base de datos
        loadPedidos();

        // Configurar el filtro de búsqueda
        FilteredList<Pedido> filteredData = new FilteredList<>(masterData, p -> true);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(pedido -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (String.valueOf(pedido.getIdPedido()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (pedido.getEstado().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(pedido.getIdCliente()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Pedido> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(pedidosTableView.comparatorProperty());
        pedidosTableView.setItems(sortedData);
    }

    private void loadPedidos() {
        List<Pedido> pedidosList = pedidoDAO.getAllPedidos();
        masterData = FXCollections.observableArrayList(pedidosList);
        pedidosTableView.setItems(masterData);
    }

    // Métodos para manejar los eventos de los botones
    @FXML
    private void handleCrearPedido() {
        mostrarAlerta("Crear Pedidos", "La funcionalidad de crear un nuevo pedido se abrirá en una ventana emergente.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleVerPedidos() {
        mostrarAlerta("Ver Pedidos", "Mostrando todos los pedidos existentes en la tabla.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleModificarPedido() {
        Pedido selectedPedido = pedidosTableView.getSelectionModel().getSelectedItem();
        if (selectedPedido != null) {
            mostrarAlerta("Modificar Pedido", "Se abrirá un formulario para modificar el pedido seleccionado.", Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Modificar Pedido", "Por favor, seleccione un pedido de la tabla para modificarlo.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleAsignarPedido() {
        Pedido selectedPedido = pedidosTableView.getSelectionModel().getSelectedItem();
        if (selectedPedido != null) {
            mostrarAlerta("Asignar Pedido", "Se abrirá un formulario para asignar este pedido.", Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Asignar Pedido", "Por favor, seleccione un pedido de la tabla para asignarlo.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleRegistrarPago() {
        Pedido selectedPedido = pedidosTableView.getSelectionModel().getSelectedItem();
        if (selectedPedido != null) {
            mostrarAlerta("Registrar Pagos", "Se abrirá una interfaz para registrar pagos para este pedido.", Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Registrar Pagos", "Por favor, seleccione un pedido para registrar el pago.", Alert.AlertType.WARNING);
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