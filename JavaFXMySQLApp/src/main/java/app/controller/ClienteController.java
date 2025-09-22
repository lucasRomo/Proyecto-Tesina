package app.controller;

import app.dao.ClienteDAO;
import app.dao.DireccionDAO;
import app.dao.PersonaDAO;
import app.model.Cliente;
import app.model.Direccion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
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
    @FXML private TableColumn<Cliente, Void> accionColumn;

    @FXML private TextField filterField;
    @FXML private ChoiceBox<String> estadoChoiceBox;

    @FXML private Button nuevoClienteButton;
    @FXML private Button modificarClienteButton;
    @FXML private Button refreshButton;

    private ClienteDAO clienteDAO;
    private PersonaDAO personaDAO;
    private DireccionDAO direccionDAO;
    private ObservableList<Cliente> masterData = FXCollections.observableArrayList();
    private FilteredList<Cliente> filteredData;


    public ClienteController() {
        this.clienteDAO = new ClienteDAO();
        this.personaDAO = new PersonaDAO();
        this.direccionDAO = new DireccionDAO();
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

        // Celdas editables y validaciones
        nombreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nombreColumn.setOnEditCommit(event -> {
            if (event.getNewValue() == null || event.getNewValue().trim().isEmpty()) {
                mostrarAlerta("Advertencia", "El nombre no puede quedar vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }
            if (validarSoloLetras(event.getNewValue())) {
                event.getRowValue().setNombre(event.getNewValue());
            } else {
                mostrarAlerta("Advertencia", "El nombre solo puede contener letras.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
            }
        });

        apellidoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        apellidoColumn.setOnEditCommit(event -> {
            if (event.getNewValue() == null || event.getNewValue().trim().isEmpty()) {
                mostrarAlerta("Advertencia", "El apellido no puede quedar vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }
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
            if (nuevoDocumento == null || nuevoDocumento.trim().isEmpty()) {
                mostrarAlerta("Advertencia", "El número de documento no puede quedar vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }
            if (validarNumeroDocumento(cliente.getIdTipoDocumento(), nuevoDocumento, cliente.getIdPersona())) {
                cliente.setNumeroDocumento(nuevoDocumento);
            } else {
                clientesTableView.refresh();
            }
        });

        telefonoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        telefonoColumn.setOnEditCommit(event -> {
            String nuevoTelefono = event.getNewValue();
            if (nuevoTelefono == null || nuevoTelefono.trim().isEmpty()) {
                mostrarAlerta("Advertencia", "El teléfono no puede quedar vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }
            if (validarSoloNumeros(nuevoTelefono) && validarLongitudTelefono(nuevoTelefono)) {
                event.getRowValue().setTelefono(nuevoTelefono);
            } else {
                mostrarAlerta("Advertencia", "El teléfono solo puede contener de 7 a 11 dígitos.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
            }
        });

        emailColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        emailColumn.setOnEditCommit(event -> {
            String nuevoEmail = event.getNewValue();
            if (nuevoEmail == null || nuevoEmail.trim().isEmpty()) {
                mostrarAlerta("Advertencia", "El email no puede quedar vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }
            if (validarEmail(nuevoEmail)) {
                event.getRowValue().setEmail(nuevoEmail);
            } else {
                mostrarAlerta("Advertencia", "El formato del correo electrónico no es válido.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
            }
        });

        razonSocialColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        razonSocialColumn.setOnEditCommit(event -> {
            if (event.getNewValue() == null || event.getNewValue().trim().isEmpty()) {
                mostrarAlerta("Advertencia", "La razón social no puede quedar vacía.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }
            event.getRowValue().setRazonSocial(event.getNewValue());
        });

        personaContactoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        personaContactoColumn.setOnEditCommit(event -> {
            if (event.getNewValue() == null || event.getNewValue().trim().isEmpty()) {
                mostrarAlerta("Advertencia", "La persona de contacto no puede quedar vacía.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }
            event.getRowValue().setPersonaContacto(event.getNewValue());
        });

        condicionesPagoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        condicionesPagoColumn.setOnEditCommit(event -> {
            if (event.getNewValue() == null || event.getNewValue().trim().isEmpty()) {
                mostrarAlerta("Advertencia", "Las condiciones de pago no pueden quedar vacías.", Alert.AlertType.WARNING);
                clientesTableView.refresh();
                return;
            }
            event.getRowValue().setCondicionesPago(event.getNewValue());
        });

        estadoColumn.setCellFactory(column -> new TableCell<Cliente, String>() {
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>();

            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
                    return;
                }
                super.startEdit();

                choiceBox.setItems(FXCollections.observableArrayList("Activo", "Desactivado"));
                choiceBox.getSelectionModel().select(getItem());

                choiceBox.setOnAction(event -> {
                    commitEdit(choiceBox.getSelectionModel().getSelectedItem());
                });

                setGraphic(choiceBox);
                setText(null);
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setGraphic(null);
                setText(getItem());
                applyCellStyle(getItem());
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                getStyleClass().removeAll("activo-cell", "desactivado-cell");

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        choiceBox.getSelectionModel().select(item);
                        setText(null);
                        setGraphic(choiceBox);
                    } else {
                        setGraphic(null);
                        setText(item);
                        applyCellStyle(item);
                    }
                }
            }

            private void applyCellStyle(String item) {
                if ("Activo".equalsIgnoreCase(item)) {
                    getStyleClass().add("activo-cell");
                } else if ("Desactivado".equalsIgnoreCase(item)) {
                    getStyleClass().add("desactivado-cell");
                }
            }
        });

        estadoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoEstado = event.getNewValue();

            cliente.setEstado(nuevoEstado);

            boolean exito = clienteDAO.modificarEstadoCliente(cliente.getIdCliente(), nuevoEstado);

            if (exito) {
                mostrarAlerta("Éxito", "Estado del cliente actualizado.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el estado.", Alert.AlertType.ERROR);
                cliente.setEstado(event.getOldValue());
            }
            clientesTableView.refresh();
        });

        accionColumn.setCellFactory(param -> new TableCell<Cliente, Void>() {
            private final Button btn = new Button("Ver Dirección");

            {
                btn.prefWidthProperty().bind(accionColumn.widthProperty());
                btn.setOnAction(event -> {
                    Cliente cliente = getTableView().getItems().get(getIndex());
                    mostrarDireccionCliente(cliente.getIdDireccion());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        estadoChoiceBox.setItems(FXCollections.observableArrayList("Todos", "Activo", "Desactivado"));
        estadoChoiceBox.getSelectionModel().select("Todos");

        cargarClientesYConfigurarFiltros();
    }

    private void mostrarDireccionCliente(int idDireccion) {
        Direccion direccion = direccionDAO.obtenerPorId(idDireccion);
        if (direccion == null) {
            mostrarAlerta("Error", "No se encontró la dirección asociada a este cliente.", Alert.AlertType.ERROR);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/verDireccion.fxml"));
            Parent root = loader.load();

            VerDireccionController direccionController = loader.getController();
            direccionController.setDireccion(direccion);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Dirección del Cliente");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la ventana de dirección.", Alert.AlertType.ERROR);
        }
    }

    public void refreshClientesTable() {
        String filtroTexto = filterField.getText();
        String filtroEstado = estadoChoiceBox.getValue();

        masterData.clear();
        masterData.addAll(clienteDAO.getAllClientes());

        filterField.setText(filtroTexto);
        estadoChoiceBox.setValue(filtroEstado);

        updateFilteredList();
    }

    private void cargarClientesYConfigurarFiltros() {
        masterData = clienteDAO.getAllClientes();
        filteredData = new FilteredList<>(masterData, p -> true);

        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilteredList();
        });

        estadoChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateFilteredList();
        });

        SortedList<Cliente> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(clientesTableView.comparatorProperty());
        clientesTableView.setItems(sortedData);
    }

    private void updateFilteredList() {
        filteredData.setPredicate(cliente -> {
            String searchText = filterField.getText() == null ? "" : filterField.getText().toLowerCase();
            String selectedStatus = estadoChoiceBox.getSelectionModel().getSelectedItem();

            boolean matchesSearchText = searchText.isEmpty() ||
                    (cliente.getNombre() != null && cliente.getNombre().toLowerCase().contains(searchText)) ||
                    (cliente.getApellido() != null && cliente.getApellido().toLowerCase().contains(searchText)) ||
                    (cliente.getNumeroDocumento() != null && cliente.getNumeroDocumento().toLowerCase().contains(searchText));

            boolean matchesStatus = selectedStatus.equals("Todos") ||
                    selectedStatus.equalsIgnoreCase(cliente.getEstado());

            return matchesSearchText && matchesStatus;
        });
    }

    @FXML
    public void handleRegistrarClienteButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registroCliente.fxml"));
            Parent root = loader.load();

            RegistroController registroController = loader.getController();
            registroController.setClienteController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Registrar Nuevo Cliente");
            stage.showAndWait();

            refreshClientesTable();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el formulario de registro de cliente.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleRefreshButton(ActionEvent event) {
        refreshClientesTable();
    }

    @FXML
    public void handleModificarClienteButton(ActionEvent event) {
        Cliente selectedCliente = clientesTableView.getSelectionModel().getSelectedItem();
        if (selectedCliente != null) {
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


    private boolean validarEmailRepetido(String email){
        boolean bol = true;
        if (personaDAO.verificarSiMailExiste(email)) {
            mostrarAlerta("Error de Registro", "El email que ingresó ya se encuentra registrado.", Alert.AlertType.WARNING);
            return false; // Detiene la ejecución para no ir a la siguiente pantalla
        }
        return bol;
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

        if (personaDAO.verificarSiDocumentoExiste(numeroDocumento, idPersonaActual)) {
            mostrarAlerta("Error", "El número de documento ya existe.", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private String obtenerNombreTipoDocumento(int idTipoDocumento) {
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
    @FXML
    private void handleVolverButton(ActionEvent event) {
        try {
            // Carga el FXML de la pantalla a la que quieres regresar.
            // Asegúrate de que la ruta sea correcta.
            Parent root = FXMLLoader.load(getClass().getResource("/menuAbms.fxml"));

            // Obtiene la Stage (ventana) actual del botón
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Crea una nueva Scene con la pantalla anterior
            Scene scene = new Scene(root);

            // Reemplaza la Scene actual con la nueva
            stage.setScene(scene);
            stage.setTitle("Menú Principal"); // O el título de la pantalla anterior
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Maneja el error si no se puede cargar el archivo FXML
        }
    }
}