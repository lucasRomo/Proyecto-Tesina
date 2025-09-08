package app.controller;

import app.model.Cliente;
import app.model.dao.ClienteDAO;
import app.model.dao.PersonaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClienteController {

    @FXML private TableView<Cliente> clientesTableView;
    @FXML private TableColumn<Cliente, String> nombreColumn;
    @FXML private TableColumn<Cliente, String> apellidoColumn;
    @FXML private TableColumn<Cliente, Number> tipoDocumentoColumn;
    @FXML private TableColumn<Cliente, String> numeroDocumentoColumn;
    @FXML private TableColumn<Cliente, String> telefonoColumn;
    @FXML private TableColumn<Cliente, String> emailColumn;
    @FXML private TableColumn<Cliente, Number> idClienteColumn;
    @FXML private TableColumn<Cliente, String> razonSocialColumn;
    @FXML private TableColumn<Cliente, String> personaContactoColumn;
    @FXML private TableColumn<Cliente, String> condicionesPagoColumn;
    @FXML private TableColumn<Cliente, String> estadoColumn;
    @FXML private TextField filterField;
    @FXML private Button nuevoClienteButton;
    @FXML private Button modificarClienteButton;

    private ClienteDAO clienteDAO;
    private PersonaDAO personaDAO;

    public ClienteController() {
        this.clienteDAO = new ClienteDAO();
        this.personaDAO = new PersonaDAO(); // Necesario para la validación de documento
    }

    @FXML
    private void initialize() {
        clientesTableView.setEditable(true);

        nombreColumn.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        apellidoColumn.setCellValueFactory(cellData -> cellData.getValue().apellidoProperty());
        tipoDocumentoColumn.setCellValueFactory(new PropertyValueFactory<>("idTipoDocumento"));
        numeroDocumentoColumn.setCellValueFactory(cellData -> cellData.getValue().numeroDocumentoProperty());
        telefonoColumn.setCellValueFactory(cellData -> cellData.getValue().telefonoProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        idClienteColumn.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        razonSocialColumn.setCellValueFactory(cellData -> cellData.getValue().razonSocialProperty());
        personaContactoColumn.setCellValueFactory(cellData -> cellData.getValue().personaContactoProperty());
        condicionesPagoColumn.setCellValueFactory(cellData -> cellData.getValue().condicionesPagoProperty());
        estadoColumn.setCellValueFactory(cellData -> cellData.getValue().estadoProperty());

        // Configurar las columnas para edición en línea y agregar validaciones
        nombreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nombreColumn.setOnEditCommit(event -> {
            if (validarSoloLetras(event.getNewValue())) {
                event.getRowValue().setNombre(event.getNewValue());
            } else {
                mostrarAlerta("Advertencia", "El nombre solo puede contener letras.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); // Refresca la tabla para revertir el valor
            }
        });

        apellidoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        apellidoColumn.setOnEditCommit(event -> {
            if (validarSoloLetras(event.getNewValue())) {
                event.getRowValue().setApellido(event.getNewValue());
            } else {
                mostrarAlerta("Advertencia", "El apellido solo puede contener letras.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
            }
        });

        numeroDocumentoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        numeroDocumentoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoDocumento = event.getNewValue();
            if (validarNumeroDocumento(cliente.getIdTipoDocumento(), nuevoDocumento, cliente.getIdPersona())) {
                cliente.setNumeroDocumento(nuevoDocumento);
            } else {
                clientesTableView.refresh();
            }
        });

        telefonoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        telefonoColumn.setOnEditCommit(event -> {
            if (validarSoloNumeros(event.getNewValue()) && validarLongitudTelefono(event.getNewValue())) {
                event.getRowValue().setTelefono(event.getNewValue());
            } else {
                mostrarAlerta("Advertencia", "El teléfono solo puede contener de 7 a 11 dígitos.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
            }
        });

        emailColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        emailColumn.setOnEditCommit(event -> {
            if (validarEmail(event.getNewValue())) {
                event.getRowValue().setEmail(event.getNewValue());
            } else {
                mostrarAlerta("Advertencia", "El formato del correo electrónico no es válido.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
            }
        });

        // El resto de columnas (razonSocial, etc.) no tienen validación estricta, pero podrías añadirlas si lo deseas.
        razonSocialColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        razonSocialColumn.setOnEditCommit(event -> event.getRowValue().setRazonSocial(event.getNewValue()));

        personaContactoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        personaContactoColumn.setOnEditCommit(event -> event.getRowValue().setPersonaContacto(event.getNewValue()));

        condicionesPagoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        condicionesPagoColumn.setOnEditCommit(event -> event.getRowValue().setCondicionesPago(event.getNewValue()));

        // Configurar la columna de estado con un desplegable (ChoiceBox)
        ObservableList<String> estados = FXCollections.observableArrayList("Activo", "Desactivado");
        estadoColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn(estados));

        // Manejar el evento de edición de la columna de estado
        estadoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoEstado = event.getNewValue();

            boolean exito = clienteDAO.modificarEstadoCliente(cliente.getIdCliente(), nuevoEstado);

            if (exito) {
                cliente.setEstado(nuevoEstado);
                mostrarAlerta("Éxito", "Estado del cliente actualizado.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el estado.", Alert.AlertType.ERROR);
                clientesTableView.refresh();
            }
        });

        cargarClientesYConfigurarBuscador();
    }

    private void cargarClientesYConfigurarBuscador() {
        ObservableList<Cliente> masterData = clienteDAO.getAllClientes();

        FilteredList<Cliente> filteredData = new FilteredList<>(masterData, p -> true);

        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(cliente -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return cliente.getNombre().toLowerCase().contains(lowerCaseFilter) ||
                        cliente.getApellido().toLowerCase().contains(lowerCaseFilter) ||
                        cliente.getNumeroDocumento().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Cliente> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(clientesTableView.comparatorProperty());
        clientesTableView.setItems(sortedData);
    }

    @FXML
    public void handleRegistrarClienteButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registroCliente.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Registrar Nuevo Cliente");
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el formulario de registro de cliente.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleModificarClienteButton(ActionEvent event) {
        Cliente selectedCliente = clientesTableView.getSelectionModel().getSelectedItem();
        if (selectedCliente != null) {
            // Se asume que las validaciones ya se hicieron en el onEditCommit
            boolean exito = clienteDAO.modificarCliente(selectedCliente);
            if (exito) {
                mostrarAlerta("Éxito", "Cliente modificado exitosamente.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo modificar el cliente en la base de datos.", Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Advertencia", "Por favor, seleccione una fila y modifique los datos antes de guardar.", Alert.AlertType.WARNING);
        }
    }

    // Métodos de validación adaptados para el ClienteController
    private boolean validarSoloLetras(String texto) {
        return texto.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+");
    }

    private boolean validarSoloNumeros(String texto) {
        return texto.matches("\\d+");
    }

    private boolean validarLongitudTelefono(String telefono) {
        int longitudTelefono = telefono.trim().length();
        return longitudTelefono >= 7 && longitudTelefono <= 11;
    }

    private boolean validarEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean validarNumeroDocumento(int idTipoDocumento, String numeroDocumento, int idPersonaActual) {
        String tipoDocumento = obtenerNombreTipoDocumento(idTipoDocumento);
        int longitudDocumento = numeroDocumento.length();

        // Validación de longitud
        if ("DNI".equals(tipoDocumento) && longitudDocumento != 8) {
            mostrarAlerta("Advertencia", "El DNI debe tener 8 caracteres.", Alert.AlertType.WARNING);
            return false;
        } else if (("CUIT".equals(tipoDocumento) || "CUIL".equals(tipoDocumento)) && longitudDocumento != 11) {
            mostrarAlerta("Advertencia", "El " + tipoDocumento + " debe tener 11 caracteres.", Alert.AlertType.WARNING);
            return false;
        } else if ("Pasaporte".equals(tipoDocumento) && (longitudDocumento < 6 || longitudDocumento > 20)) {
            mostrarAlerta("Advertencia", "El Pasaporte debe tener entre 6 y 20 caracteres.", Alert.AlertType.WARNING);
            return false;
        }

        // Validación de existencia, excluyendo el cliente actual
        if (personaDAO.verificarSiDocumentoExiste(numeroDocumento, idPersonaActual)) {
            mostrarAlerta("Error", "El número de documento ya existe.", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }


    private String obtenerNombreTipoDocumento(int idTipoDocumento) {
        // Asumiendo que esta lógica es suficiente para tu caso.
        // En una aplicación real, lo ideal sería cargarlo desde la base de datos.
        if (idTipoDocumento == 1) return "DNI";
        if (idTipoDocumento == 2) return "CUIL";
        if (idTipoDocumento == 3) return "CUIT";
        if (idTipoDocumento == 4) return "Pasaporte";
        return "";
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}