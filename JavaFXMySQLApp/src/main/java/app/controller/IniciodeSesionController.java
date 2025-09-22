package app.controller;

import app.model.Usuario;
import app.dao.UsuarioDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class IniciodeSesionController {
    @FXML
    private TextField UsuarioField;
    @FXML
    private TextField ContraseniaField;


    // Desde Aca //
    @FXML
    private void IniciarSesion() {

        String Usuario = UsuarioField.getText().trim();
        String Contrasenia = ContraseniaField.getText().trim();

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        if (!Usuario.isEmpty() && !Contrasenia.isEmpty()) {
            Usuario usuario = new Usuario(Usuario, Contrasenia);

            if (usuarioDAO.verificarUsuario(Usuario, Contrasenia)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Inicio de Sesion Exitoso");
                alert.setHeaderText(null);
                alert.setContentText("Inicio de Sesión Correcto, Bienvenido");
                alert.showAndWait();

                try {
                    // Obtener el 'Stage' (ventana) actual desde un elemento de la escena
                    Stage stage = (Stage) UsuarioField.getScene().getWindow();
                    // Cargar el nuevo archivo FXML
                    Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/menuAbms.fxml")));

                    // Crear y establecer la nueva escena
                    Scene scene = new Scene(root, app.MainApp.WINDOW_WIDTH, app.MainApp.WINDOW_HEIGHT);
                    stage.setScene(scene);
                    stage.setTitle("Menú Principal");
                    stage.show();


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else{
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
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
    }