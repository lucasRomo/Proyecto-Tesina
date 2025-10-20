package app.controller;

import app.dao.ClienteDAO;
import app.dao.DireccionDAO;
import app.dao.PersonaDAO;
import app.dao.HistorialActividadDAO;
import app.dao.UsuarioDAO;
import app.model.Cliente;
import app.model.Direccion;
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

import static app.controller.MenuController.loadScene;

public class ClienteController {

    // Configuración de la conexión (Asegúrate que estos datos sean correctos)
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    @FXML private TableView<Cliente> clientesTableView;
    @FXML private TableColumn<Cliente, String> nombreColumn;
    @FXML private TableColumn<Cliente, String> apellidoColumn;
    @FXML private TableColumn<Cliente, Number> tipoDocumentoColumn;
    @FXML private TableColumn<Cliente, String> numeroDocumentoColumn;
    @FXML private TableColumn<Cliente, String> telefonoColumn;
    @FXML private TableColumn<Cliente, String> emailColumn;
    @FXML private TableColumn<Cliente, Number> idClienteColumn;
    @FXML private TableColumn<Cliente, String> razonSocialColumn;
    @FXML private TableColumn<Cliente, String> personaContactoColumn;
    @FXML private TableColumn<Cliente, String> condicionesPagoColumn;
    @FXML private TableColumn<Cliente, String> estadoColumn;
    @FXML private TableColumn<Cliente, Void> accionColumn;

    @FXML private TextField filterField;
    @FXML private ChoiceBox<String> estadoChoiceBox;

    @FXML private Button nuevoClienteButton;
    @FXML private Button modificarClienteButton;
    @FXML private Button refreshButton;

    private ClienteDAO clienteDAO;
    private PersonaDAO personaDAO;
    private DireccionDAO direccionDAO;
    private HistorialActividadDAO historialDAO;
    private UsuarioDAO usuarioDAO;
    private ObservableList<Cliente> masterData = FXCollections.observableArrayList();
    private FilteredList<Cliente> filteredData;

    // Set para rastrear todos los clientes que tienen cambios pendientes de guardar en DB
    private Set<Cliente> clientesPendientesDeGuardar = new HashSet<>();

    // Opciones fijas para Razón Social
    private static final ObservableList<String> RAZON_SOCIAL_OPCIONES = FXCollections.observableArrayList(
            "Responsable Inscripto", "Monotributista", "Persona"
    );


    public ClienteController() {
        this.clienteDAO = new ClienteDAO();
        this.personaDAO = new PersonaDAO();
        this.direccionDAO = new DireccionDAO();
        this.historialDAO = new HistorialActividadDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    @FXML
    private void initialize() {
        clientesTableView.setEditable(true);

        nombreColumn.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        apellidoColumn.setCellValueFactory(cellData -> cellData.getValue().apellidoProperty());
        tipoDocumentoColumn.setCellValueFactory(new PropertyValueFactory<>("idTipoDocumento"));
        numeroDocumentoColumn.setCellValueFactory(cellData -> cellData.getValue().numeroDocumentoProperty());
        telefonoColumn.setCellValueFactory(cellData -> cellData.getValue().telefonoProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        idClienteColumn.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        razonSocialColumn.setCellValueFactory(cellData -> cellData.getValue().razonSocialProperty());
        personaContactoColumn.setCellValueFactory(cellData -> cellData.getValue().personaContactoProperty());
        condicionesPagoColumn.setCellValueFactory(cellData -> cellData.getValue().condicionesPagoProperty());
        estadoColumn.setCellValueFactory(cellData -> cellData.getValue().estadoProperty());

        // =========================================================================================
        // === CONFIGURACIONES DE EDICIÓN Y VALIDACIÓN (SOLO ACTUALIZAN EL MODELO Y REGISTRAN CAMBIO)
        // =========================================================================================

        // --- Columna Nombre (Editable con validación) ---
        nombreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nombreColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoNombre = event.getNewValue().trim();
            String nombreOriginal = event.getOldValue();

            if (nuevoNombre.isEmpty()) {
                mostrarAlerta("Advertencia", "El nombre no puede quedar vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }
            if (!validarSoloLetras(nuevoNombre)) {
                mostrarAlerta("Advertencia", "El nombre solo puede contener letras y espacios.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }

            cliente.setNombre(nuevoNombre);
            clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO
            clientesTableView.refresh();
        });

        // --- Columna Apellido (Editable con validación) ---
        apellidoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        apellidoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoApellido = event.getNewValue().trim();
            String apellidoOriginal = event.getOldValue();

            if (nuevoApellido.isEmpty()) {
                mostrarAlerta("Advertencia", "El apellido no puede quedar vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }
            if (!validarSoloLetras(nuevoApellido)) {
                mostrarAlerta("Advertencia", "El apellido solo puede contener letras y espacios.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }

            cliente.setApellido(nuevoApellido);
            clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO
            clientesTableView.refresh();
        });

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
            String telefonoOriginal = event.getOldValue();

            // Teléfono puede ser NULL, pero si se ingresa, debe ser válido.
            if (!nuevoTelefono.isEmpty()) {
                if (!validarSoloNumeros(nuevoTelefono) || !validarLongitudTelefono(nuevoTelefono)) {
                    mostrarAlerta("Advertencia", "El formato del teléfono es inválido o la longitud es incorrecta (7 a 11 dígitos).", Alert.AlertType.WARNING);
                    clientesTableView.refresh();
                    return;
                }
            }

            cliente.setTelefono(nuevoTelefono.isEmpty() ? null : nuevoTelefono);
            clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO
            clientesTableView.refresh();
        });

        // --- Columna Email (Editable con validación) ---
        emailColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        emailColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoEmail = event.getNewValue();
            String emailOriginal = event.getOldValue();

            if (validarYGuardarEmail(cliente, nuevoEmail, emailOriginal)) {
                clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO si la validación es exitosa
            }
            clientesTableView.refresh();
        });


        // =========================================================================================
        // === COLUMNA RAZÓN SOCIAL (CHOICEBOX) - SOLO ACTUALIZA MODELO Y REGISTRA CAMBIO ===========
        // =========================================================================================
        razonSocialColumn.setCellFactory(column -> new TableCell<Cliente, String>() {
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>(RAZON_SOCIAL_OPCIONES);

            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable() || isEmpty()) {
                    return;
                }
                super.startEdit();

                // Selecciona el valor actual para empezar la edición
                choiceBox.getSelectionModel().select(getItem());

                // Al seleccionar un nuevo valor, se confirma la edición (commit)
                choiceBox.setOnAction(event -> {
                    commitEdit(choiceBox.getSelectionModel().getSelectedItem());
                });

                setGraphic(choiceBox);
                setText(null);
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setGraphic(null);
                setText(getItem());
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        choiceBox.getSelectionModel().select(item);
                        setGraphic(choiceBox);
                        setText(null);
                    } else {
                        setGraphic(null);
                        setText(item);
                    }
                }
            }
        });

        razonSocialColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevaRazonSocial = event.getNewValue();
            String razonSocialOriginal = event.getOldValue();

            if (nuevaRazonSocial == null || nuevaRazonSocial.trim().isEmpty()) {
                mostrarAlerta("Advertencia", "La razón social no puede quedar vacía.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }

            cliente.setRazonSocial(nuevaRazonSocial);
            clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO

            clientesTableView.refresh();
        });
        // =========================================================================================

        // --- Columna Persona Contacto (Editable con validación) ---
        personaContactoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        personaContactoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevaPersonaContacto = event.getNewValue().trim();
            String personaContactoOriginal = event.getOldValue();

            if (nuevaPersonaContacto.isEmpty()) {
                mostrarAlerta("Advertencia", "La persona de contacto no puede quedar vacía.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }

            cliente.setPersonaContacto(nuevaPersonaContacto);
            clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO
            clientesTableView.refresh();
        });

        // --- Columna Condiciones Pago (Editable con validación) ---
        condicionesPagoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        condicionesPagoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevasCondiciones = event.getNewValue().trim();
            String condicionesOriginales = event.getOldValue();

            if (nuevasCondiciones.isEmpty()) {
                mostrarAlerta("Advertencia", "Las condiciones de pago no pueden quedar vacías.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }

            cliente.setCondicionesPago(nuevasCondiciones);
            clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO
            clientesTableView.refresh();
        });

        // --- Columna Estado (ChoiceBox) - SOLO ACTUALIZA MODELO Y REGISTRA CAMBIO ---
        estadoColumn.setCellFactory(column -> new TableCell<Cliente, String>() {
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>();

            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
                    return;
                }
                super.startEdit();

                choiceBox.setItems(FXCollections.observableArrayList("Activo", "Desactivado"));
                choiceBox.getSelectionModel().select(getItem());

                choiceBox.setOnAction(event -> {
                    commitEdit(choiceBox.getSelectionModel().getSelectedItem());
                });

                setGraphic(choiceBox);
                setText(null);
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setGraphic(null);
                setText(getItem());
                applyCellStyle(getItem());
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                getStyleClass().removeAll("activo-cell", "desactivado-cell");

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle(null); // Limpiar estilo
                } else {
                    if (isEditing()) {
                        choiceBox.getSelectionModel().select(item);
                        setText(null);
                        setGraphic(choiceBox);
                    } else {
                        setGraphic(null);
                        setText(item);
                        applyCellStyle(item);
                    }
                }
            }

            private void applyCellStyle(String item) {
                if ("Activo".equalsIgnoreCase(item)) {
                    getStyleClass().add("activo-cell");
                    setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
                } else if ("Desactivado".equalsIgnoreCase(item)) {
                    getStyleClass().add("desactivado-cell");
                    setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
                } else {
                    setStyle(null); // Limpiar estilo si no coincide
                }
            }
        });


        estadoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoEstado = event.getNewValue();
            String estadoOriginal = event.getOldValue();

            cliente.setEstado(nuevoEstado);
            clientesPendientesDeGuardar.add(cliente); // REGISTRA EL CAMBIO
            clientesTableView.refresh();
        });


        // --- Columna Accion (Se mantiene igual) ---
        accionColumn.setCellFactory(param -> new TableCell<Cliente, Void>() {
            private final Button btn = new Button("Direccion");

            {
                btn.prefWidthProperty().bind(accionColumn.widthProperty());
                btn.setOnAction(event -> {
                    Cliente cliente = getTableView().getItems().get(getIndex());
                    mostrarDireccionCliente(cliente.getIdDireccion());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });


        estadoChoiceBox.setItems(FXCollections.observableArrayList("Todos", "Activo", "Desactivado"));
        estadoChoiceBox.getSelectionModel().select("Todos");

        cargarClientesYConfigurarFiltros();
    }

    // --- Métodos de Lógica (refreshClientesTable, handleRegistrarClienteButton, etc.) ---

    private void mostrarDireccionCliente(int idDireccion) {
        Direccion direccion = direccionDAO.obtenerPorId(idDireccion);
        if (direccion == null) {
            mostrarAlerta("Error", "No se encontró la dirección asociada a este cliente.", Alert.AlertType.ERROR);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/verDireccion.fxml"));
            Parent root = loader.load();

            VerDireccionController direccionController = loader.getController();
            direccionController.setDireccion(direccion);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Dirección del Cliente");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la ventana de dirección.", Alert.AlertType.ERROR);
        }
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

    private void cargarClientesYConfigurarFiltros() {
        masterData = clienteDAO.getAllClientes();
        filteredData = new FilteredList<>(masterData, p -> true);

        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilteredList();
        });

        estadoChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateFilteredList();
        });

        SortedList<Cliente> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(clientesTableView.comparatorProperty());
        clientesTableView.setItems(sortedData);
    }

    private void updateFilteredList() {
        filteredData.setPredicate(cliente -> {
            String searchText = filterField.getText() == null ? "" : filterField.getText().toLowerCase();
            String selectedStatus = estadoChoiceBox.getSelectionModel().getSelectedItem();

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
    public void handleRegistrarClienteButton(ActionEvent event) {
        try {
            // 1. Cargar el FXML de registro
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registroCliente.fxml"));
            Parent root = loader.load();

            // 2. Configurar el controlador y el callback
            Object controller = loader.getController();
            if (controller instanceof RegistroController) {
                ((RegistroController) controller).setClienteController(this);
            } else {
                System.err.println("Error: El controlador cargado no es RegistroController.");
            }

            // 3. Crear el nuevo Stage (ventana) y la Scene
            Stage newStage = new Stage();
            Scene newScene = new Scene(root);
            newStage.setScene(newScene);

            // 4. Obtener las dimensiones de la pantalla (Screen)
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double screenHeight = screenBounds.getHeight();

            // 5. Aplicar el dimensionamiento solicitado:
            newStage.setHeight(screenHeight);
            newStage.sizeToScene();

            // 6. Configurar el modo (modal) y mostrar
            newStage.setTitle("Registrar Nuevo Cliente");
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.centerOnScreen();

            // Mostrar la nueva ventana y esperar a que se cierre (modal)
            newStage.showAndWait();

            // 7. Refrescar la tabla al volver
            refreshClientesTable();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el formulario de registro de cliente. Verifique la ruta del FXML.", Alert.AlertType.ERROR);
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

    private boolean validarSoloLetras(String texto) {
        return texto.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+");
    }

    private boolean validarSoloNumeros(String texto) {
        return texto.matches("\\d+");
    }

    private boolean validarLongitudTelefono(String telefono) {
        int longitudTelefono = telefono.trim().length();
        return longitudTelefono >= 7 && longitudTelefono <= 11;
    }

    private boolean validarYGuardarEmail(Cliente cliente, String nuevoEmail, String emailOriginal) {
        String trimmedEmail = nuevoEmail != null ? nuevoEmail.trim() : "";

        if (trimmedEmail.isEmpty()) {
            mostrarAlerta("Advertencia", "El email no puede quedar vacío.", Alert.AlertType.WARNING);
            return false;
        }

        if (!validarFormatoEmail(trimmedEmail)) {
            mostrarAlerta("Advertencia", "El formato del correo electrónico no es válido.", Alert.AlertType.WARNING);
            return false;
        }

        if (!trimmedEmail.equalsIgnoreCase(emailOriginal)) {
            if (personaDAO.verificarSiMailExisteParaOtro(trimmedEmail, cliente.getIdPersona())) {
                mostrarAlerta("Error de Modificación", "El email que ingresó ya se encuentra registrado para otro cliente.", Alert.AlertType.WARNING);
                return false;
            }
        }

        cliente.setEmail(trimmedEmail);
        return true;
    }

    private boolean validarFormatoEmail(String email) {
        // Regex mejorada para un formato de email más estricto
        String regex = "^[A-Za-z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[A-Za-z0-9_!#$%&'*+/=?`{|}~^-]+)*@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


    private boolean validarNumeroDocumento(int idTipoDocumento, String numeroDocumento, int idPersonaActual) {
        String tipoDocumento = obtenerNombreTipoDocumento(idTipoDocumento);
        int longitudDocumento = numeroDocumento.length();

        if (!validarSoloNumeros(numeroDocumento)) {
            mostrarAlerta("Advertencia", "El número de documento solo puede contener dígitos.", Alert.AlertType.WARNING);
            return false;
        }

        if ("DNI".equals(tipoDocumento) && longitudDocumento != 8) {
            mostrarAlerta("Advertencia", "El DNI debe tener 8 caracteres.", Alert.AlertType.WARNING);
            return false;
        } else if (("CUIT".equals(tipoDocumento) || "CUIL".equals(tipoDocumento)) && longitudDocumento != 11) {
            mostrarAlerta("Advertencia", "El " + tipoDocumento + " debe tener 11 caracteres.", Alert.AlertType.WARNING);
            return false;
        } else if ("Pasaporte".equals(tipoDocumento) && (longitudDocumento < 6 || longitudDocumento > 20)) {
            mostrarAlerta("Advertencia", "El Pasaporte debe tener entre 6 y 20 caracteres.", Alert.AlertType.WARNING);
            return false;
        }

        if (personaDAO.verificarSiDocumentoExiste(numeroDocumento, idPersonaActual)) {
            mostrarAlerta("Error", "El número de documento ya existe para otra persona.", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private String obtenerNombreTipoDocumento(int idTipoDocumento) {
        if (idTipoDocumento == 1) return "DNI";
        if (idTipoDocumento == 2) return "CUIT";
        if (idTipoDocumento == 3) return "CUIL";
        if (idTipoDocumento == 4) return "Pasaporte";
        return "";
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void handleVolverButton(ActionEvent event) {
        try {
            loadScene(
                    (Node) event.getSource(),
                    "/menuAbms.fxml",
                    "Menú ABMs"
            );

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Navegación", "No se pudo cargar la vista anterior.", Alert.AlertType.ERROR);
        }
    }
}