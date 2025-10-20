package app.controller;

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
    private Set<Proveedor> proveedoresPendientesDeGuardar = new HashSet<>();

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

        // ====================================================================
        // === CONFIGURACIÓN DE EDICIÓN: SOLO ACTUALIZA MODELO Y REGISTRA CAMBIO
        // ====================================================================

        // --- Columna Nombre ---
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        nombreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nombreColumn.setOnEditCommit(event -> {
            Proveedor proveedor = event.getRowValue();
            String nuevoNombre = event.getNewValue().trim();

            if (nuevoNombre.isEmpty()) {
                mostrarAlerta("Advertencia", "El nombre no puede quedar vacío.", Alert.AlertType.WARNING);
                proveedoresTableView.refresh();
                return;
            }
            if (validarSoloLetras(nuevoNombre)) {
                proveedor.setNombre(nuevoNombre);
                proveedoresPendientesDeGuardar.add(proveedor); // REGISTRA EL CAMBIO
            } else {
                mostrarAlerta("Advertencia", "El nombre solo puede contener letras.", Alert.AlertType.WARNING);
            }
            proveedoresTableView.refresh();
        });

        // --- Columna Contacto ---
        contactoColumn.setCellValueFactory(new PropertyValueFactory<>("contacto"));
        contactoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        contactoColumn.setOnEditCommit(event -> {
            Proveedor proveedor = event.getRowValue();
            String nuevoContacto = event.getNewValue().trim();

            if (nuevoContacto.isEmpty()) {
                mostrarAlerta("Advertencia", "El contacto no puede quedar vacío.", Alert.AlertType.WARNING);
                proveedoresTableView.refresh();
                return;
            }
            if (validarSoloLetras(nuevoContacto)) {
                proveedor.setContacto(nuevoContacto);
                proveedoresPendientesDeGuardar.add(proveedor); // REGISTRA EL CAMBIO
            } else {
                mostrarAlerta("Advertencia", "El contacto solo puede contener letras.", Alert.AlertType.WARNING);
            }
            proveedoresTableView.refresh();
        });

        // --- Columna Email ---
        mailColumn.setCellValueFactory(new PropertyValueFactory<>("mail"));
        mailColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        mailColumn.setOnEditCommit(event -> {
            Proveedor proveedor = event.getRowValue();
            String nuevoMail = event.getNewValue();
            String mailOriginal = event.getOldValue();

            if (validarYGuardarMail(proveedor, nuevoMail, mailOriginal)) {
                proveedoresPendientesDeGuardar.add(proveedor); // REGISTRA EL CAMBIO
            }
            proveedoresTableView.refresh();
        });

        // --- Columna Tipo Proveedor (ChoiceBox) ---
        tipoProveedorColumn.setCellValueFactory(cellData -> cellData.getValue().descripcionTipoProveedorProperty());
        tipoProveedorColumn.setCellFactory(column -> new TableCell<Proveedor, String>() {
            private final ChoiceBox<TipoProveedor> choiceBox = new ChoiceBox<>();
            private boolean isEditing = false;

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

                    // IMPORTANTE: Al seleccionar un valor, se hace commitEdit con la descripción
                    choiceBox.setOnAction(event -> {
                        TipoProveedor selectedTipo = choiceBox.getSelectionModel().getSelectedItem();
                        if (selectedTipo != null) {
                            commitEdit(selectedTipo.getDescripcion());
                        } else {
                            cancelEdit();
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
                isEditing = true;

                Proveedor proveedor = getTableView().getItems().get(getIndex());
                // Selecciona el tipo actual del proveedor
                tiposProveedor.stream()
                        .filter(t -> t.getId() == proveedor.getIdTipoProveedor())
                        .findFirst()
                        .ifPresent(choiceBox.getSelectionModel()::select);

                setGraphic(choiceBox);
                setText(null);
                choiceBox.show();
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                isEditing = false;
                setGraphic(null);
                setText(getItem());
            }

            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);
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

        tipoProveedorColumn.setOnEditCommit(event -> {
            Proveedor proveedor = event.getRowValue();
            String nuevaDescripcion = event.getNewValue();

            try {
                TipoProveedor nuevoTipo = tipoProveedorDAO.getTipoProveedorByDescription(nuevaDescripcion);
                if (nuevoTipo != null) {
                    // Solo modificamos si el ID es diferente
                    if(proveedor.getIdTipoProveedor() != nuevoTipo.getId()){
                        proveedor.setIdTipoProveedor(nuevoTipo.getId());
                        proveedor.setDescripcionTipoProveedor(nuevoTipo.getDescripcion());
                        proveedoresPendientesDeGuardar.add(proveedor); // REGISTRA EL CAMBIO
                    }
                } else {
                    mostrarAlerta("Error", "Tipo de proveedor no encontrado.", Alert.AlertType.ERROR);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error de BD", "Ocurrió un error al procesar el tipo de proveedor.", Alert.AlertType.ERROR);
            } finally {
                proveedoresTableView.refresh();
            }
        });


        // --- Columna Estado (ChoiceBox) ---
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

            // Solo registra el cambio en el modelo y en el set pendiente
            if (!nuevoEstado.equals(event.getOldValue())) {
                proveedor.setEstado(nuevoEstado);
                proveedoresPendientesDeGuardar.add(proveedor); // REGISTRA EL CAMBIO
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

        // --- Configuración de Filtros (Se mantiene igual) ---
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

    // ====================================================================
    // === MÉTODOS DE LÓGICA (AUXILIARES Y HANDLERS)
    // ====================================================================

    private void mostrarDireccionProveedor(int idDireccion) {
        Direccion direccion = direccionDAO.obtenerPorId(idDireccion);
        if (direccion == null) {
            mostrarAlerta("Error", "No se encontró la dirección asociada a este proveedor.", Alert.AlertType.ERROR);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/verDireccion.fxml"));
            Parent root = loader.load();

            // Asumo que tienes un VerDireccionController
            // VerDireccionController direccionController = loader.getController();
            // direccionController.setDireccion(direccion);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Dirección del Proveedor");
            stage.initModality(Modality.APPLICATION_MODAL);
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
                    (proveedor.getNombre() != null && proveedor.getNombre().toLowerCase().contains(searchText)) ||
                    (proveedor.getContacto() != null && proveedor.getContacto().toLowerCase().contains(searchText)) ||
                    (proveedor.getMail() != null && proveedor.getMail().toLowerCase().contains(searchText));

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

            // Asumo que existe RegistroProveedorController
            // RegistroProveedorController registroController = loader.getController();
            // registroController.setProveedorController(this);

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

            newStage.showAndWait();

            refreshProveedoresTable();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el formulario de registro de proveedor.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Refactorizado: Ahora procesa el Set de cambios pendientes, registra el historial y guarda con transacción.
     */
    @FXML
    public void handleModificarProveedorButton(ActionEvent event) {

        if (proveedoresPendientesDeGuardar.isEmpty()) {
            mostrarAlerta("Advertencia", "No hay modificaciones pendientes para guardar.", Alert.AlertType.WARNING);
            return;
        }

        // Asumo que SessionManager está implementado y proporciona el ID del usuario logeado.
        int loggedInUserId = SessionManager.getInstance().getLoggedInUserId();

        // --- INICIO DEL PROCESO DE GUARDADO MÚLTIPLE ---
        int exitos = 0;
        int fallos = 0;

        // Copia para iterar
        Set<Proveedor> proveedoresAGuardar = new HashSet<>(proveedoresPendientesDeGuardar);

        for (Proveedor selectedProveedor : proveedoresAGuardar) {

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                conn.setAutoCommit(false);

                // 1. Obtener datos originales para el historial (Necesita ProveedorDAO.getProveedorById(int, Connection))
                Proveedor originalProveedor = proveedorDAO.getProveedorById(selectedProveedor.getIdProveedor(), conn);

                if (originalProveedor == null) {
                    mostrarAlerta("Error Interno", "No se encontraron datos originales para el proveedor ID: " + selectedProveedor.getIdProveedor() + ". Transacción fallida.", Alert.AlertType.ERROR);
                    fallos++;
                    continue;
                }

                boolean exitoHistorial = true;

                // 2. Comparar y registrar cambios en Historial

                if (!selectedProveedor.getNombre().equals(originalProveedor.getNombre())) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Proveedor", "nombre", selectedProveedor.getIdProveedor(),
                            originalProveedor.getNombre(), selectedProveedor.getNombre(), conn
                    );
                }

                if (!selectedProveedor.getContacto().equals(originalProveedor.getContacto())) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Proveedor", "contacto", selectedProveedor.getIdProveedor(),
                            originalProveedor.getContacto(), selectedProveedor.getContacto(), conn
                    );
                }

                if (!selectedProveedor.getMail().equals(originalProveedor.getMail())) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Proveedor", "mail", selectedProveedor.getIdProveedor(),
                            originalProveedor.getMail(), selectedProveedor.getMail(), conn
                    );
                }

                if (!selectedProveedor.getEstado().equals(originalProveedor.getEstado())) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Proveedor", "estado", selectedProveedor.getIdProveedor(),
                            originalProveedor.getEstado(), selectedProveedor.getEstado(), conn
                    );
                }

                if (selectedProveedor.getIdTipoProveedor() != originalProveedor.getIdTipoProveedor()) {
                    // Obtener descripciones para el historial (Necesita TipoProveedorDAO.getTipoProveedorById(int))
                    String descPrevio = tipoProveedorDAO.getTipoProveedorById(originalProveedor.getIdTipoProveedor()).getDescripcion();
                    String descNuevo = tipoProveedorDAO.getTipoProveedorById(selectedProveedor.getIdTipoProveedor()).getDescripcion();

                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Proveedor", "id_tipo_proveedor", selectedProveedor.getIdProveedor(),
                            descPrevio, descNuevo, conn // Se registran las descripciones
                    );
                }

                // 3. Actualizar la tabla Proveedor
                String updateProveedorSql = "UPDATE Proveedor SET nombre = ?, contacto = ?, mail = ?, estado = ?, id_tipo_proveedor = ? WHERE id_proveedor = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateProveedorSql)) {
                    stmt.setString(1, selectedProveedor.getNombre());
                    stmt.setString(2, selectedProveedor.getContacto());
                    stmt.setString(3, selectedProveedor.getMail());
                    stmt.setString(4, selectedProveedor.getEstado());
                    stmt.setInt(5, selectedProveedor.getIdTipoProveedor());
                    stmt.setInt(6, selectedProveedor.getIdProveedor());
                    stmt.executeUpdate();
                }

                // 4. Commit o Rollback
                if (exitoHistorial) {
                    conn.commit();
                    exitos++;
                    proveedoresPendientesDeGuardar.remove(selectedProveedor);
                } else {
                    conn.rollback();
                    fallos++;
                    mostrarAlerta("Error de Historial", "Fallo al registrar historial para el proveedor ID: " + selectedProveedor.getIdProveedor() + ". Transacción revertida.", Alert.AlertType.ERROR);
                }

            } catch (SQLException e) {
                fallos++;
                e.printStackTrace();
                mostrarAlerta("Error de Guardado", "Error de base de datos al guardar los cambios para el proveedor ID: " + selectedProveedor.getIdProveedor() + ". Error: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } // Fin del bucle for

        // --- Mostrar Resultado Final ---
        if (exitos > 0 || fallos > 0) {
            String mensaje = "Proceso de Guardado Finalizado:\n" +
                    "- Modificaciones Guardadas con Éxito: " + exitos + "\n" +
                    "- Fallos en el Guardado: " + fallos;
            mostrarAlerta("Proceso Finalizado", mensaje, Alert.AlertType.INFORMATION);
        }

        refreshProveedoresTable();
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
        String originalMailTrimmed = mailOriginal != null ? mailOriginal.trim() : "";

        if (trimmedMail.isEmpty()) {
            mostrarAlerta("Advertencia", "El email no puede quedar vacío.", Alert.AlertType.WARNING);
            return false;
        }

        if (!validarFormatoEmail(trimmedMail)) {
            mostrarAlerta("Advertencia", "El formato del correo electrónico no es válido.", Alert.AlertType.WARNING);
            return false;
        }

        // Solo verificamos unicidad si el email realmente cambió
        if (!trimmedMail.equalsIgnoreCase(originalMailTrimmed)) {
            if (proveedorDAO.verificarSiMailExisteParaOtro(trimmedMail, proveedor.getIdProveedor())) {
                mostrarAlerta("Error de Modificación", "El email que ingresó ya se encuentra registrado para otro proveedor.", Alert.AlertType.WARNING);
                return false;
            }
        }

        // Actualiza el modelo en memoria
        proveedor.setMail(trimmedMail);
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
            MenuController.loadScene(
                    (Node) event.getSource(),
                    "/menuAbmStock.fxml",
                    "Menú ABMs de Stock"
            );
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Navegación", "No se pudo cargar la vista anterior.", Alert.AlertType.ERROR);
        }
    }
}