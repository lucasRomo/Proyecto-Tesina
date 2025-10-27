package app.controller;

import app.dao.PersonaDAO;
import app.model.Cliente;
import app.model.TipoDocumento;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.util.Optional;

public class EdicionDocumentoController {

    @FXML private ChoiceBox<TipoDocumento> tipoDocumentoChoiceBox;
    @FXML private TextField numeroDocumentoField;

    private Cliente clienteParaEditar;
    private ClienteController clienteController;
    private PersonaDAO personaDAO = new PersonaDAO();

    private int idTipoDocumentoOriginal;
    private String numeroDocumentoOriginal;


    /**
     * Inicializa el controlador y configura el ChoiceBox.
     */
    @FXML
    public void initialize() {
        // Configuración para que el ChoiceBox muestre el nombre, pero maneje el objeto TipoDocumento
        tipoDocumentoChoiceBox.setConverter(new StringConverter<TipoDocumento>() {
            @Override
            public String toString(TipoDocumento tipo) {
                return tipo != null ? tipo.getNombreTipo() : "";
            }

            @Override
            public TipoDocumento fromString(String string) {
                return tipoDocumentoChoiceBox.getItems().stream()
                        .filter(t -> t.getNombreTipo().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    /**
     * Método llamado por ClienteController para inyectar datos y referencias.
     */
    public void setClienteParaEdicion(Cliente cliente, ObservableList<TipoDocumento> tiposDocumento, ClienteController controller) {
        this.clienteParaEditar = cliente;
        this.clienteController = controller;
        this.idTipoDocumentoOriginal = cliente.getIdTipoDocumento();
        this.numeroDocumentoOriginal = cliente.getNumeroDocumento();

        // 1. Cargar opciones
        tipoDocumentoChoiceBox.setItems(tiposDocumento);

        // 2. Cargar datos actuales del cliente (Muestra los datos existentes al abrir)
        numeroDocumentoField.setText(cliente.getNumeroDocumento());

        // 3. Seleccionar el Tipo Documento actual
        tiposDocumento.stream()
                .filter(t -> t.getIdTipoDocumento() == cliente.getIdTipoDocumento())
                .findFirst()
                .ifPresent(tipoDocumentoChoiceBox.getSelectionModel()::select);
    }

    @FXML
    private void handleGuardar() {
        TipoDocumento nuevoTipo = tipoDocumentoChoiceBox.getSelectionModel().getSelectedItem();
        String nuevoNumero = numeroDocumentoField.getText().trim();

        // 1. Validación de campos vacíos/solo números
        if (nuevoTipo == null || nuevoNumero.isEmpty() || !validarSoloNumeros(nuevoNumero)) {
            mostrarAlerta("Error de Validación", "Por favor, seleccione un tipo de documento y asegúrese que el número sea válido (solo dígitos).", Alert.AlertType.ERROR);
            return;
        }

        String tipoDocumentoSeleccionado = nuevoTipo.getNombreTipo();

        // ====================================================================
        // === VALIDACIÓN DE LONGITUD DE DOCUMENTO (RESTAURADA) ===============
        // ====================================================================

        if ("DNI".equalsIgnoreCase(tipoDocumentoSeleccionado)) {
            if (nuevoNumero.length() != 8) {
                mostrarAlerta("Advertencia", "El DNI debe tener exactamente 8 caracteres.");
                return;
            }
        } else if ("CUIT".equalsIgnoreCase(tipoDocumentoSeleccionado) || "CUIL".equalsIgnoreCase(tipoDocumentoSeleccionado)) {
            if (nuevoNumero.length() != 11) {
                mostrarAlerta("Advertencia", tipoDocumentoSeleccionado + " debe tener 11 caracteres.");
                return;
            }
        } else if ("Pasaporte".equalsIgnoreCase(tipoDocumentoSeleccionado)) {
            if (nuevoNumero.length() < 6 || nuevoNumero.length() > 20) {
                mostrarAlerta("Advertencia", "El Pasaporte debe tener entre 6 y 20 caracteres.");
                return;
            }
        }

        // ====================================================================
        // === CONTINÚA CON LA LÓGICA DE GUARDADO =============================
        // ====================================================================


        int nuevoTipoId = nuevoTipo.getIdTipoDocumento();

        // 2. Validación de Duplicidad (Usando el método del DAO)
        if (personaDAO.verificarSiDocumentoExisteParaOtro(nuevoTipoId, nuevoNumero, clienteParaEditar.getIdPersona())) {
            mostrarAlerta("Error de Validación", "El Tipo y Número de Documento ya están registrados para otro cliente.", Alert.AlertType.ERROR);
            return;
        }

        // 3. Guardar en la Base de Datos (Usando el método del DAO)
        boolean exito = personaDAO.modificarDocumento(clienteParaEditar.getIdPersona(), nuevoTipoId, nuevoNumero);

        if (exito) {
            // Actualizar el modelo en memoria
            clienteParaEditar.setIdTipoDocumento(nuevoTipoId);
            clienteParaEditar.setNumeroDocumento(nuevoNumero);

            // ******************************************************
            // * LLAMAR AL CONTROLADOR PRINCIPAL PARA REGISTRAR EN HISTORIAL *
            // ******************************************************
            if (clienteController != null) {
                clienteController.registrarCambioDocumentoYRefrescar(
                        clienteParaEditar,
                        this.idTipoDocumentoOriginal,
                        this.numeroDocumentoOriginal
                );
            }

            // La alerta de éxito y el refresh se manejan ahora en ClienteController.
            cerrarVentana();
        } else {
            mostrarAlerta("Error", "No se pudo actualizar el documento en la base de datos.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelar() {
        Optional<ButtonType> result = mostrarAlertaConfirmacion("Confirmar", "¿Está seguro que desea cancelar? Los cambios no guardados se perderán.");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cerrarVentana();
        }
    }

    private void cerrarVentana() {
        Stage stage = (Stage) tipoDocumentoChoiceBox.getScene().getWindow();
        stage.close();
    }

    // =========================================================================
    // === Métodos de Utilidad =================================================
    // =========================================================================

    private boolean validarSoloNumeros(String texto) {
        return texto != null && texto.matches("\\d+");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private Optional<ButtonType> mostrarAlertaConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        return alert.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        mostrarAlerta(titulo, mensaje, Alert.AlertType.WARNING);
    }

    @FXML
    private void handleHelpButton() {
        // Creamos una nueva alerta de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuramos el título y los encabezados del mensaje
        alert.setTitle("Ayuda - Modificación de Tipo de Documento y Nª de Documento");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite la Modificación del Tipo de Documento y el Nº de Documento del Cliente:\n"
                + "\n"
                + "Paso 1: Seleccionar el Tipo de Documento (DNI, CUIL, CUIT, o Pasaporte).\n"
                + "----------------------------------------------------------------------\n"
                + "Paso 2: Ingresar el Nuevo numero de Documento.\n"
                + "----------------------------------------------------------------------\n"
                + "Paso 3: Hacer Click en el Boton Guardar.\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();
    }
}

