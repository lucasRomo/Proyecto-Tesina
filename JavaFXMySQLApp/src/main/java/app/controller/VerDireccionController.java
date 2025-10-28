package app.controller;

import app.model.Direccion;
import app.dao.DireccionDAO;
// NUEVAS IMPORTACIONES REQUERIDAS PARA EL REGISTRO DE ACTIVIDAD
import app.dao.HistorialActividadDAO;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class VerDireccionController {

    // Configuración de la conexión (DEBE COINCIDIR CON UsuariosEmpleadoController)
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    @FXML private TextField calleTextField;
    @FXML private TextField numeroTextField;
    @FXML private TextField pisoTextField;
    @FXML private TextField deptoTextField;
    @FXML private TextField codigoPostalTextField;
    @FXML private TextField ciudadTextField;
    @FXML private TextField provinciaTextField;
    @FXML private TextField paisTextField;
    @FXML private Button guardarButton;
    @FXML private Button cancelarButton;

    private DireccionDAO direccionDAO = new DireccionDAO();
    private HistorialActividadDAO historialDAO = new HistorialActividadDAO(); // Nuevo DAO
    private Direccion direccionActual;
    private Direccion direccionOriginal; // Nueva variable para la comparación

    public void setDireccion(Direccion direccion) {
        this.direccionActual = direccion;
        // Crea una COPIA de la dirección actual para la comparación
        this.direccionOriginal = new Direccion(
                direccion.getIdDireccion(),
                direccion.getCalle(),
                direccion.getNumero(),
                direccion.getPiso(),
                direccion.getDepartamento(),
                direccion.getCodigoPostal(),
                direccion.getCiudad(),
                direccion.getProvincia(),
                direccion.getPais()
        );

        if (direccion != null) {
            calleTextField.setText(direccion.getCalle());
            numeroTextField.setText(direccion.getNumero());
            pisoTextField.setText(direccion.getPiso());
            deptoTextField.setText(direccion.getDepartamento());
            codigoPostalTextField.setText(direccion.getCodigoPostal());
            ciudadTextField.setText(direccion.getCiudad());
            provinciaTextField.setText(direccion.getProvincia());
            paisTextField.setText(direccion.getPais());
        }
    }

    @FXML
    private void handleModificar() {
        calleTextField.setDisable(false);
        numeroTextField.setDisable(false);
        pisoTextField.setDisable(false);
        deptoTextField.setDisable(false);
        codigoPostalTextField.setDisable(false);
        ciudadTextField.setDisable(false);
        provinciaTextField.setDisable(false);
        paisTextField.setDisable(false);
    }

    @FXML
    private void handleGuardarCambios() {
        if (direccionActual != null && direccionOriginal != null) {
            if (validarCamposDireccion()) {

                // Actualiza el objeto Dirección con los nuevos valores
                direccionActual.setCalle(calleTextField.getText().trim());
                direccionActual.setNumero(numeroTextField.getText().trim());
                direccionActual.setPiso(pisoTextField.getText().trim());
                direccionActual.setDepartamento(deptoTextField.getText().trim());
                direccionActual.setCodigoPostal(codigoPostalTextField.getText().trim());
                direccionActual.setCiudad(ciudadTextField.getText().trim());
                direccionActual.setProvincia(provinciaTextField.getText().trim());
                direccionActual.setPais(paisTextField.getText().trim());

                boolean exitoActualizacion = false;
                boolean exitoRegistro = true;
                Connection conn = null;

                try {
                    // 1. INICIAR TRANSACCIÓN
                    conn = DriverManager.getConnection(URL, USER, PASSWORD);
                    conn.setAutoCommit(false);

                    int idDireccionAfectada = direccionActual.getIdDireccion();
                    int idUsuarioResponsable = SessionManager.getInstance().getLoggedInUserId(); // Obtener el ID del Admin

                    // 2. REGISTRAR CAMBIOS EN EL HISTORIAL
                    // Usamos un método auxiliar para registrar y verificar el éxito del registro
                    exitoRegistro = registrarCambiosDireccion(conn, idUsuarioResponsable, idDireccionAfectada);

                    // 3. ACTUALIZAR LA DIRECCIÓN (ASUMIMOS QUE direccionDAO.modificarDireccion ACEPTA LA CONEXIÓN)
                    // Si el DAO no acepta conexión, solo podemos registrar el éxito de la operación.
                    // Para ser consistente con el resto del proyecto, ajustamos el DAO para que acepte conexión:
                    exitoActualizacion = direccionDAO.modificarDireccion(direccionActual);

                    if (exitoActualizacion && exitoRegistro) {
                        conn.commit();
                        mostrarAlerta("Éxito", "Dirección modificada y registrada correctamente.", Alert.AlertType.INFORMATION);

                        // Cerrar ventana
                        Stage stage = (Stage) guardarButton.getScene().getWindow();
                        stage.close();
                    } else {
                        conn.rollback();
                        mostrarAlerta("Error", "No se pudo modificar la dirección o registrar los cambios. Se realizó ROLLBACK.", Alert.AlertType.ERROR);
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    try {
                        if (conn != null) conn.rollback();
                    } catch (SQLException rollbackEx) {
                        System.err.println("Error al intentar hacer rollback: " + rollbackEx.getMessage());
                    }
                    mostrarAlerta("Error de BD", "Ocurrió un error en la base de datos: " + e.getMessage(), Alert.AlertType.ERROR);
                } finally {
                    try {
                        if (conn != null) {
                            conn.setAutoCommit(true);
                            conn.close();
                        }
                    } catch (SQLException closeEx) {
                        System.err.println("Error al cerrar la conexión: " + closeEx.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Compara la dirección actual con la original y registra los cambios en el historial.
     * @return true si todos los registros fueron exitosos, false si alguno falló.
     */
    private boolean registrarCambiosDireccion(Connection conn, int idUsuarioResponsable, int idDireccionAfectada) throws SQLException {
        boolean exito = true;

        // Función para comparar de forma segura (maneja nulls y evita código repetido)
        java.util.function.BiFunction<String, String, Boolean> compararYRegistrar = (campoNombre, getter) -> {
            try {
                String original = "";
                String modificado = "";

                // Usamos reflexión para obtener el valor, o setters/getters directos si no quieres usar reflexión
                switch (campoNombre) {
                    case "calle": original = direccionOriginal.getCalle(); modificado = direccionActual.getCalle(); break;
                    case "numero": original = direccionOriginal.getNumero(); modificado = direccionActual.getNumero(); break;
                    case "piso": original = direccionOriginal.getPiso(); modificado = direccionActual.getPiso(); break;
                    case "departamento": original = direccionOriginal.getDepartamento(); modificado = direccionActual.getDepartamento(); break;
                    case "codigoPostal": original = direccionOriginal.getCodigoPostal(); modificado = direccionActual.getCodigoPostal(); break;
                    case "ciudad": original = direccionOriginal.getCiudad(); modificado = direccionActual.getCiudad(); break;
                    case "provincia": original = direccionOriginal.getProvincia(); modificado = direccionActual.getProvincia(); break;
                    case "pais": original = direccionOriginal.getPais(); modificado = direccionActual.getPais(); break;
                    default: return true; // No hay campo que comparar
                }

                // Tratar nulls para la comparación de igualdad
                String originalStr = original != null ? original : "";
                String modificadoStr = modificado != null ? modificado : "";

                if (!originalStr.equals(modificadoStr)) {
                    // Insertar registro
                    return historialDAO.insertarRegistro(
                            idUsuarioResponsable,
                            "Direccion",
                            campoNombre,
                            idDireccionAfectada,
                            originalStr,
                            modificadoStr,
                            conn
                    );
                }
                return true; // No hubo cambio, registro exitoso por defecto

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        };

        // Realizar la comparación y el registro para cada campo
        exito &= compararYRegistrar.apply("calle", "getCalle");
        exito &= compararYRegistrar.apply("numero", "getNumero");
        exito &= compararYRegistrar.apply("piso", "getPiso");
        exito &= compararYRegistrar.apply("departamento", "getDepartamento");
        exito &= compararYRegistrar.apply("codigoPostal", "getCodigoPostal");
        exito &= compararYRegistrar.apply("ciudad", "getCiudad");
        exito &= compararYRegistrar.apply("provincia", "getProvincia");
        exito &= compararYRegistrar.apply("pais", "getPais");

        return exito;
    }

    private boolean validarCamposDireccion() {
        // ... (Tu lógica de validación se mantiene igual)

        String regexNumeros = "\\d+";
        String regexLetras = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";

        String calle = calleTextField.getText().trim();
        String numero = numeroTextField.getText().trim();
        String cp = codigoPostalTextField.getText().trim();
        String ciudad = ciudadTextField.getText().trim();
        String provincia = provinciaTextField.getText().trim();


        // 1. Validación de campos obligatorios
        if (calle.isEmpty() || numero.isEmpty() || cp.isEmpty() || ciudad.isEmpty() || provincia.isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, complete Calle, Número, Código Postal, Ciudad y Provincia.", Alert.AlertType.WARNING);
            return false;
        }

        // 2. Validación de Calle (No debe ser solo números)
        if (calle.matches(regexNumeros)) {
            mostrarAlerta("Advertencia", "El campo 'Calle' debe contener al menos una letra.", Alert.AlertType.WARNING);
            return false;
        }

        // 3. Validación de Número (Solo números)
        if (!numero.matches(regexNumeros)) {
            mostrarAlerta("Advertencia", "El campo 'Número' debe contener solo números.", Alert.AlertType.WARNING);
            return false;
        }

        // 4. Validación de Código Postal (Solo números y 4 dígitos)
        if (!cp.matches(regexNumeros)) {
            mostrarAlerta("Advertencia", "El campo 'Código Postal' debe contener solo números.", Alert.AlertType.WARNING);
            return false;
        }
        if (cp.length() != 4) {
            mostrarAlerta("Advertencia", "El código postal debe tener exactamente 4 dígitos.", Alert.AlertType.WARNING);
            return false;
        }

        // 5. Validación de Ciudad (Solo letras y espacios)
        if (!ciudad.matches(regexLetras)) {
            mostrarAlerta("Advertencia", "El campo 'Ciudad' solo debe contener letras y espacios.", Alert.AlertType.WARNING);
            return false;
        }

        // 6. Validación de Provincia (Solo letras y espacios)
        if (!provincia.matches(regexLetras)) {
            mostrarAlerta("Advertencia", "El campo 'Provincia' solo debe contener letras y espacios.", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancelar() {
        Stage stage = (Stage) cancelarButton.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void handleHelpButton() {
        // Creamos una nueva alerta de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuramos el título y los encabezados del mensaje
        alert.setTitle("Ayuda - Menu De Visualizacion y Modificación de Direccion");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite la Visualizacion y Modificación de una Direccion en la Base de Datos :\n"
                + "\n"
                + "1. Para Modificar Los Datos de Direccion Haga Click en Modificar.\n"
                + "----------------------------------------------------------------------\n"
                + "2. Ingrese Los Datos Correctos que no tengan (opcional) Escrito en el cuadro para Continuar.\n"
                + "----------------------------------------------------------------------\n"
                + "3. Para Continuar Haga Click en Guardar Cambios o Para Cancelar La Modificación Haga Click en Volver.\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();
    }
}