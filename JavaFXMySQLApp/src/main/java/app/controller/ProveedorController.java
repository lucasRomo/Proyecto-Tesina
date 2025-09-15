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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.SQLException;
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

        mailColumn.setCellValueFactory(new PropertyValueFactory<>("mail"));
        mailColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        mailColumn.setOnEditCommit(event -> {
            if (validarEmail(event.getNewValue())) {
                event.getRowValue().setMail(event.getNewValue());
            } else {
                mostrarAlerta("Advertencia", "El formato del correo electrónico no es válido.", Alert.AlertType.WARNING);
                proveedoresTableView.refresh();
            }
        });

        // **INICIO DE LA CORRECCIÓN**
        // Configuración para que TipoProveedor sea editable con un ChoiceBox
        tipoProveedorColumn.setCellValueFactory(new PropertyValueFactory<>("descripcionTipoProveedor"));
        tipoProveedorColumn.setCellFactory(column -> new TableCell<Proveedor, String>() {
            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
                    return;
                }
                super.startEdit();

                final ChoiceBox<TipoProveedor> choiceBox = new ChoiceBox<>();
                ObservableList<TipoProveedor> tipos = FXCollections.observableArrayList();

                try {
                    tipos.addAll(tipoProveedorDAO.getAllTipoProveedores());
                    choiceBox.setItems(tipos);
                    choiceBox.setConverter(new StringConverter<TipoProveedor>() {
                        @Override
                        public String toString(TipoProveedor tipo) {
                            return tipo == null ? "" : tipo.getDescripcion();
                        }
                        @Override
                        public TipoProveedor fromString(String string) {
                            return null;
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarAlerta("Error de Carga", "No se pudieron cargar los tipos de proveedor para la edición.", Alert.AlertType.ERROR);
                }

                Proveedor proveedor = getTableView().getItems().get(getIndex());
                if (proveedor != null) {
                    tipos.stream()
                            .filter(t -> t.getId() == proveedor.getIdTipoProveedor())
                            .findFirst()
                            .ifPresent(choiceBox.getSelectionModel()::select);
                }

                choiceBox.setOnAction(event -> {
                    if (choiceBox.getValue() != null) {
                        commitEdit(choiceBox.getValue().getDescripcion());
                    } else {
                        cancelEdit();
                    }
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
                        setGraphic(new ChoiceBox<>()); // Se crea un nuevo ChoiceBox en update para que funcione el startEdit()
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
                    proveedor.setIdTipoProveedor(nuevoTipo.getId());
                    proveedor.setDescripcionTipoProveedor(nuevoTipo.getDescripcion());

                    boolean exito = proveedorDAO.modificarProveedor(proveedor);

                    if (exito) {
                        mostrarAlerta("Éxito", "Tipo de proveedor actualizado.", Alert.AlertType.INFORMATION);
                    } else {
                        mostrarAlerta("Error", "No se pudo actualizar el tipo de proveedor en la base de datos.", Alert.AlertType.ERROR);
                        proveedor.setDescripcionTipoProveedor(event.getOldValue());
                    }
                } else {
                    mostrarAlerta("Error", "Tipo de proveedor no encontrado.", Alert.AlertType.ERROR);
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
        // **FIN DE LA CORRECCIÓN**

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
                getStyleClass().removeAll("activo-cell", "desactivado-cell");
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        choiceBox.getSelectionModel().select(item);
                        setText(null);
                        setGraphic(choiceBox);
                    } else {
                        setGraphic(null);
                        setText(item);
                        if ("Activo".equalsIgnoreCase(item)) {
                            getStyleClass().add("activo-cell");
                        } else if ("Desactivado".equalsIgnoreCase(item)) {
                            getStyleClass().add("desactivado-cell");
                        }
                    }
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

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Registrar Nuevo Proveedor");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

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
        // Guarda los valores actuales de los filtros.
        String filtroTexto = filterField.getText();
        String filtroEstado = estadoChoiceBox.getValue();
        TipoProveedor filtroTipo = tipoProveedorChoiceBox.getValue();

        // Recarga los datos desde la base de datos.
        masterData.clear();
        masterData.addAll(proveedorDAO.getAllProveedores());

        // Vuelve a aplicar los valores guardados a los controles de filtro.
        filterField.setText(filtroTexto);
        estadoChoiceBox.setValue(filtroEstado);
        tipoProveedorChoiceBox.setValue(filtroTipo);

        // Llama a la función de actualización de la lista filtrada directamente.
        updateFilteredList();
    }

    private boolean validarSoloLetras(String texto) {
        return texto.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+");
    }

    private boolean validarEmail(String email) {
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
}