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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

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
     * Carga todas las categorías desde la base de datos y las añade al ComboBox.
     */
    private void cargarCategorias() {
        try {
            List<Categoria> listaCategorias = categoriaDAO.getAllCategorias();
            ObservableList<Categoria> categoriasObservable = FXCollections.observableArrayList(listaCategorias);

            // Permitir seleccionar "Ninguna Categoría" (representado por NULL / 0 en el DAO)
            Categoria ninguna = new Categoria(0, "-- Sin Categoría --", "Producto sin categoría asignada");
            categoriasObservable.add(0, ninguna); // Añadir al inicio de la lista

            cmbCategoria.setItems(categoriasObservable);
            cmbCategoria.getSelectionModel().select(0); // Seleccionar la opción "Sin Categoría" por defecto

        } catch (Exception e) {
            System.err.println("Error al cargar categorías: " + e.getMessage());
            lblMensajeError.setText("Error al cargar categorías. Verifique la conexión a la DB.");
        }
    }

    /**
     * Valida los datos obligatorios e intenta guardar el producto en la base de datos.
     * @param event Evento de acción del botón.
     */
    @FXML
    private void handleGuardarProducto(ActionEvent event) {
        // 1. Limpiar mensajes anteriores
        lblMensajeError.setText("");

        // 2. Obtener y validar datos OBLIGATORIOS (Nombre y Precio)
        String nombre = txtNombre.getText().trim();
        String precioStr = txtPrecio.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAlertaError("El nombre del producto es obligatorio.");
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

        // 3. Obtener datos NO OBLIGATORIOS
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

        // 4. Crear el objeto Producto
        Producto nuevoProducto = new Producto(
                nombre,
                descripcion,
                precio,
                stock,
                idCategoria
        );

        // 5. Guardar en la base de datos
        if (productoDAO.saveProducto(nuevoProducto)) {
            mostrarAlertaExito("Producto '" + nuevoProducto.getNombreProducto() + "' guardado con éxito.");
            limpiarCampos();
        } else {
            mostrarAlertaError("Error al guardar el producto en la base de datos.");
        }
    }

    /**
     * Muestra un mensaje de error en el Label.
     * @param mensaje El mensaje de error.
     */
    private void mostrarAlertaError(String mensaje) {
        lblMensajeError.setText(mensaje);
        // Opcional: mostrar un Alert si es necesario para el usuario
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Validación");
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
     * Maneja la acción del botón Volver.
     * @param event Evento de acción.
     */
    @FXML
    private void handleVolver(ActionEvent event) {
        try {
            // Asumiendo que /menuStock.fxml es el menú al que se debe volver
            Parent root = FXMLLoader.load(getClass().getResource("/menuStock.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menú Stock");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback en caso de no encontrar la vista
            System.err.println("Error al cargar la vista de retorno: /menuStock.fxml no encontrado.");
        }
    }
}