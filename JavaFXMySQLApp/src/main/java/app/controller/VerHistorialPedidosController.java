package app.controller;

import app.dao.PedidoDAO;
import app.model.Pedido;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable; // Importación CLAVE
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL; // Necesario para Initializable
import java.util.ResourceBundle; // Necesario para Initializable

// 1. Implementar la interfaz Initializable
public class VerHistorialPedidosController implements Initializable {

    // Campos FXML (Están correctos)
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
    private static final String ESTADO_RETIRADO = "Retirado";

    // 2. Usar la firma correcta del método initialize (sin @FXML)
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pedidoDAO = new PedidoDAO();

        // Configurar las columnas de la tabla
        // Estas llamadas ahora funcionarán porque las columnas ya fueron inyectadas.
        idPedidoColumn.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        clienteColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        empleadoColumn.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        metodoPagoColumn.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        montoTotalColumn.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));
        montoEntregadoColumn.setCellValueFactory(new PropertyValueFactory<>("montoEntregado"));
        fechaEntregaEstimadaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaEntregaEstimada"));
        instruccionesColumn.setCellValueFactory(new PropertyValueFactory<>("instrucciones"));

        // Cargar SOLO los pedidos con estado 'Retirado'
        cargarPedidosRetirados();
    }

    /**
     * Carga y filtra los pedidos con estado "Retirado".
     * Si el DAO no soporta filtrado SQL, se filtra en memoria.
     */
    private void cargarPedidosRetirados() {
        System.out.println("Cargando historial de pedidos con estado: " + ESTADO_RETIRADO);

        // Carga todos los pedidos y luego filtra en memoria.
        ObservableList<Pedido> todosPedidos = FXCollections.observableArrayList(pedidoDAO.getAllPedidos());
        ObservableList<Pedido> pedidosRetirados = todosPedidos.filtered(
                // El método .filtered() solo conserva los elementos que cumplen la condición:
                pedido -> ESTADO_RETIRADO.equalsIgnoreCase(pedido.getEstado())
        );

        pedidosTable.setItems(pedidosRetirados);

        if (pedidosRetirados.isEmpty()) {
            System.out.println("No hay pedidos con estado '" + ESTADO_RETIRADO + "' para mostrar.");
        }
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