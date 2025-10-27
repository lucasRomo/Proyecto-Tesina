package app.controller;

import app.model.Usuario;
import app.dao.UsuarioDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.IOException;

public class IniciodeSesionController {

    // Campos FXML originales
    @FXML private TextField UsuarioField;
    @FXML private PasswordField ContraseniaField;

    // Campos FXML para el control de visibilidad
    @FXML private TextField ContraseniaVisibleField;
    @FXML private ToggleButton toggleVisibilityButton;
    @FXML private ImageView eyeIconView;

    // Rutas de tus imágenes
    private static final String EYE_OPEN_ICON = "/imagenes/ojo.png";
    private static final String EYE_CLOSED_ICON = "/imagenes/ojo (1).png";


    @FXML
    private void initialize() {
        // 1. Sincronizar el texto (se mantiene)
        ContraseniaVisibleField.textProperty().addListener((observable, oldValue, newValue) -> {
            ContraseniaField.setText(newValue);
        });

        ContraseniaField.textProperty().addListener((observable, oldValue, newValue) -> {
            ContraseniaVisibleField.setText(newValue);
        });
    }


    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        boolean isVisible = toggleVisibilityButton.isSelected();

        if (isVisible) {
            // Mostrar Contraseña: Ocultamos el PasswordField y mostramos el TextField
            ContraseniaField.setVisible(false);
            ContraseniaVisibleField.setVisible(true);
            // Cargar imagen de ojo abierto
            eyeIconView.setImage(new Image(getClass().getResource(EYE_OPEN_ICON).toExternalForm()));
        } else {
            // Ocultar Contraseña: Mostramos el PasswordField y ocultamos el TextField
            ContraseniaField.setVisible(true);
            ContraseniaVisibleField.setVisible(false);
            // Cargar imagen de ojo cerrado
            eyeIconView.setImage(new Image(getClass().getResource(EYE_CLOSED_ICON).toExternalForm()));
        }

        // Forzar el foco al campo actualmente visible
        if (isVisible) {
            ContraseniaVisibleField.requestFocus();
        } else {
            ContraseniaField.requestFocus();
        }
    }


    @FXML
    private void IniciarSesion(ActionEvent event) {
        String usuario = UsuarioField.getText().trim();
        String contrasena = ContraseniaField.getText().trim();

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        if (!usuario.isEmpty() && !contrasena.isEmpty()) {

            Usuario usuarioLogueado = usuarioDAO.obtenerUsuarioPorCredenciales(usuario, contrasena);

            if (usuarioLogueado != null) {

                // =========================================================
                // INICIO DE VALIDACIÓN DE ESTADO (NUEVO CÓDIGO)
                // =========================================================
                if ("desactivado".equalsIgnoreCase(usuarioLogueado.getEstado())) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Acceso Denegado");
                    alert.setHeaderText(null);
                    alert.setContentText("Su cuenta de usuario se encuentra Desactivada, No tiene Permitido Iniciar Sesion.");
                    alert.showAndWait();
                    return; // Detiene el proceso de inicio de sesión
                }
                // =========================================================
                // FIN DE VALIDACIÓN DE ESTADO
                // =========================================================

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
                    MenuController.loadScene(
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
            MenuController.loadScene(
                    (Node) event.getSource(),
                    "/menuInicial.fxml",
                    "Menú Principal"
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHelpButton() {
        // Creamos una nueva alerta de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuramos el título y los encabezados del mensaje
        alert.setTitle("Ayuda - Menu De Inicio de Sesion");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite el Ingreso de Datos para Iniciar Sesion:\n"
                + "\n"
                + "Ingrese el Nombre De Usuario y la Contraseña para Continuar.\n"
                + "----------------------------------------------------------------------\n"
                + "Ingrese el Nombre De Usuario y la Contraseña de Administrador para Habilitar el Menu Administrador.\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();
    }
}