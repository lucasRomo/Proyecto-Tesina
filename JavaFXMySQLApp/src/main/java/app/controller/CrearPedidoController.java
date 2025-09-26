package app.controller;

import app.MainApp;
import app.model.Cliente;
import app.model.Empleado;
import app.model.Pedido;
import app.model.dao.ClienteDAO;
import app.model.EmpleadoDAO;
import app.model.PedidoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class CrearPedidoController {

    @FXML
    private ComboBox<Cliente> clienteComboBox;
    @FXML
    private ComboBox<Empleado> empleadoComboBox;
    @FXML
    private ComboBox<String> estadoComboBox;
    @FXML
    private DatePicker fechaEntregaEstimadaPicker;
    @FXML
    private DatePicker fechaFinalizacionPicker;
    @FXML
    private TextArea instruccionesArea;
    @FXML
    private TextField montoTotalField;
    @FXML
    private TextField montoEntregadoField;

    private ClienteDAO clienteDAO = new ClienteDAO();
    private EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private PedidoDAO pedidoDAO = new PedidoDAO();

    @FXML
    private void initialize() {
        // Cargar clientes en el ComboBox y configurar cómo se muestran
        cargarClientes();

        // Cargar empleados en el ComboBox y configurar cómo se muestran
        cargarEmpleados();

        // Cargar opciones de estado
        ObservableList<String> estados = FXCollections.observableArrayList("Pendiente", "En proceso", "Finalizado");
        estadoComboBox.setItems(estados);
        estadoComboBox.getSelectionModel().select("Pendiente"); // Seleccionar "Pendiente" por defecto
    }

    private void cargarClientes() {
        List<Cliente> clientes = clienteDAO.getAllClientes();
        clienteComboBox.setItems(FXCollections.observableArrayList(clientes));
        // Configurar la visualización del ComboBox de clientes
        clienteComboBox.setCellFactory(lv -> new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente cliente, boolean empty) {
                super.updateItem(cliente, empty);
                setText(empty ? "" : cliente.getNombre() + " " + cliente.getApellido());
            }
        });
        clienteComboBox.setButtonCell(new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente cliente, boolean empty) {
                super.updateItem(cliente, empty);
                setText(empty ? "Seleccione Cliente" : cliente.getNombre() + " " + cliente.getApellido());
            }
        });
    }

    private void cargarEmpleados() {
        List<Empleado> empleados = empleadoDAO.getAllEmpleados();
        empleadoComboBox.setItems(FXCollections.observableArrayList(empleados));
        // Configurar la visualización del ComboBox de empleados
        empleadoComboBox.setCellFactory(lv -> new ListCell<Empleado>() {
            @Override
            protected void updateItem(Empleado empleado, boolean empty) {
                super.updateItem(empleado, empty);
                setText(empty ? "" : empleado.getNombre() + " " + empleado.getApellido());
            }
        });
        empleadoComboBox.setButtonCell(new ListCell<Empleado>() {
            @Override
            protected void updateItem(Empleado empleado, boolean empty) {
                super.updateItem(empleado, empty);
                setText(empty ? "Seleccione Empleado" : empleado.getNombre() + " " + empleado.getApellido());
            }
        });
    }

    @FXML
    private void handleGuardar() {
        try {
            Cliente clienteSeleccionado = clienteComboBox.getSelectionModel().getSelectedItem();
            Empleado empleadoSeleccionado = empleadoComboBox.getSelectionModel().getSelectedItem();

            if (clienteSeleccionado == null) {
                mostrarAlerta("Error", "Por favor, seleccione un cliente.", Alert.AlertType.ERROR);
                return;
            }

            if (empleadoSeleccionado == null) {
                mostrarAlerta("Error", "Por favor, seleccione un empleado.", Alert.AlertType.ERROR);
                return;
            }

            if (montoTotalField.getText().isEmpty() || montoEntregadoField.getText().isEmpty()) {
                mostrarAlerta("Error", "Por favor, complete los campos de monto.", Alert.AlertType.ERROR);
                return;
            }

            LocalDateTime fechaCreacion = LocalDateTime.now();
            LocalDateTime fechaEntregaEstimada = (fechaEntregaEstimadaPicker.getValue() != null) ? fechaEntregaEstimadaPicker.getValue().atStartOfDay() : null;
            LocalDateTime fechaFinalizacion = null;
            String estado = estadoComboBox.getSelectionModel().getSelectedItem();
            String instrucciones = instruccionesArea.getText();
            double montoTotal = Double.parseDouble(montoTotalField.getText());
            double montoEntregado = Double.parseDouble(montoEntregadoField.getText());

            Pedido nuevoPedido = new Pedido(
                    clienteSeleccionado.getIdCliente(),
                    empleadoSeleccionado.getIdEmpleado(),
                    fechaCreacion,
                    fechaEntregaEstimada,
                    fechaFinalizacion,
                    estado,
                    instrucciones,
                    montoTotal,
                    montoEntregado
            );

            boolean exito = pedidoDAO.savePedido(nuevoPedido);
            if (exito) {
                mostrarAlerta("Éxito", "Pedido creado exitosamente.", Alert.AlertType.INFORMATION);

                // Redirigir al menú principal
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/pedidosPrimerMenu.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) clienteComboBox.getScene().getWindow();
                    stage.setScene(new Scene(root, MainApp.WINDOW_WIDTH, MainApp.WINDOW_HEIGHT));
                    stage.setTitle("Menú de Pedidos");
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                mostrarAlerta("Error", "No se pudo crear el pedido.", Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Error de Formato", "Por favor, ingrese valores numéricos válidos para los montos.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelar() {
        // Redirigir al menú principal sin guardar
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pedidosPrimerMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) clienteComboBox.getScene().getWindow();
            stage.setScene(new Scene(root, MainApp.WINDOW_WIDTH, MainApp.WINDOW_HEIGHT));
            stage.setTitle("Menú de Pedidos");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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
