package app.controller;

import app.MainApp;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MenuController {

    @FXML
    public void handleRegistroButton(ActionEvent event) {
        try {
            // Carga la nueva ventana de registro
            Parent root = FXMLLoader.load(getClass().getResource("/registroCliente.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Registro de Cliente");
            stage.setScene(new Scene(root, app.MainApp.WINDOW_WIDTH, app.MainApp.WINDOW_HEIGHT));
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleIniciodeSesionButton(ActionEvent event) {
        try {
            // Carga la nueva ventana de registro
            Parent root = FXMLLoader.load(getClass().getResource("/menuInicial2.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Registro de Cliente");
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
            Parent root = FXMLLoader.load(getClass().getResource("/menuinicial3.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Registro de Cliente");
            stage.setScene(new Scene(root, app.MainApp.WINDOW_WIDTH, app.MainApp.WINDOW_HEIGHT));
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}