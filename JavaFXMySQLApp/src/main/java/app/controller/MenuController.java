package app.controller;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import app.controller.SessionManager;
// Importamos SessionManager (del primer fragmento)
// Asumo que MainApp (del segundo fragmento) no es estrictamente necesario ya que las dimensiones están hardcodeadas.

public class MenuController {

    // ===================================
    // CAMPOS DE PROPIEDADES DE SESIÓN
    // ===================================
    private String loggedInUserPassword;
    private String loggedInUsername;
    private int loggedInUserId;

    // ===================================
    // CAMPOS FXML (@FXML)
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
    // MÉTODOS HANDLER DE NAVEGACIÓN
    // =========================================================================================

    @FXML
    public void handleStock(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/menuStock.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(1800);
            stage.setHeight(1000);
            stage.centerOnScreen();
            stage.setTitle("Menú de Stock");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleStockabm(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/menuAbmStock.fxml"));
            // Mantengo el título original de uno de los fragmentos, aunque parezca incorrecto para "AbmStock"
            stage.setTitle("Menu de Proveedor");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(1800);
            stage.setHeight(1000);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleProveedor(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/menuProveedor.fxml"));
            stage.setTitle("Menu de Proveedor");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setWidth(1800);
            stage.setHeight(1000);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Fusión de handleRegistroButton (Lucas) y handleClientesButton (Balles)
    @FXML
    public void handleClientesButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuCliente.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Usando las dimensiones grandes
            Scene scene = new Scene(root, 1800, 1000);

            stage.setScene(scene);
            stage.setTitle("Registro de Cliente");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método de Pedidos del segundo fragmento
    @FXML
    public void handlePedidosButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PedidosPrimerMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root, 1800, 1000);

            stage.setScene(scene);
            stage.setTitle("Menú de Pedidos");
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
            stage.setWidth(1800);
            stage.setHeight(1000);
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
            stage.setWidth(1800);
            stage.setHeight(1000);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleOpcionesDeAdminButton(ActionEvent event) {
        // Lógica de validación de Administrador del primer fragmento
        int userIdType = SessionManager.getInstance().getLoggedInUserIdType();

        if (userIdType == 4) {
            try {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("/MenuAdmin.fxml"));
                stage.setTitle("Menú de Admin");
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setWidth(1800);
                stage.setHeight(1000);
                stage.centerOnScreen();
                stage.show();

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionUsuarios.fxml"));
            Parent root = loader.load();

            // Lógica de pasaje de datos del segundo fragmento (más completa)
            // Se asume que UsuariosEmpleadoController existe y tiene los setters.
            UsuariosEmpleadoController usuariosController = loader.getController();
            if (usuariosController != null) {
                usuariosController.setLoggedInUserPassword(this.loggedInUserPassword);
                usuariosController.setLoggedInUsername(this.loggedInUsername);
                usuariosController.setLoggedInUserId(this.loggedInUserId);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Gestión de Usuarios Empleados");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Métodos de Informes, Facturas y Comprobantes (Solo en el primer fragmento)

    @FXML
    public void handleInformesMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/InformesAdmin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menu de Informes");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleFacturas(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FacturasAdmin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menu de Informes"); // Título debería ser más específico, pero se mantiene el original
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleComprobantes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ComprobantesAdmin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menu de Informes"); // Título debería ser más específico, pero se mantiene el original
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ===================================
    // MÉTODOS VOLVER
    // ===================================

    @FXML
    private void handleVolverButton(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/menuAbms.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menú Principal"); // O "Menú ABMs"
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
            stage.setTitle("Menú Principal"); // O "Menú ABMs"
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverButtonInformes(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/menuAdmin.fxml"));
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
    private void handleVolverButtonComprobantes(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/InformesAdmin.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menú Principal");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}