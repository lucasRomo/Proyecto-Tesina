package app.controller;

import app.dao.EmpleadoDAO;
import app.dao.PersonaDAO;
import app.dao.UsuarioDAO;
import app.model.Empleado;
import app.model.Persona;
import app.model.Usuario;
import javafx.event.ActionEvent; // Se debe importar ActionEvent
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;

public class EmpleadoController {

    @FXML private Label nombrePersonaLabel;
    @FXML private TextField usuarioField;
    @FXML private TextField contraseniaField;
    @FXML private DatePicker fechaContratacionPicker;
    @FXML private TextField cargoField;
    @FXML private TextField salarioField;

    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // DAOs declarados como variables de instancia
    private PersonaDAO personaDAO;
    private UsuarioDAO usuarioDAO;
    private EmpleadoDAO empleadoDAO;

    private Persona personaData;

    // Constructor para inicializar los DAOs
    public EmpleadoController() {
        this.personaDAO = new PersonaDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.empleadoDAO = new EmpleadoDAO();
    }

    // Método para recibir los datos del controlador anterior
    public void setDatosPersona(String nombre, String apellido, int idTipoDocumento, String numeroDocumento, int idDireccion, String telefono, String email) {
        this.personaData = new Persona(nombre, apellido, idTipoDocumento, numeroDocumento, idDireccion, telefono, email);
        nombrePersonaLabel.setText(nombre + " " + apellido);
    }

    @FXML
    public void handleGuardarEmpleado(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        String nuevoUsuarioStr = usuarioField.getText().trim();

        if (usuarioDAO.verificarSiUsuarioExiste(nuevoUsuarioStr)) {
            mostrarAlerta("Error de Registro", "El nombre de usuario ya está registrado. Por favor, elija otro.");
            return;
        }

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false);

            int idPersona = personaDAO.insertarPersona(this.personaData, conn);

            if (idPersona != -1) {
                Usuario nuevoUsuario = new Usuario(usuarioField.getText(), contraseniaField.getText());
                nuevoUsuario.setIdPersona(idPersona);

                if (usuarioDAO.insertar(nuevoUsuario, conn)) {
                    LocalDate fechaContratacion = fechaContratacionPicker.getValue();
                    String cargo = cargoField.getText();
                    double salario = Double.parseDouble(salarioField.getText());

                    Empleado nuevoEmpleado = new Empleado(fechaContratacion, cargo, salario, idPersona);

                    if (empleadoDAO.insertarEmpleado(nuevoEmpleado, conn)) {
                        conn.commit();
                        mostrarAlerta("Éxito", "Empleado y Usuario registrados exitosamente.");
                        limpiarCampos();

                        // **CÓDIGO CORREGIDO PARA REDIRECCIONAR**
                        try {
                            // Cargar la nueva pantalla
                            Parent root = FXMLLoader.load(getClass().getResource("/inicioSesion.fxml"));
                            // Obtener el Stage de la ventana actual
                            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                            // Establecer la nueva escena
                            stage.setScene(new Scene(root));
                            stage.setTitle("Inicio de Sesión");
                            stage.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            mostrarAlerta("Error", "No se pudo cargar la pantalla de inicio de sesión.");
                        }
                    } else {
                        conn.rollback();
                        mostrarAlerta("Error", "Error al registrar los datos del empleado.");
                    }
                } else {
                    conn.rollback();
                    mostrarAlerta("Error", "Error al registrar el usuario. La operación fue cancelada.");
                }
            } else {
                mostrarAlerta("Error", "Error al registrar la persona.");
            }
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            mostrarAlerta("Error", "Ocurrió un error en la base de datos. La operación fue cancelada.");
            e.printStackTrace();
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

    private boolean validarCampos() {
        if (usuarioField.getText().isEmpty() || contraseniaField.getText().isEmpty() ||
                fechaContratacionPicker.getValue() == null || cargoField.getText().isEmpty() ||
                salarioField.getText().isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, complete todos los campos obligatorios del empleado.");
            return false;
        }
        try {
            Double.parseDouble(salarioField.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("Advertencia", "El salario debe ser un número válido.");
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void limpiarCampos() {
        usuarioField.clear();
        contraseniaField.clear();
        fechaContratacionPicker.setValue(null);
        cargoField.clear();
        salarioField.clear();
    }
}