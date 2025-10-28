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
import javafx.scene.control.PasswordField; // Asegurarse de que esté importado
import javafx.scene.control.ToggleButton; // Importar ToggleButton
import javafx.scene.image.Image; // Importar Image
import javafx.scene.image.ImageView; // Importar ImageView
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Pattern;

public class EmpleadoController {

    @FXML private Label nombrePersonaLabel;
    @FXML private TextField usuarioField;
    @FXML private PasswordField contraseniaField; // PasswordField original

    // --- Nuevos campos FXML para el control de visibilidad de contraseña ---
    @FXML private TextField contraseniaVisibleField; // TextField para mostrar la contraseña
    @FXML private ToggleButton toggleVisibilityButton; // Botón "Ojito"
    @FXML private ImageView eyeIconView; // Para cambiar el ícono del ojo

    // Rutas de tus imágenes (asegúrate de que estas sean las correctas para este contexto)
    // Según tu último pedido:
    // EYE_OPEN_ICON usará 'ojo.png' (ojo sin raya)
    // EYE_CLOSED_ICON usará 'ojo (1).png' (ojo con raya/cerrado)
    private static final String EYE_OPEN_ICON = "/imagenes/ojo.png";
    private static final String EYE_CLOSED_ICON = "/imagenes/ojo (1).png";
    // --- Fin de nuevos campos FXML ---

    @FXML private DatePicker fechaContratacionPicker;
    @FXML private TextField cargoField;
    @FXML private TextField salarioField;

    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private PersonaDAO personaDAO;
    private UsuarioDAO usuarioDAO;
    private EmpleadoDAO empleadoDAO;

    private Persona personaData;
    private Stage dialogStage;

    public EmpleadoController() {
        this.personaDAO = new PersonaDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.empleadoDAO = new EmpleadoDAO();
    }

    @FXML
    private void initialize() {
        // Sincronizar el texto entre el campo de contraseña y el campo visible
        contraseniaVisibleField.textProperty().addListener((observable, oldValue, newValue) -> {
            contraseniaField.setText(newValue);
        });

        contraseniaField.textProperty().addListener((observable, oldValue, newValue) -> {
            contraseniaVisibleField.setText(newValue);
        });

        // Establecer el ícono inicial (ojo cerrado)
        eyeIconView.setImage(new Image(getClass().getResource(EYE_CLOSED_ICON).toExternalForm()));
    }

    // Método para alternar la visibilidad de la contraseña
    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        boolean isVisible = toggleVisibilityButton.isSelected();

        if (isVisible) {
            // Mostrar Contraseña: Ocultamos el PasswordField y mostramos el TextField
            contraseniaField.setVisible(false);
            contraseniaVisibleField.setVisible(true);
            // Cargar imagen de ojo abierto
            eyeIconView.setImage(new Image(getClass().getResource(EYE_OPEN_ICON).toExternalForm()));
        } else {
            // Ocultar Contraseña: Mostramos el PasswordField y ocultamos el TextField
            contraseniaField.setVisible(true);
            contraseniaVisibleField.setVisible(false);
            // Cargar imagen de ojo cerrado
            eyeIconView.setImage(new Image(getClass().getResource(EYE_CLOSED_ICON).toExternalForm()));
        }

        // Forzar el foco al campo actualmente visible
        if (isVisible) {
            contraseniaVisibleField.requestFocus();
        } else {
            contraseniaField.requestFocus();
        }
    }


    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setDatosPersona(String nombre, String apellido, int idTipoDocumento, String numeroDocumento, int idDireccion, String telefono, String email) {
        this.personaData = new Persona(nombre, apellido, idTipoDocumento, numeroDocumento, idDireccion, telefono, email, 2); // '2' para id_tipo_persona de Empleado
        nombrePersonaLabel.setText(nombre + " " + apellido);
    }

    @FXML
    public void handleGuardarEmpleado(ActionEvent event) {
        if (personaData == null) {
            mostrarAlerta("Error", "No se recibieron los datos de la persona.", Alert.AlertType.ERROR);
            return;
        }
        if (!validarCampos()) {
            return;
        }

        String nuevoUsuarioStr = usuarioField.getText().trim();
        if (usuarioDAO.verificarSiUsuarioExiste(nuevoUsuarioStr)) {
            mostrarAlerta("Error de Registro", "El nombre de usuario ya está registrado. Por favor, elija otro.", Alert.AlertType.ERROR);
            return;
        }

        // Obtener la contraseña desde el PasswordField (ya que está sincronizado)
        String contrasenia = contraseniaField.getText();
        if (contrasenia.isEmpty()) {
            mostrarAlerta("Advertencia", "La contraseña no puede estar vacía.", Alert.AlertType.WARNING);
            return;
        }


        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false);

            int idPersona = personaDAO.insertarPersona(this.personaData, conn);

            if (idPersona != -1) {
                Usuario nuevoUsuario = new Usuario(usuarioField.getText(), contrasenia); // Usar la contraseña sincronizada
                nuevoUsuario.setIdPersona(idPersona);

                if (usuarioDAO.insertar(nuevoUsuario, conn)) {
                    LocalDate fechaContratacion = fechaContratacionPicker.getValue();
                    String cargo = cargoField.getText();
                    double salario = Double.parseDouble(salarioField.getText());
                    String estadoPorDefecto = "Activo";

                    Empleado nuevoEmpleado = new Empleado(fechaContratacion, cargo, salario, estadoPorDefecto, idPersona);

                    if (empleadoDAO.insertarEmpleado(nuevoEmpleado, conn)) {
                        conn.commit();
                        mostrarAlerta("Éxito", "Persona, Usuario y Empleado registrados exitosamente.", Alert.AlertType.INFORMATION);
                        limpiarCampos();

                        try {
                            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/inicioSesion.fxml")));
                            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                            stage.setScene(new Scene(root));
                            stage.setTitle("Inicio de Sesión");
                            stage.setMaximized(true);
                            stage.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            mostrarAlerta("Error", "No se pudo cargar la pantalla de inicio de sesión.", Alert.AlertType.ERROR);
                        }
                    } else {
                        conn.rollback();
                        mostrarAlerta("Error", "Error al registrar los datos del empleado. La operación fue cancelada.", Alert.AlertType.ERROR);
                    }
                } else {
                    conn.rollback();
                    mostrarAlerta("Error", "Error al registrar el usuario. La operación fue cancelada.", Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Error", "Error al registrar la persona. La operación fue cancelada.", Alert.AlertType.ERROR);
            }
        } catch (SQLException | NumberFormatException e) {
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

    @FXML
    private void handleCancelar(ActionEvent event) {
        if (dialogStage != null) {
            dialogStage.close();
            return;
        }

        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/inicioSesion.fxml")));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Inicio de Sesión");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo volver a la pantalla de inicio de sesión. Verifique la ruta /inicioSesion.fxml", Alert.AlertType.ERROR);
        }
    }

    private boolean validarCampos() {
        // La validación de contraseniaField.getText().trim().isEmpty() se moverá a handleGuardarEmpleado
        // para que se haga DENTRO del flujo de obtener la contraseña.
        if (usuarioField.getText().trim().isEmpty() ||
                fechaContratacionPicker.getValue() == null || cargoField.getText().trim().isEmpty() ||
                salarioField.getText().trim().isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, complete todos los campos obligatorios del empleado y usuario.", Alert.AlertType.WARNING);
            return false;
        }

        String cargo = cargoField.getText().trim();
        if (cargo.isEmpty()) {
            mostrarAlerta("Advertencia", "El campo 'Cargo' no puede estar vacío.", Alert.AlertType.WARNING);
            return false;
        }
        if (!validarSoloLetras(cargo)) {
            mostrarAlerta("Advertencia", "El campo 'Cargo' solo puede contener letras y espacios.", Alert.AlertType.WARNING);
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

    private boolean validarSoloLetras(String texto) {
        String regex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
        return texto.matches(regex);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        mostrarAlerta(titulo, mensaje, Alert.AlertType.INFORMATION);
    }

    private void limpiarCampos() {
        usuarioField.clear();
        contraseniaField.clear();
        contraseniaVisibleField.clear(); // Limpiar también el campo visible
        fechaContratacionPicker.setValue(null);
        cargoField.clear();
        salarioField.clear();
    }

    @FXML
    private void handleHelpButton() {
        // Creamos una nueva alerta de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuramos el título y los encabezados del mensaje
        alert.setTitle("Ayuda - Menu De Creación de Empleado");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite la Creacion de un Usuario Empleado :\n"
                + "\n"
                + "1. Ingrese Los Datos Correctos para Continuar.\n"
                + "----------------------------------------------------------------------\n"
                + "2. Para Seleccionar la Fecha de Contratación Haga Click en el Boton en forma de Calendario a la Derecha Y selecciona una Fecha Para Continuar.\n"
                + "----------------------------------------------------------------------\n"
                + "3. Para Guardar el Usuario Haga Click en Guardar Empleado o Para Cancelar el Registro Haga Click en Cancelar.\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();
    }
}