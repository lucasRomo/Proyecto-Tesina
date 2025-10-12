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

public class MenuController {

    // ===================================
    // TAMAÑOS FIJOS DEFINIDOS (Unificados)
    // ===================================
    private static final double DEFAULT_WIDTH = 1200;
    private static final double DEFAULT_HEIGHT = 700;
    private static final double MAXIMIZED_WIDTH = 1800; // El nuevo "maximizado" fijo
    private static final double MAXIMIZED_HEIGHT = 1000;

    // Bandera para rastrear si estamos en el modo "Grande"
    private static final String IS_LARGE_MODE_KEY = "isLargeMode";


    // ===================================
    // FUNCIÓN DE NAVEGACIÓN ESTATICA (Asegura el tamaño FIJO y la NO redimensión)
    // ===================================

    /**
     * Navega a un nuevo FXML, aplicando el tamaño prudencial o el grande (si estaba activo),
     * y mantiene la ventana NO redimensionable por el usuario.
     */
    public static void loadFixedSizeScene(Node sourceNode, String fxmlPath, String newTitle) throws IOException {
        Stage currentStage = (Stage) sourceNode.getScene().getWindow();

        // 1. Obtener el estado actual del modo "Grande"
        boolean wasLargeMode = (boolean) currentStage.getProperties().getOrDefault(IS_LARGE_MODE_KEY, false);

        FXMLLoader loader = new FXMLLoader(MenuController.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        currentStage.setScene(scene);
        currentStage.setTitle(newTitle);

        // 2. Aplicar el tamaño persistente
        if (wasLargeMode) {
            currentStage.setWidth(MAXIMIZED_WIDTH);
            currentStage.setHeight(MAXIMIZED_HEIGHT);
        } else {
            currentStage.setWidth(DEFAULT_WIDTH);
            currentStage.setHeight(DEFAULT_HEIGHT);
        }

        // CLAVE: Mantener la ventana NO redimensionable en todas las escenas
        currentStage.setResizable(false);
        currentStage.setMaximized(false); // Siempre desactivar el maximizado nativo

        currentStage.getProperties().put(IS_LARGE_MODE_KEY, wasLargeMode);
        currentStage.centerOnScreen();
        currentStage.show();
    }


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
    @FXML
    private Button maximizarButton; // Este botón controla el cambio entre 1200x700 y 1800x1000

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
    // MÉTODOS HANDLER DE NAVEGACIÓN (TODOS USANDO loadFixedSizeScene)
    // =========================================================================================

    @FXML
    public void handleStock(ActionEvent event) {
        try {
            loadFixedSizeScene((Node) event.getSource(), "/menuStock.fxml", "Menú de Stock");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleStockabm(ActionEvent event) {
        try {
            // Nota: el FXML es "AbmStock" pero el título es "Proveedor". Se mantiene la lógica de tamaño fijo.
            loadFixedSizeScene((Node) event.getSource(), "/menuAbmStock.fxml", "Menú de Proveedor");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCrearProducto(ActionEvent event) {
        try {
            // Este método NO estaba en el controlador anterior, se incluye con la nueva lógica.
            loadFixedSizeScene((Node) event.getSource(), "/menuProducto.fxml", "Creación/Edición de Producto");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void handleProveedor(ActionEvent event) {
        try {
            loadFixedSizeScene((Node) event.getSource(), "/menuProveedor.fxml", "Menú de Proveedor");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleClientesButton(ActionEvent event) {
        try {
            loadFixedSizeScene((Node) event.getSource(), "/menuCliente.fxml", "Registro de Cliente");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePedidosButton(ActionEvent event) {
        try {
            loadFixedSizeScene((Node) event.getSource(), "/PedidosPrimerMenu.fxml", "Menú de Pedidos");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void handleIniciodeSesionButton(ActionEvent event) {
        try {
            loadFixedSizeScene((Node) event.getSource(), "/inicioSesion.fxml", "Inicio de Sesión");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegistrodeNuevoEmpleadoButton(ActionEvent event) {
        try {
            loadFixedSizeScene((Node) event.getSource(), "/registroEmpleado.fxml", "Registro de Empleado");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleOpcionesDeAdminButton(ActionEvent event) {
        int userIdType = SessionManager.getInstance().getLoggedInUserIdType();

        if (userIdType == 4) {
            try {
                loadFixedSizeScene((Node) event.getSource(), "/MenuAdmin.fxml", "Menú de Admin");
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
            loadFixedSizeScene((Node) event.getSource(), "/GestionUsuarios.fxml", "Gestión de Usuarios Empleados");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleInformesMenu(ActionEvent event) {
        try {
            loadFixedSizeScene((Node) event.getSource(), "/InformesAdmin.fxml", "Menu de Informes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleFacturas(ActionEvent event) {
        try {
            loadFixedSizeScene((Node) event.getSource(), "/FacturasAdmin.fxml", "Menu de Facturas");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleComprobantes(ActionEvent event) {
        try {
            loadFixedSizeScene((Node) event.getSource(), "/ComprobantesAdmin.fxml", "Menu de Comprobantes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ===================================
    // MÉTODOS VOLVER (USANDO loadFixedSizeScene)
    // ===================================

    @FXML
    private void handleVolverButton(ActionEvent event) {
        try {
            loadFixedSizeScene((Node) event.getSource(), "/menuAbms.fxml", "Menú ABMs");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverButtonAbms(ActionEvent event) {
        try {
            loadFixedSizeScene((Node) event.getSource(), "/menuInicial.fxml", "Menú Principal");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverButtonStock(ActionEvent event) {
        try {
            loadFixedSizeScene((Node) event.getSource(), "/menuAbms.fxml", "Menú ABMs");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método Volver para Informes (Se asume la ruta lógica)
    @FXML
    private void handleVolverButtonInformes(ActionEvent event) {
        try {
            loadFixedSizeScene((Node) event.getSource(), "/menuAdmin.fxml", "Menú Principal");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método Volver para Comprobantes (Se asume la ruta lógica)
    @FXML
    private void handleVolverButtonComprobantes(ActionEvent event) {
        try {
            loadFixedSizeScene((Node) event.getSource(), "/InformesAdmin.fxml", "Menú de Informes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ===================================
    // MÉTODO MAXIMIZAR/RESTAURAR (Cambia entre los dos tamaños fijos)
    // ===================================

    @FXML
    private void handleMaximizarButton(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Obtener el estado actual
        boolean isLargeMode = (boolean) stage.getProperties().getOrDefault(IS_LARGE_MODE_KEY, false);

        if (!isLargeMode) {
            // Activar Modo Grande (1800x1000)
            stage.setWidth(MAXIMIZED_WIDTH);
            stage.setHeight(MAXIMIZED_HEIGHT);
            stage.getProperties().put(IS_LARGE_MODE_KEY, true);
            stage.centerOnScreen();
            if (maximizarButton != null) {
                maximizarButton.setText("Restaurar");
            }
        } else {
            // Restaurar a Modo Prudencial (1200x700)
            stage.setWidth(DEFAULT_WIDTH);
            stage.setHeight(DEFAULT_HEIGHT);
            stage.getProperties().put(IS_LARGE_MODE_KEY, false);
            stage.centerOnScreen();
            if (maximizarButton != null) {
                maximizarButton.setText("Maximizar");
            }
        }
        // CLAVE: Asegurar que NO sea redimensionable, independientemente del tamaño
        stage.setResizable(false);
        stage.setMaximized(false);
    }
}