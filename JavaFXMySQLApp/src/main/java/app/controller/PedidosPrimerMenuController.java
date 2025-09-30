package app.controller;

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

            stage.setScene(new Scene(root, 1800, 1000));

            stage.setTitle("Crear Pedido");

            stage.centerOnScreen();

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la vista 'Crear Pedido'.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void handleVerPedidos(ActionEvent event) {
        System.out.println("Intentando cargar la vista de pedidos activos...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/verPedidos.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root, 1800, 1000));

            stage.setTitle("Ver Pedidos (Activos)");

            stage.centerOnScreen();

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la vista 'Ver Pedidos'.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Maneja la acción del botón "Historial de Pedidos (Retirados)".
     * Carga la vista para ver solo los pedidos con estado "Retirado".
     */
    @FXML
    private void handleVerHistorial(ActionEvent event) {
        System.out.println("Intentando cargar la vista de historial de pedidos...");
        try {
            // Carga el nuevo FXML para el historial
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/verHistorialPedidos.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root, 1800, 1000));

            stage.setTitle("Historial de Pedidos (Retirados)");

            stage.centerOnScreen();

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la vista 'Historial de Pedidos'.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void handleVolverPedido(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuAbms.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root, 1800, 1000));

            stage.setTitle("Menú Principal");

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