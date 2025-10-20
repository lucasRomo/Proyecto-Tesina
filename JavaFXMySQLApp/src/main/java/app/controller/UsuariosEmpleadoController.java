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
import java.util.HashSet; // <-- NUEVO: Para rastrear cambios
import java.util.Set; // <-- NUEVO: Para rastrear cambios
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

    // NUEVO: Conjunto para rastrear qué usuarios tienen cambios pendientes de guardar
    private Set<UsuarioEmpleadoTableView> usuariosPendientesDeGuardar = new HashSet<>();

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
                usuariosPendientesDeGuardar.add(event.getRowValue()); // REGISTRA CAMBIO
            } else {
                mostrarAlerta("Advertencia", "El nombre solo puede contener letras y no puede estar vacío.", Alert.AlertType.WARNING);
                usuariosEditableView.refresh();
            }
        });

        // Se configura la edición de la columna de Usuario y su validación
        UsuarioColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        UsuarioColumn.setOnEditCommit(event -> {
            String nuevoUsuario = event.getNewValue();
            if (nuevoUsuario != null && !nuevoUsuario.trim().isEmpty() && !nuevoUsuario.contains(" ")) {
                event.getRowValue().setUsuario(nuevoUsuario);
                usuariosPendientesDeGuardar.add(event.getRowValue()); // REGISTRA CAMBIO
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
                usuariosPendientesDeGuardar.add(event.getRowValue()); // REGISTRA CAMBIO
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
                usuariosPendientesDeGuardar.add(event.getRowValue()); // REGISTRA CAMBIO
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
                usuariosPendientesDeGuardar.add(event.getRowValue()); // REGISTRA CAMBIO
            } else {
                mostrarAlerta("Advertencia", "El salario debe ser un número válido y no puede ser negativo.", Alert.AlertType.WARNING);
                usuariosEditableView.refresh();
            }
        });

        // La columna de estado mantiene su lógica de guardado inmediato dentro de updateUsuarioEstado,
        // ya que implica una validación de seguridad (contraseña) y una transacción inmediata.
        EstadoColumn.setCellFactory(column -> new TableCell<UsuarioEmpleadoTableView, String>() {
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>();

            {
                choiceBox.setItems(FXCollections.observableArrayList("Activo", "Desactivado"));
                choiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null && newVal != null && !newVal.equals(oldVal)) {
                        UsuarioEmpleadoTableView usuario = getTableRow().getItem();
                        // Llama al método para manejar el cambio de estado con la validación de contraseña
                        // ESTE MÉTODO HACE EL GUARDADO INMEDIATO POR SEGURIDAD
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

        if (usuariosPendientesDeGuardar.isEmpty()) {
            mostrarAlerta("Advertencia", "No hay modificaciones pendientes para guardar.", Alert.AlertType.WARNING);
            return;
        }

        // Diálogo de confirmación con contraseña para todos los cambios
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Confirmar Modificaciones Múltiples");
        dialog.setHeaderText("Verificación de seguridad");
        dialog.setContentText("Por favor, ingresa tu contraseña para confirmar el guardado de todos los cambios pendientes:");

        Optional<String> result = dialog.showAndWait();

        if (!result.isPresent()) {
            mostrarAlerta("Advertencia", "Modificación cancelada por el usuario.", Alert.AlertType.INFORMATION);
            return;
        }

        String inputPassword = result.get().trim();
        String loggedInUserPassword = SessionManager.getInstance().getLoggedInUserPassword().trim();
        int loggedInUserId = SessionManager.getInstance().getLoggedInUserId();

        if (!inputPassword.equals(loggedInUserPassword)) {
            mostrarAlerta("Error", "Contraseña incorrecta. La modificación ha sido cancelada.", Alert.AlertType.ERROR);
            return;
        }

        // --- INICIO DEL PROCESO DE GUARDADO MÚLTIPLE ---
        int exitos = 0;
        int fallos = 0;
        UsuarioEmpleadoTableView usuarioLogueadoModificado = null; // Para verificar si el usuario logueado cambió credenciales

        for (UsuarioEmpleadoTableView selectedUsuario : usuariosPendientesDeGuardar) {

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                conn.setAutoCommit(false);
                int idUsuarioResponsable = loggedInUserId;

                // 1. Obtener datos originales para el historial
                Usuario originalUser = usuarioDAO.obtenerUsuarioPorId(selectedUsuario.getIdUsuario(), conn);
                Persona originalPersona = personaDAO.obtenerPersonaPorId(originalUser.getIdPersona(), conn);
                Empleado originalEmpleado = empleadoDAO.obtenerEmpleadoPorId(originalUser.getIdPersona(), conn);

                if (originalUser == null || originalPersona == null || originalEmpleado == null) {
                    mostrarAlerta("Error Interno", "No se encontraron datos originales para el usuario ID: " + selectedUsuario.getIdUsuario() + ". Transacción fallida.", Alert.AlertType.ERROR);
                    fallos++;
                    continue;
                }

                String originalUsuarioNombre = originalUser.getUsuario();
                String originalContrasena = originalUser.getContrasenia();

                boolean exitoHistorial = true;

                // 2. Comparar y registrar cambios en Historial

                if (!selectedUsuario.getUsuario().equals(originalUsuarioNombre)) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(idUsuarioResponsable, "Usuario", "nombre_usuario", selectedUsuario.getIdUsuario(), originalUsuarioNombre, selectedUsuario.getUsuario(), conn);
                }

                if (!selectedUsuario.getContrasena().equals(originalContrasena)) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(idUsuarioResponsable, "Usuario", "contrasena", selectedUsuario.getIdUsuario(), originalContrasena, selectedUsuario.getContrasena(), conn);
                }

                if (!selectedUsuario.getNombre().equals(originalPersona.getNombre())) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(idUsuarioResponsable, "Persona", "nombre", selectedUsuario.getIdPersona(), originalPersona.getNombre(), selectedUsuario.getNombre(), conn);
                }
                if (!selectedUsuario.getApellido().equals(originalPersona.getApellido())) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(idUsuarioResponsable, "Persona", "apellido", selectedUsuario.getIdPersona(), originalPersona.getApellido(), selectedUsuario.getApellido(), conn);
                }

                if (selectedUsuario.getSalario() != originalEmpleado.getSalario()) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(idUsuarioResponsable, "Empleado", "salario", selectedUsuario.getIdPersona(), String.valueOf(originalEmpleado.getSalario()), String.valueOf(selectedUsuario.getSalario()), conn);
                }

                // NOTA: El estado se guarda inmediatamente en updateUsuarioEstado, no es necesario registrarlo aquí.

                // 3. Actualizar Tablas

                // Usuario
                String updateUsuarioSql = "UPDATE Usuario SET nombre_usuario = ?, contrasena = ? WHERE id_usuario = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateUsuarioSql)) {
                    stmt.setString(1, selectedUsuario.getUsuario());
                    stmt.setString(2, selectedUsuario.getContrasena());
                    stmt.setInt(3, selectedUsuario.getIdUsuario());
                    stmt.executeUpdate();
                }

                // Persona
                String updatePersonaSql = "UPDATE Persona SET nombre = ?, apellido = ? WHERE id_persona = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updatePersonaSql)) {
                    stmt.setString(1, selectedUsuario.getNombre());
                    stmt.setString(2, selectedUsuario.getApellido());
                    stmt.setInt(3, selectedUsuario.getIdPersona());
                    stmt.executeUpdate();
                }

                // Empleado
                String updateEmpleadoSql = "UPDATE Empleado SET salario = ? WHERE id_persona = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateEmpleadoSql)) {
                    stmt.setDouble(1, selectedUsuario.getSalario());
                    stmt.setInt(2, selectedUsuario.getIdPersona());
                    stmt.executeUpdate();
                }

                // 4. Commit o Rollback
                if (exitoHistorial) {
                    conn.commit();
                    exitos++;

                    // Si el usuario logueado cambió sus credenciales, lo marcamos para redirección
                    if (selectedUsuario.getIdUsuario() == loggedInUserId &&
                            (!selectedUsuario.getContrasena().equals(originalContrasena) || !selectedUsuario.getUsuario().equals(originalUsuarioNombre))) {
                        usuarioLogueadoModificado = selectedUsuario;
                    }

                } else {
                    conn.rollback();
                    fallos++;
                    mostrarAlerta("Error de Historial", "Fallo al registrar historial para el usuario ID: " + selectedUsuario.getIdUsuario() + ". Se realizó ROLLBACK.", Alert.AlertType.ERROR);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                fallos++;
                mostrarAlerta("Error de BD", "Fallo en conexión o query para usuario ID: " + selectedUsuario.getIdUsuario() + ". " + e.getMessage(), Alert.AlertType.ERROR);
            } catch (Exception e) {
                e.printStackTrace();
                fallos++;
                mostrarAlerta("Error Inesperado", "Error al procesar usuario ID: " + selectedUsuario.getIdUsuario() + ". " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }

        // --- LIMPIEZA Y RESULTADO FINAL ---

        // 1. Redirección si el usuario logueado se modificó
        if (usuarioLogueadoModificado != null) {
            SessionManager.getInstance().clearSession();
            Platform.runLater(() -> {
                Alert alertaRedirect = new Alert(Alert.AlertType.INFORMATION);
                alertaRedirect.setTitle("Credenciales Modificadas");
                alertaRedirect.setHeaderText(null);
                alertaRedirect.setContentText("Su nombre de usuario y/o contraseña ha sido modificado. Será redirigido al menú de inicio para volver a iniciar sesión.");
                alertaRedirect.showAndWait();
                redireccionarAInicioSesion(event);
            });
            return;
        }

        // 2. Mostrar resumen del guardado
        if (fallos == 0) {
            mostrarAlerta("Éxito", "Columna modificada exitosamente.", Alert.AlertType.INFORMATION);
        } else if (exitos > 0) {
            mostrarAlerta("Advertencia", "Se guardaron " + exitos + " usuarios, pero falló el guardado de " + fallos + " usuarios. Verifique los errores individuales.", Alert.AlertType.WARNING);
        } else {
            mostrarAlerta("Error", "Fallo al guardar la modificación. Ningún cambio fue persistido correctamente.", Alert.AlertType.ERROR);
        }

        // 3. Limpiar Set y Refrescar Tabla
        usuariosPendientesDeGuardar.clear();
        usuariosEditableView.refresh();
        try {
            cargarDatosyConfigurarFiltros();
        } catch (SQLException e) {
            e.printStackTrace();
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
            loadScene((Node) event.getSource(), "/inicioSesion.fxml", "Menu De Inicio de Sesion");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isProcessing = false; // Bandera para evitar re-entrada en el cambio de estado

    // Mantiene la lógica de guardado inmediato por requerimiento de seguridad (contraseña)
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

                        // Si el cambio fue exitoso, eliminamos el usuario del set si estaba allí (aunque este cambio
                        // no fue por el botón principal, ayuda a mantener limpio el estado).
                        if (exitoUpdate && exitoHistorial) {
                            conn.commit();
                            usuario.setEstado(newVal);
                            usuariosPendientesDeGuardar.remove(usuario); // Limpia si se guardó aquí
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

        usuariosEditableView.setEditable(true);
        if (!isProcessing) {
            usuariosEditableView.refresh();
        }
        isProcessing = false;
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