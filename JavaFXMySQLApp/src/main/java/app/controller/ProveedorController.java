package app.controller;

import app.controller.SessionManager;
import app.dao.DireccionDAO;
import app.dao.ProveedorDAO;
import app.dao.TipoProveedorDAO;
import app.dao.HistorialActividadDAO;
import app.model.Direccion;
import app.model.Proveedor;
import app.model.TipoProveedor;
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
import javafx.stage.Stage;
import javafx.util.StringConverter;

import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Optional;

// Nota: Asumo que MenuController.loadScene está disponible.
// import static app.controller.MenuController.loadScene;


public class ProveedorController {

    // Configuración de la conexión (DEBE COINCIDIR CON LA DE TUS DAOs)
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";


    @FXML private TableView<Proveedor> proveedoresTableView;
    @FXML private TableColumn<Proveedor, Number> idProveedorColumn;
    @FXML private TableColumn<Proveedor, String> nombreColumn;
    @FXML private TableColumn<Proveedor, String> contactoColumn;
    @FXML private TableColumn<Proveedor, String> mailColumn;
    @FXML private TableColumn<Proveedor, String> estadoColumn;
    @FXML private TableColumn<Proveedor, String> tipoProveedorColumn;
    @FXML private TableColumn<Proveedor, Void> accionColumn;

    @FXML private TextField filterField;
    @FXML private ChoiceBox<String> estadoChoiceBox;
    @FXML private ChoiceBox<TipoProveedor> tipoProveedorChoiceBox;
    @FXML private Button nuevoProveedorButton;
    @FXML private Button modificarProveedorButton;
    @FXML private Button refreshButton;

    private ProveedorDAO proveedorDAO;
    private DireccionDAO direccionDAO;
    private TipoProveedorDAO tipoProveedorDAO;
    private HistorialActividadDAO historialDAO;
    private ObservableList<Proveedor> masterData = FXCollections.observableArrayList();
    private FilteredList<Proveedor> filteredData;
    private List<TipoProveedor> tiposProveedor;

    // Set para rastrear todos los proveedores que tienen cambios pendientes de guardar en DB

    public ProveedorController() {
        this.proveedorDAO = new ProveedorDAO();
        this.direccionDAO = new DireccionDAO();
        this.tipoProveedorDAO = new TipoProveedorDAO();
        this.historialDAO = new HistorialActividadDAO();
    }

    @FXML
    private void initialize() {
        proveedoresTableView.setEditable(true);
        idProveedorColumn.setCellValueFactory(new PropertyValueFactory<>("idProveedor"));

        // ==========================================================
        // === VINCULACIÓN DEL ANCHO DE COLUMNAS PORCENTUAL ========
        // ==========================================================
        // Asegúrate de que el FXML tiene <TableView fx:constant="CONSTRAINED_RESIZE_POLICY" />
        idProveedorColumn.prefWidthProperty().bind(proveedoresTableView.widthProperty().multiply(0.05));
        nombreColumn.prefWidthProperty().bind(proveedoresTableView.widthProperty().multiply(0.20));
        contactoColumn.prefWidthProperty().bind(proveedoresTableView.widthProperty().multiply(0.15));
        mailColumn.prefWidthProperty().bind(proveedoresTableView.widthProperty().multiply(0.25)); // Email es el más largo
        estadoColumn.prefWidthProperty().bind(proveedoresTableView.widthProperty().multiply(0.10));
        tipoProveedorColumn.prefWidthProperty().bind(proveedoresTableView.widthProperty().multiply(0.15));
        accionColumn.prefWidthProperty().bind(proveedoresTableView.widthProperty().multiply(0.10));


        // =========================================================================================
        // === ONEDITCOMMIT: SOLO VALIDACIÓN Y ACTUALIZACIÓN DEL MODELO ============================
        // =========================================================================================

        // --- Columna Nombre ---
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        nombreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nombreColumn.setOnEditCommit(event -> {
            String nuevoNombre = event.getNewValue().trim();
            if (nuevoNombre.isEmpty()) {
                mostrarAlerta("Advertencia", "El nombre no puede quedar vacío.", Alert.AlertType.WARNING);
                proveedoresTableView.refresh(); return;
            }
            if (validarSoloLetras(nuevoNombre)) {
                event.getRowValue().setNombre(nuevoNombre); // Actualiza el modelo
            } else {
                mostrarAlerta("Advertencia", "El nombre solo puede contener letras.", Alert.AlertType.WARNING);
                proveedoresTableView.refresh();
            }
        });

        // --- Columna Contacto ---
        contactoColumn.setCellValueFactory(new PropertyValueFactory<>("contacto"));
        contactoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        contactoColumn.setOnEditCommit(event -> {
            String nuevoContacto = event.getNewValue().trim();
            if (nuevoContacto.isEmpty()) {
                mostrarAlerta("Advertencia", "El contacto no puede quedar vacío.", Alert.AlertType.WARNING);
                proveedoresTableView.refresh(); return;
            }
            if (validarSoloLetras(nuevoContacto)) {
                event.getRowValue().setContacto(nuevoContacto); // Actualiza el modelo
            } else {
                mostrarAlerta("Advertencia", "El contacto solo puede contener letras.", Alert.AlertType.WARNING);
                proveedoresTableView.refresh();
            }
        });

        // --- Columna Mail ---
        mailColumn.setCellValueFactory(new PropertyValueFactory<>("mail"));
        mailColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        mailColumn.setOnEditCommit(event -> {
            Proveedor proveedor = event.getRowValue();
            String nuevoMail = event.getNewValue();
            String mailOriginal = event.getOldValue();

            if (!validarYGuardarMail(proveedor, nuevoMail, mailOriginal)) {
                proveedoresTableView.refresh();
            }
        });

        // =========================================================================================
        // === COLUMNA TIPO PROVEEDOR (ChoiceBox) - CORREGIDO PARA PERSISTENCIA EN EL MODELO =======
        // =========================================================================================
        tipoProveedorColumn.setCellValueFactory(cellData -> cellData.getValue().descripcionTipoProveedorProperty());
        tipoProveedorColumn.setCellFactory(column -> new TableCell<Proveedor, String>() {
            private final ChoiceBox<TipoProveedor> choiceBox = new ChoiceBox<>();

            {
                try {
                    tiposProveedor = tipoProveedorDAO.getAllTipoProveedores();
                    choiceBox.setItems(FXCollections.observableArrayList(tiposProveedor));
                    choiceBox.setConverter(new StringConverter<TipoProveedor>() {
                        @Override
                        public String toString(TipoProveedor tipo) {
                            return tipo == null ? "" : tipo.getDescripcion();
                        }
                        @Override
                        public TipoProveedor fromString(String string) {
                            return tiposProveedor.stream()
                                    .filter(t -> t.getDescripcion().equals(string))
                                    .findFirst()
                                    .orElse(null);
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarAlerta("Error de Carga", "No se pudieron cargar los tipos de proveedor para la edición.", Alert.AlertType.ERROR);
                }
            }

            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
                    return;
                }
                super.startEdit();

                Proveedor proveedor = getTableView().getItems().get(getIndex());
                // Selecciona el tipo actual
                tiposProveedor.stream()
                        .filter(t -> t.getId() == proveedor.getIdTipoProveedor())
                        .findFirst()
                        .ifPresent(choiceBox.getSelectionModel()::select);

                // *** CAMBIO CRUCIAL: El commit se hace al seleccionar un valor ***
                choiceBox.setOnAction(event -> {
                    TipoProveedor selectedType = choiceBox.getSelectionModel().getSelectedItem();
                    if (selectedType != null) {
                        // Importante: El commit debe devolver el String de la columna (la descripción)
                        commitEdit(selectedType.getDescripcion());
                    } else {
                        cancelEdit();
                    }
                });

                setGraphic(choiceBox);
                setText(null);
                choiceBox.show(); // Ayuda a que se despliegue inmediatamente
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
                        setGraphic(choiceBox);
                        setText(null);
                    } else {
                        setGraphic(null);
                        setText(item);
                    }
                }
            }
        });

        // La lógica de onEditCommit solo actualiza el modelo
        tipoProveedorColumn.setOnEditCommit(event -> {
            Proveedor proveedor = event.getRowValue();
            String nuevaDescripcion = event.getNewValue();

            try {
                // Buscamos el objeto TipoProveedor para obtener el ID
                Optional<TipoProveedor> nuevoTipoOpt = tiposProveedor.stream()
                        .filter(t -> t.getDescripcion().equals(nuevaDescripcion))
                        .findFirst();

                if (nuevoTipoOpt.isPresent()) {
                    TipoProveedor nuevoTipo = nuevoTipoOpt.get();
                    // Actualiza el modelo con el nuevo ID y Descripción
                    proveedor.setIdTipoProveedor(nuevoTipo.getId());
                    proveedor.setDescripcionTipoProveedor(nuevoTipo.getDescripcion());
                    // NO guardamos en DB aquí
                } else {
                    mostrarAlerta("Error", "Tipo de proveedor no encontrado.", Alert.AlertType.ERROR);
                    // Revertir en el modelo si es inválido (usa el valor que estaba en la DB antes de la edición)
                    // Para revertir en el modelo, necesitamos recargar el valor original de la lista masterData,
                    // pero para simplificar la lógica de la tabla, solo forzamos el refresh.
                }
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Error", "Ocurrió un error al procesar el tipo de proveedor.", Alert.AlertType.ERROR);
            } finally {
                proveedoresTableView.refresh();
            }
        });
        // =========================================================================================


        // --- Columna Estado (Mantiene guardado inmediato) ---
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        estadoColumn.setCellFactory(column -> new TableCell<Proveedor, String>() {
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>();

            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
                    return;
                }
                super.startEdit();
                choiceBox.setItems(FXCollections.observableArrayList("Activo", "Desactivado"));
                choiceBox.getSelectionModel().select(getItem());
                // Aquí el setOnAction es correcto porque queremos guardar inmediatamente
                choiceBox.setOnAction(event -> commitEdit(choiceBox.getSelectionModel().getSelectedItem()));
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
                    setStyle(null);
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
                    setStyle(null);
                }
            }
        });

        estadoColumn.setOnEditCommit(event -> {
            Proveedor proveedor = event.getRowValue();
            String nuevoEstado = event.getNewValue();
            String estadoOriginal = event.getOldValue();

            if (nuevoEstado.equals(estadoOriginal)) { // No hay cambio, salir.
                proveedoresTableView.refresh();
                return;
            }

            // 1. Guardar en DB
            boolean exito = proveedorDAO.modificarEstadoProveedor(proveedor.getIdProveedor(), nuevoEstado);

            if (exito) {
                proveedor.setEstado(nuevoEstado);

                // 2. REGISTRO DE ACTIVIDAD PARA CAMBIO DE ESTADO
                try {
                    int loggedInUserId = app.controller.SessionManager.getInstance().getLoggedInUserId();

                    historialDAO.insertarRegistro(
                            loggedInUserId,             // 1. idUsuarioResponsable
                            "Proveedor",                // 2. tabla_afectada
                            "estado",                   // 3. columna_afectada
                            proveedor.getIdProveedor(), // 4. id_registro_modificado
                            estadoOriginal,             // 5. dato_previo_modificacion
                            nuevoEstado                 // 6. dato_modificado
                    );
                } catch (Exception e) {
                    System.err.println("Advertencia: Fallo al registrar la actividad en el historial para Proveedor ID " + proveedor.getIdProveedor());
                    e.printStackTrace();
                }
                // ------------------------------------------

                mostrarAlerta("Éxito", "Estado del proveedor actualizado.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el estado.", Alert.AlertType.ERROR);
                proveedor.setEstado(estadoOriginal); // Revertir en el modelo si falla la DB
            }
            proveedoresTableView.refresh();
        });

        // --- Columna Acción (Ver Dirección) ---
        accionColumn.setCellFactory(param -> new TableCell<Proveedor, Void>() {
            private final Button btn = new Button("Ver Dirección");
            {
                btn.setOnAction(event -> {
                    Proveedor proveedor = getTableView().getItems().get(getIndex());
                    mostrarDireccionProveedor(proveedor.getIdDireccion());
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

        try {
            ObservableList<TipoProveedor> tipos = FXCollections.observableArrayList();
            TipoProveedor todos = new TipoProveedor(0, "Todos");
            tipos.add(todos);
            tipos.addAll(tipoProveedorDAO.getAllTipoProveedores());
            tipoProveedorChoiceBox.setItems(tipos);
            tipoProveedorChoiceBox.getSelectionModel().select(todos);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Carga", "No se pudieron cargar los tipos de proveedor.", Alert.AlertType.ERROR);
        }

        cargarProveedoresYConfigurarFiltros();
    }

    // =========================================================================================
    // === MÉTODO PARA GUARDAR LOS CAMBIOS EN LA BASE DE DATOS (Botón) =========================
    // =========================================================================================

    @FXML
    public void handleModificarProveedorButton(ActionEvent event) {
        Proveedor selectedProveedor = proveedoresTableView.getSelectionModel().getSelectedItem();
        Proveedor proveedorOriginal = null; // [cite: 86]

        if (selectedProveedor != null) {
            // Encerramos toda la operación en un try-with-resources para manejar la Conexión
            try (Connection conn = proveedorDAO.getConnection()) { // <-- Es necesaria la conexión para getProveedorById

                // 1. OBTENER DATOS ORIGINALES
                proveedorOriginal = proveedorDAO.getProveedorById(selectedProveedor.getIdProveedor(), conn); // [cite: 87]

                if (proveedorOriginal == null) {
                    mostrarAlerta("Error", "No se encontraron datos originales para el proveedor. No se pudo guardar.", Alert.AlertType.ERROR); // [cite: 88]
                    proveedoresTableView.refresh();
                    return;
                }

                // *** Tu lógica de Validación existente comienza aquí ***
                String emailEnModelo = selectedProveedor.getMail();
                // ... (Tus validaciones de campos obligatorios, email, etc.) ... // [cite: 89]

                // 4. GUARDAR EN DB
                boolean exito = proveedorDAO.modificarProveedor(selectedProveedor);

                if (exito) { //
                    // 5. REGISTRAR ACTIVIDAD
                    int loggedInUserId = SessionManager.getInstance().getLoggedInUserId(); //

                    // --- 1. Comparar y Registrar Nombre ---
                    if (!proveedorOriginal.getNombre().equals(selectedProveedor.getNombre())) {
                        historialDAO.insertarRegistro(
                                loggedInUserId, "Proveedor", "nombre", selectedProveedor.getIdProveedor(),
                                proveedorOriginal.getNombre(), selectedProveedor.getNombre() // [cite: 91, 92]
                        );
                    }

                    // 🚨 CORRECCIÓN: --- 2. Comparar y Registrar Contacto ---
                    if (!proveedorOriginal.getContacto().equals(selectedProveedor.getContacto())) {
                        historialDAO.insertarRegistro(
                                loggedInUserId, "Proveedor", "contacto", selectedProveedor.getIdProveedor(),
                                proveedorOriginal.getContacto(), selectedProveedor.getContacto()
                        );
                    }

                    // 🚨 CORRECCIÓN: --- 3. Comparar y Registrar Mail (Email) ---
                    if (!proveedorOriginal.getMail().equals(selectedProveedor.getMail())) {
                        historialDAO.insertarRegistro(
                                loggedInUserId, "Proveedor", "mail", selectedProveedor.getIdProveedor(),
                                proveedorOriginal.getMail(), selectedProveedor.getMail()
                        );
                    }

                    // 🚨 CORRECCIÓN: --- 4. Comparar y Registrar Tipo Proveedor (ChoiceBox) ---
                    if (proveedorOriginal.getIdTipoProveedor() != selectedProveedor.getIdTipoProveedor()) {
                        // Almacena solo la descripción (nombre) en el historial para ambos valores.
                        historialDAO.insertarRegistro(
                                loggedInUserId,
                                "Proveedor",
                                "id_tipo_proveedor",
                                selectedProveedor.getIdProveedor(),
                                // 5. Dato Previo: Solo la descripción del tipo anterior
                                proveedorOriginal.getDescripcionTipoProveedor(),
                                // 6. Dato Nuevo: Solo la descripción del tipo nuevo
                                selectedProveedor.getDescripcionTipoProveedor()
                        );
                    }

                    mostrarAlerta("Éxito", "Proveedor modificado y guardado exitosamente en la base de datos.", Alert.AlertType.INFORMATION); // [cite: 94]
                } else {
                    mostrarAlerta("Error", "No se pudo modificar el proveedor en la base de datos.", Alert.AlertType.ERROR); // [cite: 95]
                }
            } catch (SQLException e) {
                e.printStackTrace(); // [cite: 96]
                mostrarAlerta("Error de Base de Datos", "Ocurrió un error de DB al intentar obtener o modificar el proveedor: " + e.getMessage(), Alert.AlertType.ERROR); // [cite: 97]
            } catch (Exception e) {
                e.printStackTrace(); // [cite: 98]
                mostrarAlerta("Error Interno", "Ocurrió un error inesperado: " + e.getMessage(), Alert.AlertType.ERROR); // [cite: 99]
            }

            proveedoresTableView.refresh(); // [cite: 100]
        } else {
            mostrarAlerta("Advertencia", "Por favor, seleccione una fila y modifique los datos antes de guardar.", Alert.AlertType.WARNING); // [cite: 101, 102]
        }
    }

    // =========================================================================================
    // === MÉTODOS DE UTILIDAD Y LÓGICA DE NAVEGACIÓN ==========================================
    // =========================================================================================

    private void mostrarDireccionProveedor(int idDireccion) {
        Direccion direccion = direccionDAO.obtenerPorId(idDireccion);
        if (direccion == null) {
            mostrarAlerta("Error", "No se encontró la dirección asociada a este proveedor.", Alert.AlertType.ERROR);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/verDireccion.fxml"));
            Parent root = loader.load();

            VerDireccionController direccionController = loader.getController();
            direccionController.setDireccion(direccion);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Dirección del Proveedor");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la ventana de dirección.", Alert.AlertType.ERROR);
        }
    }

    private void cargarProveedoresYConfigurarFiltros() {
        masterData = proveedorDAO.getAllProveedores();
        filteredData = new FilteredList<>(masterData, p -> true);

        filterField.textProperty().addListener((observable, oldValue, newValue) -> updateFilteredList());
        estadoChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateFilteredList());
        tipoProveedorChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateFilteredList());

        SortedList<Proveedor> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(proveedoresTableView.comparatorProperty());
        proveedoresTableView.setItems(sortedData);
    }

    private void updateFilteredList() {
        filteredData.setPredicate(proveedor -> {
            String searchText = filterField.getText() == null ? "" : filterField.getText().toLowerCase();
            String selectedStatus = estadoChoiceBox.getSelectionModel().getSelectedItem();
            TipoProveedor selectedTipo = tipoProveedorChoiceBox.getSelectionModel().getSelectedItem();

            boolean matchesSearchText = searchText.isEmpty() ||
                    proveedor.getNombre().toLowerCase().contains(searchText) ||
                    proveedor.getContacto().toLowerCase().contains(searchText) ||
                    proveedor.getMail().toLowerCase().contains(searchText);

            boolean matchesStatus = selectedStatus.equals("Todos") ||
                    selectedStatus.equalsIgnoreCase(proveedor.getEstado());

            boolean matchesTipo = (selectedTipo != null && selectedTipo.getId() == 0) ||
                    (selectedTipo != null && selectedTipo.getId() == proveedor.getIdTipoProveedor());

            return matchesSearchText && matchesStatus && matchesTipo;
        });
    }

    @FXML
    public void handleRegistrarProveedorButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registroProveedor.fxml"));
            Parent root = loader.load();

            RegistroProveedorController registroController = loader.getController();
            registroController.setProveedorController(this);

            Stage newStage = new Stage();
            Scene newScene = new Scene(root);
            newStage.setScene(newScene);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double screenHeight = screenBounds.getHeight();

            newStage.setHeight(screenHeight);
            newStage.sizeToScene();

            newStage.setTitle("Registrar Nuevo Proveedor");
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.centerOnScreen();

            newStage.setResizable(false);
            newStage.showAndWait();

            refreshProveedoresTable();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el formulario de registro de proveedor.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleRefreshButton(ActionEvent event) {
        refreshProveedoresTable();
    }

    public void refreshProveedoresTable() {
        String filtroTexto = filterField.getText();
        String filtroEstado = estadoChoiceBox.getValue();
        TipoProveedor filtroTipo = tipoProveedorChoiceBox.getValue();

        masterData.clear();
        masterData.addAll(proveedorDAO.getAllProveedores());

        filterField.setText(filtroTexto);
        estadoChoiceBox.setValue(filtroEstado);
        tipoProveedorChoiceBox.setValue(filtroTipo);

        updateFilteredList();
    }

    private boolean validarSoloLetras(String texto) {
        return texto.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+");
    }

    /**
     * Modificado: Solo valida el formato y la unicidad en la DB. Actualiza el modelo.
     */
    private boolean validarYGuardarMail(Proveedor proveedor, String nuevoMail, String mailOriginal) {
        String trimmedMail = nuevoMail != null ? nuevoMail.trim() : "";

        if (trimmedMail.isEmpty()) {
            mostrarAlerta("Advertencia", "El email no puede quedar vacío.", Alert.AlertType.WARNING);
            return false;
        }

        if (!validarFormatoEmail(trimmedMail)) {
            mostrarAlerta("Advertencia", "El formato del correo electrónico no es válido.", Alert.AlertType.WARNING);
            return false;
        }

        if (!trimmedMail.equalsIgnoreCase(mailOriginal)) {
            if (proveedorDAO.verificarSiMailExisteParaOtro(trimmedMail, proveedor.getIdProveedor())) {
                mostrarAlerta("Error de Modificación", "El email que ingresó ya se encuentra registrado para otro proveedor.", Alert.AlertType.WARNING);
                return false;
            }
        }

        proveedor.setMail(trimmedMail); // Actualiza el modelo
        return true;
    }

    private boolean validarFormatoEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
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
            // Se usa el método estático unificado para asegurar la navegación
            // Es necesario que MenuController.loadScene exista
            // Se asume que MenuController existe en el paquete app.controller
            Class.forName("app.controller.MenuController");
            MenuController.loadScene(
                    (Node) event.getSource(),
                    "/menuAbmStock.fxml",
                    "Menú ABMs de Stock"
            );
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Navegación", "No se pudo cargar la vista anterior.", Alert.AlertType.ERROR);
        } catch (ClassNotFoundException e) {
            mostrarAlerta("Error de Navegación", "Clase MenuController no encontrada. No se puede volver.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleHelpButton() {
        // Creamos una nueva alerta de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuramos el título y los encabezados del mensaje
        alert.setTitle("Ayuda - Menu de Proveedores Registrados");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite La Visualizacion y Modificación de Los datos de Proveedores Registrados:\n"
                + "\n"
                + "1. Visualización y Edición: Modifique directamente los campos de la tabla (Nombre, Contacto y Email) Al hacer doble click en la Columna.\n"
                + "----------------------------------------------------------------------\n"
                + "2. Para Modificar la Categoría y el Estado del Proveedor Haga Click y luego Seleccione la opcion requerida en el ChoiceBox.\n"
                + "----------------------------------------------------------------------\n"
                + "3. Para Registrar un nuevo Proveedor Haga Click en el boton Registrar Nuevo Proveedor.\n"
                + "----------------------------------------------------------------------\n"
                + "4. Para Actualizar o Reiniciar la Tabla haga click en el boton Refrescar.\n"
                + "----------------------------------------------------------------------\n"
                + "5. Filtros: Utilice el campo de texto para buscar usuarios por Nombre, Contacto o Email, y el *ChoiceBox* para filtrar por Estado (Activo/Inactivo) y Por Tipo de Proveedor.\n"
                + "----------------------------------------------------------------------\n"
                + "8. Guardar Cambios: El botón 'Modificar Proveedor' aplica todas las modificaciones realizadas en las celdas de la tabla a la base de datos.\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();
    }
}