package app.controller;

import app.dao.DireccionDAO;
import app.model.Direccion;
import app.dao.UsuarioDAO;
import app.dao.PersonaDAO;
import app.model.Usuario;
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
    private DireccionDAO direccionDAO;
    private ObservableList<UsuarioEmpleadoTableView> listaUsuariosEmpleados;
    private FilteredList<UsuarioEmpleadoTableView> filteredData;
    private String loggedInUserPassword;
    private String loggedInUsername;
    private int loggedInUserId;


    @FXML
    private void initialize() {
        this.usuarioDAO = new UsuarioDAO();
        this.personaDAO = new PersonaDAO();
        this.direccionDAO = new DireccionDAO();

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

        EstadoColumn.setOnEditCommit(event -> {
            UsuarioEmpleadoTableView usuario = event.getRowValue();
            String estadoOriginal = event.getOldValue();
            String nuevoEstado = event.getNewValue();

            // Si el estado no cambia, no hacemos nada
            if (nuevoEstado == null || nuevoEstado.equals(estadoOriginal)) {
                return;
            }

            // Diálogo de confirmación con contraseña
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Confirmar cambio de estado");
            dialog.setHeaderText("Verificación de seguridad");
            dialog.setContentText("Por favor, ingresa tu contraseña para confirmar el cambio de estado:");

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(password -> {
                if (password.trim().equals(loggedInUserPassword.trim())) {
                    usuario.setEstado(nuevoEstado);
                    boolean exito = usuarioDAO.modificarUsuariosEmpleados(usuario);
                    if (exito) {
                        mostrarAlerta("Éxito", "Estado del usuario actualizado correctamente.", Alert.AlertType.INFORMATION);
                    } else {
                        mostrarAlerta("Error", "No se pudo actualizar el estado en la base de datos.", Alert.AlertType.ERROR);
                        usuariosEditableView.refresh();
                    }
                } else {
                    mostrarAlerta("Error", "Contraseña incorrecta. La modificación ha sido cancelada.", Alert.AlertType.ERROR);
                    usuariosEditableView.refresh();
                }
            });
            // Si el usuario cancela el diálogo, también se revierte el cambio
            if (!result.isPresent()) {
                usuariosEditableView.refresh();
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

    public void setLoggedInUserPassword(String password) {
        this.loggedInUserPassword = password;
    }

    public void setLoggedInUsername(String User) {
        this.loggedInUsername = User;
    }

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
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
                if (loggedInUserPassword != null && password.trim().equals(loggedInUserPassword.trim())) {
                    try {
                        // Obtiene la contraseña original del usuario antes de la modificación
                        String originalContrasena = usuarioDAO.obtenerContrasenaPorUsuario(selectedUsuario.getUsuario());

                        // Realiza la modificación en la base de datos
                        boolean exito = usuarioDAO.modificarUsuariosEmpleados(selectedUsuario);

                        if (exito) {
                            mostrarAlerta("Éxito", "Usuario modificado exitosamente.", Alert.AlertType.INFORMATION);

                            // Si el usuario modificado es el que está logueado y la contraseña ha cambiado, redirigimos
                            if (selectedUsuario.getIdUsuario() == this.loggedInUserId && !selectedUsuario.getContrasena().equals(originalContrasena)) {
                                Alert alertaRedirect = new Alert(Alert.AlertType.INFORMATION);
                                alertaRedirect.setTitle("Credenciales Modificadas");
                                alertaRedirect.setHeaderText(null);
                                alertaRedirect.setContentText("Su Usuario y/o contraseña ha sido modificada. Será redirigido al menú de inicio para volver a iniciar sesión.");
                                alertaRedirect.showAndWait();
                                redireccionarAInicioSesion(event);
                            }
                        } else {
                            mostrarAlerta("Error", "No se pudo modificar el usuario en la base de datos.", Alert.AlertType.ERROR);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        mostrarAlerta("Error", "Ocurrió un error al intentar modificar el usuario.", Alert.AlertType.ERROR);
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
            if (password.trim().equals(loggedInUserPassword.trim())) {
                usuario.setEstado(newVal);
                boolean exito = usuarioDAO.modificarUsuariosEmpleados(usuario);
                if (exito) {
                    mostrarAlerta("Éxito", "Estado del usuario actualizado correctamente.", Alert.AlertType.INFORMATION);
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
