package app.controller;

import app.dao.CategoriaDAO;
import app.dao.ProductoDAO;
import app.dao.HistorialActividadDAO;
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
import java.util.HashMap;
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
    private final HistorialActividadDAO historialDAO = new HistorialActividadDAO();
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

        // =========================================================
        // 1. CORRECCIÓN DEL MAPA (CRUCIAL para setCellValueFactory)
        // =========================================================
        // Limpiamos y rellenamos el mapa para que contenga todas las categorías (incluida la nueva)
        if (categoriaNamesMap == null) {
            categoriaNamesMap = new HashMap<>(); // Inicializar si es null
        }
        categoriaNamesMap.clear();

        Categoria sinCategoria = new Categoria(0, "-- Sin Categoría --", "");

        // 🚨 CLAVE: Llenar el mapa con ID y Nombre para la visualización de la grilla
        categoriaNamesMap.put(0, sinCategoria.getNombre());
        for (Categoria c : categoriasDB) {
            categoriaNamesMap.put(c.getIdCategoria(), c.getNombre());
        }

        // =========================================================
        // 2. CORRECCIÓN DE LA LISTA OBSERVABLE (CRUCIAL para ComboBoxTableCell)
        // =========================================================
        // Usamos clear() y addAll() para mantener la misma referencia,
        // que es la que usa ComboBoxTableCell.forTableColumn(...).
        if (categoriaNamesObservableList == null) {
            categoriaNamesObservableList = FXCollections.observableArrayList(); // Inicializar si es null
        }
        categoriaNamesObservableList.clear();

        // Rellenamos con la opción "Sin Categoría" y luego los nombres reales
        categoriaNamesObservableList.add(sinCategoria.getNombre());

        List<String> realNames = categoriasDB.stream()
                .map(Categoria::getNombre)
                .collect(Collectors.toList());

        categoriaNamesObservableList.addAll(realNames);


        // =========================================================
        // 3. Manejo de la lista de FILTRO (Simplificado y corregido)
        // =========================================================
        if (categoriaFilterList == null) {
            categoriaFilterList = FXCollections.observableArrayList();
        }
        categoriaFilterList.clear();

        Categoria todos = new Categoria(-1, "Todos", "");
        Categoria sinCategoriaFilter = new Categoria(0, "-- Sin Categoría --", "");

        categoriaFilterList.add(todos);
        categoriaFilterList.add(sinCategoriaFilter);
        categoriaFilterList.addAll(categoriasDB);

        // Eliminamos la lógica compleja de reordenamiento al final,
        // ya que al limpiar y agregar en el orden deseado, se garantiza la posición.
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

    // En app.controller.ProductoMenuController.java

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
            newStage.setHeight(screenHeight);
            newStage.sizeToScene();

            // 5. Configurar el modo (modal) y mostrar
            newStage.setTitle("Registro de Nuevo Producto");
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            newStage.setResizable(false);

            // Mostrar la nueva ventana y esperar a que se cierre (modal)
            newStage.showAndWait();

            // 🚨 MODIFICACIÓN CLAVE: Sincronizar categorías y productos 🚨

            // 6. Recargar la lista de categorías (y el mapa categoriaNamesMap)
            // Esto es necesario para que la nueva categoría esté disponible.
            loadCategoriaLists();

            // 7. Actualizar el ComboBox de filtro con la nueva lista
            // Esto es vital porque loadCategoriaLists() reasigna la lista.
            cmbCategoriaFilter.setItems(categoriaFilterList);

            // 8. Recargar la tabla de productos, que ahora usará el mapa actualizado
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
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Por favor, seleccione una fila y modifique los datos antes de guardar.");
            return;
        }

        Producto productoOriginal = null;
        try {
            // **CLAVE:** Obtener el estado ORIGINAL del producto desde la DB antes de la modificación.
            productoOriginal = productoDAO.getProductoById(selectedProducto.getIdProducto());

            if (productoOriginal == null) {
                showAlert(Alert.AlertType.ERROR, "Error de Datos", "No se encontraron datos originales para el producto ID: " + selectedProducto.getIdProducto() + ". No se pudo guardar.");
                loadProductos();
                return;
            }

            // 2. Intentar actualizar el producto en la BD (el DAO retorna true solo si hubo cambios)
            boolean exito = productoDAO.updateProducto(selectedProducto);

            if (exito) {
                // 3. REGISTRAR ACTIVIDAD - solo si la modificación fue exitosa en la DB
                int loggedInUserId = app.controller.SessionManager.getInstance().getLoggedInUserId(); // Asume que SessionManager está disponible

                // --- Comparar y Registrar Nombre ---
                if (!productoOriginal.getNombreProducto().equals(selectedProducto.getNombreProducto())) {
                    historialDAO.insertarRegistro(
                            loggedInUserId, "Producto", "nombreProducto", selectedProducto.getIdProducto(),
                            productoOriginal.getNombreProducto(), selectedProducto.getNombreProducto()
                    );
                }

                // --- Comparar y Registrar Descripción ---
                if (!productoOriginal.getDescripcion().equals(selectedProducto.getDescripcion())) {
                    historialDAO.insertarRegistro(
                            loggedInUserId, "Producto", "descripcion", selectedProducto.getIdProducto(),
                            productoOriginal.getDescripcion(), selectedProducto.getDescripcion()
                    );
                }

                // --- Comparar y Registrar Precio ---
                if (productoOriginal.getPrecio() != selectedProducto.getPrecio()) {
                    historialDAO.insertarRegistro(
                            loggedInUserId, "Producto", "precio", selectedProducto.getIdProducto(),
                            String.valueOf(productoOriginal.getPrecio()), String.valueOf(selectedProducto.getPrecio())
                    );
                }

                // --- Comparar y Registrar Stock ---
                if (productoOriginal.getStock() != selectedProducto.getStock()) {
                    historialDAO.insertarRegistro(
                            loggedInUserId, "Producto", "stock", selectedProducto.getIdProducto(),
                            String.valueOf(productoOriginal.getStock()), String.valueOf(selectedProducto.getStock())
                    );
                }

                // --- Comparar y Registrar Categoría ---
                if (productoOriginal.getIdCategoria() != selectedProducto.getIdCategoria()) {
                    // Obtener los nombres de las categorías para un historial más legible
                    String nombreCatOriginal = categoriaNamesMap.getOrDefault(productoOriginal.getIdCategoria(), "N/A");
                    String nombreCatNuevo = categoriaNamesMap.getOrDefault(selectedProducto.getIdCategoria(), "N/A");

                    historialDAO.insertarRegistro(
                            loggedInUserId, "Producto", "idCategoria", selectedProducto.getIdProducto(),
                            nombreCatOriginal, nombreCatNuevo
                    );
                }

                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Producto modificado y registrado en el historial exitosamente.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Sin Cambios Detectados", "El producto seleccionado no ha sido modificado o los datos son idénticos a los actuales.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error de Base de Datos", "Ocurrió un error al intentar modificar el producto: " + e.getMessage());
            e.printStackTrace();
        }

        // 4. Recargar la tabla para asegurar la sincronización y refrescar la vista
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

    @FXML
    private void handleHelpButton() {
        // Creamos una nueva alerta de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuramos el título y los encabezados del mensaje
        alert.setTitle("Ayuda - Menu de Productos Registrados");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite La Visualizacion y Modificación de Los datos de Productos Registrados:\n"
                + "\n"
                + "1. Visualización y Edición: Modifique directamente los campos de la tabla (Nombre del Producto, Descripcion, Precio y Stock) Al hacer doble click en la Columna.\n"
                + "----------------------------------------------------------------------\n"
                + "2. Para Modificar la Categoría Haga Click y luego Seleccione la opcion requerida en el ChoiceBox.\n"
                + "----------------------------------------------------------------------\n"
                + "3. Para Registrar un nuevo Producto Haga Click en el boton Registrar Nuevo Producto.\n"
                + "----------------------------------------------------------------------\n"
                + "4. Para Actualizar o Reiniciar la Tabla haga click en el boton Refrescar.\n"
                + "----------------------------------------------------------------------\n"
                + "5. Filtros: Utilice el campo de texto para buscar usuarios por Nombre o Descripción, y el *ChoiceBox* para filtrar por Estado (Activo/Inactivo).\n"
                + "----------------------------------------------------------------------\n"
                + "8. Guardar Cambios: El botón 'Modificar Producto' aplica todas las modificaciones realizadas en las celdas de la tabla a la base de datos.\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();
    }
}