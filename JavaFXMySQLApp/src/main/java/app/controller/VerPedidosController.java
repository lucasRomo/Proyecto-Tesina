package app.controller;

import app.MainApp;
import app.model.Pedido;
import app.model.PedidoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;

public class VerPedidosController {

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
    private TableColumn<Pedido, Double> montoTotalColumn;
    @FXML
    private TableColumn<Pedido, Double> montoEntregadoColumn;
    @FXML
    private TableColumn<Pedido, String> fechaEntregaEstimadaColumn; // Puede ser String o LocalDate
    @FXML
    private TableColumn<Pedido, String> instruccionesColumn;

    private PedidoDAO pedidoDAO;

    @FXML
    private void initialize() {
        pedidoDAO = new PedidoDAO();

        // Configurar las columnas de la tabla
        idPedidoColumn.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        clienteColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        empleadoColumn.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        montoTotalColumn.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));
        montoEntregadoColumn.setCellValueFactory(new PropertyValueFactory<>("montoEntregado"));
        fechaEntregaEstimadaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaEntregaEstimada"));
        instruccionesColumn.setCellValueFactory(new PropertyValueFactory<>("instrucciones"));

        // Cargar los pedidos en la tabla
        cargarPedidos();
    }

    private void cargarPedidos() {
        // Obtiene la lista de pedidos del DAO
        ObservableList<Pedido> pedidos = FXCollections.observableArrayList(pedidoDAO.getAllPedidos());
        // Asigna la ObservableList a la tabla
        pedidosTable.setItems(pedidos);
    }

    @FXML
    private void handleGuardarCambios(ActionEvent event) {
        // Lógica para guardar cambios en la base de datos
        // ...
        System.out.println("Guardando cambios...");
        mostrarAlerta("Éxito", "Cambios guardados correctamente.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleVolver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PedidosPrimerMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, MainApp.WINDOW_WIDTH, MainApp.WINDOW_HEIGHT));
            stage.setTitle("Menú de Pedidos");
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