package app.controller;

import app.dao.PersonaDAO;
import app.model.TipoDocumento;
import app.dao.DireccionDAO;
import app.dao.TipoDocumentoDAO;
import app.model.Direccion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistroEmpleadoController {
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

    @FXML
    public void initialize() {
        // Carga los tipos de documento desde la base de datos
        List<TipoDocumento> tipos = tipoDocumentoDAO.obtenerTodos();
        for (TipoDocumento tipo : tipos) {
            tipoDocumentoComboBox.getItems().add(tipo.getNombreTipo());
        }
    }

    @FXML
    public void handleSiguienteButton(ActionEvent event) {
        // 1. Ejecutar validaciones de formato y obligatorios
        if (!validarCampos()) {
            return;
        }

        // Obtener datos para validaciones cruzadas y específicas
        String tipoDocumentoSeleccionado = tipoDocumentoComboBox.getValue();
        String numeroDocumento = numeroDocumentoField.getText().trim();
        String email = emailField.getText().trim();
        PersonaDAO personaDAO = new PersonaDAO();

        // 2. Validaciones de longitud del documento
        if (tipoDocumentoSeleccionado == null) {
            mostrarAlerta("Advertencia", "Por favor, seleccione un tipo de documento.");
            return;
        }

        if ("DNI".equals(tipoDocumentoSeleccionado)) {
            if (numeroDocumento.length() != 8) {
                mostrarAlerta("Advertencia", "El DNI debe tener exactamente 8 caracteres.");
                return;
            }
        } else if ("CUIT".equals(tipoDocumentoSeleccionado) || "CUIL".equals(tipoDocumentoSeleccionado)) {
            if (numeroDocumento.length() != 11) {
                mostrarAlerta("Advertencia", "El CUIT/CUIL debe tener 11 caracteres.");
                return;
            }
        } else if ("Pasaporte".equals(tipoDocumentoSeleccionado)) {
            if (numeroDocumento.length() < 6 || numeroDocumento.length() > 20) {
                mostrarAlerta("Advertencia", "El Pasaporte debe tener entre 6 y 20 caracteres.");
                return;
            }
        }

        // 3. Validaciones de duplicidad (Documento y Email)
        if (personaDAO.verificarSiDocumentoExiste(numeroDocumento)) {
            mostrarAlerta("Error de Registro", "El número de documento que ingresó ya se encuentra registrado.");
            return;
        }
        if (personaDAO.verificarSiMailExiste(email)) {
            mostrarAlerta("Error de Registro", "El email que ingresó ya se encuentra registrado.");
            return;
        }

        // 4. Validaciones de longitud de datos obligatorios

        // Validación de Código Postal - SOLO LONGITUD (el formato numérico y 4 dígitos ya se validó en validarCampos)
        // Esta validación es redundante con la de validarCampos, pero se deja por seguridad si se modifica validarCampos.
        if (codigoPostalField.getText().trim().length() != 4) {
            // Este caso ya debería ser manejado por validarCampos, pero se deja como fallback
            return;
        }

        // Validación de longitud del Teléfono (el formato numérico ya se validó en validarCampos)
        int longitudTelefono = telefonoField.getText().trim().length();
        if (longitudTelefono <= 6 || longitudTelefono >= 11) {
            mostrarAlerta("Advertencia", "El número de teléfono debe tener entre 7 y 11 dígitos.");
            return;
        }

        // 5. Proceder al registro de la dirección y navegación
        try {
            // Insertar la dirección y obtener el ID
            Direccion nuevaDireccion = new Direccion(
                    calleField.getText(), numeroField.getText(), pisoField.getText(),
                    departamentoField.getText(), codigoPostalField.getText(),
                    ciudadField.getText(), provinciaField.getText(), paisField.getText()
            );
            int idDireccion = direccionDAO.insertarDireccion(nuevaDireccion);

            if (idDireccion != -1) {
                int idTipoDocumento = tipoDocumentoDAO.obtenerIdPorNombre(tipoDocumentoComboBox.getValue());

                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/registroEmpleadoDatosEspecificos.fxml")));
                Parent root = loader.load();

                EmpleadoController empleadoController = loader.getController();

                empleadoController.setDatosPersona(
                        nombreField.getText(),
                        apellidoField.getText(),
                        idTipoDocumento,
                        numeroDocumentoField.getText(),
                        idDireccion,
                        telefonoField.getText(),
                        emailField.getText()
                );

                // Obtener el 'Stage' (ventana) actual y mostrar la nueva escena
                Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);

                stage.centerOnScreen();
                stage.setTitle("Registro de Empleado");

                stage.setResizable(false);
                stage.show();
            } else {
                mostrarAlerta("Error", "No se pudo registrar la dirección. Intente de nuevo.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Valida formatos básicos y campos obligatorios antes de las validaciones de longitud/duplicidad.
     */
    private boolean validarCampos() {
        String regexNumeros = "\\d+"; // Regex para asegurar que solo sean números

        // Validar que los campos personales obligatorios no estén vacíos
        if (nombreField.getText().isEmpty() || apellidoField.getText().isEmpty() ||
                tipoDocumentoComboBox.getValue() == null || numeroDocumentoField.getText().isEmpty() ||
                emailField.getText().isEmpty() || telefonoField.getText().isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, complete todos los campos personales obligatorios.");
            return false;
        }

        // Validar que los campos de dirección obligatorios no estén vacíos
        if (calleField.getText().isEmpty() || numeroField.getText().isEmpty() ||
                codigoPostalField.getText().isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, complete todos los campos de dirección obligatorios.");
            return false;
        }

        // 1. Validación de Nombre y Apellido (Solo letras y espacios)
        if (!validarSoloLetras(nombreField.getText())) {
            mostrarAlerta("Advertencia", "El campo 'Nombre' solo puede contener letras y espacios.");
            return false;
        }

        if (!validarSoloLetras(apellidoField.getText())) {
            mostrarAlerta("Advertencia", "El campo 'Apellido' solo puede contener letras y espacios.");
            return false;
        }

        // 2. Validación de Calle (Solo letras y espacios)
        if (!validarSoloLetras(calleField.getText())) {
            mostrarAlerta("Advertencia", "El campo 'Calle' solo puede contener letras y espacios.");
            return false;
        }

        // 3. Validación de Número de Calle (Solo números)
        if (!numeroField.getText().matches(regexNumeros)) {
            mostrarAlerta("Advertencia", "El campo 'Número' (calle) debe contener solo números.");
            return false;
        }

        // 4. Validación de Teléfono (Solo dígitos numéricos)
        if (!telefonoField.getText().trim().matches(regexNumeros)) {
            mostrarAlerta("Advertencia", "El campo 'Teléfono' solo debe contener dígitos numéricos (sin guiones ni espacios).");
            return false;
        }

        // 5. Validación de Código Postal (Solo números Y 4 dígitos)
        String cp = codigoPostalField.getText().trim();
        if (!cp.matches("\\d{4}")) {
            mostrarAlerta("Advertencia", "El campo 'Código Postal' debe contener exactamente 4 dígitos numéricos.");
            return false;
        }

        // 6. Validación de Correo Electrónico
        String regexEmail = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(regexEmail);
        Matcher matcher = pattern.matcher(emailField.getText());
        if (!matcher.matches()) {
            mostrarAlerta("Advertencia", "El formato del correo electrónico no es válido.");
            return false;
        }

        return true;
    }

    /**
     * Valida si un texto contiene solo letras (incluyendo ñ, tildes y espacios).
     */
    private boolean validarSoloLetras(String texto) {
        // Expresión regular que permite letras mayúsculas, minúsculas,
        // tildes (áéíóúÁÉÍÓÚ), la letra ñ/Ñ y espacios en blanco.
        String regex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
        return texto.matches(regex);
    }

    @FXML
    private void handleVolverButton(ActionEvent event) {
        // Obtenemos el Node (el botón) que disparó el evento y cerramos la ventana.
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
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
        alert.setTitle("Ayuda - Menu De Creación de Empleado");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite la Creacion de un Usuario Empleado :\n"
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