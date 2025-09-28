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

            // Llamamos a un nuevo método que devuelve el objeto Usuario
            Usuario usuarioLogueado = usuarioDAO.obtenerUsuarioPorCredenciales(usuario, contrasena);


            // Verificamos si se devolvió un objeto Usuario (si no es null)
            if (usuarioLogueado != null) {

                SessionManager session = SessionManager.getInstance();
                session.setLoggedInUserPassword(usuarioLogueado.getContrasenia());
                session.setLoggedInUsername(usuarioLogueado.getUsuario());
                session.setLoggedInUserId(usuarioLogueado.getIdTipoUsuario());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Inicio de Sesion Exitoso");
                alert.setHeaderText(null);
                alert.setContentText("Inicio de Sesión Correcto, Bienvenido");
                alert.showAndWait();

                try {
                    // Carga el FXML de la pantalla de menú del administrador
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/MenuAbms.fxml"));
                    Parent root = loader.load();

                    // Obtenemos el controlador del menú de admin
                    MenuController menuAdminController = loader.getController();

                    // Obtener el 'Stage' (ventana) actual desde un elemento de la escena
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                    // Crear y establecer la nueva escena
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.setWidth(1800);
                    stage.setHeight(1000);

                    // Centra la ventana en la pantalla (opcional)
                    stage.centerOnScreen();
                    stage.setTitle("Menú Principal");
                    stage.show();

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
            // Carga el FXML de la pantalla a la que quieres regresar.
            // Asegúrate de que la ruta sea correcta.
            Parent root = FXMLLoader.load(getClass().getResource("/menuInicial.fxml"));

            // Obtiene la Stage (ventana) actual del botón
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Crea una nueva Scene con la pantalla anterior
            Scene scene = new Scene(root);

            // Reemplaza la Scene actual con la nueva
            stage.setScene(scene);
            stage.setTitle("Menú Principal"); // O el título de la pantalla anterior
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Maneja el error si no se puede cargar el archivo FXML
        }
    }
}