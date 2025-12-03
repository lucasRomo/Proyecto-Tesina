package app.controller;

import app.dao.PedidoDAO;
import app.model.Pedido;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class CrearPedidoController implements Initializable {

    // ComboBox existentes
    @FXML private ComboBox<String> clienteComboBox;
    @FXML private ComboBox<String> empleadoComboBox;
    @FXML private ComboBox<String> estadoComboBox;
    @FXML private ComboBox<String> tipoPagoComboBox;

    // Otros campos
    @FXML private DatePicker fechaEntregaEstimadaPicker;
    @FXML private TextField montoTotalField;
    @FXML private TextField montoEntregadoField;
    @FXML private TextArea instruccionesArea;

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    /**
     * Inicializa el controlador. Se llama automáticamente después de que se carga el FXML.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicializar ComboBox de Estado
        estadoComboBox.setItems(FXCollections.observableArrayList(
                "Pendiente", "En Proceso", "Finalizado", "Entregado", "Cancelado"
        ));
        // Seleccionar Pendiente por defecto
        estadoComboBox.getSelectionModel().select("Pendiente");


        // --- Carga de ComboBox desde el DAO ---

        // Inicializar ComboBox de Tipo de Pago (usa el método del DAO)
        try {
            List<String> listaTiposPago = pedidoDAO.getTiposPago();
            tipoPagoComboBox.setItems(FXCollections.observableArrayList(listaTiposPago));
            if (!listaTiposPago.isEmpty()) {
                tipoPagoComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            System.err.println("Error al cargar tipos de pago: " + e.getMessage());
        }

        // Cargar Clientes
        try {
            List<String> listaClientes = pedidoDAO.getAllClientesDisplay();
            clienteComboBox.setItems(FXCollections.observableArrayList(listaClientes));
        } catch (Exception e) {
            System.err.println("Error al cargar clientes: " + e.getMessage());
            mostrarAlerta("Error de BD", "No se pudieron cargar los clientes.", "Verifica la conexión a la base de datos.", Alert.AlertType.ERROR);
        }

        // Cargar Empleados
        try {
            List<String> listaEmpleados = pedidoDAO.getAllEmpleadosDisplay();
            listaEmpleados.add(0, "0 - Sin Asignar");
            empleadoComboBox.setItems(FXCollections.observableArrayList(listaEmpleados));
            empleadoComboBox.getSelectionModel().selectFirst(); // Seleccionar "Sin Asignar" por defecto
        } catch (Exception e) {
            System.err.println("Error al cargar empleados: " + e.getMessage());
        }
    }

    /**
     * Establece el escenario (Stage) de este diálogo (usado solo si se abre como modal).
     */


    /**
     * Maneja el evento de guardar el pedido y regresa al menú de pedidos.
     */
    @FXML
    private void handleGuardar(ActionEvent event) {
        if (isInputValid()) {

            int idCliente = extractIdFromComboBox(clienteComboBox.getSelectionModel().getSelectedItem());
            String empleadoSeleccionado = empleadoComboBox.getSelectionModel().getSelectedItem();
            int idEmpleado = (empleadoSeleccionado != null) ? extractIdFromComboBox(empleadoSeleccionado) : 0;

            String estado = estadoComboBox.getSelectionModel().getSelectedItem();
            String tipoPago = tipoPagoComboBox.getSelectionModel().getSelectedItem();
            // NOTA: fechaCreacion se calcula dentro del constructor de 6 parámetros
            // LocalDateTime fechaCreacion = LocalDateTime.now(); // Se puede eliminar esta línea

            LocalDateTime fechaEntregaEstimada = (fechaEntregaEstimadaPicker.getValue() != null)
                    ? fechaEntregaEstimadaPicker.getValue().atStartOfDay() : null;

            // LocalDateTime fechaFinalizacion = null; // Lo maneja el constructor de 6 parámetros

            String instrucciones = instruccionesArea.getText();
            double montoTotal = Double.parseDouble(montoTotalField.getText());
            double montoEntregado = (montoEntregadoField.getText() != null && !montoEntregadoField.getText().isEmpty())
                    ? Double.parseDouble(montoEntregadoField.getText())
                    : 0.0;


            // ---------------------------------------------------------------------
            // CÓDIGO CORREGIDO: Usando el constructor de 6 parámetros
            // ---------------------------------------------------------------------
            Pedido nuevoPedido = new Pedido(
                    idCliente,
                    idEmpleado,
                    tipoPago, // Parámetro 3: tipoPago
                    fechaEntregaEstimada,
                    instrucciones,
                    montoTotal
            );

            // Aplicamos los valores que el constructor por defecto estableció o que deben ser sobreescritos.
            // El constructor ya puso estado="Pendiente" y montoEntregado=0.0. Los actualizamos aquí:
            if (!estado.equalsIgnoreCase(nuevoPedido.getEstado())) {
                nuevoPedido.setEstado(estado);
            }
            if (montoEntregado != 0.0) {
                nuevoPedido.setMontoEntregado(montoEntregado);
            }
            // ---------------------------------------------------------------------

            if (pedidoDAO.savePedido(nuevoPedido, tipoPago)) {
                mostrarAlerta("Éxito", "Pedido guardado", "El nuevo pedido se ha guardado exitosamente junto con su asignación y comprobante de pago.", Alert.AlertType.INFORMATION);
                volverAlMenuPedidos(event);
            } else {
                mostrarAlerta("Error", "Error al guardar", "No se pudo guardar el pedido ni su comprobante de pago en la base de datos.", Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Método auxiliar para extraer el ID de un String con formato "ID - Nombre".
     */
    private int extractIdFromComboBox(String selectedItem) {
        if (selectedItem == null || selectedItem.isEmpty()) {
            return 0;
        }
        try {
            String idString = selectedItem.split(" - ")[0].trim();
            return Integer.parseInt(idString);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Error al parsear ID desde ComboBox: " + selectedItem);
            mostrarAlerta("Error de Formato", "ID Inválido", "El formato de selección de Cliente/Empleado es incorrecto. Contacte a soporte.", Alert.AlertType.ERROR);
            return 0;
        }
    }


    /**
     * Valida la entrada del usuario.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (clienteComboBox.getSelectionModel().isEmpty()) {
            errorMessage += "Debes seleccionar un cliente.\n";
        }
        if (estadoComboBox.getSelectionModel().isEmpty()) {
            errorMessage += "Debes seleccionar un estado para el pedido.\n";
        }
        if (tipoPagoComboBox.getSelectionModel().isEmpty()) {
            errorMessage += "Debes seleccionar un tipo de pago.\n";
        }

        if (montoTotalField.getText() == null || montoTotalField.getText().isEmpty()) {
            errorMessage += "El monto total no puede estar vacío.\n";
        } else {
            try {
                if (Double.parseDouble(montoTotalField.getText()) < 0) {
                    errorMessage += "El monto total debe ser un número positivo.\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "El monto total debe ser un número válido.\n";
            }
        }

        if (montoEntregadoField.getText() != null && !montoEntregadoField.getText().isEmpty()) {
            try {
                if (Double.parseDouble(montoEntregadoField.getText()) < 0) {
                    errorMessage += "El monto entregado debe ser un número positivo o cero.\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "El monto entregado debe ser un número válido.\n";
            }
        }


        if (errorMessage.isEmpty()) {
            return true;
        } else {
            mostrarAlerta("Campos Inválidos", "Por favor, corrige los campos", errorMessage, Alert.AlertType.ERROR);
            return false;
        }
    }


    /**
     * Maneja el evento de cancelar y vuelve al menú de pedidos anterior.
     */
    @FXML
    private void handleCancelar(ActionEvent event) {
        // Llama a la función que usa el patrón de navegación unificado
        volverAlMenuPedidos(event);
    }

    /**
     * Lógica compartida para volver a la vista del menú de Pedidos.
     * USA MenuController.loadScene() para mantener la maximización.
     */
    private void volverAlMenuPedidos(ActionEvent event) {
        try {
            // *** CORRECCIÓN CRÍTICA: Uso del patrón de navegación unificado ***
            MenuController.loadScene(
                    (Node) event.getSource(),
                    "/pedidosPrimerMenu.fxml",
                    "Menú de Pedidos"
            );
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Carga", "No se pudo volver al menú de pedidos.", "Hubo un error al cargar 'pedidosPrimerMenu.fxml'.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Método auxiliar para mostrar alertas.
     */
    private void mostrarAlerta(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleHelpButton() {
        // Creamos una nueva alerta de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuramos el título y los encabezados del mensaje
        alert.setTitle("Ayuda - Menu De Creación de Pedido");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite la Creacion y la Asignacion de un Pedido :\n"
                + "\n"
                + "1. Haga Click en el *ChoiceBox* para Seleccionar El Cliente, El Empleado a Cargo de completar el Pedido, El Estado del Encargo, y el Tipo del Pago con el que se Efectuó el Pedido para Continuar.\n"
                + "----------------------------------------------------------------------\n"
                + "2. En Fecha de Entrega Estimada Haga Click en el Boton con el Icono de Calendario para Seleccionar la Fecha de Entrega Estimada del Pedido Para Continuar.\n"
                + "----------------------------------------------------------------------\n"
                + "3. Complete los Campos Vacios para Continuar.\n"
                + "----------------------------------------------------------------------\n"
                + "4. Para Continuar Haga Click en Guardar Pedido o Para Cancelar el Registro Haga Click en Cancelar.\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();
    }
}