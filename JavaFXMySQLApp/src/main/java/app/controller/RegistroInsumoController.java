package app.controller;

import app.dao.InsumoDAO;
import app.dao.TipoProveedorDAO;
import app.model.Insumo;
import app.model.TipoProveedor;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RegistroInsumoController {

    @FXML private TextField nombreField;
    @FXML private TextArea descripcionArea;
    @FXML private TextField stockMinimoField;
    @FXML private TextField stockActualField;
    @FXML private ChoiceBox<TipoProveedor> tipoProveedorChoiceBox;

    private InsumoDAO insumoDAO;
    private TipoProveedorDAO tipoProveedorDAO;
    private StockController stockController;

    public RegistroInsumoController() {
        this.insumoDAO = new InsumoDAO();
        this.tipoProveedorDAO = new TipoProveedorDAO();
    }

    @FXML
    private void initialize() {
        cargarTiposDeProveedor();
    }

    public void setStockController(StockController stockController) {
        this.stockController = stockController;
    }


    private void cargarTiposDeProveedor() {
        tipoProveedorChoiceBox.getItems().clear();
        try {
            tipoProveedorChoiceBox.getItems().addAll(tipoProveedorDAO.getAllTipoProveedores());
        } catch (SQLException e) {
            // Maneja la excepción si ocurre un error en la base de datos
            e.printStackTrace();
            mostrarAlerta("Error de Carga", "No se pudieron cargar los tipos de proveedor. Verifique la conexión a la base de datos.", Alert.AlertType.ERROR);
        }
    }



    @FXML
    private void handleGuardarInsumoButton(ActionEvent event) {
        if (validarCampos()) {
            String nombre = nombreField.getText().trim();
            String descripcion = descripcionArea.getText().trim();
            int stockMinimo = Integer.parseInt(stockMinimoField.getText());
            int stockActual = Integer.parseInt(stockActualField.getText());

            TipoProveedor tipoProveedorSeleccionado = tipoProveedorChoiceBox.getValue();
            int idTipoProveedor = tipoProveedorSeleccionado.getId();

            Insumo nuevoInsumo = new Insumo(0, nombre, descripcion, stockMinimo, stockActual, "Activo", idTipoProveedor);

            if (insumoDAO.insertarInsumo(nuevoInsumo)) {
                mostrarAlerta("Éxito", "Insumo registrado correctamente.", Alert.AlertType.INFORMATION);
                stockController.refreshInsumosTable();
                closeWindow();
            } else {
                mostrarAlerta("Error", "No se pudo registrar el insumo en la base de datos.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleCancelarButton(ActionEvent event) {
        closeWindow();
    }

    private boolean validarCampos() {
        String nombre = nombreField.getText().trim();
        String descripcion = descripcionArea.getText().trim();
        String stockMinimo = stockMinimoField.getText().trim();
        String stockActual = stockActualField.getText().trim();

        if (nombre.isEmpty() || descripcion.isEmpty() || stockMinimo.isEmpty() || stockActual.isEmpty() || tipoProveedorChoiceBox.getValue() == null) {
            mostrarAlerta("Advertencia", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return false;
        }

        if (!esNumero(stockMinimo) || Integer.parseInt(stockMinimo) < 0) {
            mostrarAlerta("Advertencia", "El stock mínimo debe ser un número entero no negativo.", Alert.AlertType.WARNING);
            return false;
        }

        if (!esNumero(stockActual) || Integer.parseInt(stockActual) < 0) {
            mostrarAlerta("Advertencia", "El stock actual debe ser un número entero no negativo.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean esNumero(String texto) {
        try {
            Integer.parseInt(texto);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) nombreField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleHelpButton() {
        // Creamos una nueva alerta de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuramos el título y los encabezados del mensaje
        alert.setTitle("Ayuda - Menu De Creación de Insumo");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite la Creacion de un Insumo en la Base de Datos :\n"
                + "\n"
                + "1. Ingrese Los Datos Correctos para Continuar.\n"
                + "----------------------------------------------------------------------\n"
                + "2. Para Seleccionar el Tipo de Proveedor haga Click en el *ChoiceBox* y Seleccione una de las opciones Para Continuar.\n"
                + "----------------------------------------------------------------------\n"
                + "3. Para Continuar Haga Click en Guardar Insumo o Para Cancelar el Registro Haga Click en Cancelar.\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();
    }
}