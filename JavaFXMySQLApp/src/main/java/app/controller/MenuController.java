package app.controller;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MenuController {

    @FXML
    public void handleStock(ActionEvent event){
        try {
            // Carga la nueva ventana de registro
            Parent root = FXMLLoader.load(getClass().getResource("/menuStock.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Menu de Proveedor");
            stage.setScene(new Scene(root, app.MainApp.WINDOW_WIDTH2, app.MainApp.WINDOW_HEIGHT));
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleStockabm(ActionEvent event){
        try {
            // Carga la nueva ventana de registro
            Parent root = FXMLLoader.load(getClass().getResource("/menuAbmStock.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Menu de Proveedor");
            stage.setScene(new Scene(root, app.MainApp.WINDOW_WIDTH, app.MainApp.WINDOW_HEIGHT));
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleProveedor(ActionEvent event){
        try {
            // Carga la nueva ventana de registro
            Parent root = FXMLLoader.load(getClass().getResource("/menuProveedor.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Menu de Proveedor");
            stage.setScene(new Scene(root, app.MainApp.WINDOW_WIDTH2, app.MainApp.WINDOW_HEIGHT));
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegistroButton(ActionEvent event) {
        try {
            // Carga la nueva ventana de registro
            Parent root = FXMLLoader.load(getClass().getResource("/menuCliente.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Registro de Cliente");
            stage.setScene(new Scene(root, app.MainApp.WINDOW_WIDTH2, app.MainApp.WINDOW_HEIGHT));
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleIniciodeSesionButton(ActionEvent event) {
        try {
            // Carga la nueva ventana de registro
            Parent root = FXMLLoader.load(getClass().getResource("/inicioSesion.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Inicio de sesion");
            stage.setScene(new Scene(root, app.MainApp.WINDOW_WIDTH, app.MainApp.WINDOW_HEIGHT));
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegistrodeNuevoEmpleadoButton(ActionEvent event) {
        try {
            // Carga la nueva ventana de registro
            Parent root = FXMLLoader.load(getClass().getResource("/registroEmpleado.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Registro de Empleado");
            stage.setScene(new Scene(root, app.MainApp.WINDOW_WIDTH, app.MainApp.WINDOW_HEIGHT));
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void handleOpcionesDeAdminButton(ActionEvent event) {
        try {
            // Carga la nueva ventana de registro
            Parent root = FXMLLoader.load(getClass().getResource("/MenuAdmin.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Menú de Admin");
            stage.setScene(new Scene(root, app.MainApp.WINDOW_WIDTH, app.MainApp.WINDOW_HEIGHT));
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleGestionDeUsuariosButton(ActionEvent event) {
        try {
            // Carga la nueva ventana de registro
            Parent root = FXMLLoader.load(getClass().getResource("/GestionUsuarios.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Gestión de Usuarios");
            stage.setScene(new Scene(root, app.MainApp.WINDOW_WIDTH2, app.MainApp.WINDOW_HEIGHT));
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverButton(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
}