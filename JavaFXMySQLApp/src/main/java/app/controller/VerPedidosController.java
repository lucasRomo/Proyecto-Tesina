package app.controller;

import app.dao.PedidoDAO;
import app.model.Pedido;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.ComboBoxTableCell; // Importación clave para desplegables
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class VerPedidosController implements Initializable {

    @FXML
    private TableView<Pedido> pedidosTable;
    @FXML
    private TableColumn<Pedido, Integer> idPedidoColumn;
    @FXML
    private TableColumn<Pedido, String> clienteColumn;
    @FXML
    private TableColumn<Pedido, String> empleadoColumn;
    @FXML
    private TableColumn<Pedido, String> estadoColumn; // Será ComboBox
    @FXML
    private TableColumn<Pedido, String> metodoPagoColumn; // Será ComboBox
    @FXML
    private TableColumn<Pedido, Double> montoTotalColumn; // Editable numérico
    @FXML
    private TableColumn<Pedido, Double> montoEntregadoColumn; // Editable numérico
    @FXML
    private TableColumn<Pedido, String> fechaEntregaEstimadaColumn; // No editable
    @FXML
    private TableColumn<Pedido, String> instruccionesColumn; // Editable texto

    private PedidoDAO pedidoDAO;

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
        metodoPagoColumn.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        montoTotalColumn.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));
        montoEntregadoColumn.setCellValueFactory(new PropertyValueFactory<>("montoEntregado"));
        fechaEntregaEstimadaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaEntregaEstimada"));
        instruccionesColumn.setCellValueFactory(new PropertyValueFactory<>("instrucciones"));

        // Deshabilitar edición en columnas que no deben cambiar
        clienteColumn.setEditable(false);
        empleadoColumn.setEditable(false);
        fechaEntregaEstimadaColumn.setEditable(false);

        // --- 1. Columna ESTADO (ComboBoxTableCell) ---
        ObservableList<String> estados = FXCollections.observableArrayList("En Proceso", "Terminado", "Retirado");
        estadoColumn.setCellFactory(ComboBoxTableCell.forTableColumn(estados));
        estadoColumn.setOnEditCommit(event -> {
            event.getRowValue().setEstado(event.getNewValue());
            guardarCambiosEnBD(event.getRowValue(), "Estado");
        });

        // --- 2. Columna MÉTODO DE PAGO (ComboBoxTableCell) ---
        ObservableList<String> metodosPago = FXCollections.observableArrayList("Tarjeta", "Efectivo");
        metodoPagoColumn.setCellFactory(ComboBoxTableCell.forTableColumn(metodosPago));
        metodoPagoColumn.setOnEditCommit(event -> {
            event.getRowValue().setMetodoPago(event.getNewValue());
            guardarCambiosEnBD(event.getRowValue(), "Método de Pago");
        });

        // --- 3 & 4. Columnas NUMÉRICAS (Double) ---
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

        // --- 5. Columna INSTRUCCIONES (TextFieldTableCell) ---
        instruccionesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        instruccionesColumn.setOnEditCommit(event -> {
            event.getRowValue().setInstrucciones(event.getNewValue());
            guardarCambiosEnBD(event.getRowValue(), "Instrucciones");
        });

        cargarPedidos();
    }

    /**
     * Llama al DAO para sobrescribir los datos del pedido en la base de datos.
     * @param pedido El objeto Pedido actualizado.
     * @param campoEditado El nombre del campo que fue modificado.
     */
    private void guardarCambiosEnBD(Pedido pedido, String campoEditado) {
        // Llama al método modificarPedido() que actualizará el registro completo en la BD
        boolean exito = pedidoDAO.modificarPedido(pedido);

        if (exito) {
            System.out.println("Pedido ID " + pedido.getIdPedido() + " actualizado. Campo modificado: " + campoEditado);
        } else {
            mostrarAlerta("Error de Guardado", "No se pudo actualizar el " + campoEditado + " del pedido ID " + pedido.getIdPedido() + " en la base de datos.", Alert.AlertType.ERROR);
            pedidosTable.refresh(); // Refresca para restaurar el valor antiguo
        }
    }


    private void cargarPedidos() {
        // Obtiene la lista de pedidos del DAO
        ObservableList<Pedido> pedidos = FXCollections.observableArrayList(pedidoDAO.getAllPedidos());
        // Asigna la ObservableList a la tabla
        pedidosTable.setItems(pedidos);
    }

    /**
     * Maneja el clic en el botón "Guardar Modificación".
     */
    @FXML
    private void handleGuardarCambios(ActionEvent event) {
        Pedido pedidoSeleccionado = pedidosTable.getSelectionModel().getSelectedItem();

        if (pedidoSeleccionado != null) {
            // Se asume que los cambios ya se guardaron en 'setOnEditCommit',
            // pero se puede usar este botón como un guardado explícito de la fila seleccionada
            boolean exito = pedidoDAO.modificarPedido(pedidoSeleccionado);

            if (exito) {
                mostrarAlerta("Éxito", "El Pedido ID " + pedidoSeleccionado.getIdPedido() + " ha sido modificado y guardado correctamente.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo modificar el pedido ID " + pedidoSeleccionado.getIdPedido() + " en la base de datos.", Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Advertencia", "Por favor, seleccione una fila antes de usar el botón 'Guardar Modificación'.", Alert.AlertType.WARNING);
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