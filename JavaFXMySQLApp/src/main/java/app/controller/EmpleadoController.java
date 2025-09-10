package app.controller;

import app.model.Persona;
import app.model.Usuario;
import app.model.Empleado;
import app.model.PersonaDAO;
import app.model.UsuarioDAO;
import app.model.EmpleadoDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    private Persona personaData; // Objeto para guardar los datos de la persona

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
    public void handleGuardarEmpleado() {
        // Validación inicial de los campos del empleado
        if (!validarCampos()) {
            return;
        }

        String nuevoUsuarioStr = usuarioField.getText().trim();

        // 1. Validar si el nombre de usuario ya existe
        if (usuarioDAO.verificarSiUsuarioExiste(nuevoUsuarioStr)) {
            mostrarAlerta("Error de Registro", "El nombre de usuario ya está registrado. Por favor, elija otro.");
            return;
        }

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false); // Inicia la transacción

            // 2. Insertar en la tabla Persona
            // Nota: Se asume que insertarPersona de PersonaDAO ha sido modificado
            // para aceptar un objeto Connection para la transacción.
            int idPersona = personaDAO.insertarPersona(this.personaData, conn);

            if (idPersona != -1) {
                // 3. Insertar en la tabla Usuario
                // Nota: Similarmente, se asume que el método insertar de UsuarioDAO
                // ha sido modificado para aceptar la conexión.
                Usuario nuevoUsuario = new Usuario(usuarioField.getText(), contraseniaField.getText());
                nuevoUsuario.setIdPersona(idPersona);

                if (usuarioDAO.insertar(nuevoUsuario, conn)) {
                    // 4. Insertar en la tabla Empleado
                    LocalDate fechaContratacion = fechaContratacionPicker.getValue();
                    String cargo = cargoField.getText();
                    double salario = Double.parseDouble(salarioField.getText());

                    Empleado nuevoEmpleado = new Empleado(fechaContratacion, cargo, salario, idPersona);

                    if (empleadoDAO.insertarEmpleado(nuevoEmpleado, conn)) {
                        conn.commit(); // Confirma la transacción
                        mostrarAlerta("Éxito", "Empleado y Usuario registrados exitosamente.");
                        limpiarCampos();
                        Stage stage = (Stage) usuarioField.getScene().getWindow();
                        stage.close();
                    } else {
                        conn.rollback(); // Deshace todo si falla la inserción de Empleado
                        mostrarAlerta("Error", "Error al registrar los datos del empleado.");
                    }
                } else {
                    conn.rollback(); // Deshace la inserción de la Persona
                    mostrarAlerta("Error", "Error al registrar el usuario. La operación fue cancelada.");
                }
            } else {
                mostrarAlerta("Error", "Error al registrar la persona.");
            }
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); // Deshace todo si hay un error
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            mostrarAlerta("Error", "Ocurrió un error en la base de datos. La operación fue cancelada.");
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close(); // Cierra la conexión
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

        // Validación de salario para asegurar que sea un número válido
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
