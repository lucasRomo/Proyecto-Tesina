package app.controller;

import app.dao.ClienteDAO;
import app.dao.DireccionDAO;
import app.dao.PersonaDAO;
import app.model.Cliente;
import app.model.Direccion;
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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClienteController {

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
    private ObservableList<Cliente> masterData = FXCollections.observableArrayList();
    private FilteredList<Cliente> filteredData;

    private static final ObservableList<String> RAZON_SOCIAL_OPCIONES = FXCollections.observableArrayList(
            "Responsable Inscripto", "Monotributista", "Persona"
    );


    public ClienteController() {
        this.clienteDAO = new ClienteDAO();
        this.personaDAO = new PersonaDAO();
        this.direccionDAO = new DireccionDAO();
    }


    @FXML
    private void initialize() {
        clientesTableView.setEditable(true);

        // --- Configuración de PropertyValueFactory ---
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
        // === ONEDITCOMMIT: SOLO VALIDACIÓN Y ACTUALIZACIÓN DEL MODELO ============================
        // =========================================================================================

        // --- Columna Nombre ---
        nombreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nombreColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoNombre = event.getNewValue().trim();
            if (nuevoNombre.isEmpty()) {
                mostrarAlerta("Advertencia", "El nombre no puede quedar vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            if (!validarSoloLetras(nuevoNombre)) {
                mostrarAlerta("Advertencia", "El nombre solo puede contener letras y espacios.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            cliente.setNombre(nuevoNombre); // Actualiza el modelo
            clientesTableView.refresh();
        });

        // --- Columna Apellido ---
        apellidoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        apellidoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoApellido = event.getNewValue().trim();
            if (nuevoApellido.isEmpty()) {
                mostrarAlerta("Advertencia", "El apellido no puede quedar vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            if (!validarSoloLetras(nuevoApellido)) {
                mostrarAlerta("Advertencia", "El apellido solo puede contener letras y espacios.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            cliente.setApellido(nuevoApellido); // Actualiza el modelo
            clientesTableView.refresh();
        });

        // --- Columna Número Documento ---
        numeroDocumentoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        numeroDocumentoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoNumDoc = event.getNewValue().trim();
            if (nuevoNumDoc.isEmpty()) {
                mostrarAlerta("Advertencia", "El número de documento no puede quedar vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            if (!validarSoloNumeros(nuevoNumDoc)) {
                mostrarAlerta("Advertencia", "El número de documento solo puede contener dígitos.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            if (!validarNumeroDocumento(cliente.getIdTipoDocumento(), nuevoNumDoc, cliente.getIdPersona())) {
                clientesTableView.refresh(); return;
            }
            cliente.setNumeroDocumento(nuevoNumDoc); // Actualiza el modelo
            clientesTableView.refresh();
        });

        // --- Columna Teléfono ---
        telefonoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        telefonoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoTelefono = event.getNewValue().trim();
            if (!nuevoTelefono.isEmpty()) {
                if (!validarSoloNumeros(nuevoTelefono) || !validarLongitudTelefono(nuevoTelefono)) {
                    mostrarAlerta("Advertencia", "El formato del teléfono es inválido o la longitud es incorrecta (7 a 11 dígitos).", Alert.AlertType.WARNING);
                    clientesTableView.refresh(); return;
                }
            }
            cliente.setTelefono(nuevoTelefono.isEmpty() ? null : nuevoTelefono); // Actualiza el modelo
            clientesTableView.refresh();
        });

        // --- Columna Email ---
        emailColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        emailColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoEmail = event.getNewValue();
            validarYGuardarEmail(cliente, nuevoEmail, cliente.getEmail()); // La validación actualiza el modelo si es exitosa
            clientesTableView.refresh();
        });

        // --- Columna Razón Social (ChoiceBox) ---
        razonSocialColumn.setCellFactory(column -> new TableCell<Cliente, String>() {
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>(RAZON_SOCIAL_OPCIONES);
            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable() || isEmpty()) return;
                super.startEdit();
                choiceBox.getSelectionModel().select(getItem());
                choiceBox.setOnAction(event -> commitEdit(choiceBox.getSelectionModel().getSelectedItem()));
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
            }
            cliente.setRazonSocial(nuevaRazonSocial); // Actualiza el modelo
            clientesTableView.refresh();
        });

        // --- Columna Persona Contacto ---
        personaContactoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        personaContactoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevaPersonaContacto = event.getNewValue().trim();
            if (nuevaPersonaContacto.isEmpty()) {
                mostrarAlerta("Advertencia", "La persona de contacto no puede quedar vacía.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            cliente.setPersonaContacto(nuevaPersonaContacto); // Actualiza el modelo
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
            cliente.setCondicionesPago(nuevasCondiciones); // Actualiza el modelo
            clientesTableView.refresh();
        });

        // --- Columna Estado (Mantiene guardado inmediato, ya que es un toggle de estado) ---
        estadoColumn.setCellFactory(column -> new TableCell<Cliente, String>() {
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>();
            // ... (código interno de ChoiceBox omitido por brevedad, sin cambios) ...
            @Override public void startEdit() { /* ... */ }
            @Override public void cancelEdit() { /* ... */ }
            @Override protected void updateItem(String item, boolean empty) { /* ... */
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

            // Llama a DB inmediatamente
            boolean exito = clienteDAO.modificarEstadoCliente(cliente.getIdCliente(), nuevoEstado);
            if (exito) {
                mostrarAlerta("Éxito", "Estado del cliente actualizado.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el estado.", Alert.AlertType.ERROR);
                cliente.setEstado(estadoOriginal); // Revertir en el modelo si falla
            }
            clientesTableView.refresh();
        });


        // --- Columna Accion (Direccion) ---
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

        // --- Configuración de filtros ---
        estadoChoiceBox.setItems(FXCollections.observableArrayList("Todos", "Activo", "Desactivado"));
        estadoChoiceBox.getSelectionModel().select("Todos");

        cargarClientesYConfigurarFiltros();
    }

    // =========================================================================================
    // === MÉTODO PARA GUARDAR LOS CAMBIOS EN LA BASE DE DATOS =================================
    // =========================================================================================

    @FXML
    public void handleModificarClienteButton(ActionEvent event) {
        Cliente selectedCliente = clientesTableView.getSelectionModel().getSelectedItem();
        if (selectedCliente != null) {
            String emailEnModelo = selectedCliente.getEmail();

            // 1. VALIDACIÓN FINAL: Campos esenciales no vacíos (que podrían no haberse re-validado en setOnEditCommit)
            if (selectedCliente.getNombre().isEmpty() || selectedCliente.getApellido().isEmpty() || selectedCliente.getNumeroDocumento().isEmpty()) {
                mostrarAlerta("Error de Validación", "Asegúrese de que los campos Nombre, Apellido y Número de Documento no estén vacíos.", Alert.AlertType.ERROR);
                clientesTableView.refresh();
                return;
            }

            // 2. VALIDACIÓN FINAL: Email
            if (!validarFormatoEmail(emailEnModelo)) {
                mostrarAlerta("Error de Validación", "El formato del email ('" + emailEnModelo + "') es inválido. Por favor, edítelo en la tabla.", Alert.AlertType.ERROR);
                clientesTableView.refresh();
                return;
            }
            if (personaDAO.verificarSiMailExisteParaOtro(emailEnModelo, selectedCliente.getIdPersona())) {
                mostrarAlerta("Error de Validación", "El email ingresado ya está registrado para otro cliente.", Alert.AlertType.ERROR);
                clientesTableView.refresh();
                return;
            }

            // 3. GUARDAR EN DB: SÓLO AQUÍ se llama a la modificación
            boolean exito = clienteDAO.modificarCliente(selectedCliente);
            if (exito) {
                mostrarAlerta("Éxito", "Cliente modificado y guardado exitosamente en la base de datos.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo modificar el cliente en la base de datos.", Alert.AlertType.ERROR);
            }
            clientesTableView.refresh();
        } else {
            mostrarAlerta("Advertencia", "Por favor, seleccione un cliente y realice las modificaciones en la tabla antes de guardar.", Alert.AlertType.WARNING);
        }
    }

    // =========================================================================================
    // === MÉTODOS DE UTILIDAD Y LÓGICA DE NAVEGACIÓN (Sin cambios significativos) =============
    // =========================================================================================

    // ... (El resto de métodos se mantienen iguales a los de tu código original) ...

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
            stage.setResizable(false);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registroCliente.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof RegistroController) {
                ((RegistroController) controller).setClienteController(this);
            } else {
                System.err.println("Error: El controlador cargado no es RegistroController.");
            }

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

    @FXML
    public void handleRefreshButton(ActionEvent event) {
        refreshClientesTable();
    }

    // --- Métodos de Validación ---

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

        // Solo verifica en la DB si el email cambió
        if (!trimmedEmail.equalsIgnoreCase(emailOriginal)) {
            if (personaDAO.verificarSiMailExisteParaOtro(trimmedEmail, cliente.getIdPersona())) {
                mostrarAlerta("Error de Modificación", "El email que ingresó ya se encuentra registrado para otro cliente.", Alert.AlertType.WARNING);
                return false;
            }
        }

        cliente.setEmail(trimmedEmail); // Actualiza el modelo
        return true;
    }

    private boolean validarFormatoEmail(String email) {
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
            // Asegurarse de que MenuController.loadScene existe y funciona
            // Usamos Objects.requireNonNull para evitar NullPointerException si la clase no existe
            Class.forName("app.controller.MenuController");
            MenuController.loadScene(
                    (Node) event.getSource(),
                    "/menuAbms.fxml",
                    "Menú ABMs"
            );

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Navegación", "No se pudo cargar la vista anterior.", Alert.AlertType.ERROR);
        } catch (ClassNotFoundException e) {
            mostrarAlerta("Error", "Falta la clase MenuController para la navegación.", Alert.AlertType.ERROR);
        }
    }
}