package app.controller;

import app.dao.CategoriaDAO;
import app.dao.ProductoDAO;
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
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProductoMenuController {

    // =========================================================================
    // CLASES CONVERTIDORAS AUXILIARES ANIDADAS Y ESTÁTICAS
    // =========================================================================

    /**
     * Conversor seguro para Double. Si la conversión falla (ej: hay letras/vacío),
     * devuelve null, permitiendo que el handleEditCommit maneje la validación y la alerta.
     */
    private static class SafeDoubleStringConverter extends StringConverter<Double> {
        @Override
        public String toString(Double object) {
            return object != null ? object.toString() : "";
        }

        @Override
        public Double fromString(String string) {
            if (string == null || string.trim().isEmpty()) {
                return null; // Devuelve null si está vacío.
            }
            try {
                return Double.parseDouble(string.trim());
            } catch (NumberFormatException e) {
                return null; // Devuelve null si hay letras/formato inválido.
            }
        }
    }

    /**
     * Conversor seguro para Integer. Si la conversión falla (ej: hay letras/vacío),
     * devuelve null, permitiendo que el handleEditCommit maneje la validación y la alerta.
     */
    private static class SafeIntegerStringConverter extends StringConverter<Integer> {
        @Override
        public String toString(Integer object) {
            return object != null ? object.toString() : "";
        }

        @Override
        public Integer fromString(String string) {
            if (string == null || string.trim().isEmpty()) {
                return null; // Devuelve null si está vacío.
            }
            try {
                // Usamos Integer.parseInt() que automáticamente valida si hay punto decimal.
                return Integer.parseInt(string.trim());
            } catch (NumberFormatException e) {
                return null; // Devuelve null si hay letras/formato inválido.
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
    private ObservableList<Producto> masterData;
    private FilteredList<Producto> filteredData;
    private Map<Integer, String> categoriaNamesMap;

    private ObservableList<Categoria> categoriaFilterList;
    private ObservableList<String> categoriaNamesObservableList;


    @FXML
    public void initialize() {
        productosTableView.setEditable(true);

        categoriaNamesMap = categoriaDAO.getCategoriaNamesMap();
        loadCategoriaLists();

        setupColumns();
        loadProductos();
        setupFilter();
    }

    // --------------------------------------------------------------------------
    // LÓGICA DE CARGA Y CONFIGURACIÓN DE COLUMNAS
    // --------------------------------------------------------------------------

    private void loadCategoriaLists() {
        List<Categoria> categoriasDB = categoriaDAO.getAllCategorias();

        List<String> names = categoriasDB.stream()
                .map(Categoria::getNombre)
                .collect(Collectors.toList());

        names.add(0, new Categoria(0, "Sin Categoría", "").getNombre());
        categoriaNamesObservableList = FXCollections.observableArrayList(names);

        categoriaFilterList = FXCollections.observableArrayList(categoriasDB);

        Categoria todos = new Categoria(-1, "Todos", "");
        Categoria sinCategoria = new Categoria();

        categoriaFilterList.add(0, todos);
        if (!categoriaFilterList.stream().anyMatch(c -> c.getIdCategoria() == 0)) {
            categoriaFilterList.add(1, sinCategoria);
        } else {
            categoriaFilterList.remove(sinCategoria);
            categoriaFilterList.add(1, sinCategoria);
        }
    }

    private void setupColumns() {
        idProductoColumn.setCellValueFactory(cellData -> cellData.getValue().idProductoProperty().asObject());

        nombreProductoColumn.setCellValueFactory(cellData -> cellData.getValue().nombreProductoProperty());
        nombreProductoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nombreProductoColumn.setOnEditCommit(event ->
                handleNameEditCommit(event.getTableView().getItems().get(event.getTablePosition().getRow()), event.getNewValue())
        );

        descripcionColumn.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
        descripcionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        descripcionColumn.setOnEditCommit(event ->
                handleDescriptionEditCommit(event.getTableView().getItems().get(event.getTablePosition().getRow()), event.getNewValue())
        );

        // APLICANDO EL CONVERSOR SEGURO DE DOUBLE
        precioColumn.setCellValueFactory(cellData -> cellData.getValue().precioProperty().asObject());
        precioColumn.setCellFactory(TextFieldTableCell.forTableColumn(new SafeDoubleStringConverter()));
        precioColumn.setOnEditCommit(event ->
                handlePriceEditCommit(event.getTableView().getItems().get(event.getTablePosition().getRow()), event.getNewValue())
        );

        // APLICANDO EL CONVERSOR SEGURO DE INTEGER
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());
        stockColumn.setCellFactory(TextFieldTableCell.forTableColumn(new SafeIntegerStringConverter()));
        stockColumn.setOnEditCommit(event ->
                handleStockEditCommit(event.getTableView().getItems().get(event.getTablePosition().getRow()), event.getNewValue())
        );

        categoriaNombreColumn.setCellValueFactory(cellData -> {
            int idCategoria = cellData.getValue().getIdCategoria();
            String nombre = categoriaNamesMap.getOrDefault(idCategoria, "N/A");
            return new SimpleStringProperty(nombre);
        });

        categoriaNombreColumn.setCellFactory(ComboBoxTableCell.forTableColumn(categoriaNamesObservableList));

        categoriaNombreColumn.setOnEditCommit(event ->
                handleCategoryEditCommit(event.getTableView().getItems().get(event.getTablePosition().getRow()), event.getNewValue())
        );
    }

    // --------------------------------------------------------------------------
    // MANEJO DE EDICIÓN Y VALIDACIONES
    // --------------------------------------------------------------------------

    private void handleNameEditCommit(Producto producto, String newValue) {
        String trimmedValue = newValue.trim();
        if (trimmedValue.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "El nombre del producto no puede estar vacío.");
            productosTableView.refresh();
            return;
        }

        if (productoDAO.isNombreProductoDuplicated(trimmedValue, producto.getIdProducto())) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "Ya existe un producto con el nombre: " + trimmedValue);
            productosTableView.refresh();
            return;
        }

        producto.setNombreProducto(trimmedValue);
        applyChangeToModel(producto);
    }

    private void handleDescriptionEditCommit(Producto producto, String newValue) {
        producto.setDescripcion(newValue != null ? newValue.trim() : "");
        applyChangeToModel(producto);
    }

    /**
     * Valida el precio editado: no vacío, numérico, y positivo.
     * El SafeDoubleStringConverter asegura que llegamos aquí con un Double o null (si hay error de formato/vacío).
     */
    private void handlePriceEditCommit(Producto producto, Double newValue) {

        // El conversor devuelve null si la entrada no es numérica o está vacía.
        if (newValue == null) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "El precio no puede estar vacío y debe ser un valor numérico positivo (ej: 12.50).");
            productosTableView.refresh(); // Vuelve al valor anterior
            return;
        }

        // Validación de valor (debe ser positivo)
        if (newValue <= 0) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "El precio debe ser un valor positivo.");
            productosTableView.refresh(); // Vuelve al valor anterior
            return;
        }

        producto.setPrecio(newValue);
        applyChangeToModel(producto);
    }

    /**
     * Valida el stock editado: no vacío, entero, y no negativo.
     * El SafeIntegerStringConverter asegura que llegamos aquí con un Integer o null (si hay error de formato/vacío).
     */
    private void handleStockEditCommit(Producto producto, Integer newValue) {

        // El conversor devuelve null si la entrada no es numérica entera o está vacía.
        if (newValue == null) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "El stock no puede estar vacío y debe ser un número entero no negativo.");
            productosTableView.refresh(); // Vuelve al valor anterior
            return;
        }

        // Validación de valor (debe ser no negativo)
        if (newValue < 0) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "El stock no puede ser negativo.");
            productosTableView.refresh(); // Vuelve al valor anterior
            return;
        }

        producto.setStock(newValue);
        applyChangeToModel(producto);
    }

    private void handleCategoryEditCommit(Producto producto, String newCategoryName) {
        int newIdCategoria = categoriaNamesMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(newCategoryName))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0);

        producto.setIdCategoria(newIdCategoria);
        applyChangeToModel(producto);
    }

    private void applyChangeToModel(Producto producto) {
        productosTableView.refresh();
    }

    // --------------------------------------------------------------------------
    // LÓGICA DE DATOS Y FILTRO
    // --------------------------------------------------------------------------

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

    // --------------------------------------------------------------------------
    // MANEJO DE BOTONES Y NAVEGACIÓN
    // --------------------------------------------------------------------------

    @FXML
    private void handleRegistrarProductoButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registrarProducto.fxml"));
            Parent root = loader.load();
            // ProductoController registroController = loader.getController(); // No es necesario si no se pasa data

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Registro de Nuevo Producto");

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());

            stage.sizeToScene();
            stage.setResizable(false);

            stage.showAndWait();

            loadProductos();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Navegación", "No se pudo cargar la vista de registro de producto.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModificarProductoButton(ActionEvent event) {
        boolean allSuccess = true;
        for (Producto producto : masterData) {
            // Se asume que el método applyChangeToModel no llama al DAO, sino que
            // la actualización masiva se hace aquí al presionar "Modificar Producto"
            if (!productoDAO.updateProducto(producto)) {
                allSuccess = false;
                System.err.println("Fallo al actualizar producto con ID: " + producto.getIdProducto());
            }
        }

        if (allSuccess) {
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "¡Productos modificados correctamente en la base de datos!");
        } else {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Algunos productos no pudieron ser modificados en la base de datos.");
        }

        loadProductos();
    }

    @FXML
    private void handleRefreshButton(ActionEvent event) {
        loadProductos();
        loadCategoriaLists();
        filterField.setText("");
        cmbCategoriaFilter.getSelectionModel().select(0);
    }

    @FXML
    private void handleVolverButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuAbmStock.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menú de Stock y ABMs");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Navegación", "No se pudo cargar la vista de Menu Abm Stock.");
            e.printStackTrace();
        }
    }

    // --------------------------------------------------------------------------
    // UTILIDADES
    // --------------------------------------------------------------------------

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}