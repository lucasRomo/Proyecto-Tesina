package app.controller;

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
            stage.setScene(new Scene(root));
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}