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
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Set;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.control.TableCell;
import app.controller.SessionManager;
import javafx.fxml.Initializable;
import javafx.scene.control.cell.ChoiceBoxTableCell;

import static app.controller.MenuController.loadScene;

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

    @FXML private TableColumn<UsuarioEmpleadoTableView, Number> idTipoUsuarioColumn;

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

    // Conjunto para rastrear qué usuarios tienen cambios pendientes de guardar
    private Set<UsuarioEmpleadoTableView> usuariosPendientesDeGuardar = new HashSet<>();

    // MODIFICACIÓN CLAVE: Restringir a solo 2 y 4
    private final ObservableList<Number> rolesDisponibles = FXCollections.observableArrayList(2, 4); // 2=Empleado, 4=Administrador

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

        // Configuración para la columna de Tipo de Usuario
        idTipoUsuarioColumn.setCellValueFactory(new PropertyValueFactory<>("idTipoUsuario"));

        usuariosEditableView.setEditable(true);

        // --- Configuración de edición de celdas (Nombre, Usuario, Contrasena, Apellido, Salario - SIN CAMBIOS) ---
        NombreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        NombreColumn.setOnEditCommit(event -> {
            String nuevoNombre = event.getNewValue();
            if (nuevoNombre != null && !nuevoNombre.trim().isEmpty() && nuevoNombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
                event.getRowValue().setNombre(nuevoNombre);
                usuariosPendientesDeGuardar.add(event.getRowValue());
            } else {
                mostrarAlerta("Advertencia", "El nombre solo puede contener letras y no puede estar vacío.", Alert.AlertType.WARNING);
                usuariosEditableView.refresh();
            }
        });

        UsuarioColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        UsuarioColumn.setOnEditCommit(event -> {
            String nuevoUsuario = event.getNewValue();
            if (nuevoUsuario != null && !nuevoUsuario.trim().isEmpty() && !nuevoUsuario.contains(" ")) {
                event.getRowValue().setUsuario(nuevoUsuario);
                usuariosPendientesDeGuardar.add(event.getRowValue());
            } else {
                mostrarAlerta("Advertencia", "El nombre de usuario no puede estar vacío y no puede contener espacios.", Alert.AlertType.WARNING);
                usuariosEditableView.refresh();
            }
        });

        ContrasenaColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        ContrasenaColumn.setOnEditCommit(event -> {
            String nuevaContrasena = event.getNewValue();
            if (nuevaContrasena != null && !nuevaContrasena.trim().isEmpty()) {
                event.getRowValue().setContrasena(nuevaContrasena);
                usuariosPendientesDeGuardar.add(event.getRowValue());
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
                usuariosPendientesDeGuardar.add(event.getRowValue());
            } else {
                mostrarAlerta("Advertencia", "El apellido solo puede contener letras y no puede estar vacío.", Alert.AlertType.WARNING);
                usuariosEditableView.refresh();
            }
        });

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
                    return null;
                }
            }
        }));

        SalarioColumn.setOnEditCommit(event -> {
            Number nuevoSalario = event.getNewValue();
            if (nuevoSalario != null && nuevoSalario.doubleValue() >= 0) {
                event.getRowValue().setSalario(nuevoSalario.doubleValue());
                usuariosPendientesDeGuardar.add(event.getRowValue());
            } else {
                mostrarAlerta("Advertencia", "El salario debe ser un número válido y no puede ser negativo.", Alert.AlertType.WARNING);
                usuariosEditableView.refresh();
            }
        });

        // --- Configuración para la columna de ID Tipo de Usuario (Rol) ---
        idTipoUsuarioColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                // Conversión para mostrar el nombre en lugar del ID
                if (object != null) {
                    int id = object.intValue();
                    if (id == 4) return "4 (Administrador)";
                    if (id == 2) return "2 (Empleado)";
                    return object.toString();
                }
                return "";
            }

            @Override
            public Number fromString(String string) {
                // Si el ChoiceBox devuelve un string con el formato "X (Rol)", extraemos solo X
                try {
                    String idStr = string.split(" ")[0];
                    return Integer.parseInt(idStr);
                } catch (Exception e) {
                    return null;
                }
            }
        }, rolesDisponibles)); // Usa la lista restringida (2, 4)

        idTipoUsuarioColumn.setOnEditCommit(event -> {
            Number nuevoIdTipoUsuario = event.getNewValue();

            // VALIDACIÓN CLAVE: Solo permitir 2 o 4
            if (nuevoIdTipoUsuario != null && (nuevoIdTipoUsuario.intValue() == 2 || nuevoIdTipoUsuario.intValue() == 4)) {
                event.getRowValue().setIdTipoUsuario(nuevoIdTipoUsuario.intValue());
                usuariosPendientesDeGuardar.add(event.getRowValue());
            } else {
                mostrarAlerta("Advertencia", "El ID de Rol solo puede ser 2 (Empleado) o 4 (Administrador).", Alert.AlertType.WARNING);
                usuariosEditableView.refresh();
            }
        });


        // --- Configuración de EstadoColumn (SIMPLIFICADA - SOLO EDICIÓN VISUAL) ---
        // --- Configuración de EstadoColumn (SOLO PARA MOSTRAR CHOICEBOX) ---
        EstadoColumn.setCellFactory(column -> new TableCell<UsuarioEmpleadoTableView, String>() {
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList("Activo", "Desactivado"));

            {
                // Listener que se activa cuando CAMBIA el valor SELECCIONADO en el ChoiceBox
                choiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    // SI la celda está en modo edición Y el valor es nuevo,
                    // intenta confirmar la edición con ese nuevo valor.
                    if (isEditing() && newVal != null && !newVal.equals(getItem())) { // Compara con getItem() que es el valor original
                        commitEdit(newVal); // Esto disparará setOnEditCommit de la COLUMNA
                    }
                });
                // La celda empieza mostrando solo texto
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }

            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable() || isEmpty()) {
                    return; // No iniciar edición si no se cumplen condiciones
                }
                super.startEdit(); // NECESARIO para que la celda entre en modo edición

                // Configurar y mostrar el ChoiceBox
                choiceBox.getSelectionModel().select(getItem()); // Seleccionar valor actual
                setText(null); // Ocultar texto
                setGraphic(choiceBox); // Mostrar ChoiceBox
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY); // Asegurar que solo se vea el ChoiceBox

                // Pedir foco para el ChoiceBox para interacción inmediata
                Platform.runLater(choiceBox::requestFocus);
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit(); // NECESARIO para salir del modo edición correctamente
                // Revertir la vista a solo texto
                setText(getItem());
                setGraphic(null);
                setContentDisplay(ContentDisplay.TEXT_ONLY);
                applyCellStyle(getItem()); // Re-aplicar estilo visual
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("activo-cell", "desactivado-cell"); // Limpiar estilos

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle(null);
                } else {
                    // Si está editando, muestra el ChoiceBox (manejado por startEdit/cancelEdit)
                    // Si NO está editando, muestra el texto
                    if (!isEditing()) {
                        setText(item);
                        setGraphic(null);
                        applyCellStyle(item);
                        setContentDisplay(ContentDisplay.TEXT_ONLY);
                    }
                }
            }

            // Método applyCellStyle se mantiene igual
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

        // --- LÓGICA DE VALIDACIÓN Y GUARDADO (CON SOLUCIÓN DEFINITIVA AL BUG VISUAL) ---
        EstadoColumn.setOnEditCommit(event -> {
            UsuarioEmpleadoTableView usuario = event.getRowValue();
            String newVal = event.getNewValue();
            String oldVal = event.getOldValue();

            // 1. Evitar procesamiento si el valor no cambió
            if (newVal == null || newVal.equals(oldVal)) {
                // Si no hay cambio, forzamos un redibujo para salir del modo edición de forma limpia
                Platform.runLater(() -> event.getTableView().refresh());
                return;
            }

            // 🚨 PASO 2: PEDIR CONTRASEÑA 🚨
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Confirmar cambio de estado");
            dialog.setHeaderText("Verificación de seguridad");
            dialog.setContentText("Por favor, ingresa tu contraseña para confirmar el cambio de estado a '" + newVal + "':");

            Optional<String> result = dialog.showAndWait();

            // 3. Manejo de la respuesta del usuario
            if (!result.isPresent()) {
                // --- CASO 1: USUARIO CANCELA O CIERRA ---
                mostrarAlerta("Advertencia", "Cambio de estado cancelado por el usuario.", Alert.AlertType.WARNING);

                // 🚨 SOLUCIÓN AL BUG VISUAL 🚨
                // Hacemos que la TableView re-lea el valor de la celda original (oldVal)
                Platform.runLater(() -> {
                    // El truco es revertir el valor del modelo EN LA FILA, y luego forzar el refresh
                    usuario.setEstado(oldVal); // Asegurar que el modelo tenga el valor antiguo
                    event.getTableView().refresh();
                    // Alternativa más focalizada: event.getTableView().getTableView().getItems().set(event.getTablePosition().getRow(), usuario);
                });

            } else {
                // --- CASO 2: CONTRASEÑA INGRESADA ---
                String password = result.get().trim();
                String loggedInUserPassword = SessionManager.getInstance().getLoggedInUserPassword().trim();

                if (password.equals(loggedInUserPassword)) {
                    // --- CASO 2a: Contraseña CORRECTA: Llamar a la lógica de DB ---
                    actualizarEstadoEnBaseDeDatos(usuario, oldVal, newVal);
                    // Si la DB tiene éxito, el modelo (usuario) se actualiza a newVal dentro de actualizarEstadoEnBaseDeDatos

                } else {
                    // --- CASO 2b: Contraseña INCORRECTA ---
                    mostrarAlerta("Error", "Contraseña incorrecta. La modificación ha sido cancelada.", Alert.AlertType.ERROR);

                    // 🚨 SOLUCIÓN AL BUG VISUAL 🚨
                    // Hacemos que la TableView re-lea el valor de la celda original (oldVal)
                    Platform.runLater(() -> {
                        // Asegurar que el modelo tenga el valor antiguo y refrescar
                        usuario.setEstado(oldVal);
                        event.getTableView().refresh();
                    });
                }
            }
        });





        // --- Configuración de accionUsuarioColumn (SIN CAMBIOS) ---
        accionUsuarioColumn.setCellFactory(param -> new TableCell<UsuarioEmpleadoTableView, Void>() {
            private final Button btn = new Button("Ver Dirección");

            {
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(event -> {
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

        // Cargar datos
        try {
            cargarDatosyConfigurarFiltros();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los datos de los usuarios.", Alert.AlertType.ERROR);
        }
    }

    // --- Métodos Auxiliares (SIN CAMBIOS) ---

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

        // Diálogo de confirmación con contraseña
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
        UsuarioEmpleadoTableView usuarioLogueadoModificado = null;

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
                int originalIdTipoUsuario = originalUser.getIdTipoUsuario();

                boolean exitoHistorial = true;

                // 2. Comparar y registrar cambios en Historial (Usuario, Contraseña, Nombre, Apellido, Salario)
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

                // Registro de cambio de idTipoUsuario (Rol)
                if (selectedUsuario.getIdTipoUsuario() != originalIdTipoUsuario) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            idUsuarioResponsable,
                            "Persona", // La tabla donde se actualiza el rol
                            "id_tipo_persona",
                            selectedUsuario.getIdPersona(),
                            String.valueOf(originalIdTipoUsuario),
                            String.valueOf(selectedUsuario.getIdTipoUsuario()),
                            conn
                    );
                }

                // 3. Actualizar Tablas

                // Usuario
                String updateUsuarioSql = "UPDATE Usuario SET nombre_usuario = ?, contrasena = ? WHERE id_usuario = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateUsuarioSql)) {
                    stmt.setString(1, selectedUsuario.getUsuario());
                    stmt.setString(2, selectedUsuario.getContrasena());
                    stmt.setInt(3, selectedUsuario.getIdUsuario());
                    stmt.executeUpdate();
                }

                // Persona (ACTUALIZACIÓN CLAVE: Se incluye id_tipo_persona)
                String updatePersonaSql = "UPDATE Persona SET nombre = ?, apellido = ?, id_tipo_persona = ? WHERE id_persona = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updatePersonaSql)) {
                    stmt.setString(1, selectedUsuario.getNombre());
                    stmt.setString(2, selectedUsuario.getApellido());
                    stmt.setInt(3, selectedUsuario.getIdTipoUsuario()); // <-- Se actualiza el rol
                    stmt.setInt(4, selectedUsuario.getIdPersona());
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

        // --- LIMPIEZA Y RESULTADO FINAL (SIN CAMBIOS) ---
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

        if (fallos == 0) {
            mostrarAlerta("Éxito", "Cambios guardados exitosamente.", Alert.AlertType.INFORMATION);
        } else if (exitos > 0) {
            mostrarAlerta("Advertencia", "Se guardaron " + exitos + " usuarios, pero falló el guardado de " + fallos + " usuarios. Verifique los errores individuales.", Alert.AlertType.WARNING);
        } else {
            mostrarAlerta("Error", "Fallo al guardar la modificación. Ningún cambio fue persistido correctamente.", Alert.AlertType.ERROR);
        }

        usuariosPendientesDeGuardar.clear();
        usuariosEditableView.refresh();
        try {
            cargarDatosyConfigurarFiltros();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- Métodos de alerta y redirección (SIN CAMBIOS) ---

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

    private boolean isProcessing = false;

    private void actualizarEstadoEnBaseDeDatos(UsuarioEmpleadoTableView usuarioTableView, String oldVal, String newVal) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false); // Iniciar transacción

            int idUsuarioResponsable = SessionManager.getInstance().getLoggedInUserId();
            boolean exitoHistorial = true;

            // 1. Registrar en Historial si el valor cambió
            if (!newVal.equals(oldVal)) {
                exitoHistorial = historialDAO.insertarRegistro(
                        idUsuarioResponsable,
                        "Empleado", // Tabla donde está el campo 'estado'
                        "estado", // Campo modificado
                        usuarioTableView.getIdPersona(), // ID de la entidad afectada (Empleado usa idPersona)
                        oldVal, // Valor anterior
                        newVal, // Valor nuevo
                        conn    // Conexión transaccional
                );
            }

            // 2. Preparar el objeto Usuario para el DAO
            // TU DAO (modificarUsuariosEmpleados(Usuario, Connection)) espera un objeto Usuario,
            // aunque solo actualice el estado en la tabla Empleado. Creamos uno temporal.
            Usuario userParaDAO = new Usuario();
            userParaDAO.setIdUsuario(usuarioTableView.getIdUsuario()); // Necesario para identificar
            userParaDAO.setEstado(newVal); // El nuevo estado a guardar

            // 3. Llamar al DAO para actualizar el estado en la tabla Empleado (dentro de la transacción)
            // Usamos el método DAO que ya tienes: modificarUsuariosEmpleados(Usuario, Connection)
            boolean exitoUpdate = usuarioDAO.modificarUsuariosEmpleados(userParaDAO, conn);

            // 4. Commit o Rollback
            if (exitoUpdate && exitoHistorial) {
                conn.commit(); // Confirmar transacción
                usuarioTableView.setEstado(newVal); // Actualizar el modelo de la tabla EN MEMORIA
                mostrarAlerta("Éxito", "Estado actualizado exitosamente.", Alert.AlertType.INFORMATION);
                // No es necesario refresh aquí, el commitEdit ya actualiza la celda visualmente.
            } else {
                conn.rollback(); // Deshacer cambios si algo falló
                mostrarAlerta("Error", "No se pudo actualizar el estado o registrar el historial. Se realizó ROLLBACK.", Alert.AlertType.ERROR);
                // Forzar redibujo con el valor antiguo si falla la DB
                Platform.runLater(() -> usuariosEditableView.refresh());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "Fallo en conexión o query: " + e.getMessage() + ". ROLLBACK si aplica.", Alert.AlertType.ERROR);
            // Forzar redibujo con el valor antiguo si falla la DB
            Platform.runLater(() -> usuariosEditableView.refresh());
        } catch (Exception e) { // Captura genérica por si algo más falla
            e.printStackTrace();
            mostrarAlerta("Error Inesperado", "Ocurrió un error: " + e.getMessage(), Alert.AlertType.ERROR);
            Platform.runLater(() -> usuariosEditableView.refresh());
        }
    }

    @FXML
    private void handleVolverButton(ActionEvent event) {
        try {
            loadScene((Node) event.getSource(), "/menuAdmin.fxml", "Menú Admin");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHelpButton() {
        // Creamos una nueva alerta de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuramos el título y los encabezados del mensaje
        alert.setTitle("Ayuda - Gestión de Usuarios Empleados");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite la administración completa de las cuentas de usuario con rol de empleado. Las funciones principales incluyen:\n"
                + "\n"
                + "1. Visualización y Edición: Modifique directamente los campos de la tabla (Usuario, Contraseña, Salario, Dirección, etc) Al hacer doble click en la Columna.\n"
                + "----------------------------------------------------------------------\n"
                + "2. Filtros: Utilice el campo de texto para buscar usuarios por Nombre, Apellido o Usuario, y el *ChoiceBox* para filtrar por Estado (Activo/Inactivo).\n"
                + "----------------------------------------------------------------------\n"
                + "3. Ver Direccion: Para Visualizar o Modificar la Direccion Registrada del Ussuario haga CLick en el boton Ver Direccion.\n"
                + "----------------------------------------------------------------------\n"
                + "4. Guardar Cambios: El botón 'Guardar Cambios' aplica todas las modificaciones realizadas en las celdas de la tabla a la base de datos. (Se le Solicitará la contraseña del Administrador Nuevamente.\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();
    }
}