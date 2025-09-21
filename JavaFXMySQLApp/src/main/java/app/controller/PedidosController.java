package app.controller;

import app.model.Pedido;
import app.model.PedidoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
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

        idPedidoColumn.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        idClienteColumn.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        montoTotalColumn.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));

        loadPedidos();

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

    @FXML
    private void handleCrearPedido() {
        try {
            // Se corrigió la ruta para asegurarse de que sea relativa a la raíz de recursos.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/crearPedido.fxml"));
            Parent root = loader.load();
            CrearPedidoController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Crear Nuevo Pedido");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            controller.setStage(stage);
            stage.showAndWait();

            // Refrescar la tabla después de que se cierre la ventana
            loadPedidos();
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar la vista de 'Crear Pedido'. Verifique que el archivo FXML exista.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVerPedidos() {
        loadPedidos();
        mostrarAlerta("Ver Pedidos", "Mostrando todos los pedidos existentes en la tabla.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleModificarPedido() {
        Pedido selectedPedido = pedidosTableView.getSelectionModel().getSelectedItem();
        if (selectedPedido != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/modificarPedido.fxml"));
                Parent root = loader.load();
                ModificarPedidoController controller = loader.getController();
                controller.setPedido(selectedPedido);

                Stage stage = new Stage();
                stage.setTitle("Modificar Pedido");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                controller.setStage(stage);
                stage.showAndWait();

                // Refrescar la tabla después de que se cierre la ventana
                loadPedidos();
            } catch (IOException e) {
                mostrarAlerta("Error", "No se pudo cargar la vista de 'Modificar Pedido'.", Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Modificar Pedido", "Por favor, seleccione un pedido de la tabla para modificarlo.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleAsignarPedido() {
        Pedido selectedPedido = pedidosTableView.getSelectionModel().getSelectedItem();
        if (selectedPedido != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/asignarPedido.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setTitle("Asignar Pedido");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();
            } catch (IOException e) {
                mostrarAlerta("Error", "No se pudo cargar la vista de 'Asignar Pedido'.", Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Asignar Pedido", "Por favor, seleccione un pedido de la tabla para asignarlo.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleRegistrarPago() {
        Pedido selectedPedido = pedidosTableView.getSelectionModel().getSelectedItem();
        if (selectedPedido != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/registrarPago.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setTitle("Registrar Pago");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();
            } catch (IOException e) {
                mostrarAlerta("Error", "No se pudo cargar la vista de 'Registrar Pago'.", Alert.AlertType.ERROR);
                e.printStackTrace();
            }
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