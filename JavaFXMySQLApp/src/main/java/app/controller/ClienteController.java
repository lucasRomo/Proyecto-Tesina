package app.controller;

import app.dao.ClienteDAO;
import app.dao.DireccionDAO;
import app.dao.PersonaDAO;
import app.dao.TipoDocumentoDAO;
import app.model.Cliente;
import app.model.Direccion;
import app.model.TipoDocumento;
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
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.application.Platform;

import javafx.geometry.Rectangle2D;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Arrays;

public class ClienteController {

    @FXML private TableView<Cliente> clientesTableView;
    @FXML private TableColumn<Cliente, String> nombreColumn;
    @FXML private TableColumn<Cliente, String> apellidoColumn;
    @FXML private TableColumn<Cliente, String> tipoDocumentoColumn;
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
    private TipoDocumentoDAO tipoDocumentoDAO;

    private ObservableList<Cliente> masterData = FXCollections.observableArrayList();
    private FilteredList<Cliente> filteredData;

    private static final ObservableList<String> RAZON_SOCIAL_OPCIONES = FXCollections.observableArrayList(
            "Responsable Inscripto", "Monotributista", "Persona"
    );

    private ObservableList<TipoDocumento> TIPOS_DOCUMENTO_OPCIONES = FXCollections.observableArrayList();

    private Cliente clienteOriginal;
    private int clienteOriginalIndex = -1;


    public ClienteController() {
        this.clienteDAO = new ClienteDAO();
        this.personaDAO = new PersonaDAO();
        this.direccionDAO = new DireccionDAO();
        this.tipoDocumentoDAO = new TipoDocumentoDAO();
        cargarTiposDocumento();
    }

    private void cargarTiposDocumento() {
        try {
            List<TipoDocumento> tipos = tipoDocumentoDAO.obtenerTodos();
            TIPOS_DOCUMENTO_OPCIONES.addAll(tipos);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error de Carga", "No se pudieron cargar los tipos de documento desde la base de datos.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void initialize() {
        clientesTableView.setEditable(true);

        clientesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                clienteOriginal = new Cliente(newSelection);
                clienteOriginalIndex = clientesTableView.getSelectionModel().getSelectedIndex();
            }
        });

        // ==========================================================
        // === VINCULACIÓN DEL ANCHO DE COLUMNAS PORCENTUAL ========
        // ==========================================================
        idClienteColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.03));
        nombreColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.08));
        apellidoColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.08));
        tipoDocumentoColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.07));
        numeroDocumentoColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.09));
        telefonoColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.07));
        emailColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.18));
        razonSocialColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.10));
        personaContactoColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.10));
        condicionesPagoColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.10));
        estadoColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.05));
        accionColumn.prefWidthProperty().bind(clientesTableView.widthProperty().multiply(0.08));

        // --- Configuración de PropertyValueFactory ---
        nombreColumn.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        apellidoColumn.setCellValueFactory(cellData -> cellData.getValue().apellidoProperty());

        tipoDocumentoColumn.setCellValueFactory(cellData -> {
            int id = cellData.getValue().getIdTipoDocumento();
            String nombre = obtenerNombreTipoDocumento(id);
            return new javafx.beans.property.SimpleStringProperty(nombre);
        });

        numeroDocumentoColumn.setCellValueFactory(cellData -> cellData.getValue().numeroDocumentoProperty());
        telefonoColumn.setCellValueFactory(cellData -> cellData.getValue().telefonoProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        idClienteColumn.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        razonSocialColumn.setCellValueFactory(cellData -> cellData.getValue().razonSocialProperty());
        personaContactoColumn.setCellValueFactory(cellData -> cellData.getValue().personaContactoProperty());
        condicionesPagoColumn.setCellValueFactory(cellData -> cellData.getValue().condicionesPagoProperty());
        estadoColumn.setCellValueFactory(cellData -> cellData.getValue().estadoProperty());

        // =========================================================================================
        // === ONEDITCOMMIT: SOLO ACTUALIZACIÓN DEL MODELO Y REFRESCADO DE FILA ====================
        // =========================================================================================

        // --- Columna Tipo Documento (ChoiceBox) ---
        tipoDocumentoColumn.setCellFactory(column -> {
            return new TableCell<Cliente, String>() {
                private final ChoiceBox<TipoDocumento> choiceBox = new ChoiceBox<>();
                private final StringConverter<TipoDocumento> converter = new StringConverter<TipoDocumento>() {
                    @Override
                    public String toString(TipoDocumento tipo) {
                        return tipo == null ? "" : tipo.getNombreTipo();
                    }
                    @Override
                    public TipoDocumento fromString(String string) {
                        return TIPOS_DOCUMENTO_OPCIONES.stream()
                                .filter(t -> t.getNombreTipo().equals(string))
                                .findFirst()
                                .orElse(null);
                    }
                };

                {
                    choiceBox.setItems(TIPOS_DOCUMENTO_OPCIONES);
                    choiceBox.setConverter(converter);

                    // *** SOLUCIÓN DEFINITIVA: Aplazar el refresco total después del commit ***
                    choiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null && isEditing() && !newVal.equals(oldVal)) {
                            // 1. Commit del valor. Esto dispara setOnEditCommit
                            commitEdit(converter.toString(newVal));

                            // 2. Aplazar el refresco forzado y el cierre de la edición
                            //    para asegurar que el CellValueFactory se ejecute.
                            Platform.runLater(() -> {
                                getTableView().refresh(); // Forzar el repintado (Redundante pero efectivo aquí)
                                cancelEdit();
                            });
                        }
                    });
                }

                @Override
                public void startEdit() {
                    if (!isEmpty()) {
                        super.startEdit();
                        Cliente cliente = getTableView().getItems().get(getIndex());

                        TIPOS_DOCUMENTO_OPCIONES.stream()
                                .filter(t -> t.getIdTipoDocumento() == cliente.getIdTipoDocumento())
                                .findFirst()
                                .ifPresent(choiceBox.getSelectionModel()::select);

                        setGraphic(choiceBox);
                        setText(null);
                    }
                }

                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    setGraphic(null);
                    setText(newValue);
                }


                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setGraphic(null);
                    setText(getItem());
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else if (isEditing()) {
                        setGraphic(choiceBox);
                        setText(null);
                    } else {
                        setGraphic(null);
                        setText(item);
                    }
                }
            };
        });

        // Bloque setOnEditCommit: responsable de actualizar el modelo y forzar el refresco de la fila. (CRUCIAL)
        tipoDocumentoColumn.setOnEditCommit(event -> {
            String nuevoNombreTipo = event.getNewValue();
            Cliente cliente = event.getRowValue();

            TipoDocumento nuevoTipo = TIPOS_DOCUMENTO_OPCIONES.stream()
                    .filter(t -> t.getNombreTipo().equals(nuevoNombreTipo))
                    .findFirst()
                    .orElse(null);

            if (nuevoTipo != null) {
                int nuevoId = nuevoTipo.getIdTipoDocumento();

                // 1. ACTUALIZA EL MODELO
                cliente.setIdTipoDocumento(nuevoId);

                // 2. FUERZA EL RE-INSERTADO DEL OBJETO (CRUCIAL para re-ejecutar CellValueFactory)
                clientesTableView.getItems().set(event.getTablePosition().getRow(), cliente);

            } else {
                mostrarAlerta("Error", "Tipo de documento no encontrado. Se revierte el valor.", Alert.AlertType.ERROR);
                clientesTableView.refresh();
            }
        });

        // --- Columna Número Documento ---
        numeroDocumentoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        numeroDocumentoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoNumDoc = event.getNewValue().trim();

            if (nuevoNumDoc.isEmpty()) {
                mostrarAlerta("Advertencia", "El número de documento no puede quedar vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }

            // *** SOLO ACTUALIZA EL MODELO ***
            cliente.setNumeroDocumento(nuevoNumDoc);
            clientesTableView.refresh();
        });

        // --- Columna Nombre (sin cambios) ---
        nombreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nombreColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoNombre = event.getNewValue().trim();
            if (nuevoNombre.isEmpty()) {
                mostrarAlerta("Advertencia", "El nombre no puede quedar vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            if (!validarSoloLetrasYEspacios(nuevoNombre)) {
                mostrarAlerta("Advertencia", "El nombre solo puede contener letras y espacios.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            cliente.setNombre(nuevoNombre);
            clientesTableView.refresh();
        });

        // --- Columna Apellido (sin cambios) ---
        apellidoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        apellidoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoApellido = event.getNewValue().trim();
            if (nuevoApellido.isEmpty()) {
                mostrarAlerta("Advertencia", "El apellido no puede quedar vacío.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            if (!validarSoloLetrasYEspacios(nuevoApellido)) {
                mostrarAlerta("Advertencia", "El apellido solo puede contener letras y espacios.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            cliente.setApellido(nuevoApellido);
            clientesTableView.refresh();
        });

        // --- Columna Teléfono ---
        telefonoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        telefonoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoTelefono = event.getNewValue().trim();
            if (!nuevoTelefono.isEmpty()) {
                if (!validarSoloNumeros(nuevoTelefono) || !validarLongitudTelefono(nuevoTelefono)) {
                    mostrarAlerta("Advertencia", "El formato del teléfono es inválido o la longitud es incorrecta (7 a 11 dígitos).", Alert.AlertType.WARNING);
                    clientesTableView.refresh(); return;
                }
            }
            cliente.setTelefono(nuevoTelefono.isEmpty() ? null : nuevoTelefono);
            clientesTableView.refresh();
        });

        // --- Columna Email ---
        emailColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        emailColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoEmail = event.getNewValue();
            if(!validarYGuardarEmail(cliente, nuevoEmail, cliente.getEmail())) {
                clientesTableView.refresh();
            }
            clientesTableView.refresh();
        });

        // --- Columna Razón Social (ChoiceBox) ---
        razonSocialColumn.setCellFactory(column -> new TableCell<Cliente, String>() {
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>(RAZON_SOCIAL_OPCIONES);
            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable() || isEmpty()) return;
                super.startEdit();
                choiceBox.getSelectionModel().select(getItem());
                // Asegurar commit al seleccionar
                choiceBox.setOnAction(event -> {
                    commitEdit(choiceBox.getSelectionModel().getSelectedItem());
                    cancelEdit();
                });
                setGraphic(choiceBox); setText(null);
            }
            @Override
            public void cancelEdit() { super.cancelEdit(); setGraphic(null); setText(getItem()); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); }
                else if (isEditing()) { choiceBox.getSelectionModel().select(item); setGraphic(choiceBox); setText(null); }
                else { setGraphic(null); setText(item); }
            }
        });

        razonSocialColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevaRazonSocial = event.getNewValue();
            if (nuevaRazonSocial == null || nuevaRazonSocial.trim().isEmpty()) {
                mostrarAlerta("Advertencia", "La razón social no puede quedar vacía.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            cliente.setRazonSocial(nuevaRazonSocial);
            clientesTableView.refresh();
        });

        // --- Columna Persona Contacto ---
        personaContactoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        personaContactoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevaPersonaContacto = event.getNewValue().trim();
            if (nuevaPersonaContacto.isEmpty()) {
                mostrarAlerta("Advertencia", "La persona de contacto no puede quedar vacía.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            if (!validarSoloLetrasYEspacios(nuevaPersonaContacto)) {
                mostrarAlerta("Advertencia", "La persona de contacto solo puede contener letras y espacios.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            cliente.setPersonaContacto(nuevaPersonaContacto);
            clientesTableView.refresh();
        });

        // --- Columna Condiciones Pago ---
        condicionesPagoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        condicionesPagoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevasCondiciones = event.getNewValue().trim();
            if (nuevasCondiciones.isEmpty()) {
                mostrarAlerta("Advertencia", "Las condiciones de pago no pueden quedar vacías.", Alert.AlertType.WARNING);
                clientesTableView.refresh(); return;
            }
            cliente.setCondicionesPago(nuevasCondiciones);
            clientesTableView.refresh();
        });

        // --- Columna Estado (Guardado Inmediato, sin cambios) ---
        estadoColumn.setCellFactory(column -> new TableCell<Cliente, String>() {
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>();

            {
                choiceBox.setItems(FXCollections.observableArrayList("Activo", "Desactivado"));
                choiceBox.setOnAction(event -> {
                    if (isEditing()) {
                        commitEdit(choiceBox.getSelectionModel().getSelectedItem());
                        cancelEdit();
                    }
                });
            }

            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable() || isEmpty()) return;
                super.startEdit();
                choiceBox.getSelectionModel().select(getItem());
                setGraphic(choiceBox); setText(null);
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit(); setGraphic(null); setText(getItem());
                applyCellStyle(getItem());
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("activo-cell", "desactivado-cell");
                if (empty || item == null) { setText(null); setGraphic(null); setStyle(null); }
                else if (isEditing()) { choiceBox.getSelectionModel().select(item); setText(null); setGraphic(choiceBox); }
                else { setGraphic(null); setText(item); applyCellStyle(item); }
            }

            private void applyCellStyle(String item) {
                if ("Activo".equalsIgnoreCase(item)) { getStyleClass().add("activo-cell"); setStyle("-fx-text-fill: black; -fx-font-weight: bold;"); }
                else if ("Desactivado".equalsIgnoreCase(item)) { getStyleClass().add("desactivado-cell"); setStyle("-fx-text-fill: black; -fx-font-weight: bold;"); }
                else { setStyle(null); }
            }
        });

        estadoColumn.setOnEditCommit(event -> {
            Cliente cliente = event.getRowValue();
            String nuevoEstado = event.getNewValue();
            String estadoOriginal = event.getOldValue();
            cliente.setEstado(nuevoEstado);

            boolean exito = clienteDAO.modificarEstadoCliente(cliente.getIdCliente(), nuevoEstado);
            if (exito) {
                mostrarAlerta("Éxito", "Estado del cliente actualizado.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el estado.", Alert.AlertType.ERROR);
                cliente.setEstado(estadoOriginal);
            }
            clientesTableView.refresh();
        });


        // --- Columna Accion (Direccion) ---
        accionColumn.setCellFactory(param -> new TableCell<Cliente, Void>() {
            private final Button btn = new Button("Direccion");
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

        // --- Configuración de filtros ---
        estadoChoiceBox.setItems(FXCollections.observableArrayList("Todos", "Activo", "Desactivado"));
        estadoChoiceBox.getSelectionModel().select("Todos");

        cargarClientesYConfigurarFiltros();
    }

    // =========================================================================================
    // === MÉTODO PARA GUARDAR LOS CAMBIOS EN LA BASE DE DATOS (VALIDACIÓN CENTRALIZADA) ========
    // =========================================================================================

    @FXML
    public void handleModificarClienteButton(ActionEvent event) {
        Cliente selectedCliente = clientesTableView.getSelectionModel().getSelectedItem();

        if (selectedCliente != null) {
            String emailEnModelo = selectedCliente.getEmail();

            // 1. Validaciones
            // Campos esenciales no vacíos
            if (selectedCliente.getNombre().isEmpty() || selectedCliente.getApellido().isEmpty() || selectedCliente.getNumeroDocumento().isEmpty()) {
                mostrarAlerta("Error de Validación", "Asegúrese de que los campos Nombre, Apellido y Número de Documento no estén vacíos.", Alert.AlertType.ERROR);
                revertirCambios(selectedCliente); // *** REVERTIR EN CASO DE ERROR ***
                return;
            }

            // Formato de Número de Documento (solo dígitos)
            if (!validarSoloNumeros(selectedCliente.getNumeroDocumento())) {
                mostrarAlerta("Error de Validación", "El Número de Documento solo puede contener dígitos.", Alert.AlertType.ERROR);
                revertirCambios(selectedCliente); // *** REVERTIR EN CASO DE ERROR ***
                return;
            }

            // Tipo y Número (Longitud y Duplicidad)
            if (!validarNumeroDocumento(selectedCliente.getIdTipoDocumento(), selectedCliente.getNumeroDocumento(), selectedCliente.getIdPersona())) {
                revertirCambios(selectedCliente); // *** REVERTIR EN CASO DE ERROR ***
                return;
            }

            // Persona de Contacto
            if (!validarSoloLetrasYEspacios(selectedCliente.getPersonaContacto())) {
                mostrarAlerta("Error de Validación", "La persona de contacto solo puede contener letras y espacios.", Alert.AlertType.ERROR);
                revertirCambios(selectedCliente); // *** REVERTIR EN CASO DE ERROR ***
                return;
            }

            // Email
            if (emailEnModelo != null && !emailEnModelo.isEmpty() && !validarFormatoEmail(emailEnModelo)) {
                mostrarAlerta("Error de Validación", "El formato del email ('" + emailEnModelo + "') es inválido.", Alert.AlertType.ERROR);
                revertirCambios(selectedCliente); // *** REVERTIR EN CASO DE ERROR ***
                return;
            }
            // Asumo que tienes un método verificarSiMailExisteParaOtro en PersonaDAO
            if (emailEnModelo != null && !emailEnModelo.isEmpty() && personaDAO.verificarSiMailExisteParaOtro(emailEnModelo, selectedCliente.getIdPersona())) {
                mostrarAlerta("Error de Validación", "El email ingresado ya está registrado para otro cliente.", Alert.AlertType.ERROR);
                revertirCambios(selectedCliente); // *** REVERTIR EN CASO DE ERROR ***
                return;
            }

            // 2. GUARDAR EN DB
            boolean exito = clienteDAO.modificarCliente(selectedCliente);
            if (exito) {
                mostrarAlerta("Éxito", "Cliente modificado y guardado exitosamente en la base de datos.", Alert.AlertType.INFORMATION);

                // Actualiza el objeto clienteOriginal para el siguiente ciclo de edición
                clienteOriginal = new Cliente(selectedCliente);
            } else {
                mostrarAlerta("Error", "No se pudo modificar el cliente en la base de datos.", Alert.AlertType.ERROR);
                revertirCambios(selectedCliente); // *** REVERTIR EN CASO DE FALLA EN LA DB ***
            }
            clientesTableView.refresh();
        } else {
            mostrarAlerta("Advertencia", "Por favor, seleccione un cliente y realice las modificaciones en la tabla antes de guardar.", Alert.AlertType.WARNING);
        }
    }

    /**
     * Revierto los cambios visuales y en el modelo si la validación final falla.
     */
    private void revertirCambios(Cliente clienteModificado) {
        if (clienteOriginal != null && clienteOriginalIndex != -1) {

            // Revertir el estado del objeto Cliente en la lista observable (modelo)
            clienteModificado.setNombre(clienteOriginal.getNombre());
            clienteModificado.setApellido(clienteOriginal.getApellido());
            clienteModificado.setIdTipoDocumento(clienteOriginal.getIdTipoDocumento());
            clienteModificado.setNumeroDocumento(clienteOriginal.getNumeroDocumento());
            clienteModificado.setTelefono(clienteOriginal.getTelefono());
            clienteModificado.setEmail(clienteOriginal.getEmail());
            clienteModificado.setRazonSocial(clienteOriginal.getRazonSocial());
            clienteModificado.setPersonaContacto(clienteOriginal.getPersonaContacto());
            clienteModificado.setCondicionesPago(clienteOriginal.getCondicionesPago());

            // Forzar el refresco de la fila con los valores revertidos
            clientesTableView.getItems().set(clienteOriginalIndex, clienteModificado);

            // Clonar de nuevo para que el objeto 'original' refleje el estado real
            clienteOriginal = new Cliente(clienteModificado);

            clientesTableView.refresh();
        }
    }

    // =========================================================================================
    // === MÉTODOS DE UTILIDAD Y LÓGICA ========================================================
    // =========================================================================================

    private void mostrarDireccionCliente(int idDireccion) {
        Direccion direccion = direccionDAO.obtenerPorId(idDireccion);
        if (direccion == null) {
            mostrarAlerta("Error", "No se encontró la dirección asociada a este cliente.", Alert.AlertType.ERROR);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/verDireccion.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof VerDireccionController) {
                ((VerDireccionController) controller).setDireccion(direccion);
            } else {
                System.err.println("Error: El controlador cargado no es VerDireccionController.");
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Dirección del Cliente");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
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

            Object controller = loader.getController();
            if (controller instanceof RegistroController) {
                ((RegistroController) controller).setClienteController(this);
            } else {
                System.err.println("Error: El controlador cargado no es RegistroController.");
            }

            Stage newStage = new Stage();
            Scene newScene = new Scene(root);
            newStage.setScene(newScene);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double screenHeight = screenBounds.getHeight();

            newStage.setHeight(screenHeight);
            newStage.sizeToScene();

            newStage.setTitle("Registrar Nuevo Cliente");
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.centerOnScreen();

            newStage.setResizable(false);
            newStage.showAndWait();

            refreshClientesTable();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el formulario de registro de cliente. Verifique la ruta del FXML.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleRefreshButton(ActionEvent event) {
        refreshClientesTable();
    }

    // --- Métodos de Validación ---

    private boolean validarSoloLetrasYEspacios(String texto) {
        if (texto == null || texto.trim().isEmpty()) return true;
        return texto.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+");
    }

    private boolean validarSoloNumeros(String texto) {
        return texto.matches("\\d+");
    }

    private boolean validarLongitudTelefono(String telefono) {
        int longitudTelefono = telefono.trim().length();
        return longitudTelefono >= 7 && longitudTelefono <= 11;
    }

    private boolean validarYGuardarEmail(Cliente cliente, String nuevoEmail, String emailOriginal) {
        String trimmedEmail = nuevoEmail != null ? nuevoEmail.trim() : "";

        if (trimmedEmail.isEmpty()) {
            mostrarAlerta("Advertencia", "El email no puede quedar vacío.", Alert.AlertType.WARNING);
            return false;
        }

        if (!validarFormatoEmail(trimmedEmail)) {
            mostrarAlerta("Advertencia", "El formato del correo electrónico no es válido.", Alert.AlertType.WARNING);
            return false;
        }

        if (!trimmedEmail.equalsIgnoreCase(emailOriginal)) {
            if (personaDAO.verificarSiMailExisteParaOtro(trimmedEmail, cliente.getIdPersona())) {
                mostrarAlerta("Error de Modificación", "El email que ingresó ya se encuentra registrado para otro cliente.", Alert.AlertType.WARNING);
                return false;
            }
        }

        cliente.setEmail(trimmedEmail);
        return true;
    }

    private boolean validarFormatoEmail(String email) {
        if (email == null || email.trim().isEmpty()) return true;
        String regex = "^[A-Za-z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[A-Za-z0-9_!#$%&'*+/=?`{|}~^-]+)*@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean validarNumeroDocumento(int idTipoDocumento, String numeroDocumento, int idPersonaActual) {
        String tipoDocumento = obtenerNombreTipoDocumento(idTipoDocumento);
        int longitudDocumento = numeroDocumento != null ? numeroDocumento.length() : 0;

        if (longitudDocumento == 0 && idTipoDocumento > 0) {
            mostrarAlerta("Advertencia", "El número de documento no puede estar vacío para el tipo: " + tipoDocumento, Alert.AlertType.WARNING);
            return false;
        }

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
            mostrarAlerta("Error", "El número de documento ya existe para otra persona.", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private String obtenerNombreTipoDocumento(int idTipoDocumento) {
        return TIPOS_DOCUMENTO_OPCIONES.stream()
                .filter(t -> t.getIdTipoDocumento() == idTipoDocumento)
                .findFirst()
                .map(TipoDocumento::getNombreTipo)
                .orElse("ID " + idTipoDocumento + " (Desconocido)");
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
            Class.forName("app.controller.MenuController");

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // Asumiendo que /menuAbms.fxml es la escena anterior
            Parent root = FXMLLoader.load(getClass().getResource("/menuAbms.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Menú ABMs");
            stage.show();


        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Navegación", "No se pudo cargar la vista anterior.", Alert.AlertType.ERROR);
        } catch (ClassNotFoundException e) {
            mostrarAlerta("Error", "Falta la clase MenuController para la navegación.", Alert.AlertType.ERROR);
        }
    }
}