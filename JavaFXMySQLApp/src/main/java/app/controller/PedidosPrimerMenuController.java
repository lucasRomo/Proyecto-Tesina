package app.controller;

import app.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class PedidosPrimerMenuController {

    @FXML
    private void handlecrearPedido(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/crearPedido.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // CORRECCIÓN: Usar 1800x1000 en lugar de constantes
            stage.setScene(new Scene(root, 1800, 1000));

            stage.setTitle("Crear Pedido");

            // CORRECCIÓN: Centrar la ventana
            stage.centerOnScreen();

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleVerPedidos(ActionEvent event) {
        System.out.println("Intentando cargar la vista de pedidos...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/verPedidos.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // CORRECCIÓN: Usar 1800x1000 en lugar de constantes
            stage.setScene(new Scene(root, 1800, 1000));

            stage.setTitle("Ver Pedidos");

            // CORRECCIÓN: Centrar la ventana
            stage.centerOnScreen();

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleVolverPedido(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuAbms.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // CORRECCIÓN: Usar 1800x1000 en lugar de constantes
            stage.setScene(new Scene(root, 1800, 1000));

            stage.setTitle("Menú Principal");

            // CORRECCIÓN: Centrar la ventana
            stage.centerOnScreen();

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}