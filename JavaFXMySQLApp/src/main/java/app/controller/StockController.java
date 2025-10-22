package app.controller;

import app.dao.InsumoDAO;
import app.dao.TipoProveedorDAO;
// AGREGADO: Importaciones requeridas para Auditoría y Transacción
import app.dao.HistorialActividadDAO;
import app.controller.SessionManager; // Necesario para la auditoría
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
// FIN AGREGADO
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
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.beans.property.SimpleStringProperty;


public class StockController {

    // CONFIGURACIÓN DE CONEXIÓN
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

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
    @FXML private Button alertasStockButton;

    private final InsumoDAO insumoDAO;
    private final TipoProveedorDAO tipoProveedorDAO;
    private final HistorialActividadDAO historialDAO;
    private ObservableList<Insumo> masterData = FXCollections.observableArrayList();
    private FilteredList<Insumo> filteredData;
    private List<TipoProveedor> tiposProveedor;

    // VARIABLE CLAVE PARA AUDITORÍA Y CONTROL DE EDICIÓN
    private Insumo insumoOriginal;

    public StockController() {
        this.insumoDAO = new InsumoDAO();
        this.tipoProveedorDAO = new TipoProveedorDAO();
        this.historialDAO = new HistorialActividadDAO();
    }

    // =========================================================================
    // UTILIDADES DE AUDITORÍA Y CONTROL
    // =========================================================================

    private Insumo crearCopiaInsumo(Insumo original) {
        if (original == null) return null;
        // Se usa el constructor de 7 argumentos para crear la copia
        Insumo copia = new Insumo(
                original.getIdInsumo(),
                original.getNombreInsumo(),
                original.getDescripcion(),
                original.getStockMinimo(),
                original.getStockActual(),
                original.getEstado(),
                original.getIdTipoProveedor()
        );
        return copia;
    }

    private void revertirInsumoAlOriginal(Insumo actual, Insumo original) {
        if (actual == null || original == null) return;
        // Revertir solo si el ID coincide
        if (actual.getIdInsumo() != original.getIdInsumo()) return;

        actual.setNombreInsumo(original.getNombreInsumo());
        actual.setDescripcion(original.getDescripcion());
        actual.setStockMinimo(original.getStockMinimo());
        actual.setStockActual(original.getStockActual());
        actual.setEstado(original.getEstado());
        actual.setIdTipoProveedor(original.getIdTipoProveedor());
        insumosTableView.refresh();
    }

    private void auditarYGuardarCambio(Connection conn, Insumo insumoActual, String columna, Object valorOriginal, Object valorNuevo) throws SQLException {
        String originalStr = (valorOriginal != null) ? valorOriginal.toString() : "";
        String nuevoStr = (valorNuevo != null) ? valorNuevo.toString() : "";

        boolean exitoRegistro = historialDAO.insertarRegistro(
                SessionManager.getInstance().getLoggedInUserId(),
                "Insumo",
                columna,
                insumoActual.getIdInsumo(),
                originalStr,
                nuevoStr,
                conn
        );

        if (!exitoRegistro) {
            // Si falla el registro, lanzamos una excepción para provocar el ROLLBACK
            throw new SQLException("Fallo al registrar la actividad para la columna: " + columna);
        }
    }

    // =========================================================================
    // INICIALIZACIÓN Y CONFIGURACIÓN DE CELDAS
    // =========================================================================

    @FXML
    private void initialize() {
        insumosTableView.setEditable(true);

        // ==========================================================
        // === VINCULACIÓN DEL ANCHO DE COLUMNAS PORCENTUAL ========
        // ==========================================================
        idInsumoColumn.prefWidthProperty().bind(insumosTableView.widthProperty().multiply(0.05));
        nombreInsumoColumn.prefWidthProperty().bind(insumosTableView.widthProperty().multiply(0.20));
        descripcionColumn.prefWidthProperty().bind(insumosTableView.widthProperty().multiply(0.25));
        stockMinimoColumn.prefWidthProperty().bind(insumosTableView.widthProperty().multiply(0.10));
        stockActualColumn.prefWidthProperty().bind(insumosTableView.widthProperty().multiply(0.10));
        estadoColumn.prefWidthProperty().bind(insumosTableView.widthProperty().multiply(0.08));
        idTipoProveedorColumn.prefWidthProperty().bind(insumosTableView.widthProperty().multiply(0.12));
        accionColumn.prefWidthProperty().bind(insumosTableView.widthProperty().multiply(0.10));

        // ==========================================================
        // === FIN DE VINCULACIÓN PORCENTUAL ========================
        // ==========================================================


        idInsumoColumn.setCellValueFactory(new PropertyValueFactory<>("idInsumo"));
        nombreInsumoColumn.setCellValueFactory(cellData -> cellData.getValue().nombreInsumoProperty());
        descripcionColumn.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
        stockMinimoColumn.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        stockActualColumn.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        estadoColumn.setCellValueFactory(cellData -> cellData.getValue().estadoProperty());

        // Listener para capturar la copia original antes de cualquier edición de fila
        insumosTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                this.insumoOriginal = crearCopiaInsumo(newVal);
            }
        });

        // --- Carga de Tipo Proveedor ---
        try {
            tiposProveedor = tipoProveedorDAO.getAllTipoProveedores();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Carga", "No se pudieron cargar los tipos de proveedor.", Alert.AlertType.ERROR);
        }

        idTipoProveedorColumn.setCellValueFactory(cellData -> {
            int id = cellData.getValue().getIdTipoProveedor();
            String nombre = getTipoProveedorNombre(id);
            return new SimpleStringProperty(nombre);
        });

        // Cell Factory para idTipoProveedorColumn (Mantenido)
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

        // OnEditCommit para idTipoProveedorColumn (SOLO ACTUALIZA MODELO EN MEMORIA)
        idTipoProveedorColumn.setOnEditCommit(event -> {
            String nuevaDescripcion = event.getNewValue();
            Insumo insumoActual = event.getRowValue();

            if (event.getOldValue() != null && event.getOldValue().equals(nuevaDescripcion)) {
                return;
            }

            TipoProveedor nuevoTipo = null;
            try {
                nuevoTipo = tipoProveedorDAO.getTipoProveedorByDescription(nuevaDescripcion);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (nuevoTipo == null) {
                mostrarAlerta("Error", "Tipo de proveedor no encontrado. Se cancela la edición.", Alert.AlertType.ERROR);
                insumosTableView.refresh();
                return;
            }

            // APLICAR CAMBIO AL MODELO EN MEMORIA
            insumoActual.setIdTipoProveedor(nuevoTipo.getId());
            insumosTableView.refresh();
        });

        // OnEditCommit para nombreInsumoColumn (SOLO ACTUALIZA MODELO EN MEMORIA)
        nombreInsumoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nombreInsumoColumn.setOnEditCommit(event -> {
            String nuevoValor = event.getNewValue();
            Insumo insumoActual = event.getRowValue();

            if (nuevoValor == null || nuevoValor.trim().isEmpty()) {
                mostrarAlerta("Advertencia", "El nombre del insumo no puede quedar vacío.", Alert.AlertType.WARNING);
                insumosTableView.refresh();
                return;
            }
            if (event.getOldValue().equals(nuevoValor.trim())) {
                return;
            }

            // APLICAR CAMBIO AL MODELO EN MEMORIA
            insumoActual.setNombreInsumo(nuevoValor.trim());
            insumosTableView.refresh();
        });

        // OnEditCommit para descripcionColumn (SOLO ACTUALIZA MODELO EN MEMORIA)
        descripcionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        descripcionColumn.setOnEditCommit(event -> {
            String nuevoValor = event.getNewValue();
            Insumo insumoActual = event.getRowValue();

            String valorAUsar = (nuevoValor == null || nuevoValor.trim().isEmpty()) ? "" : nuevoValor.trim();

            if (event.getOldValue() != null && event.getOldValue().equals(valorAUsar)) {
                return;
            }

            // APLICAR CAMBIO AL MODELO EN MEMORIA
            insumoActual.setDescripcion(valorAUsar);
            insumosTableView.refresh();
        });

        // OnEditCommit para stockMinimoColumn (SOLO ACTUALIZA MODELO EN MEMORIA)
        stockMinimoColumn.setCellFactory(column -> new TextFieldTableCell<Insumo, Number>() {
            // ... (implementación de la celda de stockMinimo, MANTENIDA) ...
            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    if (this.getItem() != null) {
                        TextField textField = new TextField(String.valueOf(this.getItem()));
                        textField.setOnAction(e -> commitEdit(textField.getText()));
                        setGraphic(textField);
                        setText(null);
                        textField.requestFocus();
                    }
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
            Insumo insumoActual = event.getRowValue();
            Number nuevoStockNum = event.getNewValue();
            int nuevoValor = nuevoStockNum.intValue();

            if (event.getOldValue().intValue() == nuevoValor) {
                return;
            }

            // APLICAR CAMBIO AL MODELO EN MEMORIA
            insumoActual.setStockMinimo(nuevoValor);
            insumosTableView.refresh();
        });

        // OnEditCommit para stockActualColumn (SOLO ACTUALIZA MODELO EN MEMORIA)
        stockActualColumn.setCellFactory(column -> new TextFieldTableCell<Insumo, Number>() {
            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    if (this.getItem() != null) {
                        TextField textField = new TextField(String.valueOf(this.getItem()));
                        textField.setOnAction(e -> commitEdit(textField.getText()));
                        setGraphic(textField);
                        setText(null);
                        textField.requestFocus();
                    }
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
            Insumo insumoActual = event.getRowValue();
            Number nuevoStockNum = event.getNewValue();
            int nuevoValor = nuevoStockNum.intValue();

            if (event.getOldValue().intValue() == nuevoValor) {
                return;
            }

            if (nuevoStockNum == null) {
                insumosTableView.refresh();
                return;
            }

            // APLICAR CAMBIO AL MODELO EN MEMORIA
            insumoActual.setStockActual(nuevoValor);
            insumosTableView.refresh();
        });

        // OnEditCommit para estadoColumn (SOLO ACTUALIZA MODELO EN MEMORIA)
        estadoColumn.setCellFactory(column -> new TableCell<Insumo, String>() {
            // Lógica de ChoiceBox y estilos mantenida.
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

        // Código corregido para estadoColumn.setOnEditCommit
        estadoColumn.setOnEditCommit(event -> {
            Insumo insumoActual = event.getRowValue(); // Esta es la variable correcta
            String nuevoEstado = event.getNewValue();

            // 1. Aplicamos el cambio al modelo en memoria (antes de la persistencia)
            insumoActual.setEstado(nuevoEstado);

            // 2. Intentamos persistir el cambio en la base de datos
            // Se usa insumoActual.getIdInsumo() en lugar de insumo.getIdInsumo()
            boolean exito = insumoDAO.modificarEstadoInsumo(insumoActual.getIdInsumo(), nuevoEstado);

            if (exito) {
                mostrarAlerta("Éxito", "Estado del insumo actualizado.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el estado.", Alert.AlertType.ERROR);
                // Si falla, revertimos el estado en memoria al valor anterior
                // Se usa insumoActual.setEstado() en lugar de insumo.setEstado()
                insumoActual.setEstado(event.getOldValue());
            }

            // [LÍNEA ELIMINADA] La línea 'insumoActual.setEstado(nuevoEstado);' que estaba aquí se elimina
            // para evitar que anule la reversión si la actualización falló.

            insumosTableView.refresh();
        });

        // Restauración de la celda con el botón "Ver Proveedores" (Mantenido)
        accionColumn.setCellFactory(param -> new TableCell<Insumo, Void>() {
            private final Button btn = new Button("Ver Proveedores");
            {
                btn.prefWidthProperty().bind(accionColumn.widthProperty());
                btn.setOnAction(event -> {
                    Insumo insumo = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/verProveedores.fxml"));
                        Parent root = loader.load();
                        // El controlador 'VerProveedoresController' debe existir
                        // El snippet no lo muestra, pero se asume que funciona.
                        // VerProveedoresController proveedoresController = loader.getController();
                        // proveedoresController.setTipoProveedor(insumo.getIdTipoProveedor());
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

    // =========================================================================
    // LÓGICA DE PERSISTENCIA Y AUDITORÍA (SOLO EN EL BOTÓN)
    // =========================================================================

    @FXML
    public void handleModificarInsumoButton(ActionEvent event) {
        Insumo insumoActual = insumosTableView.getSelectionModel().getSelectedItem();

        if (insumoActual == null) {
            mostrarAlerta("Advertencia", "Por favor, seleccione una fila antes de guardar.", Alert.AlertType.WARNING);
            return;
        }

        // Validar que tenemos una copia original válida para comparar
        if (this.insumoOriginal == null || insumoActual.getIdInsumo() != this.insumoOriginal.getIdInsumo()) {
            mostrarAlerta("Advertencia", "El insumo no está listo para guardar. Vuelva a seleccionar la fila.", Alert.AlertType.WARNING);
            return;
        }

        Connection conn = null;
        boolean huboCambios = false;

        try {
            // 1. Iniciar Transacción
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false);

            // --- 2. AUDITAR Y REGISTRAR CADA POSIBLE CAMBIO ---

            // Cambio en Nombre
            if (!insumoActual.getNombreInsumo().equals(this.insumoOriginal.getNombreInsumo())) {
                auditarYGuardarCambio(conn, insumoActual, "nombreInsumo", this.insumoOriginal.getNombreInsumo(), insumoActual.getNombreInsumo());
                huboCambios = true;
            }

            // Cambio en Descripción
            if (!insumoActual.getDescripcion().equals(this.insumoOriginal.getDescripcion())) {
                auditarYGuardarCambio(conn, insumoActual, "descripcion", this.insumoOriginal.getDescripcion(), insumoActual.getDescripcion());
                huboCambios = true;
            }

            // Cambio en Stock Mínimo
            if (insumoActual.getStockMinimo() != this.insumoOriginal.getStockMinimo()) {
                auditarYGuardarCambio(conn, insumoActual, "stockMinimo", this.insumoOriginal.getStockMinimo(), insumoActual.getStockMinimo());
                huboCambios = true;
            }

            // Cambio en Stock Actual
            if (insumoActual.getStockActual() != this.insumoOriginal.getStockActual()) {
                auditarYGuardarCambio(conn, insumoActual, "stockActual", this.insumoOriginal.getStockActual(), insumoActual.getStockActual());
                huboCambios = true;
            }

            // Cambio en Estado
            if (!insumoActual.getEstado().equals(this.insumoOriginal.getEstado())) {
                auditarYGuardarCambio(conn, insumoActual, "estado", this.insumoOriginal.getEstado(), insumoActual.getEstado());
                huboCambios = true;
            }

            // Cambio en Tipo Proveedor (Registrando el NOMBRE en el historial)
            if (insumoActual.getIdTipoProveedor() != this.insumoOriginal.getIdTipoProveedor()) {
                String nombreOriginal = getTipoProveedorNombre(this.insumoOriginal.getIdTipoProveedor());
                String nombreNuevo = getTipoProveedorNombre(insumoActual.getIdTipoProveedor());

                auditarYGuardarCambio(conn, insumoActual, "tipoProveedor", nombreOriginal, nombreNuevo);
                huboCambios = true;
            }

            // --- 3. PERSISTIR LOS CAMBIOS SI LOS HUBO ---
            if (huboCambios) {
                // Guarda todos los campos del insumo actualizado en la BD (modificarInsumo)
                boolean exitoActualizacion = insumoDAO.modificarInsumo(insumoActual);

                if (exitoActualizacion) {
                    conn.commit();

                    // Lógica de Alerta de Stock (Mantenida de tu código)
                    int nuevoStock = insumoActual.getStockActual();
                    int stockMinimo = insumoActual.getStockMinimo();

                    if (nuevoStock == 0) {
                        mostrarAlerta("¡Stock Agotado! 🚫", "El insumo '" + insumoActual.getNombreInsumo() + "' se quedó sin stock.", Alert.AlertType.ERROR);
                    } else if (nuevoStock <= stockMinimo) {
                        mostrarAlerta("¡Stock Crítico! ⚠️", "El insumo '" + insumoActual.getNombreInsumo() + "' ha alcanzado o superado el stock mínimo (" + stockMinimo + ").", Alert.AlertType.WARNING);
                    } else {
                        mostrarAlerta("Éxito", "Insumo y registro de actividad actualizados.", Alert.AlertType.INFORMATION);
                    }

                    // Actualiza la copia original después de un guardado exitoso
                    this.insumoOriginal = crearCopiaInsumo(insumoActual);
                } else {
                    conn.rollback();
                    mostrarAlerta("Error", "No se pudo actualizar el insumo en la BD. ROLLBACK realizado.", Alert.AlertType.ERROR);
                    // Revertir el modelo al estado original en memoria
                    revertirInsumoAlOriginal(insumoActual, this.insumoOriginal);
                }
            } else {
                conn.rollback();
                mostrarAlerta("Advertencia", "No se detectaron cambios para guardar.", Alert.AlertType.WARNING);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { /* Ignorar */ }
            mostrarAlerta("Error de BD", "Ocurrió un error en la base de datos: " + e.getMessage(), Alert.AlertType.ERROR);
            revertirInsumoAlOriginal(insumoActual, this.insumoOriginal);
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException closeEx) { /* Ignorar */ }
            insumosTableView.refresh(); // Asegurar que la vista refleja el estado actual
        }
    }

    // =========================================================================
    // RESTO DE MÉTODOS (SIN MODIFICACIÓN CRÍTICA)
    // =========================================================================

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
            String estado = (i.getStockActual() == 0) ?
                    "AGOTADO (0)" : "CRÍTICO (" + i.getStockActual() + ")";
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
}