package app.controller;

import app.dao.CategoriaDAO;
import app.dao.ProductoDAO;
import app.dao.HistorialActividadDAO;
import app.controller.SessionManager; // Asumiendo que esta clase existe
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import app.model.Categoria;
import app.model.Producto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.geometry.Rectangle2D;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProductoMenuController {

    // CONFIGURACIÓN DE CONEXIÓN (Debe coincidir con la de StockController)
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // =========================================================================
    // CLASES CONVERTIDORAS AUXILIARES
    // =========================================================================

    private static class SafeDoubleStringConverter extends StringConverter<Double> {
        @Override
        public String toString(Double object) {
            return object != null ? String.format("%.2f", object) : "";
        }

        @Override
        public Double fromString(String string) {
            if (string == null || string.trim().isEmpty()) {
                return null;
            }
            try {
                String cleanString = string.trim().replace(',', '.');
                return Double.parseDouble(cleanString);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    private static class SafeIntegerStringConverter extends StringConverter<Integer> {
        @Override
        public String toString(Integer object) {
            return object != null ? object.toString() : "";
        }

        @Override
        public Integer fromString(String string) {
            if (string == null || string.trim().isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(string.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }


    // =========================================================================
    // ELEMENTOS FXML Y DAOs
    // =========================================================================

    @FXML private TableView<Producto> productosTableView;
    @FXML private TableColumn<Producto, Integer> idProductoColumn;
    @FXML private TableColumn<Producto, String> nombreProductoColumn;
    @FXML private TableColumn<Producto, String> descripcionColumn;
    @FXML private TableColumn<Producto, Double> precioColumn;
    @FXML private TableColumn<Producto, Integer> stockColumn;
    @FXML private TableColumn<Producto, String> categoriaNombreColumn;

    @FXML private TextField filterField;
    @FXML private ComboBox<Categoria> cmbCategoriaFilter;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final HistorialActividadDAO historialDAO = new HistorialActividadDAO();
    private ObservableList<Producto> masterData;
    private FilteredList<Producto> filteredData;
    private Map<Integer, String> categoriaNamesMap; // Mapa ID -> Nombre
    private Map<String, Integer> categoriaIdsMap;   // Mapa Nombre -> ID

    private ObservableList<Categoria> categoriaFilterList;
    private ObservableList<String> categoriaNamesObservableList;

    // CLAVE: Variable para guardar el estado original antes de la edición de CELDA
    private Producto productoOriginal;

    @FXML
    public void initialize() {
        productosTableView.setEditable(true);

        categoriaNamesMap = categoriaDAO.getCategoriaNamesMap();
        // Generar mapa inverso Nombre -> ID
        categoriaIdsMap = categoriaNamesMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        loadCategoriaLists();

        setupColumns();
        loadProductos();
        setupFilter();

        // Listener para guardar la copia original al SELECCIONAR la fila (Respaldo)
        productosTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                this.productoOriginal = crearCopiaProducto(newVal);
            }
        });
    }

    // =========================================================================
    // UTILIDADES DE AUDITORÍA Y CONTROL
    // =========================================================================

    private Producto crearCopiaProducto(Producto original) {
        if (original == null) return null;

        // CLAVE: Usar el constructor que recibe TODOS los argumentos,
        // que es el que inicializa correctamente las JavaFX Properties (final).
        Producto copia = new Producto(
                original.getIdProducto(),
                original.getNombreProducto(),
                original.getDescripcion(),
                original.getPrecio(),
                original.getStock(),
                original.getIdCategoria()
        );

        return copia;
    }

    private void revertirProductoAlOriginal(Producto actual, Producto original) {
        if (actual == null || original == null) return;
        if (actual.getIdProducto() != original.getIdProducto()) return;

        actual.setNombreProducto(original.getNombreProducto());
        actual.setDescripcion(original.getDescripcion());
        actual.setPrecio(original.getPrecio());
        actual.setStock(original.getStock());
        actual.setIdCategoria(original.getIdCategoria());
        productosTableView.refresh();
    }

    private String getCategoriaNombre(int id) {
        return categoriaNamesMap.getOrDefault(id, "-- Sin Categoría --");
    }

    /**
     * Registra el cambio en Historial. No hace Commit.
     */
    private void auditarCambio(Connection conn, Producto productoActual, String columna, Object valorOriginal, Object valorNuevo) throws SQLException {
        String originalStr = (valorOriginal != null) ? valorOriginal.toString() : "";
        String nuevoStr = (valorNuevo != null) ? valorNuevo.toString() : "";

        boolean exitoRegistro = historialDAO.insertarRegistro(
                SessionManager.getInstance().getLoggedInUserId(),
                "Producto",
                columna,
                productoActual.getIdProducto(),
                originalStr,
                nuevoStr,
                conn
        );

        if (!exitoRegistro) {
            // Si falla el registro, lanzamos una excepción para provocar el ROLLBACK de Historial
            throw new SQLException("Fallo al registrar la actividad para la columna: " + columna);
        }
    }


    // --------------------------------------------------------------------------
    // LÓGICA DE CARGA Y CONFIGURACIÓN DE COLUMNAS
    // --------------------------------------------------------------------------

    private void loadCategoriaLists() {
        // ... (Lógica para cargar listas de categorías) ...
        List<Categoria> categoriasDB = categoriaDAO.getAllCategorias();

        List<String> names = categoriasDB.stream()
                .map(Categoria::getNombre)
                .collect(Collectors.toList());

        Categoria sinCategoria = new Categoria(0, "-- Sin Categoría --", "");

        names.add(0, sinCategoria.getNombre());
        categoriaNamesObservableList = FXCollections.observableArrayList(names);

        categoriaNamesMap.put(0, sinCategoria.getNombre());
        categoriaIdsMap.put(sinCategoria.getNombre(), 0);

        categoriaFilterList = FXCollections.observableArrayList(categoriasDB);

        Categoria todos = new Categoria(-1, "Todos", "");
        categoriaFilterList.add(0, todos);

        if (!categoriaFilterList.stream().anyMatch(c -> c.getIdCategoria() == 0)) {
            categoriaFilterList.add(1, sinCategoria);
        } else {
            int indexSinCategoria = -1;
            for (int i = 0; i < categoriaFilterList.size(); i++) {
                if (categoriaFilterList.get(i).getIdCategoria() == 0) {
                    indexSinCategoria = i;
                    break;
                }
            }
            if (indexSinCategoria != 1 && indexSinCategoria != -1) {
                Categoria temp = categoriaFilterList.remove(indexSinCategoria);
                categoriaFilterList.add(1, temp);
            } else if (indexSinCategoria == -1) {
                categoriaFilterList.add(1, sinCategoria);
            }
        }
    }


    private void setupColumns() {
        idProductoColumn.setCellValueFactory(cellData -> cellData.getValue().idProductoProperty().asObject());

        // NOMBRE
        nombreProductoColumn.setCellValueFactory(cellData -> cellData.getValue().nombreProductoProperty());
        nombreProductoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nombreProductoColumn.setOnEditCommit(this::handleNameEditCommit);

        // DESCRIPCIÓN
        descripcionColumn.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
        descripcionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        descripcionColumn.setOnEditCommit(this::handleDescriptionEditCommit);

        // PRECIO
        precioColumn.setCellValueFactory(cellData -> cellData.getValue().precioProperty().asObject());
        precioColumn.setCellFactory(TextFieldTableCell.forTableColumn(new SafeDoubleStringConverter()));
        precioColumn.setOnEditCommit(this::handlePriceEditCommit);

        // STOCK
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());
        stockColumn.setCellFactory(TextFieldTableCell.forTableColumn(new SafeIntegerStringConverter()));
        stockColumn.setOnEditCommit(this::handleStockEditCommit);

        // CATEGORÍA
        categoriaNombreColumn.setCellValueFactory(cellData -> {
            int idCategoria = cellData.getValue().getIdCategoria();
            String nombre = getCategoriaNombre(idCategoria);
            return new SimpleStringProperty(nombre);
        });
        categoriaNombreColumn.setCellFactory(ComboBoxTableCell.forTableColumn(categoriaNamesObservableList));
        categoriaNombreColumn.setOnEditCommit(this::handleCategoryEditCommit);
    }

    // --------------------------------------------------------------------------
    // MANEJO DE EDICIÓN (SOLO ACTUALIZA EL MODELO EN MEMORIA)
    // --------------------------------------------------------------------------

    private void handleNameEditCommit(TableColumn.CellEditEvent<Producto, String> event) {
        Producto productoActual = event.getRowValue();
        String nuevoValor = event.getNewValue().trim();

        if (nuevoValor.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "El nombre del producto no puede estar vacío.");
            productosTableView.refresh();
            return;
        }
        if (event.getOldValue().equals(nuevoValor)) { return; }

        if (productoDAO.isNombreProductoDuplicated(nuevoValor, productoActual.getIdProducto())) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "Ya existe un producto con el nombre: " + nuevoValor);
            productosTableView.refresh();
            return;
        }

        // 1. Actualiza solo la propiedad en memoria
        productoActual.setNombreProducto(nuevoValor);
        productosTableView.refresh();
    }

    private void handleDescriptionEditCommit(TableColumn.CellEditEvent<Producto, String> event) {
        Producto productoActual = event.getRowValue();
        String nuevoValor = event.getNewValue() != null ? event.getNewValue().trim() : "";
        String valorOriginal = event.getOldValue() != null ? event.getOldValue() : "";

        if (valorOriginal.equals(nuevoValor)) { return; }

        // 1. Actualiza solo la propiedad en memoria
        productoActual.setDescripcion(nuevoValor);
        productosTableView.refresh();
    }

    private void handlePriceEditCommit(TableColumn.CellEditEvent<Producto, Double> event) {
        Producto productoActual = event.getRowValue();
        Double nuevoValor = event.getNewValue();
        Double valorOriginal = event.getOldValue();

        if (nuevoValor == null || nuevoValor <= 0) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "El precio debe ser un valor positivo.");
            productosTableView.refresh();
            return;
        }
        if (Double.compare(valorOriginal, nuevoValor) == 0) { return; }

        // 1. Actualiza solo la propiedad en memoria
        productoActual.setPrecio(nuevoValor);
        productosTableView.refresh();
    }

    private void handleStockEditCommit(TableColumn.CellEditEvent<Producto, Integer> event) {
        Producto productoActual = event.getRowValue();
        Integer nuevoValor = event.getNewValue();
        Integer valorOriginal = event.getOldValue();

        if (nuevoValor == null || nuevoValor < 0) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "El stock no puede ser negativo.");
            productosTableView.refresh();
            return;
        }
        if (valorOriginal.intValue() == nuevoValor.intValue()) { return; }

        // 1. Actualiza solo la propiedad en memoria
        productoActual.setStock(nuevoValor);
        productosTableView.refresh();
    }

    private void handleCategoryEditCommit(TableColumn.CellEditEvent<Producto, String> event) {
        Producto productoActual = event.getRowValue();
        String newCategoryName = event.getNewValue();
        String oldCategoryName = event.getOldValue();

        if (oldCategoryName.equals(newCategoryName)) { return; }

        // Obtener el ID de la nueva categoría y actualizar en memoria
        int newIdCategoria = categoriaIdsMap.getOrDefault(newCategoryName, 0);

        // 1. Actualiza solo la propiedad en memoria (ID de categoría)
        productoActual.setIdCategoria(newIdCategoria);
        productosTableView.refresh();
    }


    // =========================================================================
    // LÓGICA DE PERSISTENCIA Y AUDITORÍA (CENTRALIZADA EN EL BOTÓN)
    // =========================================================================

    @FXML
    private void handleModificarProductoButton(ActionEvent event) {
        Producto productoActual = productosTableView.getSelectionModel().getSelectedItem();

        if (productoActual == null) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Por favor, seleccione una fila antes de guardar.");
            return;
        }

        // Asegura que tenemos la copia original para comparar
        if (this.productoOriginal == null || productoActual.getIdProducto() != this.productoOriginal.getIdProducto()) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "El producto no está listo para guardar. Vuelva a seleccionar la fila.");
            return;
        }

        Connection conn = null;
        boolean huboCambios = false;

        try {
            // 1. Iniciar Transacción (solo para el HistorialActividadDAO)
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false);

            // --- 2. AUDITAR Y REGISTRAR CADA POSIBLE CAMBIO (Igual que en StockController) ---

            // Cambio en Nombre
            if (!productoActual.getNombreProducto().equals(this.productoOriginal.getNombreProducto())) {
                auditarCambio(conn, productoActual, "nombreProducto", this.productoOriginal.getNombreProducto(), productoActual.getNombreProducto());
                huboCambios = true;
            }

            // Cambio en Descripción
            if (!productoActual.getDescripcion().equals(this.productoOriginal.getDescripcion())) {
                auditarCambio(conn, productoActual, "descripcion", this.productoOriginal.getDescripcion(), productoActual.getDescripcion());
                huboCambios = true;
            }

            // Cambio en Precio
            if (Double.compare(productoActual.getPrecio(), this.productoOriginal.getPrecio()) != 0) {
                auditarCambio(conn, productoActual, "precio", this.productoOriginal.getPrecio(), productoActual.getPrecio());
                huboCambios = true;
            }

            // Cambio en Stock
            if (productoActual.getStock() != this.productoOriginal.getStock()) {
                auditarCambio(conn, productoActual, "stock", this.productoOriginal.getStock(), productoActual.getStock());
                huboCambios = true;
            }

            // Cambio en Categoría
            if (productoActual.getIdCategoria() != this.productoOriginal.getIdCategoria()) {
                String nombreOriginal = getCategoriaNombre(this.productoOriginal.getIdCategoria());
                String nombreNuevo = getCategoriaNombre(productoActual.getIdCategoria());

                auditarCambio(conn, productoActual, "categoria", nombreOriginal, nombreNuevo);
                huboCambios = true;
            }

            // --- 3. PERSISTIR LOS CAMBIOS SI LOS HUBO ---
            if (huboCambios) {
                // CLAVE: Llama al DAO sin pasar la conexión, replicando el patrón de StockController.
                boolean exitoActualizacion = productoDAO.updateProducto(productoActual);

                if (exitoActualizacion) {
                    conn.commit(); // Solo confirma la historia (el DAO ya guardó el Producto)
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", "Producto y registro de actividad actualizados.");
                    this.productoOriginal = crearCopiaProducto(productoActual); // Actualiza la copia
                } else {
                    conn.rollback(); // Deshace la historia si el DAO falló
                    showAlert(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el producto en la BD. ROLLBACK de historial realizado.");
                    revertirProductoAlOriginal(productoActual, this.productoOriginal); // Revertir en memoria
                }
            } else {
                conn.rollback();
                showAlert(Alert.AlertType.WARNING, "Advertencia", "No se detectaron cambios para guardar.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { /* Ignorar */ }
            showAlert(Alert.AlertType.ERROR, "Error de BD", "Ocurrió un error en la base de datos: " + e.getMessage());
            revertirProductoAlOriginal(productoActual, this.productoOriginal);
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException closeEx) { /* Ignorar */ }
            productosTableView.refresh();
        }
    }

    // ... (El resto de métodos: loadProductos, handleRefreshButton, handleVolverButton, etc.) ...

    private void loadProductos() {
        masterData = FXCollections.observableArrayList(productoDAO.getAllProductos());
        if (filteredData != null) {
            filteredData.setPredicate(null);
        }
        filteredData = new FilteredList<>(masterData, p -> true);

        SortedList<Producto> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(productosTableView.comparatorProperty());
        productosTableView.setItems(sortedData);
    }

    private void setupFilter() {
        cmbCategoriaFilter.setItems(categoriaFilterList);
        cmbCategoriaFilter.getSelectionModel().select(0);

        filterField.textProperty().addListener((observable, oldValue, newValue) -> filterData());
        cmbCategoriaFilter.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> filterData());
    }

    private void filterData() {
        filteredData.setPredicate(new Predicate<Producto>() {
            @Override
            public boolean test(Producto producto) {
                String searchText = filterField.getText();
                Categoria selectedCategory = cmbCategoriaFilter.getSelectionModel().getSelectedItem();

                boolean matchesSearch = true;
                if (searchText != null && !searchText.isEmpty()) {
                    String lowerCaseFilter = searchText.toLowerCase();
                    if (!(producto.getNombreProducto().toLowerCase().contains(lowerCaseFilter) ||
                            (producto.getDescripcion() != null && producto.getDescripcion().toLowerCase().contains(lowerCaseFilter)))) {
                        matchesSearch = false;
                    }
                }

                boolean matchesCategory = true;
                if (selectedCategory != null) {
                    int selectedId = selectedCategory.getIdCategoria();
                    int productoId = producto.getIdCategoria();

                    if (selectedId == -1) {
                        matchesCategory = true;
                    } else if (selectedId == 0) {
                        matchesCategory = (productoId == 0);
                    } else {
                        matchesCategory = (productoId == selectedId);
                    }
                }

                return matchesSearch && matchesCategory;
            }
        });
    }

    @FXML
    private void handleRegistrarProductoButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registrarProducto.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            Scene newScene = new Scene(root);
            newStage.setScene(newScene);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double screenHeight = screenBounds.getHeight();

            newStage.setHeight(screenHeight);
            newStage.sizeToScene();

            newStage.setTitle("Registro de Nuevo Producto");
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(((Node) event.getSource()).getScene().getWindow());

            newStage.centerOnScreen();

            newStage.showAndWait();

            loadProductos();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Navegación", "No se pudo cargar la vista de registro de producto.");
            e.printStackTrace();
        }
    }


    @FXML
    private void handleRefreshButton(ActionEvent event) {
        loadProductos();
        loadCategoriaLists();
        filterField.setText("");
        cmbCategoriaFilter.getSelectionModel().select(0);
        this.productoOriginal = null;
    }

    @FXML
    private void handleVolverButton(ActionEvent event) {
        try {
            // Asumiendo que MenuController tiene un método loadScene estático
            // (basado en la estructura de tu StockController)
            Node sourceNode = (Node) event.getSource();
            Stage stage = (Stage) sourceNode.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuAbmStock.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menú ABMs de Stock");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Navegación", "No se pudo cargar la vista de Menu Abm Stock.");
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}