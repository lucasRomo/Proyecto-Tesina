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
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.StringConverter;


import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProductoMenuController {

    // =========================================================================
    // CLASES CONVERTIDORAS AUXILIARES ANIDADAS Y ESTÁTICAS
    // =========================================================================

    private static class SafeDoubleStringConverter extends StringConverter<Double> {
        @Override
        public String toString(Double object) {
            return object != null ? object.toString() : "";
        }

        @Override
        public Double fromString(String string) {
            if (string == null || string.trim().isEmpty()) {
                return null;
            }
            try {
                return Double.parseDouble(string.trim());
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

        Categoria sinCategoria = new Categoria(0, "-- Sin Categoría --", "");

        names.add(0, sinCategoria.getNombre());
        categoriaNamesObservableList = FXCollections.observableArrayList(names);

        categoriaNamesMap.put(0, sinCategoria.getNombre());


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

        precioColumn.setCellValueFactory(cellData -> cellData.getValue().precioProperty().asObject());
        precioColumn.setCellFactory(TextFieldTableCell.forTableColumn(new SafeDoubleStringConverter()));
        precioColumn.setOnEditCommit(event ->
                handlePriceEditCommit(event.getTableView().getItems().get(event.getTablePosition().getRow()), event.getNewValue())
        );

        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());
        stockColumn.setCellFactory(TextFieldTableCell.forTableColumn(new SafeIntegerStringConverter()));
        stockColumn.setOnEditCommit(event ->
                handleStockEditCommit(event.getTableView().getItems().get(event.getTablePosition().getRow()), event.getNewValue())
        );

        categoriaNombreColumn.setCellValueFactory(cellData -> {
            int idCategoria = cellData.getValue().getIdCategoria();
            String nombre = categoriaNamesMap.getOrDefault(idCategoria, "-- Sin Categoría --");
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

        if (!trimmedValue.equals(producto.getNombreProducto()) && productoDAO.isNombreProductoDuplicated(trimmedValue, producto.getIdProducto())) {
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

    private void handlePriceEditCommit(Producto producto, Double newValue) {
        if (newValue == null) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "El precio no puede estar vacío y debe ser un valor numérico positivo (ej: 12.50).");
            productosTableView.refresh();
            return;
        }

        if (newValue <= 0) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "El precio debe ser un valor positivo.");
            productosTableView.refresh();
            return;
        }

        producto.setPrecio(newValue);
        applyChangeToModel(producto);
    }

    private void handleStockEditCommit(Producto producto, Integer newValue) {
        if (newValue == null) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "El stock no puede estar vacío y debe ser un número entero no negativo.");
            productosTableView.refresh();
            return;
        }

        if (newValue < 0) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "El stock no puede ser negativo.");
            productosTableView.refresh();
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
        // En el enfoque de edición en línea, este refresh es vital
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
            // 1. Cargar el FXML de registro
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registrarProducto.fxml"));
            Parent root = loader.load();

            // 2. Crear el nuevo Stage (ventana) y la Scene
            Stage newStage = new Stage();
            Scene newScene = new Scene(root);
            newStage.setScene(newScene);

            // 3. Obtener las dimensiones de la pantalla (Screen)
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double screenHeight = screenBounds.getHeight();

            // 4. Aplicar el dimensionamiento solicitado:
            // A. Establecer el ALTO al 100% de la pantalla
            newStage.setHeight(screenHeight);

            // B. Adaptar el ANCHO al contenido del FXML
            // sizeToScene calcula el ancho mínimo requerido por el layout del FXML.
            newStage.sizeToScene();

            // 5. Configurar el modo (modal) y mostrar
            newStage.setTitle("Registro de Nuevo Producto");
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(((Node) event.getSource()).getScene().getWindow());

            // Opcional: Centrar en pantalla
            newStage.centerOnScreen();

            // Se elimina setResizable(false) para permitir que el alto se ajuste.
            // newStage.setResizable(false);

            newStage.setResizable(false);
            // Mostrar la nueva ventana y esperar a que se cierre (modal)
            newStage.showAndWait();

            // 6. Recargar la tabla al volver
            loadProductos();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Navegación", "No se pudo cargar la vista de registro de producto.");
            e.printStackTrace();
        }
    }
    /**
     * LÓGICA CORREGIDA: Solo intenta modificar el producto seleccionado.
     */
    @FXML
    private void handleModificarProductoButton(ActionEvent event) {
        // 1. Obtener el producto seleccionado
        Producto selectedProducto = productosTableView.getSelectionModel().getSelectedItem();

        if (selectedProducto == null) {
            // Muestra la advertencia solicitada por el usuario
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Por favor, seleccione una fila y modifique los datos antes de guardar.");
            return;
        }

        // 2. Intentar actualizar el producto en la BD (el DAO retorna true solo si hubo cambios)
        boolean exito = productoDAO.updateProducto(selectedProducto);

        if (exito) {
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Producto modificado exitosamente.");
        } else {
            // Esto ocurre si el usuario seleccionó una fila, no hizo cambios reales
            // o los datos eran idénticos a los de la BD.
            showAlert(Alert.AlertType.WARNING, "Sin Cambios Detectados", "El producto seleccionado no ha sido modificado o los datos son idénticos a los actuales.");
        }

        // 3. Recargar la tabla para asegurar la sincronización y refrescar la vista
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
            // Se utiliza el método estático unificado para asegurar la navegación
            // y que la nueva vista ocupe toda la ventana maximizada.
            MenuController.loadScene(
                    (Node) event.getSource(),
                    "/menuAbmStock.fxml",
                    "Menú ABMs de Stock"
            );
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