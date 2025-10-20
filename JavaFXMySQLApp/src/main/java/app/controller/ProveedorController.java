package app.controller;

import app.dao.DireccionDAO;
import app.dao.ProveedorDAO;
import app.dao.TipoProveedorDAO;
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
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProveedorController {

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
    private ObservableList<Proveedor> masterData = FXCollections.observableArrayList();
    private FilteredList<Proveedor> filteredData;
    private List<TipoProveedor> tiposProveedor;

    public ProveedorController() {
        this.proveedorDAO = new ProveedorDAO();
        this.direccionDAO = new DireccionDAO();
        this.tipoProveedorDAO = new TipoProveedorDAO();
    }

    @FXML
    private void initialize() {
        proveedoresTableView.setEditable(true);
        idProveedorColumn.setCellValueFactory(new PropertyValueFactory<>("idProveedor"));

        // Celdas editables con validación
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        nombreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nombreColumn.setOnEditCommit(event -> {
            if (validarSoloLetras(event.getNewValue())) {
                event.getRowValue().setNombre(event.getNewValue());
            } else {
                mostrarAlerta("Advertencia", "El nombre solo puede contener letras.", Alert.AlertType.WARNING);
                proveedoresTableView.refresh();
            }
        });

        contactoColumn.setCellValueFactory(new PropertyValueFactory<>("contacto"));
        contactoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        contactoColumn.setOnEditCommit(event -> {
            if (validarSoloLetras(event.getNewValue())) {
                event.getRowValue().setContacto(event.getNewValue());
            } else {
                mostrarAlerta("Advertencia", "El contacto solo puede contener letras.", Alert.AlertType.WARNING);
                proveedoresTableView.refresh();
            }
        });

        // =========================================================================================
        // === VALIDACIÓN DE EMAIL (MANTENIDA) =====================================================
        // =========================================================================================
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

        // =========================================================================================
        // === CORRECCIÓN DEL TIPO DE PROVEEDOR (REMOVIDO setOnAction) =============================
        // =========================================================================================
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
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarAlerta("Error de Carga", "No se pudieron cargar los tipos de proveedor para la edición.", Alert.AlertType.ERROR);
                }

                // *** ¡CORRECCIÓN! Se remueve choiceBox.setOnAction para que el ChoiceBox se despliegue ***
                // El commitEdit ocurrirá automáticamente cuando el usuario seleccione un valor y pierda el foco.
            }

            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
                    return;
                }
                super.startEdit();
                isEditing = true;

                Proveedor proveedor = getTableView().getItems().get(getIndex());
                tiposProveedor.stream()
                        .filter(t -> t.getId() == proveedor.getIdTipoProveedor())
                        .findFirst()
                        .ifPresent(choiceBox.getSelectionModel()::select);

                setGraphic(choiceBox);
                setText(null);
                choiceBox.show(); // Ayuda a que se despliegue inmediatamente
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
                // Si el valor no ha cambiado, no forzamos el commit para evitar el onEditCommit
                if (newValue != null && !newValue.equals(getItem())) {
                    super.commitEdit(newValue);
                } else if (newValue != null && newValue.equals(getItem())) {
                    // Si el usuario seleccionó el mismo valor, solo cancelamos la edición para cerrar el editor.
                    cancelEdit();
                } else {
                    super.commitEdit(newValue);
                }
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

        // La lógica de guardado (onEditCommit) sigue igual, ya que solo se ejecuta cuando la edición finaliza.
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

                        boolean exito = proveedorDAO.modificarProveedor(proveedor);

                        if (exito) {
                            mostrarAlerta("Éxito", "Tipo de proveedor actualizado.", Alert.AlertType.INFORMATION);
                        } else {
                            mostrarAlerta("Error", "No se pudo actualizar el tipo de proveedor en la base de datos.", Alert.AlertType.ERROR);
                            // Revertir el modelo al valor original si falla la BD
                            proveedor.setDescripcionTipoProveedor(event.getOldValue());
                        }
                    } else {
                        // El valor es el mismo, no hacemos nada ni alertamos
                    }
                } else {
                    mostrarAlerta("Error", "Tipo de proveedor no encontrado.", Alert.AlertType.ERROR);
                    // Revertir el modelo al valor original si es inválido
                    proveedor.setDescripcionTipoProveedor(event.getOldValue());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error de BD", "Ocurrió un error al actualizar el tipo de proveedor.", Alert.AlertType.ERROR);
                proveedor.setDescripcionTipoProveedor(event.getOldValue());
            } finally {
                proveedoresTableView.refresh();
            }
        });
        // =========================================================================================


        // Configuración de la celda de estado con estilo (MANTENIDO)
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
                // En el ChoiceBox de Estado, SÍ queremos el setOnAction, ya que no tiene un conversor complejo
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

            boolean exito = proveedorDAO.modificarEstadoProveedor(proveedor.getIdProveedor(), nuevoEstado);

            if (exito) {
                proveedor.setEstado(nuevoEstado);
                mostrarAlerta("Éxito", "Estado del proveedor actualizado.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el estado.", Alert.AlertType.ERROR);
                proveedor.setEstado(event.getOldValue());
            }
            proveedoresTableView.refresh();
        });

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
            // 1. Cargar el FXML de registro
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registroProveedor.fxml"));
            Parent root = loader.load();

            // 2. Configurar el controlador y el callback
            RegistroProveedorController registroController = loader.getController();
            registroController.setProveedorController(this);

            // 3. Crear el nuevo Stage (ventana) y la Scene
            Stage newStage = new Stage();
            Scene newScene = new Scene(root);
            newStage.setScene(newScene);

            // 4. Obtener las dimensiones de la pantalla (Screen)
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double screenHeight = screenBounds.getHeight();

            // 5. Aplicar el dimensionamiento solicitado:
            // A. Establecer el ALTO al 100% de la pantalla
            newStage.setHeight(screenHeight);

            // B. Adaptar el ANCHO al contenido del FXML
            // sizeToScene calcula el ancho mínimo requerido por el layout del FXML.
            newStage.sizeToScene();

            // 6. Configurar el modo (modal) y mostrar
            newStage.setTitle("Registrar Nuevo Proveedor");
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.centerOnScreen();

            newStage.setResizable(false);
            // Mostrar la nueva ventana y esperar a que se cierre (modal)
            newStage.showAndWait();

            // 7. Refrescar la tabla al volver
            refreshProveedoresTable();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el formulario de registro de proveedor.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleModificarProveedorButton(ActionEvent event) {
        Proveedor selectedProveedor = proveedoresTableView.getSelectionModel().getSelectedItem();
        if (selectedProveedor != null) {

            String emailEnModelo = selectedProveedor.getMail();

            // 1. Validar formato de email
            if (!validarFormatoEmail(emailEnModelo)) {
                mostrarAlerta("Error de Modificación", "El formato del email ('" + emailEnModelo + "') es inválido.", Alert.AlertType.ERROR);
                proveedoresTableView.refresh();
                return;
            }

            // 2. Validar duplicidad de email (excluyendo al proveedor actual)
            if (proveedorDAO.verificarSiMailExisteParaOtro(emailEnModelo, selectedProveedor.getIdProveedor())) {
                mostrarAlerta("Error de Modificación", "El email ingresado ya está registrado para otro proveedor.", Alert.AlertType.ERROR);
                proveedoresTableView.refresh();
                return;
            }

            boolean exito = proveedorDAO.modificarProveedor(selectedProveedor);
            if (exito) {
                mostrarAlerta("Éxito", "Proveedor modificado exitosamente.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo modificar el proveedor en la base de datos.", Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Advertencia", "Por favor, seleccione una fila y modifique los datos antes de guardar.", Alert.AlertType.WARNING);
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
            // Se usa el método estático unificado para asegurar la navegación
            // y que la nueva vista ocupe toda la ventana maximizada.
            MenuController.loadScene(
                    (Node) event.getSource(),
                    "/menuAbmStock.fxml", // Ruta correcta para volver al menú ABM de Stock
                    "Menú ABMs de Stock"   // Título de la ventana
            );
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Navegación", "No se pudo cargar la vista anterior.", Alert.AlertType.ERROR);
        }
    }
}