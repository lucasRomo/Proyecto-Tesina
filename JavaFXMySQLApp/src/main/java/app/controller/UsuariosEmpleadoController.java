package app.controller;

import app.dao.EmpleadoDAO;
import app.model.Empleado;
import app.dao.DireccionDAO;
import app.model.Direccion;
import app.dao.UsuarioDAO;
import app.dao.PersonaDAO;
import app.dao.HistorialActividadDAO;
import app.model.Usuario;
import app.model.Persona;
import javafx.application.Platform;
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
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.sql.Connection; // <-- Importación crucial para transacciones
import java.sql.DriverManager; // <-- Importación crucial para obtener la conexión
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.control.TableCell;
import app.controller.SessionManager;
import javafx.fxml.Initializable;

import static app.controller.MenuController.loadScene;

// IMPLEMENTACIÓN DE LA INTERFAZ INITIALIZABLE
public class UsuariosEmpleadoController implements Initializable {

    // Configuración de la conexión (Asegúrate que estos datos sean correctos)
    private static final String URL = "jdbc:mysql://localhost:3306/proyectotesina";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    @FXML private TableView<UsuarioEmpleadoTableView> usuariosEditableView;
    @FXML private TableColumn<UsuarioEmpleadoTableView, Number> idUsuarioColumn;
    @FXML private TableColumn<UsuarioEmpleadoTableView, String> UsuarioColumn;
    @FXML private TableColumn<UsuarioEmpleadoTableView, String> ContrasenaColumn;
    @FXML private TableColumn<UsuarioEmpleadoTableView, Number> idPersonaColumn;
    @FXML private TableColumn<UsuarioEmpleadoTableView, String> NombreColumn;
    @FXML private TableColumn<UsuarioEmpleadoTableView, String> ApellidoColumn;
    @FXML private TableColumn<UsuarioEmpleadoTableView, Number> SalarioColumn;
    @FXML private TableColumn<UsuarioEmpleadoTableView, String> EstadoColumn;
    @FXML private TableColumn<UsuarioEmpleadoTableView, Void> accionUsuarioColumn;
    @FXML private Button modificarUsuarioButton;
    @FXML private TextField filterField;
    @FXML private ChoiceBox<String> estadoUsuarioChoiceBox;

    private UsuarioDAO usuarioDAO;
    private PersonaDAO personaDAO;
    private EmpleadoDAO empleadoDAO;
    private DireccionDAO direccionDAO;
    private HistorialActividadDAO historialDAO;
    private ObservableList<UsuarioEmpleadoTableView> listaUsuariosEmpleados;
    private FilteredList<UsuarioEmpleadoTableView> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicialización de DAOs
        this.usuarioDAO = new UsuarioDAO();
        this.personaDAO = new PersonaDAO();
        this.empleadoDAO = new EmpleadoDAO();
        this.direccionDAO = new DireccionDAO();
        this.historialDAO = new HistorialActividadDAO();

        // Carga el archivo CSS
        usuariosEditableView.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Configuración de CellValueFactory
        idUsuarioColumn.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        UsuarioColumn.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        ContrasenaColumn.setCellValueFactory(new PropertyValueFactory<>("contrasena"));
        idPersonaColumn.setCellValueFactory(new PropertyValueFactory<>("idPersona"));
        NombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        ApellidoColumn.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        SalarioColumn.setCellValueFactory(new PropertyValueFactory<>("salario"));
        EstadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));

        usuariosEditableView.setEditable(true);

        // Se configura la edición de las celdas y sus validaciones
        NombreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        NombreColumn.setOnEditCommit(event -> {
            String nuevoNombre = event.getNewValue();
            if (nuevoNombre != null && !nuevoNombre.trim().isEmpty() && nuevoNombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
                event.getRowValue().setNombre(nuevoNombre);
            } else {
                mostrarAlerta("Advertencia", "El nombre solo puede contener letras y no puede estar vacío.", Alert.AlertType.WARNING);
                usuariosEditableView.refresh(); // Revierte el valor a su estado original
            }
        });

        // Se configura la edición de la columna de Usuario y su validación
        UsuarioColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        UsuarioColumn.setOnEditCommit(event -> {
            String nuevoUsuario = event.getNewValue();
            if (nuevoUsuario != null && !nuevoUsuario.trim().isEmpty() && !nuevoUsuario.contains(" ")) {
                event.getRowValue().setUsuario(nuevoUsuario);
            } else {
                mostrarAlerta("Advertencia", "El nombre de usuario no puede estar vacío y no puede contener espacios.", Alert.AlertType.WARNING);
                usuariosEditableView.refresh();
            }
        });

        // Se configura la edición de la columna de contraseña.
        ContrasenaColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        ContrasenaColumn.setOnEditCommit(event -> {
            String nuevaContrasena = event.getNewValue();
            if (nuevaContrasena != null && !nuevaContrasena.trim().isEmpty()) {
                event.getRowValue().setContrasena(nuevaContrasena);
            } else {
                mostrarAlerta("Advertencia", "La contraseña no puede estar vacía.", Alert.AlertType.WARNING);
                usuariosEditableView.refresh();
            }
        });

        ApellidoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        ApellidoColumn.setOnEditCommit(event -> {
            String nuevoApellido = event.getNewValue();
            if (nuevoApellido != null && !nuevoApellido.trim().isEmpty() && nuevoApellido.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
                event.getRowValue().setApellido(nuevoApellido);
            } else {
                mostrarAlerta("Advertencia", "El apellido solo puede contener letras y no puede estar vacío.", Alert.AlertType.WARNING);
                usuariosEditableView.refresh();
            }
        });

        // Se configura la edición del Salario con validación para que solo acepte números
        SalarioColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return object == null ? "" : object.toString();
            }

            @Override
            public Number fromString(String string) {
                try {
                    return Double.parseDouble(string);
                } catch (NumberFormatException e) {
                    return null; // Devuelve null si no es un número válido
                }
            }
        }));

        SalarioColumn.setOnEditCommit(event -> {
            Number nuevoSalario = event.getNewValue();
            if (nuevoSalario != null && nuevoSalario.doubleValue() >= 0) {
                event.getRowValue().setSalario(nuevoSalario.doubleValue());
            } else {
                mostrarAlerta("Advertencia", "El salario debe ser un número válido y no puede ser negativo.", Alert.AlertType.WARNING);
                usuariosEditableView.refresh();
            }
        });

        // Se configura la columna de estado para ser un ChoiceBox y aplicar estilos
        EstadoColumn.setCellFactory(column -> new TableCell<UsuarioEmpleadoTableView, String>() {
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>();

            {
                choiceBox.setItems(FXCollections.observableArrayList("Activo", "Desactivado"));
                choiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null && newVal != null && !newVal.equals(oldVal)) {
                        UsuarioEmpleadoTableView usuario = getTableRow().getItem();
                        // Llama al método para manejar el cambio de estado con la validación de contraseña
                        updateUsuarioEstado(usuario, oldVal, newVal);
                    }
                });
            }

            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
                    return;
                }
                super.startEdit();
                choiceBox.getSelectionModel().select(getItem());
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
                    setStyle(null);
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
                    setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
                } else if ("Desactivado".equalsIgnoreCase(item)) {
                    getStyleClass().add("desactivado-cell");
                    setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
                } else {
                    setStyle(null);
                }
            }
        });


        accionUsuarioColumn.setCellFactory(param -> new TableCell<UsuarioEmpleadoTableView, Void>() {
            private final Button btn = new Button("Ver Dirección");

            {
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(event -> {
                    // Petición de contraseña para ver la dirección
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Verificación de Seguridad");
                    dialog.setHeaderText("Ver dirección");
                    dialog.setContentText("Por favor, ingrese su contraseña para ver la dirección:");

                    Optional<String> result = dialog.showAndWait();

                    result.ifPresent(password -> {
                        String loggedInUserPassword = SessionManager.getInstance().getLoggedInUserPassword();
                        if (password.trim().equals(loggedInUserPassword.trim())) {
                            UsuarioEmpleadoTableView usuario = getTableView().getItems().get(getIndex());
                            mostrarDireccionUsuario(usuario.getIdDireccion());
                        } else {
                            mostrarAlerta("Error", "Contraseña incorrecta. No puede ver la dirección.", Alert.AlertType.ERROR);
                        }
                    });
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

        // Intentar cargar los datos al final de la inicialización
        try {
            cargarDatosyConfigurarFiltros();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los datos de los usuarios.", Alert.AlertType.ERROR);
        }
    }

    private void mostrarDireccionUsuario(int idDireccion) {
        Direccion direccion = direccionDAO.obtenerPorId(idDireccion);
        if (direccion == null) {
            mostrarAlerta("Error", "No se encontró la dirección asociada a este Usuario.", Alert.AlertType.ERROR);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/verDireccion.fxml"));
            Parent root = loader.load();

            VerDireccionController direccionController = loader.getController();
            direccionController.setDireccion(direccion);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Dirección del Usuario");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la ventana de dirección.", Alert.AlertType.ERROR);
        }
    }

    private void cargarDatosyConfigurarFiltros() throws SQLException {
        listaUsuariosEmpleados = FXCollections.observableArrayList(usuarioDAO.obtenerUsuariosEmpleados());
        estadoUsuarioChoiceBox.setItems(FXCollections.observableArrayList("Todos", "Activo", "Desactivado"));
        estadoUsuarioChoiceBox.getSelectionModel().select("Todos");
        filteredData = new FilteredList<>(listaUsuariosEmpleados, p -> true);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> reconfigurarFiltro());
        estadoUsuarioChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> reconfigurarFiltro());
        SortedList<UsuarioEmpleadoTableView> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(usuariosEditableView.comparatorProperty());
        usuariosEditableView.setItems(sortedData);
    }

    private void reconfigurarFiltro() {
        filteredData.setPredicate(usuario -> {
            String lowerCaseFilter = filterField.getText().toLowerCase();
            boolean coincideTexto = lowerCaseFilter.isEmpty() ||
                    usuario.getUsuario().toLowerCase().contains(lowerCaseFilter) ||
                    usuario.getNombre().toLowerCase().contains(lowerCaseFilter) ||
                    (usuario.getApellido() != null && usuario.getApellido().toLowerCase().contains(lowerCaseFilter));

            String selectedEstado = estadoUsuarioChoiceBox.getSelectionModel().getSelectedItem();
            boolean coincideEstado = selectedEstado == null ||
                    "Todos".equals(selectedEstado) ||
                    selectedEstado.equalsIgnoreCase(usuario.getEstado());

            return coincideTexto && coincideEstado;
        });
    }

    @FXML
    private void handleModificarUsuarioButton(ActionEvent event) {
        UsuarioEmpleadoTableView selectedUsuario = usuariosEditableView.getSelectionModel().getSelectedItem();
        if (selectedUsuario != null) {
            // Diálogo de confirmación con contraseña
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Confirmar modificación");
            dialog.setHeaderText("Verificación de seguridad");
            dialog.setContentText("Por favor, ingresa tu contraseña para confirmar la modificación:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String inputPassword = result.get().trim();
                String loggedInUserPassword = SessionManager.getInstance().getLoggedInUserPassword().trim();
                if (inputPassword.equals(loggedInUserPassword)) {
                    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                        conn.setAutoCommit(false);

                        int idUsuarioResponsable = SessionManager.getInstance().getLoggedInUserId();
                        // Obtener usuario original con datos de Persona y Empleado
                        Usuario originalUser = usuarioDAO.obtenerUsuarioPorId(selectedUsuario.getIdUsuario(), conn);
                        if (originalUser == null) {
                            mostrarAlerta("Error", "Usuario no encontrado.", Alert.AlertType.ERROR);
                            return;
                        }

                        // Obtener datos originales de Persona
                        Persona originalPersona = personaDAO.obtenerPersonaPorId(originalUser.getIdPersona(), conn);
                        if (originalPersona == null) {
                            mostrarAlerta("Error", "Datos de Persona no encontrados.", Alert.AlertType.ERROR);
                            return;
                        }

                        // Obtener datos originales de Empleado
                        Empleado originalEmpleado = empleadoDAO.obtenerEmpleadoPorId(originalUser.getIdPersona(), conn);
                        if (originalEmpleado == null) {
                            mostrarAlerta("Error", "Datos de Empleado no encontrados.", Alert.AlertType.ERROR);
                            return;
                        }

                        boolean exito = true;

                        // Comparar y registrar cambios en Usuario
                        if (!selectedUsuario.getUsuario().equals(originalUser.getUsuario())) {
                            exito = exito && historialDAO.insertarRegistro(idUsuarioResponsable, "Usuario", "nombre_usuario", selectedUsuario.getIdUsuario(), originalUser.getUsuario(), selectedUsuario.getUsuario(), conn);
                        }
                        if (!selectedUsuario.getContrasena().equals(originalUser.getContrasenia())) {
                            exito = exito && historialDAO.insertarRegistro(idUsuarioResponsable, "Usuario", "contrasena", selectedUsuario.getIdUsuario(), originalUser.getContrasenia(), selectedUsuario.getContrasena(), conn);
                        }

                        // Comparar y registrar cambios en Persona
                        if (!selectedUsuario.getNombre().equals(originalPersona.getNombre())) {
                            exito = exito && historialDAO.insertarRegistro(idUsuarioResponsable, "Persona", "nombre", selectedUsuario.getIdPersona(), originalPersona.getNombre(), selectedUsuario.getNombre(), conn);
                        }
                        if (!selectedUsuario.getApellido().equals(originalPersona.getApellido())) {
                            exito = exito && historialDAO.insertarRegistro(idUsuarioResponsable, "Persona", "apellido", selectedUsuario.getIdPersona(), originalPersona.getApellido(), selectedUsuario.getApellido(), conn);
                        }

                        // Comparar y registrar cambios en Empleado
                        if (selectedUsuario.getSalario() != originalEmpleado.getSalario()) {
                            exito = exito && historialDAO.insertarRegistro(idUsuarioResponsable, "Empleado", "salario", selectedUsuario.getIdPersona(), String.valueOf(originalEmpleado.getSalario()), String.valueOf(selectedUsuario.getSalario()), conn);
                        }

                        // Actualizar las tablas
                        // Actualizar Usuario
                        String updateUsuarioSql = "UPDATE Usuario SET nombre_usuario = ?, contrasena = ? WHERE id_usuario = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(updateUsuarioSql)) {
                            stmt.setString(1, selectedUsuario.getUsuario());
                            stmt.setString(2, selectedUsuario.getContrasena());
                            stmt.setInt(3, selectedUsuario.getIdUsuario());
                            stmt.executeUpdate();
                        }

                        // Actualizar Persona
                        String updatePersonaSql = "UPDATE Persona SET nombre = ?, apellido = ? WHERE id_persona = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(updatePersonaSql)) {
                            stmt.setString(1, selectedUsuario.getNombre());
                            stmt.setString(2, selectedUsuario.getApellido());
                            stmt.setInt(3, selectedUsuario.getIdPersona());
                            stmt.executeUpdate();
                        }

                        // Actualizar Empleado
                        String updateEmpleadoSql = "UPDATE Empleado SET salario = ? WHERE id_persona = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(updateEmpleadoSql)) {
                            stmt.setDouble(1, selectedUsuario.getSalario());
                            stmt.setInt(2, selectedUsuario.getIdPersona());
                            stmt.executeUpdate();
                        }

                        if (exito) {
                            conn.commit();
                            mostrarAlerta("Éxito", "Usuario modificado exitosamente.", Alert.AlertType.INFORMATION);
                            usuariosEditableView.refresh();
                        } else {
                            conn.rollback();
                            mostrarAlerta("Error", "No se pudo modificar el usuario. Se realizó ROLLBACK.", Alert.AlertType.ERROR);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        mostrarAlerta("Error", "Ocurrió un error en la base de datos: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                } else {
                    mostrarAlerta("Error", "Contraseña incorrecta. La modificación ha sido cancelada.", Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Advertencia", "Por favor, seleccione un usuario para modificar.", Alert.AlertType.WARNING);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void redireccionarAInicioSesion(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/inicioSesion.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Inicio de Sesión");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isProcessing = false; // Nueva bandera

    private void updateUsuarioEstado(UsuarioEmpleadoTableView usuario, String oldVal, String newVal) {
        // Evitar re-entrada si ya estamos procesando
        if (isProcessing) {
            return;
        }

        isProcessing = true; // Marcar que estamos procesando

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Confirmar cambio de estado");
        dialog.setHeaderText("Verificación de seguridad");
        dialog.setContentText("Por favor, ingresa tu contraseña para confirmar el cambio de estado:");

        // Deshabilitar la edición de la TableView mientras se muestra el diálogo
        usuariosEditableView.setEditable(false);

        Optional<String> result = dialog.showAndWait();

        if (!result.isPresent()) {
            usuario.setEstado(oldVal);
        } else {
            result.ifPresent(password -> {
                String loggedInUserPassword = SessionManager.getInstance().getLoggedInUserPassword().trim();
                if (password.trim().equals(loggedInUserPassword)) {
                    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                        conn.setAutoCommit(false);

                        int idUsuarioResponsable = SessionManager.getInstance().getLoggedInUserId();
                        boolean exitoHistorial = true;

                        if (!newVal.equals(oldVal)) {
                            exitoHistorial = historialDAO.insertarRegistro(
                                    idUsuarioResponsable,
                                    "Usuario",
                                    "estado",
                                    usuario.getIdUsuario(),
                                    oldVal,
                                    newVal,
                                    conn
                            );
                        }

                        Usuario userToUpdateState = new Usuario();
                        userToUpdateState.setIdUsuario(usuario.getIdUsuario());
                        userToUpdateState.setUsuario(usuario.getUsuario());
                        userToUpdateState.setContrasena(usuario.getContrasena());
                        userToUpdateState.setEstado(newVal);

                        boolean exitoUpdate = usuarioDAO.modificarUsuariosEmpleados(userToUpdateState, conn);

                        if (exitoUpdate && exitoHistorial) {
                            conn.commit();
                            usuario.setEstado(newVal);
                            mostrarAlerta("Éxito", "Estado actualizado exitosamente.", Alert.AlertType.INFORMATION);
                        } else {
                            conn.rollback();
                            mostrarAlerta("Error", "No se pudo actualizar el estado en la base de datos. Se realizó ROLLBACK.", Alert.AlertType.ERROR);
                            usuario.setEstado(oldVal);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        mostrarAlerta("Error de BD", "Fallo en conexión o query: " + e.getMessage() + ". ROLLBACK si aplica.", Alert.AlertType.ERROR);
                        usuario.setEstado(oldVal);
                    }
                } else {
                    mostrarAlerta("Error", "Contraseña incorrecta. La modificación ha sido cancelada.", Alert.AlertType.ERROR);
                    usuario.setEstado(oldVal);
                }
            });
        }

        // Restaurar la edición y refrescar solo si es necesario
        usuariosEditableView.setEditable(true);
        if (!isProcessing) { // Evitar refresco innecesario si ya se cerró
            usuariosEditableView.refresh();
        }
        isProcessing = false; // Restablecer la bandera
    }

    @FXML
    private void handleVolverButton(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/menuAdmin.fxml", "Menú Admin");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
