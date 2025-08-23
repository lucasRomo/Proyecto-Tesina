package app.controller;

import app.model.Usuario;
import app.model.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class RegistrarNuevoEmpleadoController {
    @FXML private TextField UsuarioField;
    @FXML private TextField ContraseniaField;

    @FXML
    private void guardarUsuario() {
        String Usuario = UsuarioField.getText().trim();
        String Contrasenia = ContraseniaField.getText().trim();

        if (Usuario.isEmpty() || Contrasenia.isEmpty()) {
            mostrarAlerta("Error", "Debe completar todos los campos");
            return;
        }

        Usuario usuario = new Usuario(Usuario, Contrasenia);
        if (UsuarioDAO.insertar(usuario)) {
            mostrarAlerta("Éxito", "Usuario registrado correctamente");
            UsuarioField.clear();
            ContraseniaField.clear();
        } else {
            mostrarAlerta("Error", "No se pudo registrar el usuario");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
