package app.controller;

import app.dao.InsumoDAO;
import app.dao.TipoProveedorDAO;
// AGREGADO: Importaciones requeridas para Auditoría y Transacción
import app.dao.HistorialActividadDAO;
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
import java.util.List;
import java.util.stream.Collectors;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.beans.property.SimpleStringProperty;


public class StockController {

    // AGREGADO: Configuración de la conexión (DEBE COINCIDIR)
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    // FIN AGREGADO

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

    private InsumoDAO insumoDAO;
    private TipoProveedorDAO tipoProveedorDAO;
    // AGREGADO: DAO para el historial
    private HistorialActividadDAO historialDAO;
    // FIN AGREGADO
    private ObservableList<Insumo> masterData = FXCollections.observableArrayList();
    private FilteredList<Insumo> filteredData;
    private List<TipoProveedor> tiposProveedor;

    // AGREGADO: Mapa para almacenar copias originales antes de la edición
    private Insumo insumoOriginal;
    // FIN AGREGADO

    public StockController() {
        this.insumoDAO = new InsumoDAO();
        this.tipoProveedorDAO = new TipoProveedorDAO();
        // AGREGADO: Inicialización del DAO
        this.historialDAO = new HistorialActividadDAO();
        // FIN AGREGADO
    }

    // AGREGADO: Función auxiliar para crear una copia del insumo (Modelo de trabajo para la auditoría)
    private Insumo crearCopiaInsumo(Insumo original) {
        if (original == null) return null;

        // Se usa el constructor de 7 argumentos para crear la copia, evitando el error de constructor.
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
    // FIN AGREGADO

    @FXML
    private void initialize() {
        insumosTableView.setEditable(true);

        idInsumoColumn.setCellValueFactory(new PropertyValueFactory<>("idInsumo"));
        nombreInsumoColumn.setCellValueFactory(cellData -> cellData.getValue().nombreInsumoProperty());
        descripcionColumn.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
        stockMinimoColumn.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        stockActualColumn.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        estadoColumn.setCellValueFactory(cellData -> cellData.getValue().estadoProperty());

        // AGREGADO: Listener para capturar el insumo antes de editar
        insumosTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Guarda una copia del objeto ANTES de cualquier edición
                this.insumoOriginal = crearCopiaInsumo(newVal);
            }
        });
        // FIN AGREGADO

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
            return new SimpleStringProperty(nombre);
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
                        // AGREGADO: Crea la copia antes de la edición
                        insumoOriginal = crearCopiaInsumo(getTableView().getItems().get(getIndex()));
                        // FIN AGREGADO
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

        // OnEditCommit para idTipoProveedorColumn (MODIFICADO para TRASACCIÓN y AUDITORÍA)
        idTipoProveedorColumn.setOnEditCommit(event -> {
            String nuevaDescripcion = event.getNewValue();
            Insumo insumoActual = event.getRowValue();

            // Si la descripción es la misma, no hace nada (no hay cambio)
            if (event.getOldValue() != null && event.getOldValue().equals(nuevaDescripcion)) {
                return;
            }

            Connection conn = null;
            try {
                TipoProveedor nuevoTipo = tipoProveedorDAO.getTipoProveedorByDescription(nuevaDescripcion);
                if (nuevoTipo == null) {
                    mostrarAlerta("Error", "Tipo de proveedor no encontrado.", Alert.AlertType.ERROR);
                    insumosTableView.refresh();
                    return;
                }

                int idOriginal = insumoActual.getIdTipoProveedor();
                int idNuevo = nuevoTipo.getId();

                // Si el ID de Tipo Proveedor no cambió, salir
                if (idOriginal == idNuevo) return;

                // --- Lógica de Obtención de Nombres para el Historial ---
                String nombreOriginal = getTipoProveedorNombre(idOriginal);
                String nombreNuevo = nuevoTipo.getDescripcion();
                // --------------------------------------------------------

                // 1. Iniciar Transacción
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                conn.setAutoCommit(false);

                // 2. Aplicar el cambio al modelo actual
                insumoActual.setIdTipoProveedor(idNuevo);

                // 3. Registrar el cambio en Historial - AHORA REGISTRAMOS EL NOMBRE
                boolean exitoRegistro = historialDAO.insertarRegistro(
                        SessionManager.getInstance().getLoggedInUserId(),
                        "Insumo",
                        "TipoProveedor", // Se puede cambiar el nombre de la columna para indicar que es el nombre
                        insumoActual.getIdInsumo(),
                        nombreOriginal, // Usamos el nombre original
                        nombreNuevo,    // Usamos el nombre nuevo
                        conn
                );

                // 4. Actualizar la base de datos de Insumos
                boolean exitoActualizacion = insumoDAO.modificarInsumo(insumoActual);

                if (exitoActualizacion && exitoRegistro) {
                    conn.commit();
                    mostrarAlerta("Éxito", "Tipo de proveedor y registro de actividad actualizados.", Alert.AlertType.INFORMATION);
                } else {
                    conn.rollback();
                    mostrarAlerta("Error", "No se pudo actualizar el Tipo de Proveedor o registrar la actividad. ROLLBACK realizado.", Alert.AlertType.ERROR);
                    // Revertir el modelo si falla
                    insumoActual.setIdTipoProveedor(idOriginal);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { /* Ignorar */ }
                mostrarAlerta("Error de BD", "Ocurrió un error en la base de datos.", Alert.AlertType.ERROR);
            } finally {
                try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException closeEx) { /* Ignorar */ }
            }
            insumosTableView.refresh();
        });

        // OnEditCommit para nombreInsumoColumn (MODIFICADO para AUDITORÍA)
        nombreInsumoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nombreInsumoColumn.setOnEditCommit(event -> {
            String nuevoValor = event.getNewValue();
            Insumo insumoActual = event.getRowValue();
            String valorOriginal = insumoActual.getNombreInsumo();

            if (nuevoValor == null || nuevoValor.trim().isEmpty()) {
                mostrarAlerta("Advertencia", "El nombre del insumo no puede quedar vacío.", Alert.AlertType.WARNING);
                insumosTableView.refresh();
                return;
            }
            if (valorOriginal.equals(nuevoValor.trim())) {
                return; // No hubo cambio real
            }

            Connection conn = null;
            try {
                // 1. Iniciar Transacción
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                conn.setAutoCommit(false);

                // 2. Aplicar el cambio al modelo actual
                insumoActual.setNombreInsumo(nuevoValor.trim());

                // 3. Registrar el cambio en Historial
                boolean exitoRegistro = historialDAO.insertarRegistro(
                        SessionManager.getInstance().getLoggedInUserId(),
                        "Insumo",
                        "nombreInsumo",
                        insumoActual.getIdInsumo(),
                        valorOriginal,
                        nuevoValor.trim(),
                        conn
                );

                // 4. Actualizar la base de datos de Insumos
                boolean exitoActualizacion = insumoDAO.modificarInsumo(insumoActual);

                if (exitoActualizacion && exitoRegistro) {
                    conn.commit();
                    mostrarAlerta("Éxito", "Nombre y registro de actividad actualizados.", Alert.AlertType.INFORMATION);
                } else {
                    conn.rollback();
                    mostrarAlerta("Error", "No se pudo actualizar el Nombre o registrar la actividad. ROLLBACK realizado.", Alert.AlertType.ERROR);
                    // Revertir el modelo si falla
                    insumoActual.setNombreInsumo(valorOriginal);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { /* Ignorar */ }
                mostrarAlerta("Error de BD", "Ocurrió un error en la base de datos.", Alert.AlertType.ERROR);
            } finally {
                try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException closeEx) { /* Ignorar */ }
            }
            insumosTableView.refresh();
        });

        // OnEditCommit para descripcionColumn (MODIFICADO para AUDITORÍA)
        descripcionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        descripcionColumn.setOnEditCommit(event -> {
            String nuevoValor = event.getNewValue();
            Insumo insumoActual = event.getRowValue();
            String valorOriginal = insumoActual.getDescripcion();

            if (nuevoValor == null || nuevoValor.trim().isEmpty()) {
                mostrarAlerta("Advertencia", "La descripción no puede quedar vacía.", Alert.AlertType.WARNING);
                insumosTableView.refresh();
                return;
            }
            if (valorOriginal != null && valorOriginal.equals(nuevoValor.trim())) {
                return; // No hubo cambio real
            }

            Connection conn = null;
            try {
                // 1. Iniciar Transacción
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                conn.setAutoCommit(false);

                // 2. Aplicar el cambio al modelo actual
                insumoActual.setDescripcion(nuevoValor.trim());

                // 3. Registrar el cambio en Historial
                boolean exitoRegistro = historialDAO.insertarRegistro(
                        SessionManager.getInstance().getLoggedInUserId(),
                        "Insumo",
                        "descripcion",
                        insumoActual.getIdInsumo(),
                        valorOriginal != null ? valorOriginal : "",
                        nuevoValor.trim(),
                        conn
                );

                // 4. Actualizar la base de datos de Insumos
                boolean exitoActualizacion = insumoDAO.modificarInsumo(insumoActual);

                if (exitoActualizacion && exitoRegistro) {
                    conn.commit();
                    mostrarAlerta("Éxito", "Descripción y registro de actividad actualizados.", Alert.AlertType.INFORMATION);
                } else {
                    conn.rollback();
                    mostrarAlerta("Error", "No se pudo actualizar la Descripción o registrar la actividad. ROLLBACK realizado.", Alert.AlertType.ERROR);
                    // Revertir el modelo si falla
                    insumoActual.setDescripcion(valorOriginal);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { /* Ignorar */ }
                mostrarAlerta("Error de BD", "Ocurrió un error en la base de datos.", Alert.AlertType.ERROR);
            } finally {
                try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException closeEx) { /* Ignorar */ }
            }
            insumosTableView.refresh();
        });

        // OnEditCommit para stockMinimoColumn (MODIFICADO para AUDITORÍA)
        stockMinimoColumn.setCellFactory(column -> new TextFieldTableCell<Insumo, Number>() {
            // ... (implementación de la celda de stockMinimo, MANTENIDA) ...
            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    // AGREGADO: Crea la copia antes de la edición
                    Insumo selectedInsumo = getTableView().getItems().get(getIndex());
                    insumoOriginal = crearCopiaInsumo(selectedInsumo);
                    // FIN AGREGADO
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
            int valorOriginal = event.getOldValue().intValue();
            int nuevoValor = nuevoStockNum.intValue();

            if (valorOriginal == nuevoValor) {
                return; // No hubo cambio real
            }

            Connection conn = null;
            try {
                // 1. Iniciar Transacción
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                conn.setAutoCommit(false);

                // 2. Aplicar el cambio al modelo actual
                insumoActual.setStockMinimo(nuevoValor);

                // 3. Registrar el cambio en Historial
                boolean exitoRegistro = historialDAO.insertarRegistro(
                        SessionManager.getInstance().getLoggedInUserId(),
                        "Insumo",
                        "stockMinimo",
                        insumoActual.getIdInsumo(),
                        String.valueOf(valorOriginal),
                        String.valueOf(nuevoValor),
                        conn
                );

                // 4. Actualizar la base de datos de Insumos
                boolean exitoActualizacion = insumoDAO.modificarInsumo(insumoActual);

                if (exitoActualizacion && exitoRegistro) {
                    conn.commit();
                    mostrarAlerta("Éxito", "Stock Mínimo y registro de actividad actualizados.", Alert.AlertType.INFORMATION);
                } else {
                    conn.rollback();
                    mostrarAlerta("Error", "No se pudo actualizar el Stock Mínimo o registrar la actividad. ROLLBACK realizado.", Alert.AlertType.ERROR);
                    // Revertir el modelo si falla
                    insumoActual.setStockMinimo(valorOriginal);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { /* Ignorar */ }
                mostrarAlerta("Error de BD", "Ocurrió un error en la base de datos.", Alert.AlertType.ERROR);
            } finally {
                try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException closeEx) { /* Ignorar */ }
            }
            insumosTableView.refresh();
        });

        // ====================================================================
        // === IMPLEMENTACIÓN DE ALERTA DE STOCK EN EL COMMIT DE STOCK ACTUAL (MODIFICADO para AUDITORÍA) ===
        // ====================================================================
        stockActualColumn.setCellFactory(column -> new TextFieldTableCell<Insumo, Number>() {
            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    // AGREGADO: Crea la copia antes de la edición
                    Insumo selectedInsumo = getTableView().getItems().get(getIndex());
                    insumoOriginal = crearCopiaInsumo(selectedInsumo);
                    // FIN AGREGADO
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
            int valorOriginal = event.getOldValue().intValue();
            int nuevoValor = nuevoStockNum.intValue();

            if (valorOriginal == nuevoValor) {
                return; // No hubo cambio real
            }

            // Validar stock (ya se hizo en commitEdit, pero por seguridad)
            if (nuevoStockNum == null) {
                insumosTableView.refresh();
                return;
            }

            Connection conn = null;
            try {
                // 1. Iniciar Transacción
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                conn.setAutoCommit(false);

                // 2. Aplicar el cambio al modelo actual
                insumoActual.setStockActual(nuevoValor);

                // 3. Registrar el cambio en Historial
                boolean exitoRegistro = historialDAO.insertarRegistro(
                        SessionManager.getInstance().getLoggedInUserId(),
                        "Insumo",
                        "stockActual",
                        insumoActual.getIdInsumo(),
                        String.valueOf(valorOriginal),
                        String.valueOf(nuevoValor),
                        conn
                );

                // 4. Actualizar la base de datos de Insumos
                boolean exitoActualizacion = insumoDAO.modificarInsumo(insumoActual);

                if (exitoActualizacion && exitoRegistro) {
                    conn.commit();

                    // Lógica de Alerta de Stock después del COMMIT exitoso
                    int stockMinimo = insumoActual.getStockMinimo();
                    if (nuevoValor == 0) {
                        mostrarAlerta("¡Stock Agotado! 🚫", "El insumo '" + insumoActual.getNombreInsumo() + "' se quedó sin stock.", Alert.AlertType.ERROR);
                    } else if (nuevoValor <= stockMinimo) {
                        mostrarAlerta("¡Stock Crítico! ⚠️", "El insumo '" + insumoActual.getNombreInsumo() + "' ha alcanzado o superado el stock mínimo (" + stockMinimo + ").", Alert.AlertType.WARNING);
                    } else {
                        mostrarAlerta("Éxito", "Stock y registro de actividad actualizados.", Alert.AlertType.INFORMATION);
                    }
                } else {
                    conn.rollback();
                    mostrarAlerta("Error", "No se pudo actualizar el Stock Actual o registrar la actividad. ROLLBACK realizado.", Alert.AlertType.ERROR);
                    // Revertir el modelo si falla
                    insumoActual.setStockActual(valorOriginal);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { /* Ignorar */ }
                mostrarAlerta("Error de BD", "Ocurrió un error en la base de datos.", Alert.AlertType.ERROR);
            } finally {
                try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException closeEx) { /* Ignorar */ }
            }
            insumosTableView.refresh();
        });
        // ====================================================================

        // LÓGICA DE ESTILO DE ESTADO (MODIFICADO para AUDITORÍA)
        estadoColumn.setCellFactory(column -> new TableCell<Insumo, String>() {
            // ... (implementación de la celda de estado, MANTENIDA) ...
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>();

            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
                    return;
                }
                // AGREGADO: Crea la copia antes de la edición
                insumoOriginal = crearCopiaInsumo(getTableView().getItems().get(getIndex()));
                // FIN AGREGADO
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
            Insumo insumoActual = event.getRowValue();
            String nuevoEstado = event.getNewValue();
            String valorOriginal = event.getOldValue();

            if (valorOriginal.equals(nuevoEstado)) {
                return; // No hubo cambio real
            }

            Connection conn = null;
            try {
                // 1. Iniciar Transacción
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                conn.setAutoCommit(false);

                // 2. Aplicar el cambio al modelo actual
                insumoActual.setEstado(nuevoEstado);

                // 3. Registrar el cambio en Historial
                boolean exitoRegistro = historialDAO.insertarRegistro(
                        SessionManager.getInstance().getLoggedInUserId(),
                        "Insumo",
                        "estado",
                        insumoActual.getIdInsumo(),
                        valorOriginal,
                        nuevoEstado,
                        conn
                );

                // 4. Actualizar la base de datos de Insumos (usando el método del DAO que actualiza el estado)
                boolean exitoActualizacion = insumoDAO.modificarEstadoInsumo(insumoActual.getIdInsumo(), nuevoEstado);

                if (exitoActualizacion && exitoRegistro) {
                    conn.commit();
                    mostrarAlerta("Éxito", "Estado y registro de actividad actualizados.", Alert.AlertType.INFORMATION);
                } else {
                    conn.rollback();
                    mostrarAlerta("Error", "No se pudo actualizar el Estado o registrar la actividad. ROLLBACK realizado.", Alert.AlertType.ERROR);
                    // Revertir el modelo si falla
                    insumoActual.setEstado(valorOriginal);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { /* Ignorar */ }
                mostrarAlerta("Error de BD", "Ocurrió un error en la base de datos.", Alert.AlertType.ERROR);
            } finally {
                try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException closeEx) { /* Ignorar */ }
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

    // ... resto de métodos (handleAlertasStockButton, cargarInsumosYConfigurarFiltros, etc.) MANTENIDOS ...

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
        // NOTA: Este botón no tiene lógica de auditoría directa ya que la edición se hace celda por celda.
        // Se mantiene la lógica original, asumiendo que se usa para guardados generales o secundarios.
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