package app.controller;

import app.dao.DireccionDAO;
import app.dao.PersonaDAO;
import app.dao.ProveedorDAO;
import app.dao.TipoProveedorDAO;
import app.model.Direccion;
import app.model.Proveedor;
import app.model.TipoProveedor;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistroProveedorController {
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    @FXML private TextField nombreField;
    @FXML private TextField contactoField;
    @FXML private TextField mailField;
    @FXML private TextField calleField;
    @FXML private TextField numeroField;
    @FXML private TextField pisoField;
    @FXML private TextField departamentoField;
    @FXML private TextField codigoPostalField;
    @FXML private TextField ciudadField;
    @FXML private TextField provinciaField;
    @FXML private TextField paisField;
    @FXML private ChoiceBox<TipoProveedor> tipoProveedorChoiceBox;

    private ProveedorController proveedorController;
    private DireccionDAO direccionDAO = new DireccionDAO();
    private ProveedorDAO proveedorDAO = new ProveedorDAO();
    PersonaDAO personaDAO = new PersonaDAO();
    private TipoProveedorDAO tipoProveedorDAO = new TipoProveedorDAO();


    public void setProveedorController(ProveedorController proveedorController) {
        this.proveedorController = proveedorController;
    }

    @FXML
    private void initialize() {
        cargarTiposDeProveedor();
    }

    // Nuevo método para cargar los tipos de proveedor
    private void cargarTiposDeProveedor() {
        try {
            tipoProveedorChoiceBox.setItems(tipoProveedorDAO.getAllTipoProveedores());
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Carga", "No se pudieron cargar los tipos de proveedor.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleRegistrarTipoProveedorButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registroTipoProveedor.fxml"));
            Parent root = loader.load();
            RegistroTipoProveedorController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Registrar Tipo de Proveedor");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            // Asigna un listener para recargar el ChoiceBox al cerrar la ventana
            stage.setOnHidden(e -> cargarTiposDeProveedor());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el formulario de registro de tipo de proveedor.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRegistrarProveedor(ActionEvent event) {
        if (!validarCamposProveedores() || !validarCamposDireccion()) {
            return;
        }

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false);

            // 1. Crear y guardar la dirección
            Direccion nuevaDireccion = new Direccion(
                    calleField.getText(),
                    numeroField.getText(),
                    pisoField.getText(),
                    departamentoField.getText(),
                    codigoPostalField.getText(),
                    ciudadField.getText(),
                    provinciaField.getText(),
                    paisField.getText()
            );
            int idDireccion = direccionDAO.insertarDireccion(nuevaDireccion, conn);

            if (idDireccion == -1) {
                mostrarAlerta("Error", "No se pudo registrar la dirección.", Alert.AlertType.ERROR);
                conn.rollback();
                return;
            }

            // 2. Crear y guardar el proveedor
            TipoProveedor tipoProveedor = tipoProveedorChoiceBox.getSelectionModel().getSelectedItem();
            if (tipoProveedor == null) {
                mostrarAlerta("Advertencia", "Por favor, seleccione un tipo de proveedor.", Alert.AlertType.WARNING);
                conn.rollback();
                return;
            }

            Proveedor nuevoProveedor = new Proveedor(
                    0, // ID se genera en la DB
                    nombreField.getText(),
                    contactoField.getText(),
                    mailField.getText(),
                    "Activo",
                    idDireccion,
                    tipoProveedor.getId()
            );

            boolean exito = proveedorDAO.registrarProveedor(nuevoProveedor, conn);

            if (exito) {
                conn.commit();
                mostrarAlerta("Éxito", "Proveedor registrado correctamente.", Alert.AlertType.INFORMATION);
                Stage stage = (Stage) nombreField.getScene().getWindow();
                stage.close();
            } else {
                conn.rollback();
                mostrarAlerta("Error", "No se pudo registrar el proveedor.", Alert.AlertType.ERROR);
            }

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            mostrarAlerta("Error", "Ocurrió un error inesperado de base de datos.", Alert.AlertType.ERROR);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // APLICANDO LAS VALIDACIONES QUE ME DISTE DEL REGISTRO DE CLIENTE
    private boolean validarCamposProveedores() {
        String email = mailField.getText().trim();
        if (nombreField.getText().isEmpty() || contactoField.getText().isEmpty() ||
                mailField.getText().isEmpty() || tipoProveedorChoiceBox.getValue() == null) {
            mostrarAlerta("Advertencia", "Por favor, complete todos los campos obligatorios.");
            return false;
        }
        if (personaDAO.verificarSiMailExiste(email)) {
            mostrarAlerta("Error de Registro", "El email que ingresó ya se encuentra registrado.");
            return false;
        }
        if (!mailField.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            mostrarAlerta("Advertencia", "El formato del correo electrónico no es válido.");
            return false;
        }
        return true;
    }

    private boolean validarCamposDireccion() {
        String regexNumeros = "\\d+";
        if (calleField.getText().isEmpty() || numeroField.getText().isEmpty() ||
                codigoPostalField.getText().isEmpty() || ciudadField.getText().isEmpty() ||
                provinciaField.getText().isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, complete todos los campos de dirección obligatorios.");
            return false;
        }
        else if (!numeroField.getText().matches(regexNumeros)) {
            mostrarAlerta("Advertencia", "El campo 'Número' debe contener solo números.", Alert.AlertType.WARNING);
            return false;
        }
        if (codigoPostalField.getText().trim().length() != 4) {
            mostrarAlerta("Advertencia", "El código postal debe tener exactamente 4 caracteres.");
            return false;
        }
        return true;
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        Stage stage = (Stage) nombreField.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
