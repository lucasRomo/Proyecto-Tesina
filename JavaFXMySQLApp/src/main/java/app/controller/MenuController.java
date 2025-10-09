package app.controller;

import app.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    // --- MÉTODO PARA NAVEGAR A CREAR PRODUCTO (ABM) ---
    // --------------------------------------------------------------------------------

    /**
     * Maneja la acción del botón "Crear Producto" y carga la vista de Creación de Productos.
     * La vista FXML esperada es /ProductoAbmView.fxml.
     */
    @FXML
    public void handleCrearProducto(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // Cargamos la vista de ABM de Producto
            Parent root = FXMLLoader.load(getClass().getResource("/ProductoAbmView.fxml"));
            stage.setTitle("Creación/Edición de Producto");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(1366);
            stage.setHeight(768);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista de Creación de Productos: ProductoAbmView.fxml no encontrado. Asegúrese de que el archivo existe.");
        }
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

            // Usar las dimensiones grandes
            Scene scene = new Scene(root, 1366, 768);

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

            // Usar las dimensiones grandes
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
            // Asumo que UsuariosEmpleadoController existe
            // UsuariosEmpleadoController usuariosController = loader.getController();
            // usuariosController.setLoggedInUserPassword(this.loggedInUserPassword);
            // usuariosController.setLoggedInUsername(this.loggedInUsername);
            // usuariosController.setLoggedInUserId(this.loggedInUserId);

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