package app.controller;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene; // Importar Scene explícitamente si no está
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

import app.controller.SessionManager; // Asegúrate de que esto sea correcto

public class MenuController {

    // ===================================
    // FUNCIÓN DE NAVEGACIÓN ESTATICA (Solo carga la nueva escena en la ventana existente)
    // ===================================

    /**
     * Navega a un nuevo FXML, cargándolo en la misma ventana.
     * Asume que la ventana ya está maximizada y redimensionable desde MainApp.
     */
    public static void loadScene(Node sourceNode, String fxmlPath, String newTitle) throws IOException {
        Stage currentStage = (Stage) sourceNode.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(MenuController.class.getResource(fxmlPath));
        Parent root = loader.load();

        // Obtener la escena actual del Stage
        Scene scene = currentStage.getScene();
        // Si la escena es null (poco probable aquí), o si quieres una nueva, puedes crearla.
        // Pero para reemplazar el contenido y mantener la maximización, solo necesitas setear el root.
        if (scene == null) {
            scene = new Scene(root);
            currentStage.setScene(scene);
        } else {
            scene.setRoot(root); // Reemplaza el contenido de la escena existente
        }

        currentStage.setTitle(newTitle);
        // NO TOCAR setMaximized(true) o setResizable(true) aquí, ya se hizo en MainApp.
        currentStage.show();
    }


    // ===================================
    // CAMPOS DE PROPIEDADES DE SESIÓN
    // ===================================
    private String loggedInUserPassword;
    private String loggedInUsername;
    private int loggedInUserId;

    // ===================================
    // CAMPOS FXML (@FXML) - Se elimina maximizarButton
    // ===================================
    @FXML
    private Button pedidosButton;
    @FXML
    private Button clientesButton;
    @FXML
    private Button stockButton;
    @FXML
    private Button adminButton;


    // ===================================
    // MÉTODOS SETTER
    // ===================================
    public void setLoggedInUserPassword(String password) {
        this.loggedInUserPassword = password;
    }

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
    }

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    // =========================================================================================
    // MÉTODOS HANDLER DE NAVEGACIÓN (TODOS USANDO loadScene)
    // =========================================================================================

    @FXML
    public void handleStock(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/menuStock.fxml", "Menú de Stock");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleStockabm(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/menuAbmStock.fxml", "Menú de Proveedor");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCrearProducto(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/menuProducto.fxml", "Creación/Edición de Producto");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void handleProveedor(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/menuProveedor.fxml", "Menú de Proveedor");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleClientesButton(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/menuCliente.fxml", "Registro de Cliente");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePedidosButton(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/PedidosPrimerMenu.fxml", "Menú de Pedidos");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void handleIniciodeSesionButton(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/inicioSesion.fxml", "Inicio de Sesión");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Código de un controlador externo (ej. EmpleadoMenuController)
    @FXML
    public void handleRegistrodeNuevoEmpleadoButton(ActionEvent event) {
        try {
            // Se recomienda usar un patrón para cargar vistas modales si no está centralizado
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registroEmpleado.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Registro de Empleado");
            stage.initModality(Modality.APPLICATION_MODAL); // Lo hace modal
            stage.initOwner(((Node) event.getSource()).getScene().getWindow()); // Bloquea la ventana principal

            stage.showAndWait(); // Espera a que se cierre
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleOpcionesDeAdminButton(ActionEvent event) {
        int userIdType = SessionManager.getInstance().getLoggedInUserIdType();

        if (userIdType == 4) {
            try {
                loadScene((Node) event.getSource(), "/MenuAdmin.fxml", "Menú de Admin");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Acceso Denegado");
            alert.setHeaderText(null);
            alert.setContentText("No tienes permiso para acceder. Esta sección está restringida al usuario Administrador");
            alert.showAndWait();
        }
    }

    @FXML
    public void handleGestionDeUsuariosButton(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/GestionUsuarios.fxml", "Gestión de Usuarios Empleados");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleInformesMenu(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/InformesAdmin.fxml", "Menu de Informes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleFacturas(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/FacturasAdmin.fxml", "Menu de Facturas");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleComprobantes(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/ComprobantesAdmin.fxml", "Menu de Comprobantes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleHistorialActividadButton(ActionEvent event) {
        try {
            // Carga el FXML de la tabla de usuarios
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/historialActividad.fxml"));
            Parent root = loader.load();

            // Carga la nueva escena
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene(); // Obtiene la escena existente
            if (scene == null) { // Por si acaso
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root); // Reemplaza el root de la escena existente
            }
            stage.setTitle("Historial de Actividad");

            // Ya no se necesita setMaximized(true) aquí si MainApp lo hace.
            // stage.setResizable(true);
            // stage.setMaximized(true);

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ===================================
    // MÉTODOS VOLVER (USANDO loadScene)
    // ===================================

    // Dentro de MenuController.java

    @FXML
    private void handleVolverButton(ActionEvent event) {
        try {
            // Redirige al menú ABMs, asumiendo que el Menú Admin viene de allí.
            loadScene((Node) event.getSource(), "/menuAbms.fxml", "Menú ABMs");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverButtonAbms(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/menuInicial.fxml", "Menú Principal");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverButtonStock(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/menuAbms.fxml", "Menú ABMs");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverButtonInformes(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/MenuAdmin.fxml", "Menú Principal");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverButtonComprobantes(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/InformesAdmin.fxml", "Menú de Informes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}