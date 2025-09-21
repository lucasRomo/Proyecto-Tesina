package app.controller;

import java.io.IOException;

import app.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MenuController {

    @FXML
    public void handlePedidosButton(ActionEvent event) {
        try {
            // Carga la nueva ventana de registro
            Parent root = FXMLLoader.load(getClass().getResource("/pedidosInicial.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Menu de Proveedor");
            stage.setScene(new Scene(root, app.MainApp.WINDOW_WIDTH, app.MainApp.WINDOW_HEIGHT));
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
    public void handleClientesButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registroCliente.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, MainApp.WINDOW_WIDTH, MainApp.WINDOW_HEIGHT));
            stage.setTitle("Registro de Cliente");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleStockButton(ActionEvent event) {
        // Lógica para la vista de Stock
        System.out.println("Navegando a la vista de Stock");
    }

    @FXML
    public void handleAdminButton(ActionEvent event) {
        // Lógica para la vista de Admin
        System.out.println("Navegando a la vista de Admin");
    }
}
