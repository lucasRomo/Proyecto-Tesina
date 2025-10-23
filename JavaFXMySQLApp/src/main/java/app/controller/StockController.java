package app.controller;

import app.dao.InsumoDAO;
import app.dao.TipoProveedorDAO;
import app.dao.HistorialActividadDAO;
import java.sql.Connection;
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
import javafx.scene.control.cell.ComboBoxTableCell;
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
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

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
    private HistorialActividadDAO historialDAO;

    public StockController() {
        this.insumoDAO = new InsumoDAO();
        this.tipoProveedorDAO = new TipoProveedorDAO();
        this.historialDAO = new HistorialActividadDAO();
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
        List<String> descripcionesTipos = tiposProveedor.stream()
                .map(TipoProveedor::getDescripcion)
                .collect(Collectors.toList());

        idTipoProveedorColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(descripcionesTipos)));

        idTipoProveedorColumn.setOnEditCommit(event -> {
            String nuevaDescripcion = event.getNewValue();
            Insumo insumoModificado = event.getRowValue();
            int valorOriginalId = insumoModificado.getIdTipoProveedor();

            try (Connection conn = insumoDAO.getConnection()) {
                // CORREGIDO: Buscar el objeto TipoProveedor por su descripción
                TipoProveedor nuevoTipo = tiposProveedor.stream()
                        .filter(t -> t.getDescripcion().equals(nuevaDescripcion))
                        .findFirst()
                        .orElse(null);

                if (nuevoTipo == null) {
                    mostrarAlerta("Error", "Tipo de proveedor no encontrado.", Alert.AlertType.ERROR);
                    insumosTableView.refresh();
                    return;
                }

                if (valorOriginalId == nuevoTipo.getId()) {
                    return; // No hubo cambio
                }

                Insumo insumoOriginalDB = insumoDAO.getInsumoById(insumoModificado.getIdInsumo(), conn);

                if (insumoOriginalDB == null) throw new SQLException("Datos originales del insumo no encontrados.");

                String descripcionPrevia = insumoOriginalDB.getDescripcion(); // Obtener la descripción previa de la BD

                // Aplicar el cambio de ID al modelo (para el guardado)
                insumoModificado.setIdTipoProveedor(nuevoTipo.getId());

                boolean exito = insumoDAO.modificarInsumo(insumoModificado);

                if (exito) {
                    historialDAO.insertarRegistro(
                            SessionManager.getInstance().getLoggedInUserId(),
                            "Insumo",
                            "id_tipo_proveedor",
                            insumoModificado.getIdInsumo(),
                            descripcionPrevia,
                            nuevaDescripcion
                    );
                    mostrarAlerta("Éxito", "Tipo de proveedor actualizado y logueado.", Alert.AlertType.INFORMATION);
                } else {
                    // Revertir el modelo si falla la DB
                    insumoModificado.setIdTipoProveedor(valorOriginalId);
                    mostrarAlerta("Error", "No se pudo actualizar el tipo de proveedor en la base de datos.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                insumoModificado.setIdTipoProveedor(valorOriginalId);
                e.printStackTrace();
                mostrarAlerta("Error de BD", "Ocurrió un error al actualizar el tipo de proveedor: " + e.getMessage(), Alert.AlertType.ERROR);
            } finally {
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

            Insumo insumoModificado = event.getRowValue();
            String valorOriginalVista = event.getOldValue();

            try (Connection conn = insumoDAO.getConnection()) { // <-- USO DE CONEXIÓN
                Insumo insumoOriginalDB = insumoDAO.getInsumoById(insumoModificado.getIdInsumo(), conn); // <-- OBTENER ORIGINAL

                if (insumoOriginalDB == null) throw new SQLException("Datos originales del insumo no encontrados.");

                insumoModificado.setNombreInsumo(event.getNewValue()); // Aplicar el cambio al modelo

                boolean exito = insumoDAO.modificarInsumo(insumoModificado);

                if (exito) {
                    if (!insumoOriginalDB.getNombreInsumo().equals(insumoModificado.getNombreInsumo())) {
                        historialDAO.insertarRegistro(
                                SessionManager.getInstance().getLoggedInUserId(),
                                "Insumo",
                                "nombre",
                                insumoModificado.getIdInsumo(),
                                insumoOriginalDB.getNombreInsumo(),
                                insumoModificado.getNombreInsumo()
                        );
                    }
                    mostrarAlerta("Éxito", "Nombre del insumo modificado y guardado.", Alert.AlertType.INFORMATION);
                } else {
                    insumoModificado.setNombreInsumo(valorOriginalVista);
                    mostrarAlerta("Error", "Fallo al guardar la modificación del nombre en la base de datos.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                insumoModificado.setNombreInsumo(valorOriginalVista);
                e.printStackTrace();
                mostrarAlerta("Error de BD", "Ocurrió un error al intentar modificar el nombre: " + e.getMessage(), Alert.AlertType.ERROR);
            } finally {
                insumosTableView.refresh();
            }
        });

        descripcionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        descripcionColumn.setOnEditCommit(event -> {
            if (event.getNewValue() == null || event.getNewValue().trim().isEmpty()) {
                mostrarAlerta("Advertencia", "La descripción no puede quedar vacía.", Alert.AlertType.WARNING);
                insumosTableView.refresh();
                return;
            }

            Insumo insumoModificado = event.getRowValue();
            String valorOriginalVista = event.getOldValue();

            try (Connection conn = insumoDAO.getConnection()) { // <-- USO DE CONEXIÓN
                Insumo insumoOriginalDB = insumoDAO.getInsumoById(insumoModificado.getIdInsumo(), conn); // <-- OBTENER ORIGINAL

                if (insumoOriginalDB == null) throw new SQLException("Datos originales del insumo no encontrados.");

                insumoModificado.setDescripcion(event.getNewValue());

                boolean exito = insumoDAO.modificarInsumo(insumoModificado);

                if (exito) {
                    if (!insumoOriginalDB.getDescripcion().equals(insumoModificado.getDescripcion())) {
                        historialDAO.insertarRegistro(
                                SessionManager.getInstance().getLoggedInUserId(),
                                "Insumo",
                                "descripcion",
                                insumoModificado.getIdInsumo(),
                                insumoOriginalDB.getDescripcion(),
                                insumoModificado.getDescripcion()
                        );
                    }
                    mostrarAlerta("Éxito", "Descripción del insumo modificada y guardada.", Alert.AlertType.INFORMATION);
                } else {
                    insumoModificado.setDescripcion(valorOriginalVista);
                    mostrarAlerta("Error", "Fallo al guardar la modificación de la descripción en la base de datos.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                insumoModificado.setDescripcion(valorOriginalVista);
                e.printStackTrace();
                mostrarAlerta("Error de BD", "Ocurrió un error al intentar modificar la descripción: " + e.getMessage(), Alert.AlertType.ERROR);
            } finally {
                insumosTableView.refresh();
            }
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

        stockMinimoColumn.setOnEditCommit(event -> {
            Number nuevoValor = event.getNewValue();

            if (nuevoValor == null || nuevoValor.intValue() < 0) {
                // El conversor ya muestra una alerta, pero se asegura la cancelación aquí
                insumosTableView.refresh();
                return;
            }

            Insumo insumoModificado = event.getRowValue();
            Number stockMinimoOriginal = event.getOldValue();

            try (Connection conn = insumoDAO.getConnection()) {
                Insumo insumoOriginalDB = insumoDAO.getInsumoById(insumoModificado.getIdInsumo(), conn);

                if (insumoOriginalDB == null) throw new SQLException("Datos originales del insumo no encontrados.");

                // Actualizar el modelo antes de guardar (ya lo hizo el conversor, pero es buena práctica)
                insumoModificado.setStockMinimo(nuevoValor.intValue());

                boolean exito = insumoDAO.modificarInsumo(insumoModificado);

                if (exito) {
                    if (insumoOriginalDB.getStockMinimo() != nuevoValor.intValue()) {
                        historialDAO.insertarRegistro(
                                SessionManager.getInstance().getLoggedInUserId(),
                                "Insumo",
                                "stock_minimo",
                                insumoModificado.getIdInsumo(),
                                String.valueOf(insumoOriginalDB.getStockMinimo()),
                                String.valueOf(nuevoValor.intValue())
                        );
                    }
                    mostrarAlerta("Éxito", "Stock mínimo modificado y guardado.", Alert.AlertType.INFORMATION);
                } else {
                    insumoModificado.setStockMinimo(stockMinimoOriginal.intValue());
                    mostrarAlerta("Error", "Fallo al guardar la modificación del stock mínimo.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                insumoModificado.setStockMinimo(stockMinimoOriginal.intValue());
                e.printStackTrace();
                mostrarAlerta("Error de BD", "Ocurrió un error al intentar modificar el stock mínimo: " + e.getMessage(), Alert.AlertType.ERROR);
            } finally {
                insumosTableView.refresh();
            }
        });

        // ====================================================================
        // === IMPLEMENTACIÓN DE ALERTA DE STOCK EN EL COMMIT DE STOCK ACTUAL (MANTENIDO) ===
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
            Number nuevoValor = event.getNewValue();

            if (nuevoValor == null || nuevoValor.intValue() < 0) {
                // El conversor ya muestra una alerta, pero se asegura la cancelación aquí
                insumosTableView.refresh();
                return;
            }

            Insumo insumoModificado = event.getRowValue();
            Number stockActualOriginal = event.getOldValue();
            int stockActual = nuevoValor.intValue();

            try (Connection conn = insumoDAO.getConnection()) {
                Insumo insumoOriginalDB = insumoDAO.getInsumoById(insumoModificado.getIdInsumo(), conn);

                if (insumoOriginalDB == null) throw new SQLException("Datos originales del insumo no encontrados.");

                // Actualizar el modelo antes de guardar
                insumoModificado.setStockActual(stockActual);

                boolean exito = insumoDAO.modificarInsumo(insumoModificado);

                if (exito) {
                    if (insumoOriginalDB.getStockActual() != stockActual) {
                        historialDAO.insertarRegistro(
                                SessionManager.getInstance().getLoggedInUserId(),
                                "Insumo",
                                "stock_actual",
                                insumoModificado.getIdInsumo(),
                                String.valueOf(insumoOriginalDB.getStockActual()),
                                String.valueOf(stockActual)
                        );
                    }
                    // Validación y Alerta
                    int stockMinimo = insumoModificado.getStockMinimo();
                    if (stockActual == 0) {
                        mostrarAlerta("¡Stock Agotado! 🚫", "El insumo '" + insumoModificado.getNombreInsumo() + "' se quedó sin stock.", Alert.AlertType.ERROR);
                    } else if (stockActual <= stockMinimo) {
                        mostrarAlerta("¡Stock Crítico! ⚠️", "El insumo '" + insumoModificado.getNombreInsumo() + "' ha alcanzado o superado el stock mínimo (" + stockMinimo + ").", Alert.AlertType.WARNING);
                    }
                } else {
                    insumoModificado.setStockActual(stockActualOriginal.intValue());
                    mostrarAlerta("Error", "No se pudo actualizar el stock en la base de datos.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                insumoModificado.setStockActual(stockActualOriginal.intValue());
                e.printStackTrace();
                mostrarAlerta("Error de BD", "Ocurrió un error al intentar modificar el stock: " + e.getMessage(), Alert.AlertType.ERROR);
            } finally {
                insumosTableView.refresh();
            }
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
            Insumo insumoModificado = event.getRowValue();
            String nuevoEstado = event.getNewValue();
            String estadoOriginal = event.getOldValue(); // Valor de la vista

            try (Connection conn = insumoDAO.getConnection()) { // <-- USO DE CONEXIÓN
                Insumo insumoOriginalDB = insumoDAO.getInsumoById(insumoModificado.getIdInsumo(), conn); // <-- OBTENER ORIGINAL

                if (insumoOriginalDB == null) throw new SQLException("Datos originales del insumo no encontrados.");

                insumoModificado.setEstado(nuevoEstado); // Aplicar el cambio al modelo

                // Usamos modificarEstadoInsumo ya que es un método más directo
                boolean exito = insumoDAO.modificarEstadoInsumo(insumoModificado.getIdInsumo(), nuevoEstado);

                if (exito) {
                    // REGISTRAR ACTIVIDAD
                    if (!insumoOriginalDB.getEstado().equals(insumoModificado.getEstado())) {
                        historialDAO.insertarRegistro(
                                SessionManager.getInstance().getLoggedInUserId(),
                                "Insumo",
                                "estado",
                                insumoModificado.getIdInsumo(),
                                insumoOriginalDB.getEstado(),
                                insumoModificado.getEstado()
                        );
                    }
                    mostrarAlerta("Éxito", "Estado del insumo actualizado y logueado.", Alert.AlertType.INFORMATION);
                } else {
                    insumoModificado.setEstado(estadoOriginal);
                    mostrarAlerta("Error", "No se pudo actualizar el estado.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                insumoModificado.setEstado(estadoOriginal);
                e.printStackTrace();
                mostrarAlerta("Error de BD", "Ocurrió un error al intentar modificar el estado: " + e.getMessage(), Alert.AlertType.ERROR);
            } finally {
                insumosTableView.refresh();
            }
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
                        stage.setResizable(false);
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
    // === NUEVO MÉTODO: MANEJO DEL BOTÓN DE ALERTAS GLOBALES DE STOCK (MANTENIDO) ===
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
        alert.setResizable(false);
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
            // 1. Cargar el FXML de registro
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registroInsumo.fxml"));
            Parent root = loader.load();

            // 2. Configurar el controlador y el callback
            RegistroInsumoController registroController = loader.getController();
            registroController.setStockController(this);

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
            newStage.setTitle("Registrar Nuevo Insumo");
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.centerOnScreen();

            newStage.setResizable(false);
            // Mostrar la nueva ventana y esperar a que se cierre (modal)
            newStage.showAndWait();

            // 7. Refrescar la tabla al volver
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
            // Se usa el método estático unificado para asegurar la navegación
            // y que la nueva vista ocupe toda la ventana maximizada.
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