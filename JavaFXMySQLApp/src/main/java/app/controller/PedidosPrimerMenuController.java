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
        alert.setTitle("Ayuda - Gestión de Usuarios Empleados");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite la administración completa de las cuentas de usuario con rol de empleado. Las funciones principales incluyen:\n"
                + "\n"
                + "1. Visualización y Edición: Modifique directamente los campos de la tabla (Usuario, Contraseña, Salario, Dirección, etc) Al hacer doble click en la Columna.\n"
                + "----------------------------------------------------------------------\n"
                + "2. Filtros: Utilice el campo de texto para buscar usuarios por Nombre, Apellido o Usuario, y el *ChoiceBox* para filtrar por Estado (Activo/Inactivo).\n"
                + "----------------------------------------------------------------------\n"
                + "3. Ver Direccion: Para Visualizar o Modificar la Direccion Registrada del Ussuario haga CLick en el boton Ver Direccion.\n"
                + "----------------------------------------------------------------------\n"
                + "4. Guardar Cambios: El botón 'Guardar Cambios' aplica todas las modificaciones realizadas en las celdas de la tabla a la base de datos. (Se le Solicitará la contraseña del Administrador Nuevamente.\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();

    }

}