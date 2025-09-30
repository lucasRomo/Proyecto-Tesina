package app.controller;

import app.MainApp; // Asumo que MainApp.WINDOW_WIDTH y WINDOW_HEIGHT existen en tu proyecto.
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button; // Se usa en Balles, aunque Lucas no los usa en los atributos.
import javafx.stage.Stage;

import java.io.IOException;

public class MenuController {

    // --- Atributos de Lucas (Información del usuario logueado) ---
    private String loggedInUserPassword;
    private String loggedInUsername;
    private int loggedInUserId;

    // --- Atributos FXML de Balles (Aunque no son esenciales para la lógica, se mantienen si están en el FXML) ---
    @FXML
    private Button pedidosButton;
    @FXML
    private Button clientesButton;
    @FXML
    private Button stockButton;
    @FXML
    private Button adminButton;

    // --- Setters de Lucas para la información del usuario ---
    public void setLoggedInUserPassword(String password) {
        this.loggedInUserPassword = password;
    }

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
    }

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    // --------------------------------------------------------------------------------
    // --- Métodos de Navegación de Balles (Integrados y Priorizados) ---
    // --------------------------------------------------------------------------------

    @FXML
    public void handlePedidosButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PedidosPrimerMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();

            // Usar las dimensiones estándar de MainApp, si existen, como en Balles
            // Si no existen, puedes usar las dimensiones de Lucas (1800x1000)
            // Asumiendo que MainApp existe:
            Scene scene = new Scene(root, 1366, 768); // Usando las dimensiones grandes de Lucas

            stage.setScene(scene);
            stage.setTitle("Menú de Pedidos");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Se fusiona handleClientesButton de Balles con handleRegistroButton de Lucas
    @FXML
    public void handleClientesButton(ActionEvent event) {
        try {
            // Carga la nueva ventana de registro
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuCliente.fxml")); // Ruta de Lucas
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Usar las dimensiones grandes de Lucas
            Scene scene = new Scene(root, 1366, 768);

            stage.setScene(scene);
            stage.setTitle("Registro de Cliente");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --------------------------------------------------------------------------------
    // --- Métodos de Navegación de Lucas (Completos) ---
    // --------------------------------------------------------------------------------

    @FXML
    public void handleStock(ActionEvent event){
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/menuStock.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(1366);
            stage.setHeight(768);
            stage.centerOnScreen();
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
            Parent root = FXMLLoader.load(getClass().getResource("/menuAbmStock.fxml"));
            stage.setTitle("Menu de Proveedor"); // Nota: el título dice "Proveedor" pero el FXML es "AbmStock"
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(1366);
            stage.setHeight(768);
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
            Parent root = FXMLLoader.load(getClass().getResource("/menuProveedor.fxml"));
            stage.setTitle("Menu de Proveedor");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(1366);
            stage.setHeight(768);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleIniciodeSesionButton(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/inicioSesion.fxml"));
            stage.setTitle("Inicio de sesion");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(1366);
            stage.setHeight(768);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegistrodeNuevoEmpleadoButton(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/registroEmpleado.fxml"));
            stage.setTitle("Registro de Empleado");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(1366);
            stage.setHeight(768);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleOpcionesDeAdminButton(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/MenuAdmin.fxml"));
            stage.setTitle("Menú de Admin");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(1366);
            stage.setHeight(768);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleGestionDeUsuariosButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionUsuarios.fxml"));
            Parent root = loader.load();

            // Pasar datos del usuario logueado al controlador de la gestión de usuarios
            UsuariosEmpleadoController usuariosController = loader.getController();
            usuariosController.setLoggedInUserPassword(this.loggedInUserPassword);
            usuariosController.setLoggedInUsername(this.loggedInUsername);
            usuariosController.setLoggedInUserId(this.loggedInUserId);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Gestión de Usuarios Empleados");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Métodos de Volver de Lucas ---

    @FXML
    private void handleVolverButton(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/menuAbms.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menú ABMs"); // Título ajustado
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverButtonAbms(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/menuInicial.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menú Principal");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverButtonStock(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/menuAbms.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menú ABMs"); // Título ajustado
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}