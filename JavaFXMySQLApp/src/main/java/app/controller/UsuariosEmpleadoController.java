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
import java.sql.SQLException;
import java.util.Optional;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.control.TableCell;
import app.controller.SessionManager;

public class UsuariosEmpleadoController {

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

    @FXML
    private void initialize() {
        this.usuarioDAO = new UsuarioDAO();
        this.personaDAO = new PersonaDAO();
        this.empleadoDAO = new EmpleadoDAO();
        this.direccionDAO = new DireccionDAO();
        this.historialDAO = new HistorialActividadDAO();

        // Carga el archivo CSS
        usuariosEditableView.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

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

    public void handleModificarUsuarioButton(ActionEvent event) {
        UsuarioEmpleadoTableView selectedUsuario = usuariosEditableView.getSelectionModel().getSelectedItem();
        if (selectedUsuario != null) {
            // Se crea un diálogo de entrada para solicitar la contraseña
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Confirmar Modificación");
            dialog.setHeaderText("Verificación de Seguridad");
            dialog.setContentText("Por favor, ingrese su contraseña para confirmar la modificación:");

            // Se obtiene el resultado del diálogo
            Optional<String> result = dialog.showAndWait();

            // Se valida la contraseña ingresada
            result.ifPresent(password -> {
                String loggedInUserPassword = SessionManager.getInstance().getLoggedInUserPassword();
                int loggedInUserId = SessionManager.getInstance().getLoggedInUserId();

                if (loggedInUserPassword != null && password.trim().equals(loggedInUserPassword.trim())) {
                    try {
                        // 1. OBTENER DATOS ORIGINALES DE USUARIO Y PERSONA ANTES DEL CAMBIO
                        Usuario originalUser = usuarioDAO.obtenerUsuarioPorId(selectedUsuario.getIdUsuario());
                        Persona originalPersona = personaDAO.getPersonaById(selectedUsuario.getIdPersona());

                        // OBTENER EMPLEADO ORIGINAL (PUEDE SER NULL)
                        Empleado originalEmpleado = empleadoDAO.getEmpleadoById(selectedUsuario.getIdPersona());

                        if (originalUser == null || originalPersona == null) {
                            mostrarAlerta("Error", "No se encontraron los datos originales para el usuario (Usuario o Persona).", Alert.AlertType.ERROR);
                            return;
                        }

                        // Variables de control y IDs
                        String originalContrasena = originalUser.getContrasenia();
                        String originalUsuarioNombre = originalUser.getUsuario();
                        int idUsuarioResponsable = SessionManager.getInstance().getLoggedInUserId();
                        int idRegistroAfectadoPersona = selectedUsuario.getIdPersona();

                        // 2. Realizar las modificaciones en la base de datos

                        // Crear objetos DTO con los datos modificados del TableView
                        // CORRECCIÓN: Se quita el estado del constructor y se usa el setter.
                        Usuario userToModify = new Usuario(selectedUsuario.getIdUsuario(), selectedUsuario.getUsuario(), selectedUsuario.getContrasena());
                        userToModify.setEstado(selectedUsuario.getEstado());
                        Persona personaToModify = new Persona(selectedUsuario.getIdPersona(), selectedUsuario.getNombre(), selectedUsuario.getApellido());

                        personaToModify.setNumeroDocumento(originalPersona.getNumeroDocumento());
                        personaToModify.setIdTipoDocumento(originalPersona.getIdTipoDocumento());
                        personaToModify.setIdDireccion(originalPersona.getIdDireccion());

                        // 2.1. Actualizar Usuario y Persona (siempre existen)
                        boolean exitoUsuario = usuarioDAO.modificarUsuariosEmpleados(userToModify);
                        boolean exitoPersona = personaDAO.modificarPersona(personaToModify);

                        // 2.2. Actualizar Empleado (solo si existe el registro original)
                        boolean exitoEmpleado = true; // Se asume éxito si no hay registro de empleado

                        // Solo si es un empleado, intentamos modificar su salario
                        if (originalEmpleado != null) {
                            Empleado empleadoToModify = new Empleado(selectedUsuario.getIdPersona(), selectedUsuario.getSalario());
                            exitoEmpleado = empleadoDAO.modificar(empleadoToModify);
                        }

                        boolean exitoGeneral = exitoUsuario && exitoPersona && exitoEmpleado;

                        if (exitoGeneral) {
                            // 3. REGISTRAR CAMBIOS EN EL HISTORIAL (Lógica CLAVE)

                            // 3.1. Comparación de datos de USUARIO (nombre de usuario)
                            if (!selectedUsuario.getUsuario().equals(originalUsuarioNombre)) {
                                historialDAO.insertarRegistro(
                                        idUsuarioResponsable,
                                        "Usuario",
                                        "usuario",
                                        selectedUsuario.getIdUsuario(),
                                        originalUsuarioNombre,
                                        selectedUsuario.getUsuario()
                                );
                            }

                            // 3.2. Comparación de datos de USUARIO (estado)
                            if (!selectedUsuario.getEstado().equals(originalUser.getEstado())) {
                                historialDAO.insertarRegistro(
                                        idUsuarioResponsable,
                                        "Usuario",
                                        "estado",
                                        selectedUsuario.getIdUsuario(),
                                        originalUser.getEstado(),
                                        selectedUsuario.getEstado()
                                );
                            }

                            // 3.3. Comparación de datos de USUARIO (contraseña)
                            if (!selectedUsuario.getContrasena().equals(originalContrasena)) {
                                historialDAO.insertarRegistro(
                                        idUsuarioResponsable,
                                        "Usuario",
                                        "contrasena",
                                        selectedUsuario.getIdUsuario(),
                                        "***(Oculto)***",
                                        "***(Oculto)***"
                                );
                            }

                            // 3.4. Comparación de datos de PERSONA (nombre, apellido)

                            // Nombre
                            if (!String.valueOf(selectedUsuario.getNombre()).equals(String.valueOf(originalPersona.getNombre()))) {
                                historialDAO.insertarRegistro(
                                        idUsuarioResponsable,
                                        "Persona",
                                        "nombre",
                                        idRegistroAfectadoPersona,
                                        originalPersona.getNombre(),
                                        selectedUsuario.getNombre()
                                );
                            }

                            // Apellido
                            if (!String.valueOf(selectedUsuario.getApellido()).equals(String.valueOf(originalPersona.getApellido()))) {
                                historialDAO.insertarRegistro(
                                        idUsuarioResponsable,
                                        "Persona",
                                        "apellido",
                                        idRegistroAfectadoPersona,
                                        originalPersona.getApellido(),
                                        selectedUsuario.getApellido()
                                );
                            }

                            // 3.5. Comparación de datos de EMPLEADO (salario) - Solo si originalEmpleado NO es null
                            if (originalEmpleado != null) {
                                double originalSalario = originalEmpleado.getSalario();
                                int idRegistroAfectadoEmpleado = originalEmpleado.getIdEmpleado(); // Se usa el ID de Empleado

                                if (Double.compare(selectedUsuario.getSalario(), originalSalario) != 0) {
                                    historialDAO.insertarRegistro(
                                            idUsuarioResponsable,
                                            "Empleado",
                                            "salario",
                                            idRegistroAfectadoEmpleado,
                                            String.valueOf(originalSalario),
                                            String.valueOf(selectedUsuario.getSalario())
                                    );
                                }
                            }

                            // 4. Muestra de éxito y redirección
                            mostrarAlerta("Éxito", "Usuario modificado exitosamente. Los cambios han sido registrados.", Alert.AlertType.INFORMATION);

                            // Recargar datos para reflejar los cambios en la tabla y en el historial (si se actualiza)
                            try {
                                cargarDatosyConfigurarFiltros();
                            } catch (SQLException e) {
                                System.err.println("Error al recargar datos después de la modificación: " + e.getMessage());
                                e.printStackTrace();
                            }

                            // Comprobar si el usuario modificado es el que está logueado Y si cambió usuario O contraseña
                            boolean esUsuarioLogueado = selectedUsuario.getIdUsuario() == loggedInUserId;
                            boolean cambioContrasena = !selectedUsuario.getContrasena().equals(originalContrasena);
                            boolean cambioNombreUsuario = !selectedUsuario.getUsuario().equals(originalUsuarioNombre);

                            if (esUsuarioLogueado && (cambioContrasena || cambioNombreUsuario)) {
                                // Cerrar la sesión del usuario actual
                                SessionManager.getInstance().clearSession();

                                Alert alertaRedirect = new Alert(Alert.AlertType.INFORMATION);
                                alertaRedirect.setTitle("Credenciales Modificadas");
                                alertaRedirect.setHeaderText(null);
                                alertaRedirect.setContentText("Su nombre de usuario y/o contraseña ha sido modificado. Será redirigido al menú de inicio para volver a iniciar sesión.");
                                alertaRedirect.showAndWait();
                                redireccionarAInicioSesion(event);
                            }
                        } else {
                            String mensajeErrorDetallado = "No se pudo modificar el usuario en la base de datos. Estado de las actualizaciones: \nUsuario: " + exitoUsuario + "\nPersona: " + exitoPersona + (originalEmpleado != null ? "\nEmpleado: " + exitoEmpleado : "\nEmpleado: No aplicable");
                            mostrarAlerta("Error", mensajeErrorDetallado, Alert.AlertType.ERROR);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        mostrarAlerta("Error", "Ocurrió un error al intentar modificar el usuario. Verifique la consola para detalles. Mensaje: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                } else {
                    mostrarAlerta("Error", "Contraseña incorrecta. La modificación ha sido cancelada.", Alert.AlertType.ERROR);
                }
            });
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

    private void updateUsuarioEstado(UsuarioEmpleadoTableView usuario, String oldVal, String newVal) {
        // Diálogo de confirmación con contraseña
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Confirmar cambio de estado");
        dialog.setHeaderText("Verificación de seguridad");
        dialog.setContentText("Por favor, ingresa tu contraseña para confirmar el cambio de estado:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(password -> {
            String loggedInUserPassword = SessionManager.getInstance().getLoggedInUserPassword();
            if (password.trim().equals(loggedInUserPassword.trim())) {

                // --- Lógica de Historial para Cambio de Estado ---
                int idUsuarioResponsable = SessionManager.getInstance().getLoggedInUserId();

                if (!newVal.equals(oldVal)) {
                    // El estado es un campo de la tabla Usuario
                    historialDAO.insertarRegistro(
                            idUsuarioResponsable,
                            "Usuario",
                            "estado",
                            usuario.getIdUsuario(),
                            oldVal,
                            newVal
                    );
                }
                // --- Fin Lógica de Historial ---

                usuario.setEstado(newVal);
                boolean exito = usuarioDAO.modificarUsuariosEmpleados(usuario);
                if (exito) {
                    mostrarAlerta("Éxito", "Contraseña Correcta, Tiene Permitido Editar El Estado.", Alert.AlertType.INFORMATION);
                    usuariosEditableView.refresh();
                } else {
                    mostrarAlerta("Error", "No se pudo actualizar el estado en la base de datos.", Alert.AlertType.ERROR);
                    usuario.setEstado(oldVal); // Revierte el valor en caso de error
                    usuariosEditableView.refresh();
                }
            } else {
                mostrarAlerta("Error", "Contraseña incorrecta. La modificación ha sido cancelada.", Alert.AlertType.ERROR);
                usuario.setEstado(oldVal); // Revierte el valor
                usuariosEditableView.refresh();
            }
        });
        // Si el usuario cancela, también se revierte
        if (!result.isPresent()) {
            usuario.setEstado(oldVal);
            usuariosEditableView.refresh();
        }
    }



    @FXML
    private void handleVolverButton(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MenuAdmin.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menú Principal");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
