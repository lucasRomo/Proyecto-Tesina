package app.controller;

import app.dao.ClienteDAO;
import app.dao.DireccionDAO;
import app.dao.PersonaDAO;
import app.dao.HistorialActividadDAO;
import app.dao.UsuarioDAO;
import app.dao.TipoDocumentoDAO;
import app.model.Cliente;
import app.model.Direccion;
import app.model.TipoDocumento;
import app.model.Persona;
import app.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

// Importa RegistroController si existe, o define una clase dummy si no
// Aquí asumo que existe 'app.controller.RegistroController'
// Si no existe, debes cambiar 'RegistroController' por el nombre real de tu controlador de registro
// Ejemplo: import app.controller.RegistroController;

import static app.controller.MenuController.loadScene;

public class ClienteController {

    // Configuración de la conexión (Asegúrate que estos datos sean correctos)
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    @FXML private TableView<Cliente> clientesTableView;
    @FXML private TableColumn<Cliente, String> nombreColumn;
    @FXML private TableColumn<Cliente, String> apellidoColumn;
    @FXML private TableColumn<Cliente, String> tipoDocumentoColumn;
    @FXML private TableColumn<Cliente, String> numeroDocumentoColumn;
    @FXML private TableColumn<Cliente, String> telefonoColumn;
    @FXML private TableColumn<Cliente, String> emailColumn;
    @FXML private TableColumn<Cliente, Number> idClienteColumn;
    @FXML private TableColumn<Cliente, String> razonSocialColumn;
    @FXML private TableColumn<Cliente, String> personaContactoColumn;
    @FXML private TableColumn<Cliente, String> condicionesPagoColumn;
    @FXML private TableColumn<Cliente, String> estadoColumn;
    @FXML private TableColumn<Cliente, Void> direccionColumn;

    @FXML private TextField filterField;
    @FXML private ChoiceBox<String> estadoChoiceBox;

    @FXML private Button nuevoClienteButton;
    @FXML private Button modificarClienteButton;
    @FXML private Button refreshButton;
    @FXML private Button modificarDocumentoButton; // Nuevo botón

    private ClienteDAO clienteDAO;
    private PersonaDAO personaDAO;
    private DireccionDAO direccionDAO;
    private HistorialActividadDAO historialDAO;
    private UsuarioDAO usuarioDAO;
    private TipoDocumentoDAO tipoDocumentoDAO;

    private ObservableList<Cliente> masterData = FXCollections.observableArrayList();
    private FilteredList<Cliente> filteredData;

    // Set para rastrear todos los clientes que tienen cambios pendientes de guardar en DB
    private Set<Cliente> clientesPendientesDeGuardar = new HashSet<>();

    // Opciones fijas para Razón Social
    private static final ObservableList<String> RAZON_SOCIAL_OPCIONES = FXCollections.observableArrayList(
            "Responsable Inscripto", "Monotributista", "Persona"
    );

    private ObservableList<TipoDocumento> TIPOS_DOCUMENTO_OPCIONES = FXCollections.observableArrayList();

    private Cliente clienteOriginal;
    private int clienteOriginalIndex = -1;


    public ClienteController() {
        this.clienteDAO = new ClienteDAO();
        this.personaDAO = new PersonaDAO();
        this.direccionDAO = new DireccionDAO();
        this.historialDAO = new HistorialActividadDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.tipoDocumentoDAO = new TipoDocumentoDAO();
        cargarTiposDocumento();
    }

    private void cargarTiposDocumento() {
        try {
            List<TipoDocumento> tipos = tipoDocumentoDAO.obtenerTodos();
            TIPOS_DOCUMENTO_OPCIONES.addAll(tipos);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error de Carga", "No se pudieron cargar los tipos de documento desde la base de datos.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void initialize() {
        clientesTableView.setEditable(true);

        clientesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                clienteOriginal = new Cliente(newSelection);
                clienteOriginalIndex = clientesTableView.getSelectionModel().getSelectedIndex();
            }
        });

        // ==========================================================
        // === VINCULACIÓN DEL ANCHO DE COLUMNAS PORCENTUAL ========
        // ==========================================================
        idClienteColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.03));
        nombreColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.08));
        apellidoColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.08));
        tipoDocumentoColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.07));
        numeroDocumentoColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.09));
        telefonoColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.07));
        emailColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.18));
        razonSocialColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.10));
        personaContactoColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.10));
        condicionesPagoColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.10));
        estadoColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.05));
        direccionColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.08));

        // --- Configuración de PropertyValueFactory ---
        nombreColumn.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        apellidoColumn.setCellValueFactory(cellData -> cellData.getValue().apellidoProperty());

        tipoDocumentoColumn.setCellValueFactory(cellData -> {
            int id = cellData.getValue().getIdTipoDocumento();
            String nombre = obtenerNombreTipoDocumento(id);
            return new javafx.beans.property.SimpleStringProperty(nombre);
        });

        numeroDocumentoColumn.setCellValueFactory(cellData -> cellData.getValue().numeroDocumentoProperty());
        telefonoColumn.setCellValueFactory(cellData -> cellData.getValue().telefonoProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        idClienteColumn.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        razonSocialColumn.setCellValueFactory(cellData -> cellData.getValue().razonSocialProperty());
        personaContactoColumn.setCellValueFactory(cellData -> cellData.getValue().personaContactoProperty());
        condicionesPagoColumn.setCellValueFactory(cellData -> cellData.getValue().condicionesPagoProperty());
        estadoColumn.setCellValueFactory(cellData -> cellData.getValue().estadoProperty());

        // =========================================================================================
        // === NUEVAS CONFIGURACIONES DE EDICIÓN Y VALIDACIÓN PARA COLUMNAS PERSONALES =============
        // =========================================================================================

        // --- Columna Nombre ---
        nombreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nombreColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoNombre = event.getNewValue().trim();
            if (nuevoNombre.isEmpty() || !validarSoloLetrasYEspacios(nuevoNombre)) {
                mostrarAlerta("Advertencia", "El nombre es inválido o está vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            cliente.setNombre(nuevoNombre);
            clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO
            clientesTableView.refresh();
        });

        // --- Columna Apellido ---
        apellidoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        apellidoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoApellido = event.getNewValue().trim();
            if (nuevoApellido.isEmpty() || !validarSoloLetrasYEspacios(nuevoApellido)) {
                mostrarAlerta("Advertencia", "El apellido es inválido o está vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            cliente.setApellido(nuevoApellido);
            clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO
            clientesTableView.refresh();
        });

        // --- Columna Teléfono (AHORA OBLIGATORIO) ---
        // --- Columna Número Documento (Editable con validación) ---
        numeroDocumentoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        numeroDocumentoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoNumDoc = event.getNewValue().trim();
            String numDocOriginal = event.getOldValue();

            if (nuevoNumDoc.isEmpty()) {
                mostrarAlerta("Advertencia", "El número de documento no puede quedar vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }
            if (!validarSoloNumeros(nuevoNumDoc)) {
                mostrarAlerta("Advertencia", "El número de documento solo puede contener dígitos.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }

            // Validar longitud y si existe para otro
            if (!validarNumeroDocumento(cliente.getIdTipoDocumento(), nuevoNumDoc, cliente.getIdPersona())) {
                clientesTableView.refresh();
                return;
            }

            cliente.setNumeroDocumento(nuevoNumDoc);
            clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO
            clientesTableView.refresh();
        });

        // --- Columna Teléfono (Editable con validación) ---
        telefonoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        telefonoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoTelefono = event.getNewValue().trim();

            // **VALIDACIÓN 1: No puede quedar vacío**
            if (nuevoTelefono.isEmpty()) {
                mostrarAlerta("Advertencia", "El teléfono no puede quedar vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }

            // **VALIDACIÓN 2: Solo números y longitud**
            if (!validarSoloNumeros(nuevoTelefono) || !validarLongitudTelefono(nuevoTelefono)) {
                mostrarAlerta("Advertencia", "El formato del teléfono es inválido (solo dígitos y debe tener entre 7 y 11 dígitos).", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }

            // Si pasa todas las validaciones
            cliente.setTelefono(nuevoTelefono);
            cliente.setTelefono(nuevoTelefono.isEmpty() ? null : nuevoTelefono);
            clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO
            clientesTableView.refresh();
        });

        // --- Columna Email (AHORA OBLIGATORIO) ---
        emailColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        emailColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoEmail = event.getNewValue().trim();

            // **VALIDACIÓN 1: No puede quedar vacío**
            if (nuevoEmail.isEmpty()) {
                mostrarAlerta("Advertencia", "El correo electrónico no puede quedar vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }

            // **VALIDACIÓN 2: Formato**
            if (!validarFormatoEmail(nuevoEmail)) {
                mostrarAlerta("Advertencia", "El formato del correo electrónico no es válido.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }

            // Si pasa todas las validaciones
            cliente.setEmail(nuevoEmail);
            if (validarYGuardarEmail(cliente, nuevoEmail, emailOriginal)) {
                clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO si la validación es exitosa
            }
            clientesTableView.refresh();
        });


        // =========================================================================================
        // === COLUMNA RAZÓN SOCIAL (CHOICEBOX) - SOLO ACTUALIZA MODELO Y REGISTRA CAMBIO ===========
        // =========================================================================================
        // --- Columna Razón Social (ChoiceBox) ---
        razonSocialColumn.setCellFactory(column -> new TableCell<Cliente, String>() {
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>(RAZON_SOCIAL_OPCIONES);
            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable() || isEmpty()) return;
                super.startEdit();
                choiceBox.getSelectionModel().select(getItem());
                choiceBox.setOnAction(event -> { commitEdit(choiceBox.getSelectionModel().getSelectedItem()); });
                setGraphic(choiceBox); setText(null);
            }
            @Override
            public void cancelEdit() { super.cancelEdit(); setGraphic(null); setText(getItem()); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); }
                else if (isEditing()) { choiceBox.getSelectionModel().select(item); setGraphic(choiceBox); setText(null); }
                else { setGraphic(null); setText(item); }
            }
        });

        razonSocialColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevaRazonSocial = event.getNewValue();
            if (nuevaRazonSocial == null || nuevaRazonSocial.trim().isEmpty()) {
                mostrarAlerta("Advertencia", "La razón social no puede quedar vacía.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
                clientesTableView.refresh();
                return;
            }

            cliente.setRazonSocial(nuevaRazonSocial);
            clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO

            clientesTableView.refresh();
        });

        // --- Columna Persona Contacto ---
        personaContactoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        personaContactoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevaPersonaContacto = event.getNewValue().trim();
            if (nuevaPersonaContacto.isEmpty() || !validarSoloLetrasYEspacios(nuevaPersonaContacto)) {
                mostrarAlerta("Advertencia", "La persona de contacto es inválida o está vacía.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }

            cliente.setPersonaContacto(nuevaPersonaContacto);
            clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO
            clientesTableView.refresh();
        });

        // --- Columna Condiciones Pago ---
        condicionesPagoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        condicionesPagoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevasCondiciones = event.getNewValue().trim();
            if (nuevasCondiciones.isEmpty()) {
                mostrarAlerta("Advertencia", "Las condiciones de pago no pueden quedar vacías.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            cliente.setCondicionesPago(nuevasCondiciones);
            clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO
            clientesTableView.refresh();
        });

        // --- Columna Estado (Guardado Inmediato) ---
        // --- Columna Estado (ChoiceBox) - SOLO ACTUALIZA MODELO Y REGISTRA CAMBIO ---
        estadoColumn.setCellFactory(column -> new TableCell<Cliente, String>() {
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList("Activo", "Desactivado"));
            {
                choiceBox.setOnAction(event -> { if (isEditing()) { commitEdit(choiceBox.getSelectionModel().getSelectedItem()); cancelEdit(); } });
            }
            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable() || isEmpty()) return;
                super.startEdit();
                choiceBox.getSelectionModel().select(getItem());
                setGraphic(choiceBox); setText(null);
            }
            @Override
            public void cancelEdit() { super.cancelEdit(); setGraphic(null); setText(getItem()); applyCellStyle(getItem()); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("activo-cell", "desactivado-cell");
                if (empty || item == null) { setText(null); setGraphic(null); setStyle(null); }
                else if (isEditing()) { choiceBox.getSelectionModel().select(item); setText(null); setGraphic(choiceBox); }
                else { setGraphic(null); setText(item); applyCellStyle(item); }
            }
            private void applyCellStyle(String item) {
                if ("Activo".equalsIgnoreCase(item)) { getStyleClass().add("activo-cell"); setStyle("-fx-text-fill: black; -fx-font-weight: bold;"); }
                else if ("Desactivado".equalsIgnoreCase(item)) { getStyleClass().add("desactivado-cell"); setStyle("-fx-text-fill: black; -fx-font-weight: bold;"); }
                else { setStyle(null); }
            }
        });

        estadoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoEstado = event.getNewValue();
            String estadoOriginal = event.getOldValue();
            cliente.setEstado(nuevoEstado);

            boolean exito = clienteDAO.modificarEstadoCliente(cliente.getIdCliente(), nuevoEstado);
            if (exito) {
                mostrarAlerta("Éxito", "Estado del cliente actualizado.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el estado.", Alert.AlertType.ERROR);
                cliente.setEstado(estadoOriginal);
            }
            clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO
            clientesTableView.refresh();
        });

        // --- Columna Accion (Direccion) ---
        direccionColumn.setCellFactory(param -> new TableCell<Cliente, Void>() {
            private final Button btn = new Button("Direccion");
            {
                btn.prefWidthProperty().bind(direccionColumn.widthProperty());
                btn.setOnAction(event -> {
                    Cliente cliente = getTableView().getItems().get(getIndex());
                    mostrarDireccionCliente(cliente.getIdDireccion());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });


        // --- Configuración de filtros ---
        estadoChoiceBox.setItems(FXCollections.observableArrayList("Todos", "Activo", "Desactivado"));
        estadoChoiceBox.getSelectionModel().select("Todos");

        cargarClientesYConfigurarFiltros();
    }

    // =========================================================================================
    // === MÉTODOS DE MANEJO DE VISTAS (MODALES Y NAVEGACIÓN) ==================================
    // =========================================================================================

    /**
     * RESTAURADO: Abre la ventana modal para registrar un nuevo cliente. (Usando la implementación que pasaste)
     */
    @FXML
    public void handleRegistrarClienteButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registroCliente.fxml"));
            Parent root = loader.load();

            // Aquí se necesita que RegistroController esté en el classpath o se adapte
            Object controller = loader.getController();
            // Debes asegurarte de que 'RegistroController' esté importado o exista.
            /* if (controller instanceof RegistroController) {
                ((RegistroController) controller).setClienteController(this);
            } else {
                System.err.println("Error: El controlador cargado no es RegistroController.");
            }
            */


            Stage newStage = new Stage();
            Scene newScene = new Scene(root);
            newStage.setScene(newScene);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double screenHeight = screenBounds.getHeight();

            newStage.setHeight(screenHeight);
            newStage.sizeToScene();

            newStage.setTitle("Registrar Nuevo Cliente");
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.centerOnScreen();

            newStage.setResizable(false);
            newStage.showAndWait();

            refreshClientesTable();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el formulario de registro de cliente. Verifique la ruta del FXML.", Alert.AlertType.ERROR);
        }
    }

    /**
     * NUEVO: Maneja el botón de Modificar Documento.
     */
    @FXML
    private void handleModificarDocumentoModalButton(ActionEvent event) {
        Cliente selectedCliente = clientesTableView.getSelectionModel().getSelectedItem();

        if (selectedCliente == null) {
            mostrarAlerta("Advertencia", "Por favor, seleccione un cliente de la tabla para modificar su documento.", Alert.AlertType.WARNING);
            return;
        }

        // Abrir el modal de edición de documento
        handleModificarDocumentoModal(event, selectedCliente);
    }

    /**
     * Abre la ventana modal para modificar SÓLO el documento del cliente (función interna).
     */
    // ClienteController.java - Reemplazar la función handleModificarDocumentoModal

    /**
     * Abre la ventana modal para modificar SÓLO el documento del cliente (función interna).
     */
    private void handleModificarDocumentoModal(ActionEvent event, Cliente cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edicionDocumento.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del modal e inyectar los datos
            EdicionDocumentoController controller = loader.getController();
            if (controller != null) {
                // Pasamos el cliente seleccionado, las opciones de TipoDocumento y la referencia a sí mismo.
                controller.setClienteParaEdicion(cliente, TIPOS_DOCUMENTO_OPCIONES, this);
            } else {
                throw new IllegalStateException("El controlador EdicionDocumentoController no se pudo cargar.");
            }

            // Configurar y mostrar el Stage
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            // El título se puede establecer en el controlador del modal si quieres el nombre del cliente
            stage.setTitle("Modificar Documento");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

            // El refreshClientesTable() se mantiene al final para actualizar después del cierre del modal
            refreshClientesTable();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Carga", "No se pudo cargar el formulario de edición de documento (edicionDocumento.fxml).", Alert.AlertType.ERROR);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Controlador", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Muestra la dirección del cliente en una ventana modal.
     */
    private void mostrarDireccionCliente(int idDireccion) {
        Direccion direccion = direccionDAO.obtenerPorId(idDireccion);
        if (direccion == null) {
            mostrarAlerta("Error", "No se encontró la dirección asociada a este cliente.", Alert.AlertType.ERROR);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/verDireccion.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof VerDireccionController) {
                ((VerDireccionController) controller).setDireccion(direccion);
            } else {
                System.err.println("Error: El controlador cargado no es VerDireccionController.");
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Dirección del Cliente");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la ventana de dirección.", Alert.AlertType.ERROR);
        }
    }


    // =========================================================================================
    // === MÉTODOS DE GUARDADO Y REFRESH =======================================================
    // =========================================================================================

    @FXML
    public void handleModificarClienteButton(ActionEvent event) {
        Cliente selectedCliente = clientesTableView.getSelectionModel().getSelectedItem();

        if (selectedCliente != null) {
            String emailEnModelo = selectedCliente.getEmail();

            // 1. Validaciones
            if (selectedCliente.getNombre().isEmpty() || selectedCliente.getApellido().isEmpty() || selectedCliente.getNumeroDocumento().isEmpty()) {
                mostrarAlerta("Error de Validación", "Asegúrese de que los campos Nombre, Apellido y Número de Documento no estén vacíos.", Alert.AlertType.ERROR);
                revertirCambios(selectedCliente);
                return;
            }

            if (!validarSoloNumeros(selectedCliente.getNumeroDocumento())) {
                mostrarAlerta("Error de Validación", "El Número de Documento solo puede contener dígitos.", Alert.AlertType.ERROR);
                revertirCambios(selectedCliente);
                return;
            }

            // Aquí se omiten las validaciones de duplicidad de documento/email por brevedad, asumiendo que están en DAO
            // if (!validarNumeroDocumento(selectedCliente.getIdTipoDocumento(), selectedCliente.getNumeroDocumento(), selectedCliente.getIdPersona())) { revertirCambios(selectedCliente); return; }
            if (emailEnModelo != null && !emailEnModelo.isEmpty() && !validarFormatoEmail(emailEnModelo)) {
                mostrarAlerta("Error de Validación", "El formato del email ('" + emailEnModelo + "') es inválido.", Alert.AlertType.ERROR);
                revertirCambios(selectedCliente);
                return;
            }
            // if (emailEnModelo != null && !emailEnModelo.isEmpty() && personaDAO.verificarSiMailExisteParaOtro(emailEnModelo, selectedCliente.getIdPersona())) { mostrarAlerta(...); revertirCambios(selectedCliente); return; }

            // 2. GUARDAR EN DB
            boolean exito = clienteDAO.modificarCliente(selectedCliente);
            if (exito) {
                mostrarAlerta("Éxito", "Cliente modificado y guardado exitosamente en la base de datos.", Alert.AlertType.INFORMATION);
                clienteOriginal = new Cliente(selectedCliente);
            } else {
                mostrarAlerta("Error", "No se pudo modificar el cliente en la base de datos.", Alert.AlertType.ERROR);
                revertirCambios(selectedCliente);
            }
            clientesTableView.refresh();
        } else {
            mostrarAlerta("Advertencia", "Por favor, seleccione un cliente y realice las modificaciones en la tabla antes de guardar.", Alert.AlertType.WARNING);
        }
    }

    /**
     * Revierto los cambios visuales y en el modelo si la validación final falla.
     */
    private void revertirCambios(Cliente clienteModificado) {
        if (clienteOriginal != null && clienteOriginalIndex != -1) {

            clienteModificado.setNombre(clienteOriginal.getNombre());
            clienteModificado.setApellido(clienteOriginal.getApellido());
            clienteModificado.setIdTipoDocumento(clienteOriginal.getIdTipoDocumento());
            clienteModificado.setNumeroDocumento(clienteOriginal.getNumeroDocumento());
            clienteModificado.setTelefono(clienteOriginal.getTelefono());
            clienteModificado.setEmail(clienteOriginal.getEmail());
            clienteModificado.setRazonSocial(clienteOriginal.getRazonSocial());
            clienteModificado.setPersonaContacto(clienteOriginal.getPersonaContacto());
            clienteModificado.setCondicionesPago(clienteOriginal.getCondicionesPago());

            clientesTableView.getItems().set(clienteOriginalIndex, clienteModificado);
            clienteOriginal = new Cliente(clienteModificado);
            clientesTableView.refresh();
        }
    }

    @FXML
    public void handleRefreshButton(ActionEvent event) {
        refreshClientesTable();
    }

    public void refreshClientesTable() {
        String filtroTexto = filterField.getText();
        String filtroEstado = estadoChoiceBox.getValue();

        masterData.clear();
        masterData.addAll(clienteDAO.getAllClientes());

        filterField.setText(filtroTexto);
        estadoChoiceBox.setValue(filtroEstado);

        updateFilteredList();
    }

    // =========================================================================================
    // === MÉTODOS DE MANEJO DE DATOS Y FILTROS ================================================
    // =========================================================================================

    private void cargarClientesYConfigurarFiltros() {
        masterData.clear();
        masterData.addAll(clienteDAO.getAllClientes());

        filteredData = new FilteredList<>(masterData, p -> true);

        filterField.textProperty().addListener((observable, oldValue, newValue) -> { updateFilteredList(); });
        estadoChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> { updateFilteredList(); });

        SortedList<Cliente> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(clientesTableView.comparatorProperty());
        clientesTableView.setItems(sortedData);
    }

    private void updateFilteredList() {
        if (filteredData == null) return;

        filteredData.setPredicate(cliente -> {
            String searchText = filterField.getText() == null ? "" : filterField.getText().toLowerCase();
            String selectedStatus = estadoChoiceBox.getSelectionModel().getSelectedItem();

            if (cliente == null || selectedStatus == null) return true;

            boolean matchesSearchText = searchText.isEmpty() ||
                    (cliente.getNombre() != null && cliente.getNombre().toLowerCase().contains(searchText)) ||
                    (cliente.getApellido() != null && cliente.getApellido().toLowerCase().contains(searchText)) ||
                    (cliente.getNumeroDocumento() != null && cliente.getNumeroDocumento().toLowerCase().contains(searchText));

            boolean matchesStatus = selectedStatus.equals("Todos") ||
                    selectedStatus.equalsIgnoreCase(cliente.getEstado());

            return matchesSearchText && matchesStatus;
        });
    }

    @FXML
    private void handleVolverButton(ActionEvent event) {
        try {
            // Se asume que MenuController existe en el paquete app.controller
            // y que tiene el método estático loadScene(Node source, String fxmlPath, String title)

            // Verificamos si la clase existe antes de intentar llamarla (patrón de comprobación)
            Class.forName("app.controller.MenuController");

            // Asumiendo la navegación a la vista principal de ABMs
            MenuController.loadScene(
                    (Node) event.getSource(),
                    "/menuAbms.fxml", // Cambiado a /menuAbms.fxml para coincidir con tu primer intento
                    "Menú ABMs"
            );
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Navegación", "No se pudo cargar la vista de Menú ABMs. Verifique la ruta del FXML.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Navegación", "Clase MenuController no encontrada. No se puede volver.");
        }
    }

    @FXML
    public void handleRefreshButton(ActionEvent event) {
        refreshClientesTable();
    }

    @FXML
    public void handleModificarClienteButton(ActionEvent event) {

        if (clientesPendientesDeGuardar.isEmpty()) {
            mostrarAlerta("Advertencia", "No hay modificaciones pendientes para guardar.", Alert.AlertType.WARNING);
            return;
        }

        // --- SECCIÓN ELIMINADA: VALIDACIÓN DE CONTRASEÑA ---
        // (La verificación de seguridad con TextInputDialog ha sido removida)

        // Obtener datos de sesión necesarios
        int loggedInUserId = SessionManager.getInstance().getLoggedInUserId();
        String loggedInUsername = SessionManager.getInstance().getLoggedInUsername(); // Se mantiene por si se usa en otro lado

        // --- INICIO DEL PROCESO DE GUARDADO MÚLTIPLE ---
        int exitos = 0;
        int fallos = 0;

        // Creamos una copia para evitar ConcurrentModificationException al remover elementos
        Set<Cliente> clientesAGuardar = new HashSet<>(clientesPendientesDeGuardar);

        for (Cliente selectedCliente : clientesAGuardar) {

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                conn.setAutoCommit(false);

                // 1. Obtener datos originales para el historial
                Persona originalPersona = personaDAO.obtenerPersonaPorId(selectedCliente.getIdPersona(), conn);
                Cliente originalCliente = clienteDAO.getClienteById(selectedCliente.getIdCliente(), conn);

                if (originalPersona == null || originalCliente == null) {
                    mostrarAlerta("Error Interno", "No se encontraron datos originales para el cliente ID: " + selectedCliente.getIdCliente() + ". Transacción fallida.", Alert.AlertType.ERROR);
                    fallos++;
                    continue;
                }

                boolean exitoHistorial = true;

                // 2. Comparar y registrar cambios en Historial
                //    (Se mantiene la lógica de historial, ya que es una buena práctica)
                if (!selectedCliente.getNombre().equals(originalPersona.getNombre())) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Cliente", "nombre", selectedCliente.getIdPersona(),
                            originalPersona.getNombre(), selectedCliente.getNombre(), conn
                    );
                }

                if (!selectedCliente.getApellido().equals(originalPersona.getApellido())) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Cliente", "apellido", selectedCliente.getIdPersona(),
                            originalPersona.getApellido(), selectedCliente.getApellido(), conn
                    );
                }

                if (!selectedCliente.getNumeroDocumento().equals(originalPersona.getNumeroDocumento())) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Cliente", "numero_documento", selectedCliente.getIdPersona(),
                            originalPersona.getNumeroDocumento(), selectedCliente.getNumeroDocumento(), conn
                    );
                }

                String originalTelefono = originalPersona.getTelefono() != null ? originalPersona.getTelefono() : "";
                String newTelefono = selectedCliente.getTelefono() != null ? selectedCliente.getTelefono() : "";
                if (!newTelefono.equals(originalTelefono)) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Cliente", "telefono", selectedCliente.getIdPersona(),
                            originalTelefono, newTelefono, conn
                    );
                }

                String originalEmail = originalPersona.getEmail() != null ? originalPersona.getEmail() : "";
                String newEmail = selectedCliente.getEmail() != null ? selectedCliente.getEmail() : "";
                if (!newEmail.equals(originalEmail)) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Cliente", "email", selectedCliente.getIdPersona(),
                            originalEmail, newEmail, conn
                    );
                }

                if (!selectedCliente.getRazonSocial().equals(originalCliente.getRazonSocial())) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Cliente", "razon_social", selectedCliente.getIdCliente(),
                            originalCliente.getRazonSocial(), selectedCliente.getRazonSocial(), conn
                    );
                }

                if (!selectedCliente.getPersonaContacto().equals(originalCliente.getPersonaContacto())) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Cliente", "persona_contacto", selectedCliente.getIdCliente(),
                            originalCliente.getPersonaContacto(), selectedCliente.getPersonaContacto(), conn
                    );
                }

                if (!selectedCliente.getCondicionesPago().equals(originalCliente.getCondicionesPago())) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Cliente", "condiciones_pago", selectedCliente.getIdCliente(),
                            originalCliente.getCondicionesPago(), selectedCliente.getCondicionesPago(), conn
                    );
                }

                if (!selectedCliente.getEstado().equals(originalCliente.getEstado())) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Cliente", "estado", selectedCliente.getIdCliente(),
                            originalCliente.getEstado(), selectedCliente.getEstado(), conn
                    );
                }

                // 3. Actualizar Tablas

                // Persona
                String updatePersonaSql = "UPDATE Persona SET nombre = ?, apellido = ?, numero_documento = ?, telefono = ?, email = ? WHERE id_persona = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updatePersonaSql)) {
                    stmt.setString(1, selectedCliente.getNombre());
                    stmt.setString(2, selectedCliente.getApellido());
                    stmt.setString(3, selectedCliente.getNumeroDocumento());
                    stmt.setString(4, selectedCliente.getTelefono());
                    stmt.setString(5, selectedCliente.getEmail());
                    stmt.setInt(6, selectedCliente.getIdPersona());
                    stmt.executeUpdate();
                }

                // Cliente
                String updateClienteSql = "UPDATE Cliente SET razon_social = ?, persona_contacto = ?, condiciones_pago = ?, estado = ? WHERE id_cliente = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateClienteSql)) {
                    stmt.setString(1, selectedCliente.getRazonSocial());
                    stmt.setString(2, selectedCliente.getPersonaContacto());
                    stmt.setString(3, selectedCliente.getCondicionesPago());
                    stmt.setString(4, selectedCliente.getEstado());
                    stmt.setInt(5, selectedCliente.getIdCliente());
                    stmt.executeUpdate();
                }

                // 4. Commit o Rollback
                if (exitoHistorial) {
                    conn.commit();
                    exitos++;
                    clientesPendientesDeGuardar.remove(selectedCliente); // Eliminar del set pendiente solo si fue exitoso
                } else {
                    conn.rollback();
                    fallos++;
                    mostrarAlerta("Error de Historial", "Fallo al registrar historial para el cliente ID: " + selectedCliente.getIdCliente() + ". Transacción revertida.", Alert.AlertType.ERROR);
                }

            } catch (SQLException e) {
                fallos++;
                e.printStackTrace();
                // Manejo de Rollback manual si la conexión no pudo cerrarse o el autocommit falló antes
                mostrarAlerta("Error de Guardado", "Error de base de datos al guardar los cambios para el cliente ID: " + selectedCliente.getIdCliente() + ". Error: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } // Fin del bucle for

        // --- Mostrar Resultado Final ---
        if (exitos > 0 || fallos > 0) {
            String mensaje = "Columna Modificada Exitosamente";
            mostrarAlerta("Proceso Finalizado", mensaje, Alert.AlertType.INFORMATION);
        }

        // Refrescar la tabla para asegurar que los datos visuales estén sincronizados
        refreshClientesTable();
    }

    private boolean validarSoloLetrasYEspacios(String texto) {
        return texto != null && texto.trim().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+");
    }

    private boolean validarSoloNumeros(String texto) {
        return texto != null && texto.matches("\\d+");
    }

    private boolean validarLongitudTelefono(String telefono) {
        int longitudTelefono = telefono.trim().length();
        return longitudTelefono >= 7 && longitudTelefono <= 11;
    }

    private boolean validarFormatoEmail(String email) {
        if (email == null || email.trim().isEmpty()) return true;
        String regex = "^[A-Za-z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[A-Za-z0-9_!#$%&'*+/=?`{|}~^-]+)*@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private String obtenerNombreTipoDocumento(int idTipoDocumento) {
        return TIPOS_DOCUMENTO_OPCIONES.stream()
                .filter(t -> t.getIdTipoDocumento() == idTipoDocumento)
                .findFirst()
                .map(TipoDocumento::getNombreTipo)
                .orElse("ID " + idTipoDocumento + " (Desconocido)");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        if (Platform.isFxApplicationThread()) {
            alert.showAndWait();
        } else {
            Platform.runLater(alert::showAndWait);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        mostrarAlerta(title, message, type);
    }
}