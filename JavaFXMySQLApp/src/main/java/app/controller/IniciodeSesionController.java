package app.controller;

import app.model.Usuario;
import app.dao.UsuarioDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class IniciodeSesionController {
    @FXML
    private TextField UsuarioField;
    @FXML
    private TextField ContraseniaField;

    @FXML
    private void IniciarSesion(ActionEvent event) {
        String usuario = UsuarioField.getText().trim();
        String contrasena = ContraseniaField.getText().trim();

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        if (!usuario.isEmpty() && !contrasena.isEmpty()) {

            Usuario usuarioLogueado = usuarioDAO.obtenerUsuarioPorCredenciales(usuario, contrasena);

            if (usuarioLogueado != null) {

                SessionManager session = SessionManager.getInstance();
                session.setLoggedInUserPassword(usuarioLogueado.getContrasenia());
                session.setLoggedInUsername(usuarioLogueado.getUsuario());
                session.setLoggedInUserId(usuarioLogueado.getIdUsuario());
                session.setLoggedInUserIdType(usuarioLogueado.getIdTipoUsuario());


                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Inicio de Sesion Exitoso");
                alert.setHeaderText(null);
                alert.setContentText("Inicio de Sesión Correcto, Bienvenido");
                alert.showAndWait();

                try {
                    // LLAMADA CLAVE: Usa el método que maneja el tamaño fijo (1200x700)
                    MenuController.loadFixedSizeScene(
                            (Node) event.getSource(),
                            "/MenuAbms.fxml",
                            "Menú Principal"
                    );

                } catch (IOException e) {
                    e.printStackTrace();
                    Alert alertError = new Alert(Alert.AlertType.ERROR);
                    alertError.setTitle("Error de Carga");
                    alertError.setHeaderText(null);
                    alertError.setContentText("No se pudo cargar la pantalla del menú de administración.");
                    alertError.showAndWait();
                }

            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Inicio de Sesion Fallido");
                alert.setHeaderText(null);
                alert.setContentText("Usuario o Contraseña Incorrectos.");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Campos Incompletos");
            alert.setHeaderText(null);
            alert.setContentText("Por favor Rellene uno o ambos campos para continuar.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleVolverButton(ActionEvent event) {
        try {
            MenuController.loadFixedSizeScene(
                    (Node) event.getSource(),
                    "/menuInicial.fxml",
                    "Menú Principal"
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}