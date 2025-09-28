package app.controller;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import app.controller.SessionManager;

public class MenuController {

    @FXML
    public void handleStock(ActionEvent event){
        try {
            // Carga la nueva ventana de registro
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Cargar el nuevo FXML en el mismo Stage
            Parent root = FXMLLoader.load(getClass().getResource("/menuStock.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);

            stage.setWidth(1800);
            stage.setHeight(1000);

            // Centra la ventana en la pantalla (opcional)
            stage.centerOnScreen();
            // Actualizar el título y mostrar la ventana
            stage.setTitle("Menú de Stock");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleStockabm(ActionEvent event){
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // Carga la nueva ventana de registro
            Parent root = FXMLLoader.load(getClass().getResource("/menuAbmStock.fxml"));

            stage.setTitle("Menu de Proveedor");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(1800);
            stage.setHeight(1000);

            // Centra la ventana en la pantalla (opcional)
            stage.centerOnScreen();
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleProveedor(ActionEvent event){
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Carga la nueva ventana de registro
            Parent root = FXMLLoader.load(getClass().getResource("/menuProveedor.fxml"));

            stage.setTitle("Menu de Proveedor");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(1800);
            stage.setHeight(1000);

            // Centra la ventana en la pantalla (opcional)
            stage.centerOnScreen();
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegistroButton(ActionEvent event) {
        try {
            // Carga la nueva ventana de registro
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/menuCliente.fxml"));
            stage.setTitle("Registro de Cliente");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(1800);
            stage.setHeight(1000);

            // Centra la ventana en la pantalla (opcional)
            stage.centerOnScreen();
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleIniciodeSesionButton(ActionEvent event) {
        try {
            // Carga la nueva ventana de registro
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/inicioSesion.fxml"));
            stage.setTitle("Inicio de sesion");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(1800);
            stage.setHeight(1000);

            // Centra la ventana en la pantalla (opcional)
            stage.centerOnScreen();
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegistrodeNuevoEmpleadoButton(ActionEvent event) {
        try {
            // Carga la nueva ventana de registro
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/registroEmpleado.fxml"));
            stage.setTitle("Registro de Empleado");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(1800);
            stage.setHeight(1000);

            // Centra la ventana en la pantalla (opcional)
            stage.centerOnScreen();
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void handleOpcionesDeAdminButton(ActionEvent event) {

        // 1. Obtener el ID del usuario logueado
        int userIdType = SessionManager.getInstance().getLoggedInUserIdType();

        // 2. Verificar si el ID es 4 (Administrador)
        if (userIdType == 4) {
            // Si es ID 4, permite el acceso y carga la ventana
            try {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // Carga la nueva ventana de Admin
                Parent root = FXMLLoader.load(getClass().getResource("/MenuAdmin.fxml"));
                stage.setTitle("Menú de Admin");
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setWidth(1800);
                stage.setHeight(1000);

                // Centra la ventana en la pantalla (opcional)
                stage.centerOnScreen();
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Si NO es ID 4, deniega el acceso y muestra una alerta
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Acceso Denegado");
            alert.setHeaderText(null);
            alert.setContentText("No tienes permiso para acceder. Esta sección está restringida al usuario Administrador");
            alert.showAndWait();
        }
    }

    @FXML
    public void handleGestionDeUsuariosButton(ActionEvent event) {
        try {
            // Carga el FXML de la tabla de usuarios
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionUsuarios.fxml"));
            Parent root = loader.load();

            // Carga la nueva escena
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Gestión de Usuarios Empleados");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverButton(ActionEvent event) {
        try {
            // Carga el FXML de la pantalla a la que quieres regresar.
            // Asegúrate de que la ruta sea correcta.
            Parent root = FXMLLoader.load(getClass().getResource("/menuAbms.fxml"));

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
    @FXML
    private void handleVolverButtonAbms(ActionEvent event) {
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
    @FXML
    private void handleVolverButtonStock(ActionEvent event) {
        try {
            // Carga el FXML de la pantalla a la que quieres regresar.
            // Asegúrate de que la ruta sea correcta.
            Parent root = FXMLLoader.load(getClass().getResource("/menuAbms.fxml"));

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