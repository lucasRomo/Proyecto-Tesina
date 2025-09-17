package app.controller;

import app.dao.TipoProveedorDAO;
import app.model.TipoProveedor;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class RegistroTipoProveedorController {

    @FXML
    private TextField descripcionField;

    private TipoProveedorDAO tipoProveedorDAO = new TipoProveedorDAO();

    @FXML
    private void handleRegistrar(ActionEvent event) {
        String descripcion = descripcionField.getText().trim();
        if (descripcion.isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, ingrese una descripción.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Paso 1: Verificar si ya existe un tipo de proveedor con la misma descripción
            TipoProveedor tipoExistente = tipoProveedorDAO.getTipoProveedorByDescription(descripcion);

            if (tipoExistente != null) {
                // Si el tipo ya existe, mostrar una alerta y no registrar
                mostrarAlerta("Advertencia", "Ya existe un tipo de proveedor con esa descripción. No se puede registrar dos veces el mismo.", Alert.AlertType.WARNING);
                return;
            }

            // Paso 2: Si no existe, proceder con la inserción
            TipoProveedor nuevoTipo = new TipoProveedor(0, descripcion);
            boolean exito = tipoProveedorDAO.insertarTipoProveedor(nuevoTipo);

            if (exito) {
                mostrarAlerta("Éxito", "Tipo de proveedor registrado correctamente.", Alert.AlertType.INFORMATION);
                Stage stage = (Stage) descripcionField.getScene().getWindow();
                stage.close();
            } else {
                mostrarAlerta("Error", "No se pudo registrar el tipo de proveedor.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Ocurrió un error en la base de datos.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        Stage stage = (Stage) descripcionField.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}