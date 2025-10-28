package app.controller;

import app.dao.CategoriaDAO;
import app.model.Categoria;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegistroCategoriaController {

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private Label lblMensajeError;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private ProductoController productoController; // Referencia al controlador padre

    /**
     * Establece la referencia al controlador de Producto para poder notificarle el cambio.
     */
    public void setProductoController(ProductoController controller) {
        this.productoController = controller;
    }

    @FXML
    public void initialize() {
        lblMensajeError.setText("");
    }

    @FXML
    private void handleRegistrar(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        // 1. Validación de campo vacío
        if (nombre.isEmpty()) {
            lblMensajeError.setText("El nombre de la categoría es obligatorio.");
            return;
        }

        // 2. Validación de unicidad del nombre (Asumiendo que el DAO tiene este método)
        // Nota: Si el método es 'isNombreCategoriaDuplicated(String nombre, int idToExclude)', usa 0.
        // Aquí asumimos un método simple para el registro.
        if (categoriaDAO.isNombreCategoriaDuplicated(nombre)) {
            lblMensajeError.setText("Ya existe una categoría con el nombre: " + nombre);
            return;
        }

        Categoria nuevaCategoria = new Categoria(0, nombre, descripcion);

        try {
            if (categoriaDAO.saveCategoria(nuevaCategoria)) {

                // Muestra un mensaje de éxito (opcional, pero buena práctica)
                mostrarAlertaExito("Categoría '" + nombre + "' registrada con éxito.");

                // 1. Notificar al controlador de productos para que recargue el ComboBox
                if (productoController != null) {
                    // Aseguramos que la nuevaCategoria tenga el ID asignado por el DAO
                    productoController.refreshCategorias(nuevaCategoria.getIdCategoria());
                }

                // 2. Cerrar la ventana modal
                handleCancelar(event);

            } else {
                lblMensajeError.setText("Error al guardar la categoría en la base de datos.");
            }
        } catch (Exception e) {
            lblMensajeError.setText("Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        // Cierra la ventana actual (que debe ser modal)
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Muestra un mensaje de éxito.
     */
    private void mostrarAlertaExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void handleHelpButton() {
        // Creamos una nueva alerta de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuramos el título y los encabezados del mensaje
        alert.setTitle("Ayuda - Menu De Creación de nueva Categoría de Insumo");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite la Creacion de una nueva categoría de Insumo en la Base de Datos :\n"
                + "\n"
                + "1. Ingrese Los Datos Correctos para Continuar.\n"
                + "----------------------------------------------------------------------\n"
                + "2. Para Continuar Haga Click en Guardar o Para Cancelar el Registro Haga Click en Cancelar.\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();
    }
}