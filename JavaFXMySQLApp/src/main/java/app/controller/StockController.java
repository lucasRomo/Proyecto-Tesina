package app.controller;

import app.dao.InsumoDAO;
import app.dao.TipoProveedorDAO;
import app.model.Insumo;
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
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class StockController {

    @FXML private TableView<Insumo> insumosTableView;
    @FXML private TableColumn<Insumo, Number> idInsumoColumn;
    @FXML private TableColumn<Insumo, String> nombreInsumoColumn;
    @FXML private TableColumn<Insumo, String> descripcionColumn;
    @FXML private TableColumn<Insumo, Number> stockMinimoColumn;
    @FXML private TableColumn<Insumo, Number> stockActualColumn;
    @FXML private TableColumn<Insumo, String> estadoColumn;
    @FXML private TableColumn<Insumo, String> idTipoProveedorColumn;
    @FXML private TableColumn<Insumo, Void> accionColumn;

    @FXML private TextField filterField;
    @FXML private ChoiceBox<String> estadoChoiceBox;
    // Nuevo botón inyectado
    @FXML private Button alertasStockButton;

    private InsumoDAO insumoDAO;
    private TipoProveedorDAO tipoProveedorDAO;
    private ObservableList<Insumo> masterData = FXCollections.observableArrayList();
    private FilteredList<Insumo> filteredData;
    private List<TipoProveedor> tiposProveedor;

    public StockController() {
        this.insumoDAO = new InsumoDAO();
        this.tipoProveedorDAO = new TipoProveedorDAO();
    }

    @FXML
    private void initialize() {
        insumosTableView.setEditable(true);

        idInsumoColumn.setCellValueFactory(new PropertyValueFactory<>("idInsumo"));
        nombreInsumoColumn.setCellValueFactory(cellData -> cellData.getValue().nombreInsumoProperty());
        descripcionColumn.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
        stockMinimoColumn.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        stockActualColumn.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        estadoColumn.setCellValueFactory(cellData -> cellData.getValue().estadoProperty());

        // --- Carga de Tipo Proveedor (MANTENIDO) ---
        try {
            tiposProveedor = tipoProveedorDAO.getAllTipoProveedores();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Carga", "No se pudieron cargar los tipos de proveedor.", Alert.AlertType.ERROR);
        }

        idTipoProveedorColumn.setCellValueFactory(cellData -> {
            int id = cellData.getValue().getIdTipoProveedor();
            String nombre = getTipoProveedorNombre(id);
            return new javafx.beans.property.SimpleStringProperty(nombre);
        });

        // Cell Factory para idTipoProveedorColumn (MANTENIDO)
        idTipoProveedorColumn.setCellFactory(column -> {
            return new TableCell<Insumo, String>() {
                private final ChoiceBox<TipoProveedor> choiceBox = new ChoiceBox<>();

                {
                    if (tiposProveedor != null) {
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
                        // *** CORREGIDO: Usamos el enfoque anterior para ChoiceBox en edición ***
                        choiceBox.setOnAction(e -> {
                            TipoProveedor selectedType = choiceBox.getSelectionModel().getSelectedItem();
                            if (selectedType != null) {
                                commitEdit(selectedType.getDescripcion());
                            } else {
                                cancelEdit();
                            }
                        });
                    }
                }

                @Override
                public void startEdit() {
                    if (!isEmpty()) {
                        super.startEdit();
                        setGraphic(choiceBox);
                        setText(null);

                        Insumo insumo = getTableView().getItems().get(getIndex());
                        tiposProveedor.stream()
                                .filter(t -> t.getId() == insumo.getIdTipoProveedor())
                                .findFirst()
                                .ifPresent(choiceBox.getSelectionModel()::select);
                    }
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
            };
        });

        // OnEditCommit para idTipoProveedorColumn (MANTENIDO)
        idTipoProveedorColumn.setOnEditCommit(event -> {
            String nuevaDescripcion = event.getNewValue();
            Insumo insumo = event.getRowValue();
            try {
                TipoProveedor nuevoTipo = tipoProveedorDAO.getTipoProveedorByDescription(nuevaDescripcion);
                if (nuevoTipo != null) {
                    // Validar si realmente hay un cambio para evitar el re-guardado innecesario
                    if (insumo.getIdTipoProveedor() != nuevoTipo.getId()) {
                        insumo.setIdTipoProveedor(nuevoTipo.getId());
                        if (insumoDAO.modificarInsumo(insumo)) {
                            mostrarAlerta("Éxito", "Tipo de proveedor actualizado exitosamente.", Alert.AlertType.INFORMATION);
                        } else {
                            mostrarAlerta("Error", "No se pudo actualizar el tipo de proveedor en la base de datos.", Alert.AlertType.ERROR);
                            insumosTableView.refresh();
                        }
                    }
                } else {
                    mostrarAlerta("Error", "Tipo de proveedor no encontrado.", Alert.AlertType.ERROR);
                    insumosTableView.refresh();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error de BD", "Ocurrió un error al actualizar el tipo de proveedor.", Alert.AlertType.ERROR);
                insumosTableView.refresh();
            }
        });

        // OnEditCommit para nombreInsumoColumn y descripcionColumn (MANTENIDOS)
        nombreInsumoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nombreInsumoColumn.setOnEditCommit(event -> {
            if (event.getNewValue() == null || event.getNewValue().trim().isEmpty()) {
                mostrarAlerta("Advertencia", "El nombre del insumo no puede quedar vacío.", Alert.AlertType.WARNING);
                insumosTableView.refresh();
                return;
            }
            event.getRowValue().setNombreInsumo(event.getNewValue());
        });

        descripcionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        descripcionColumn.setOnEditCommit(event -> {
            if (event.getNewValue() == null || event.getNewValue().trim().isEmpty()) {
                mostrarAlerta("Advertencia", "La descripción no puede quedar vacía.", Alert.AlertType.WARNING);
                insumosTableView.refresh();
                return;
            }
            event.getRowValue().setDescripcion(event.getNewValue());
        });

        // OnEditCommit para stockMinimoColumn (MANTENIDO)
        stockMinimoColumn.setCellFactory(column -> new TextFieldTableCell<Insumo, Number>() {
            // ... (implementación de la celda de stockMinimo, MANTENIDA) ...
            @Override
            public void startEdit() {
                super.startEdit();
                if (this.getItem() != null) {
                    TextField textField = new TextField(String.valueOf(this.getItem()));
                    textField.setOnAction(e -> commitEdit(textField.getText()));
                    setGraphic(textField);
                    setText(null);
                    textField.requestFocus();
                }
            }
            private void commitEdit(String text) {
                if (text == null || text.trim().isEmpty()) {
                    mostrarAlerta("Advertencia", "El stock mínimo no puede estar vacío.", Alert.AlertType.WARNING);
                    cancelEdit();
                    return;
                }
                try {
                    int cantidad = Integer.parseInt(text.trim());
                    if (cantidad < 0) {
                        mostrarAlerta("Advertencia", "El stock mínimo debe ser un número positivo.", Alert.AlertType.WARNING);
                        cancelEdit();
                        return;
                    }
                    super.commitEdit(cantidad);
                } catch (NumberFormatException e) {
                    mostrarAlerta("Advertencia", "El stock mínimo debe ser un número entero válido.", Alert.AlertType.WARNING);
                    cancelEdit();
                }
            }
            @Override
            public void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        TextField textField = new TextField(String.valueOf(item));
                        textField.setOnAction(e -> commitEdit(textField.getText()));
                        setGraphic(textField);
                        setText(null);
                    } else {
                        setText(String.valueOf(item));
                        setGraphic(null);
                    }
                }
            }
        });

        // ====================================================================
        // === IMPLEMENTACIÓN DE ALERTA DE STOCK EN EL COMMIT DE STOCK ACTUAL ===
        // ====================================================================
        stockActualColumn.setCellFactory(column -> new TextFieldTableCell<Insumo, Number>() {
            @Override
            public void startEdit() {
                super.startEdit();
                if (this.getItem() != null) {
                    TextField textField = new TextField(String.valueOf(this.getItem()));
                    textField.setOnAction(e -> commitEdit(textField.getText()));
                    setGraphic(textField);
                    setText(null);
                    textField.requestFocus();
                }
            }
            private void commitEdit(String text) {
                if (text == null || text.trim().isEmpty()) {
                    mostrarAlerta("Advertencia", "El stock actual no puede estar vacío.", Alert.AlertType.WARNING);
                    cancelEdit();
                    return;
                }
                try {
                    int cantidad = Integer.parseInt(text.trim());
                    if (cantidad < 0) {
                        mostrarAlerta("Advertencia", "El stock actual debe ser un número positivo o cero.", Alert.AlertType.WARNING);
                        cancelEdit();
                        return;
                    }
                    super.commitEdit(cantidad);
                } catch (NumberFormatException e) {
                    mostrarAlerta("Advertencia", "El stock actual debe ser un número entero válido.", Alert.AlertType.WARNING);
                    cancelEdit();
                }
            }
            @Override
            public void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        TextField textField = new TextField(String.valueOf(item));
                        textField.setOnAction(e -> commitEdit(textField.getText()));
                        setGraphic(textField);
                        setText(null);
                    } else {
                        setText(String.valueOf(item));
                        setGraphic(null);
                    }
                }
            }
        });

        stockActualColumn.setOnEditCommit(event -> {
            Insumo insumo = event.getRowValue();
            Number nuevoStock = event.getNewValue();

            if (nuevoStock == null) {
                insumosTableView.refresh();
                return;
            }

            int stockActual = nuevoStock.intValue();
            insumo.setStockActual(stockActual);

            // Intentar modificar en la base de datos
            boolean exito = insumoDAO.modificarInsumo(insumo);

            if (exito) {
                // Validación y Alerta después de la modificación exitosa
                int stockMinimo = insumo.getStockMinimo();
                if (stockActual == 0) {
                    mostrarAlerta("¡Stock Agotado! 🚫", "El insumo '" + insumo.getNombreInsumo() + "' se quedó sin stock.", Alert.AlertType.ERROR);
                } else if (stockActual <= stockMinimo) {
                    mostrarAlerta("¡Stock Crítico! ⚠️", "El insumo '" + insumo.getNombreInsumo() + "' ha alcanzado o superado el stock mínimo (" + stockMinimo + ").", Alert.AlertType.WARNING);
                } else {
                    // Opcional: Alerta de éxito si el stock se repone
                    // mostrarAlerta("Éxito", "Stock de " + insumo.getNombreInsumo() + " actualizado correctamente.", Alert.AlertType.INFORMATION);
                }
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el stock en la base de datos.", Alert.AlertType.ERROR);
                // Revertir el valor si la BD falla
                insumo.setStockActual(event.getOldValue().intValue());
            }
            insumosTableView.refresh();
        });
        // ====================================================================

        // LÓGICA DE ESTILO DE ESTADO (MANTENIDA)
        estadoColumn.setCellFactory(column -> new TableCell<Insumo, String>() {
            // ... (implementación de la celda de estado, MANTENIDA) ...
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
            Insumo insumo = event.getRowValue();
            String nuevoEstado = event.getNewValue();
            insumo.setEstado(nuevoEstado);
            boolean exito = insumoDAO.modificarEstadoInsumo(insumo.getIdInsumo(), nuevoEstado);
            if (exito) {
                mostrarAlerta("Éxito", "Estado del insumo actualizado.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el estado.", Alert.AlertType.ERROR);
                insumo.setEstado(event.getOldValue());
            }
            insumosTableView.refresh();
        });


        // Restauración de la celda con el botón "Ver Proveedores" (MANTENIDO)
        accionColumn.setCellFactory(param -> new TableCell<Insumo, Void>() {
            private final Button btn = new Button("Ver Proveedores");
            {
                btn.setOnAction(event -> {
                    Insumo insumo = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/verProveedores.fxml"));
                        Parent root = loader.load();
                        VerProveedoresController proveedoresController = loader.getController();
                        proveedoresController.setTipoProveedor(insumo.getIdTipoProveedor());
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Proveedores de " + insumo.getNombreInsumo());
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.showAndWait();
                    } catch (IOException e) {
                        e.printStackTrace();
                        mostrarAlerta("Error", "No se pudo cargar la ventana de proveedores.", Alert.AlertType.ERROR);
                    }
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

        cargarInsumosYConfigurarFiltros();
    }

    // ====================================================================
    // === NUEVO MÉTODO: MANEJO DEL BOTÓN DE ALERTAS GLOBALES DE STOCK ===
    // ====================================================================
    @FXML
    public void handleAlertasStockButton(ActionEvent event) {
        // Recorrer todos los insumos (masterData para asegurar que se revisan todos)
        List<Insumo> insumosFaltantes = masterData.stream()
                .filter(i -> i.getStockActual() <= i.getStockMinimo())
                .collect(Collectors.toList());

        if (insumosFaltantes.isEmpty()) {
            mostrarAlerta("Inventario OK ✅", "Todos los insumos tienen stock por encima del mínimo.", Alert.AlertType.INFORMATION);
            return;
        }

        StringBuilder sb = new StringBuilder("Los siguientes insumos requieren atención:\n\n");
        int count = 0;

        for (Insumo i : insumosFaltantes) {
            String estado = (i.getStockActual() == 0) ? "AGOTADO (0)" : "CRÍTICO (" + i.getStockActual() + ")";
            sb.append("ID ").append(i.getIdInsumo())
                    .append(": ").append(i.getNombreInsumo())
                    .append(" | Estado: ").append(estado)
                    .append(" | Mínimo: ").append(i.getStockMinimo()).append("\n");
            count++;
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("🚨 Alerta Global de Stock 🚨");
        alert.setHeaderText("¡Atención! " + count + " insumo(s) están en nivel crítico o agotados.");

        TextArea textArea = new TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(true); // Mostrar el contenido expandido por defecto

        alert.showAndWait();
    }
    // ====================================================================


    private void cargarInsumosYConfigurarFiltros() {
        masterData = insumoDAO.getAllInsumos();
        filteredData = new FilteredList<>(masterData, p -> true);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> updateFilteredList());
        estadoChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateFilteredList());
        SortedList<Insumo> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(insumosTableView.comparatorProperty());
        insumosTableView.setItems(sortedData);
    }

    private void updateFilteredList() {
        filteredData.setPredicate(insumo -> {
            String searchText = filterField.getText() == null ? "" : filterField.getText().toLowerCase();
            String selectedStatus = estadoChoiceBox.getSelectionModel().getSelectedItem();
            boolean matchesSearchText = searchText.isEmpty() || (insumo.getNombreInsumo() != null && insumo.getNombreInsumo().toLowerCase().contains(searchText));
            boolean matchesStatus = selectedStatus.equals("Todos") || selectedStatus.equalsIgnoreCase(insumo.getEstado());
            return matchesSearchText && matchesStatus;
        });
    }

    @FXML
    public void handleRegistrarInsumoButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registroInsumo.fxml"));
            Parent root = loader.load();
            RegistroInsumoController registroController = loader.getController();
            registroController.setStockController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Registrar Nuevo Insumo");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            refreshInsumosTable();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el formulario de registro de insumo.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleModificarInsumoButton(ActionEvent event) {
        Insumo selectedInsumo = insumosTableView.getSelectionModel().getSelectedItem();
        if (selectedInsumo != null) {
            boolean exito = insumoDAO.modificarInsumo(selectedInsumo);
            if (exito) {
                mostrarAlerta("Éxito", "Insumo modificado exitosamente.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo modificar el insumo en la base de datos.", Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Advertencia", "Por favor, seleccione una fila y modifique los datos antes de guardar.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void handleRefreshButton(ActionEvent event) {
        refreshInsumosTable();
    }

    public void refreshInsumosTable() {
        String filtroTexto = filterField.getText();
        String filtroEstado = estadoChoiceBox.getValue();
        masterData.clear();
        masterData.addAll(insumoDAO.getAllInsumos());
        filterField.setText(filtroTexto);
        estadoChoiceBox.setValue(filtroEstado);
        updateFilteredList();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private String getTipoProveedorNombre(int id) {
        if (tiposProveedor == null) {
            return "Cargando...";
        }
        return tiposProveedor.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .map(TipoProveedor::getDescripcion)
                .orElse("Desconocido");
    }

    @FXML
    private void handleVolverButton(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/menuAbmStock.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menú Principal");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}