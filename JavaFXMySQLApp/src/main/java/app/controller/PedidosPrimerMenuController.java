package app.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;

import java.io.IOException;

public class PedidosPrimerMenuController {

    @FXML
    private void handlecrearPedido(ActionEvent event) {
        try {
            // Usar el método unificado para cargar la escena en la ventana principal
            MenuController.loadScene(
                    (Node) event.getSource(),
                    "/crearPedido.fxml",
                    "Crear Pedido"
            );
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la vista 'Crear Pedido'.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void handleVerPedidos(ActionEvent event) {
        System.out.println("Intentando cargar la vista de pedidos activos...");
        try {
            // Usar el método unificado para cargar la escena en la ventana principal
            MenuController.loadScene(
                    (Node) event.getSource(),
                    "/verPedidos.fxml",
                    "Ver Pedidos (Activos)"
            );
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
            // Usar el método unificado para cargar la escena en la ventana principal
            MenuController.loadScene(
                    (Node) event.getSource(),
                    "/verHistorialPedidos.fxml",
                    "Historial de Pedidos (Retirados)"
            );
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la vista 'Historial de Pedidos'.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void handleVolverPedido(ActionEvent event) {
        try {
            // Usar el método unificado para volver al menú anterior
            MenuController.loadScene(
                    (Node) event.getSource(),
                    "/menuAbms.fxml",
                    "Menú de ABMs" // Título corregido para reflejar el contenido del FXML
            );
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la vista 'Menú ABMs'.", Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void handleHelpButton() {
        // Creamos una nueva alerta de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuramos el título y los encabezados del mensaje
        alert.setTitle("Ayuda - Menu Para Opciones de Pedido");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite El Ingreso a los Siguientes Menues:\n"
                + "\n"
                + "Haga Click en el Boton Crear Pedido para Ingresar a Registro de Pedidos.\n"
                + "----------------------------------------------------------------------\n"
                + "Haga Click en el Boton Ver Pedidos para Ingresar al Menu de Visualizacion y Edicion de Pedidos .\n"
                + "----------------------------------------------------------------------\n"
                + "Haga Click en el Boton Historial de Pedidos para Ingresar al Menu de Historial de Pedidos Retirados.\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();
    }

}