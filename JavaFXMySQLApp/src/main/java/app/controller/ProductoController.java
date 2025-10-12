package app.controller;

import app.dao.CategoriaDAO;
import app.dao.ProductoDAO;
import app.model.Categoria;
import app.model.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para la vista de creación y edición de productos (ABM).
 */
public class ProductoController {

    // --- Elementos FXML del Formulario ---
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private Label lblMensajeError;

    // --- DAOs ---
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    // --- Atributos de Edición (Si se implementa la edición) ---
    private Producto productoEnEdicion = null; // Usado para distinguir entre registro y edición

    /**
     * Se llama automáticamente después de que se cargan los elementos FXML.
     * Aquí inicializamos el ComboBox de categorías.
     */
    @FXML
    public void initialize() {
        cargarCategorias();
        // Inicializar el mensaje de error como vacío/oculto
        lblMensajeError.setText("");
    }

    /**
     * Método para cargar los datos de un producto si se está en modo edición.
     * (Este método no fue solicitado pero se incluye para una funcionalidad completa de la vista).
     * @param producto Producto a cargar en el formulario.
     */
    public void setProducto(Producto producto) {
        this.productoEnEdicion = producto;

        // 1. Cargar datos básicos
        txtNombre.setText(producto.getNombreProducto());
        txtDescripcion.setText(producto.getDescripcion());
        txtPrecio.setText(String.valueOf(producto.getPrecio()));
        txtStock.setText(String.valueOf(producto.getStock()));

        // 2. Seleccionar categoría
        int idCategoria = producto.getIdCategoria();
        Categoria categoriaActual = cmbCategoria.getItems().stream()
                .filter(c -> c.getIdCategoria() == idCategoria)
                .findFirst()
                .orElse(new Categoria()); // "Sin Categoría" si no se encuentra

        cmbCategoria.getSelectionModel().select(categoriaActual);
    }


    /**
     * Carga todas las categorías desde la base de datos y las añade al ComboBox.
     */
    private void cargarCategorias() {
        try {
            List<Categoria> listaCategorias = categoriaDAO.getAllCategorias();
            ObservableList<Categoria> categoriasObservable = FXCollections.observableArrayList(listaCategorias);

            // Permitir seleccionar "Ninguna Categoría" (representado por 0 en el DAO)
            Categoria ninguna = new Categoria(); // ID 0, Nombre "-- Sin Categoría --"
            categoriasObservable.add(0, ninguna);

            cmbCategoria.setItems(categoriasObservable);
            cmbCategoria.getSelectionModel().select(0); // Seleccionar la opción "Sin Categoría" por defecto

        } catch (Exception e) {
            System.err.println("Error al cargar categorías: " + e.getMessage());
            lblMensajeError.setText("Error al cargar categorías. Verifique la conexión a la DB.");
        }
    }

    //---------------------------------------------------------
    // Manejo de la Modal de Registro de Categoría
    //---------------------------------------------------------

    /**
     * Abre una ventana modal para registrar una nueva categoría.
     */
    @FXML
    private void handleRegistrarNuevaCategoriaButton(ActionEvent event) {
        try {
            // Se asume que el FXML es /RegistroCategoria.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RegistroCategoria.fxml"));
            Parent root = loader.load();

            // Pasar la referencia a este controlador para el callback
            RegistroCategoriaController controller = loader.getController();
            controller.setProductoController(this);

            Stage stage = new Stage();
            stage.setTitle("Registrar Nueva Categoría");
            stage.setScene(new Scene(root));

            // Configurar como MODAL
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlertaError("No se pudo cargar la vista de registro de categoría. Verifique la ruta del FXML.");
        }
    }

    /**
     * Método de callback llamado por RegistroCategoriaController al guardar exitosamente.
     * Recarga el ComboBox y selecciona la nueva categoría.
     * @param newCategoryId El ID de la nueva categoría registrada.
     */
    public void refreshCategorias(int newCategoryId) {
        cargarCategorias();

        // Intentar seleccionar la nueva categoría
        Categoria nueva = cmbCategoria.getItems().stream()
                .filter(c -> c.getIdCategoria() == newCategoryId)
                .findFirst()
                .orElse(null);

        if (nueva != null) {
            cmbCategoria.getSelectionModel().select(nueva);
        }
    }

    //---------------------------------------------------------
    // Métodos de ABM existentes
    //---------------------------------------------------------

    /**
     * Valida los datos obligatorios e intenta guardar (o actualizar) el producto en la base de datos.
     * @param event Evento de acción del botón.
     */
    @FXML
    private void handleGuardarProducto(ActionEvent event) {
        // 1. Limpiar mensajes anteriores
        lblMensajeError.setText("");

        // 2. Obtener y validar datos OBLIGATORIOS (Nombre y Precio)
        String nombre = txtNombre.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        int idProductoActual = (productoEnEdicion != null) ? productoEnEdicion.getIdProducto() : 0;

        if (nombre.isEmpty()) {
            mostrarAlertaError("El nombre del producto es obligatorio.");
            return;
        }

        // 3. VALIDACIÓN DE UNICIDAD DEL NOMBRE
        if (productoDAO.isNombreProductoDuplicated(nombre, idProductoActual)) {
            mostrarAlertaError("Ya existe un producto registrado con el nombre: " + nombre);
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
            if (precio <= 0) {
                mostrarAlertaError("El precio debe ser un valor positivo.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlertaError("Formato de precio inválido. Use solo números y punto decimal (ej: 12.50).");
            return;
        }

        // 4. Obtener datos NO OBLIGATORIOS
        String descripcion = txtDescripcion.getText().trim();
        String stockStr = txtStock.getText().trim();

        int stock = 0; // Por defecto, si está vacío
        if (!stockStr.isEmpty()) {
            try {
                stock = Integer.parseInt(stockStr);
                if (stock < 0) {
                    mostrarAlertaError("El stock no puede ser negativo.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarAlertaError("Formato de stock inválido. Use solo números enteros.");
                return;
            }
        }

        Categoria categoriaSeleccionada = cmbCategoria.getSelectionModel().getSelectedItem();
        int idCategoria = 0; // 0 significa NULL en el DAO (opcional)
        if (categoriaSeleccionada != null) {
            idCategoria = categoriaSeleccionada.getIdCategoria();
        }

        // 5. Crear o Actualizar el objeto Producto
        Producto productoAProcesar;
        String mensajeExito;

        if (productoEnEdicion == null) {
            // MODO REGISTRO
            productoAProcesar = new Producto(nombre, descripcion, precio, stock, idCategoria);
            if (productoDAO.saveProducto(productoAProcesar)) {
                mensajeExito = "Producto '" + productoAProcesar.getNombreProducto() + "' guardado con éxito.";
                mostrarAlertaExito(mensajeExito);
                // *** CORRECCIÓN: Cerrar la modal después del registro exitoso ***
                handleVolver(event);
            } else {
                mostrarAlertaError("Error al guardar el producto en la base de datos.");
            }
        } else {
            // MODO EDICIÓN
            productoEnEdicion.setNombreProducto(nombre);
            productoEnEdicion.setDescripcion(descripcion);
            productoEnEdicion.setPrecio(precio);
            productoEnEdicion.setStock(stock);
            productoEnEdicion.setIdCategoria(idCategoria);

            if (productoDAO.updateProducto(productoEnEdicion)) {
                mensajeExito = "Producto '" + productoEnEdicion.getNombreProducto() + "' actualizado con éxito.";
                mostrarAlertaExito(mensajeExito);
                // Cerrar la ventana después de la edición exitosa
                handleVolver(event);
            } else {
                mostrarAlertaError("Error al actualizar el producto en la base de datos.");
            }
        }
    }

    /**
     * Muestra un mensaje de error en el Label y un Alert.
     * @param mensaje El mensaje de error.
     */
    private void mostrarAlertaError(String mensaje) {
        lblMensajeError.setText(mensaje);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Validación/Guardado");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra un mensaje de éxito.
     * @param mensaje El mensaje de éxito.
     */
    private void mostrarAlertaExito(String mensaje) {
        lblMensajeError.setText(""); // Limpia cualquier error anterior
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Limpia todos los campos del formulario.
     */
    private void limpiarCampos() {
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        cmbCategoria.getSelectionModel().select(0); // Vuelve a seleccionar "Sin Categoría"
        lblMensajeError.setText("");
    }


    /**
     * Maneja la acción del botón Volver/Cancelar. Cierra la ventana actual (modal).
     *
     * NOTA: Se corrige el comportamiento para simplemente cerrar la ventana,
     * ya que esta vista se abre como una MODAL.
     *
     * @param event Evento de acción.
     */
    @FXML
    private void handleVolver(ActionEvent event) {
        // Cierra la ventana actual
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}