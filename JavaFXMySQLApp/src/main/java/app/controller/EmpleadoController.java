package app.controller;

import app.dao.EmpleadoDAO;
import app.dao.PersonaDAO;
import app.dao.UsuarioDAO;
import app.model.Empleado;
import app.model.Persona;
import app.model.Usuario;
import javafx.event.ActionEvent;
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

    // --- FXML Fields de la versión Lucas ---
    @FXML private Label nombrePersonaLabel; // Para mostrar el nombre de la persona que se está creando
    @FXML private TextField usuarioField;
    @FXML private TextField contraseniaField;

    // --- FXML Fields de la versión Balles ---
    // NOTA: nombreField, apellidoField, idPersonaField no deberían estar aquí
    // si los datos de Persona se reciben via setDatosPersona.
    // Los campos de Empleado son los siguientes:
    @FXML private DatePicker fechaContratacionPicker;
    @FXML private TextField cargoField;
    @FXML private TextField salarioField;
    // @FXML private TextField idPersonaField; // Este campo no es necesario si idPersona se obtiene de PersonaDAO

    // --- Atributos de Conexión (si tus DAOs no los manejan internamente) ---
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // DAOs
    private PersonaDAO personaDAO;
    private UsuarioDAO usuarioDAO;
    private EmpleadoDAO empleadoDAO;

    private Persona personaData; // Datos de la persona recibidos del controlador anterior
    private Stage dialogStage; // Si esta vista se usa como un diálogo (de la versión Balles)

    // Constructor para inicializar los DAOs
    public EmpleadoController() {
        this.personaDAO = new PersonaDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.empleadoDAO = new EmpleadoDAO();
    }

    // Método para inicializar, se puede usar para configurar el dialogStage si se abre como tal
    @FXML
    private void initialize() {
        // Puedes poner aquí cualquier inicialización específica si el EmpleadoController
        // tiene elementos que necesitan cargarse al iniciar la vista,
        // por ejemplo, si tuviera un ComboBox de roles o estados que no sean fijos.
    }

    // Método de la versión Balles para cerrar el diálogo
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Método para recibir los datos de la persona del controlador anterior (de Lucas)
    public void setDatosPersona(String nombre, String apellido, int idTipoDocumento, String numeroDocumento, int idDireccion, String telefono, String email) {
        // Aseguramos que el id_tipo_persona sea 2 (Empleado) al crear la Persona
        this.personaData = new Persona(nombre, apellido, idTipoDocumento, numeroDocumento, idDireccion, telefono, email, 2); // '2' para id_tipo_persona de Empleado
        nombrePersonaLabel.setText(nombre + " " + apellido);
    }

    // --- handleGuardarEmpleado de Lucas, modificado para usar el constructor de Empleado con 'estado' ---
    @FXML
    public void handleGuardarEmpleado(ActionEvent event) {
        // Validaciones generales
        if (personaData == null) {
            mostrarAlerta("Error", "No se recibieron los datos de la persona.", Alert.AlertType.ERROR);
            return;
        }
        if (!validarCampos()) {
            return; // validarCampos() ya mostrará su propia alerta
        }

        String nuevoUsuarioStr = usuarioField.getText().trim();

        if (usuarioDAO.verificarSiUsuarioExiste(nuevoUsuarioStr)) {
            mostrarAlerta("Error de Registro", "El nombre de usuario ya está registrado. Por favor, elija otro.", Alert.AlertType.ERROR);
            return;
        }

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar Persona
            // Nota: Aquí pasamos la conexión al DAO. Asegúrate de que PersonaDAO.insertarPersona acepta 'conn'.
            int idPersona = personaDAO.insertarPersona(this.personaData, conn);

            if (idPersona != -1) { // Si la persona se insertó correctamente
                // 2. Insertar Usuario
                Usuario nuevoUsuario = new Usuario(usuarioField.getText(), contraseniaField.getText());
                nuevoUsuario.setIdPersona(idPersona); // Asignar el ID de la persona recién creada

                // Asegúrate de que usuarioDAO.insertar acepta 'conn'
                if (usuarioDAO.insertar(nuevoUsuario, conn)) {
                    // 3. Insertar Empleado
                    LocalDate fechaContratacion = fechaContratacionPicker.getValue();
                    String cargo = cargoField.getText();
                    double salario = Double.parseDouble(salarioField.getText());
                    String estadoPorDefecto = "Activo"; // <-- ¡Estado por defecto!

                    // Usar el constructor de Empleado con 5 parámetros (LocalDate, String, double, String, int)
                    Empleado nuevoEmpleado = new Empleado(fechaContratacion, cargo, salario, estadoPorDefecto, idPersona);

                    // Asegúrate de que empleadoDAO.insertarEmpleado acepta 'conn'
                    if (empleadoDAO.insertarEmpleado(nuevoEmpleado, conn)) {
                        conn.commit(); // Confirmar la transacción
                        mostrarAlerta("Éxito", "Persona, Usuario y Empleado registrados exitosamente.", Alert.AlertType.INFORMATION);
                        limpiarCampos();

                        // Redireccionar a la pantalla de inicio de sesión (de la versión Lucas)
                        try {
                            Parent root = FXMLLoader.load(getClass().getResource("/inicioSesion.fxml"));
                            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                            stage.setScene(new Scene(root));
                            stage.setTitle("Inicio de Sesión");
                            stage.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            mostrarAlerta("Error", "No se pudo cargar la pantalla de inicio de sesión.", Alert.AlertType.ERROR);
                        }
                    } else {
                        conn.rollback(); // Rollback si falla la inserción del Empleado
                        mostrarAlerta("Error", "Error al registrar los datos del empleado. La operación fue cancelada.", Alert.AlertType.ERROR);
                    }
                } else {
                    conn.rollback(); // Rollback si falla la inserción del Usuario
                    mostrarAlerta("Error", "Error al registrar el usuario. La operación fue cancelada.", Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Error", "Error al registrar la persona. La operación fue cancelada.", Alert.AlertType.ERROR);
            }
        } catch (SQLException | NumberFormatException e) {
            // Rollback en caso de cualquier excepción SQL o formato de número
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            if (e instanceof NumberFormatException) {
                mostrarAlerta("Error de Formato", "El salario debe ser un número válido.", Alert.AlertType.ERROR);
            } else {
                mostrarAlerta("Error de Base de Datos", "Ocurrió un error en la base de datos: " + e.getMessage() + ". La operación fue cancelada.", Alert.AlertType.ERROR);
            }
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

    // Método para cancelar (de la versión Balles, adaptado para la lógica de Lucas)
    @FXML
    private void handleCancelar(ActionEvent event) {
        // Aquí puedes decidir si simplemente cierras la ventana (como en Balles)
        // o si rediriges a otra pantalla (como una vuelta atrás en el flujo de Lucas).
        // Por ahora, si es un diálogo, lo cierra. Si es una ventana principal, redirige.
        if (dialogStage != null) {
            dialogStage.close();
        } else {
            // Si no es un diálogo, redirigir a una pantalla anterior (ej. registro de persona)
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/registroPersona.fxml")); // Ajusta esta ruta
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Registro de Persona");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                mostrarAlerta("Error", "No se pudo volver a la pantalla anterior.", Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validarCampos() {
        if (usuarioField.getText().trim().isEmpty() || contraseniaField.getText().trim().isEmpty() ||
                fechaContratacionPicker.getValue() == null || cargoField.getText().trim().isEmpty() ||
                salarioField.getText().trim().isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, complete todos los campos obligatorios del empleado y usuario.", Alert.AlertType.WARNING);
            return false;
        }
        try {
            Double.parseDouble(salarioField.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("Advertencia", "El salario debe ser un número válido.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    // Método de alerta unificado (de Balles, pero ahora usa AlertType.ERROR/WARNING/INFORMATION)
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Sobrecarga para mantener compatibilidad con las alertas de Lucas si eran solo de INFORMATION
    private void mostrarAlerta(String titulo, String mensaje) {
        mostrarAlerta(titulo, mensaje, Alert.AlertType.INFORMATION);
    }

    private void limpiarCampos() {
        usuarioField.clear();
        contraseniaField.clear();
        fechaContratacionPicker.setValue(null);
        cargoField.clear();
        salarioField.clear();
        // nombrePersonaLabel.setText(""); // Si quieres limpiar la etiqueta
    }
}