package app.controller;

import app.dao.DireccionDAO;
import app.dao.PersonaDAO;
import app.dao.TipoDocumentoDAO;
import app.model.Direccion;
import app.model.TipoDocumento;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistroController {

    @FXML private TextField nombreField;
    @FXML private TextField apellidoField;
    @FXML private ComboBox<String> tipoDocumentoComboBox;
    @FXML private TextField numeroDocumentoField;
    @FXML private TextField emailField;
    @FXML private TextField telefonoField;
    @FXML private TextField calleField;
    @FXML private TextField numeroField;
    @FXML private TextField pisoField;
    @FXML private TextField departamentoField;
    @FXML private TextField codigoPostalField;
    @FXML private TextField ciudadField;
    @FXML private TextField provinciaField;
    @FXML private TextField paisField;

    private TipoDocumentoDAO tipoDocumentoDAO = new TipoDocumentoDAO();
    private DireccionDAO direccionDAO = new DireccionDAO();
    private PersonaDAO personaDAO = new PersonaDAO();

    private ClienteController clienteController;

    public void setClienteController(ClienteController controller) {
        this.clienteController = controller;
    }

    @FXML
    public void initialize() {
        List<TipoDocumento> tipos = tipoDocumentoDAO.obtenerTodos();
        for (TipoDocumento tipo : tipos) {
            tipoDocumentoComboBox.getItems().add(tipo.getNombreTipo());
        }
    }

    @FXML
    public void handleSiguienteButton(ActionEvent event) {
        if (validarCamposPersonales() && validarCamposDireccion()) {
            String tipoDocumentoSeleccionado = tipoDocumentoComboBox.getValue();
            String numeroDocumento = numeroDocumentoField.getText().trim();

            if ("DNI".equals(tipoDocumentoSeleccionado)) {
                if (numeroDocumento.length() != 8) {
                    mostrarAlerta("Advertencia", "El DNI debe tener exactamente 8 caracteres.");
                    return;
                }
            } else if ("CUIT".equals(tipoDocumentoSeleccionado) || "CUIL".equals(tipoDocumentoSeleccionado)) {
                if (numeroDocumento.length() != 11) {
                    mostrarAlerta("Advertencia", tipoDocumentoSeleccionado + " debe tener 11 caracteres.");
                    return;
                }
            } else if ("Pasaporte".equals(tipoDocumentoSeleccionado)) {
                if (numeroDocumento.length() < 6 || numeroDocumento.length() > 20) {
                    mostrarAlerta("Advertencia", "El Pasaporte debe tener entre 6 y 20 caracteres.");
                    return;
                }
            }

            if (personaDAO.verificarSiDocumentoExiste(numeroDocumento)) {
                mostrarAlerta("Error de Registro", "El número de documento que ingresó ya se encuentra registrado.");
                return;
            }

            // Validación de longitud de Código Postal (ya validado el formato numérico en validarCamposDireccion)
            if (codigoPostalField.getText().trim().length() != 4) {
                mostrarAlerta("Advertencia", "El código postal debe tener exactamente 4 caracteres.");
                return;
            }

            int longitudTelefono = telefonoField.getText().trim().length();
            if (longitudTelefono <= 6 || longitudTelefono > 11) {
                mostrarAlerta("Advertencia", "El número de teléfono debe tener entre 7 y 11 dígitos.");
                return;
            }

            String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(emailField.getText());
            if (!matcher.matches()) {
                mostrarAlerta("Advertencia", "El formato del correo electrónico no es válido.");
                return;
            }

            try {
                Direccion nuevaDireccion = new Direccion(
                        calleField.getText(), numeroField.getText(), pisoField.getText(),
                        departamentoField.getText(), codigoPostalField.getText(),
                        ciudadField.getText(), provinciaField.getText(), paisField.getText()
                );
                int idDireccion = direccionDAO.insertarDireccion(nuevaDireccion);

                if (idDireccion != -1) {
                    int idTipoDocumento = tipoDocumentoDAO.obtenerIdPorNombre(tipoDocumentoComboBox.getValue());

                    // Carga la siguiente vista y su controlador dedicado
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/registroClienteDatosEspecificos.fxml"));
                    Parent root = loader.load();
                    RegistroClienteDatosEspecificosController clienteDatosController = loader.getController();

                    // Pasa la referencia del controlador principal al siguiente controlador
                    clienteDatosController.setClienteController(this.clienteController);

                    // ¡CLAVE! Pasa la referencia de ESTE controlador para que la siguiente ventana pueda cerrarla
                    clienteDatosController.setRegistroController(this);

                    // Pasar los datos a la nueva vista
                    clienteDatosController.setDatosPersona(
                            nombreField.getText(), apellidoField.getText(), idTipoDocumento,
                            numeroDocumentoField.getText(), idDireccion, telefonoField.getText(), emailField.getText()
                    );

                    // Crear una nueva ventana (Stage) para la vista modal
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Datos del Cliente");
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.showAndWait();

                } else {
                    mostrarAlerta("Error", "No se pudo registrar la dirección. Intente de nuevo.");
                }

            } catch (IOException e) {
                e.printStackTrace();
                mostrarAlerta("Error", "No se pudo cargar el formulario de datos del cliente.", Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validarCamposPersonales() {
        String email = emailField.getText().trim();
        if (this.nombreField.getText().isEmpty() ||
                this.tipoDocumentoComboBox.getValue() == null || this.numeroDocumentoField.getText().isEmpty() ||
                this.emailField.getText().isEmpty() || this.telefonoField.getText().isEmpty()) {
            this.mostrarAlerta("Advertencia", "Por favor, complete todos los campos personales obligatorios.");
            return false;
        }
        if (personaDAO.verificarSiMailExiste(email)) {
            mostrarAlerta("Error de Registro", "El email que ingresó ya se encuentra registrado.");
            return false;
        }
        if (!this.apellidoField.getText().isEmpty()){
        if (!this.validarSoloLetras(this.apellidoField.getText())) {
            this.mostrarAlerta("Advertencia", "El apellido no puede contener números. Por favor, ingrese caracteres válidos.");
            return false;
        }}
        if (!this.validarSoloNumeros(this.telefonoField.getText())) {
            this.mostrarAlerta("Advertencia", "El teléfono solo puede contener números.");
            return false;
        }
        return true;
    }

    private boolean validarCamposDireccion() {
        if (calleField.getText().isEmpty() || numeroField.getText().isEmpty() ||
                codigoPostalField.getText().isEmpty() ) {
            mostrarAlerta("Advertencia", "Por favor, complete todos los campos de dirección obligatorios.");
            return false;
        }
        if (!this.validarSoloNumeros(this.numeroField.getText())) {
            this.mostrarAlerta("Advertencia", "El número de calle solo puede contener números.");
            return false;
        }

        // ====================================================================
        // === VALIDACIÓN AGREGADA: CÓDIGO POSTAL SOLO NÚMEROS ===
        // ====================================================================
        if (!this.validarSoloNumeros(this.codigoPostalField.getText())) {
            this.mostrarAlerta("Advertencia", "El Código Postal solo puede contener números.");
            return false;
        }
        // ====================================================================

        return true;
    }

    private boolean validarSoloLetras(String texto) {
        return texto.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+");
    }

    private boolean validarSoloNumeros(String texto) {
        return texto.matches("\\d+");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        mostrarAlerta(titulo, mensaje, Alert.AlertType.WARNING);
    }

    @FXML
    private void handleVolverButton(ActionEvent event) {
        // Obtiene la Stage (ventana) actual desde el evento
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        // Cierra la ventana
        stage.close();
    }

    /**
     * Método público para cerrar esta ventana, llamado desde RegistroClienteDatosEspecificosController
     * en caso de cancelación o guardado exitoso.
     */
    public void cerrarVentana() {
        // Obtiene el Stage usando uno de los elementos de la escena (por ejemplo, nombreField)
        Stage stage = (Stage) nombreField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleHelpButton() {
        // Creamos una nueva alerta de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuramos el título y los encabezados del mensaje
        alert.setTitle("Ayuda - Menu De Creación de un Cliente");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite la Creacion de un Cliente en la Base de Datos :\n"
                + "\n"
                + "1. Ingrese Los Datos Correctos que no tengan (opcional) Escrito en el cuadro para Continuar.\n"
                + "----------------------------------------------------------------------\n"
                + "2. Para Seleccionar el Tipo de Documento haga Click en el *ChoiceBox* y Seleccione una de las opciones Para Continuar.\n"
                + "----------------------------------------------------------------------\n"
                + "3. Para Continuar Haga Click en Siguiente o Para Cancelar el Registro Haga Click en Volver.\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();
    }
}