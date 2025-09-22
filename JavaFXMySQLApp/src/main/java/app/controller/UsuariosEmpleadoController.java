package app.controller;

import app.dao.DireccionDAO;
import app.model.Cliente;
import app.model.Direccion;
import app.model.Persona;
import app.dao.UsuarioDAO;
import app.dao.PersonaDAO;
import app.controller.UsuarioEmpleadoTableView;
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
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.control.TableCell;
import javafx.util.Callback;

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

        // Configurar la columna de estado con un desplegable editable y colores de fondo
        ObservableList<String> estados = FXCollections.observableArrayList("Activo", "Desactivado");

        Callback<TableColumn<UsuarioEmpleadoTableView, String>, TableCell<UsuarioEmpleadoTableView, String>> cellFactory =
                ChoiceBoxTableCell.forTableColumn(estados);

        EstadoColumn.setCellFactory(column -> {
            TableCell<UsuarioEmpleadoTableView, String> cell = cellFactory.call(column);

            // Sobrescribimos el método updateItem para agregar estilos
            cell.itemProperty().addListener((obs, oldVal, newVal) -> {
                cell.getStyleClass().removeAll("activo-status", "desactivado-status"); // Limpia clases anteriores
                if (newVal != null) {
                    if ("Activo".equalsIgnoreCase(newVal)) {
                        cell.getStyleClass().add("activo-status");
                    } else if ("Desactivado".equalsIgnoreCase(newVal)) {
                        cell.getStyleClass().add("desactivado-status");
                    }
                }
            });
            return cell;
        });

        EstadoColumn.setOnEditCommit(event -> {
            UsuarioEmpleadoTableView usuario = event.getRowValue();
            String nuevoEstado = event.getNewValue();
            usuario.setEstado(nuevoEstado);
            boolean exito = usuarioDAO.modificarUsuariosEmpleados(usuario);
            if (exito) {
                mostrarAlerta("Éxito", "Estado del usuario actualizado correctamente.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el estado.", Alert.AlertType.ERROR);
                usuariosEditableView.refresh();
            }
        });

        accionUsuarioColumn.setCellFactory(param -> new TableCell<UsuarioEmpleadoTableView, Void>() {
            private final Button btn = new Button("Ver Dirección");

            {
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(event -> {
                    UsuarioEmpleadoTableView usuario = getTableView().getItems().get(getIndex());
                    mostrarDireccionUsuario(usuario.getIdPersona());
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

        // Llamada al método refactorizado que carga los datos y configura todos los filtros.
        try {
            cargarDatosyConfigurarFiltros();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los datos de los usuarios.", Alert.AlertType.ERROR);
        }
    }

    private void mostrarDireccionUsuario(int idPersona) {
        Direccion direccion = direccionDAO.obtenerPorId(idPersona);
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


    private void cargarDatosyConfigurarFiltros() throws SQLException {
        // Carga los datos maestros desde la base de datos
        listaUsuariosEmpleados = FXCollections.observableArrayList(usuarioDAO.obtenerUsuariosEmpleados());

        // Inicializa el ChoiceBox
        estadoUsuarioChoiceBox.setItems(FXCollections.observableArrayList("Todos", "Activo", "Desactivado"));
        estadoUsuarioChoiceBox.getSelectionModel().select("Todos");

        // Crea una FilteredList que envuelve la lista de datos.
        filteredData = new FilteredList<>(listaUsuariosEmpleados, p -> true);

        // Agrega un listener para el campo de texto de búsqueda.
        filterField.textProperty().addListener((observable, oldValue, newValue) -> reconfigurarFiltro());

        // Agrega un listener para el ChoiceBox de estado.
        estadoUsuarioChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> reconfigurarFiltro());

        // Envuelve la FilteredList en una SortedList
        SortedList<UsuarioEmpleadoTableView> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(usuariosEditableView.comparatorProperty());
        usuariosEditableView.setItems(sortedData);
    }

    /**
     * Reconfigura el predicado del filtro combinando el texto de búsqueda y el estado.
     */
    private void reconfigurarFiltro() {
        filteredData.setPredicate(usuario -> {
            // Predicado para el filtro de texto
            String lowerCaseFilter = filterField.getText().toLowerCase();
            boolean coincideTexto = lowerCaseFilter.isEmpty() ||
                    usuario.getUsuario().toLowerCase().contains(lowerCaseFilter) ||
                    usuario.getNombre().toLowerCase().contains(lowerCaseFilter) ||
                    (usuario.getApellido() != null && usuario.getApellido().toLowerCase().contains(lowerCaseFilter));

            // Predicado para el filtro de estado
            String selectedEstado = estadoUsuarioChoiceBox.getSelectionModel().getSelectedItem();
            boolean coincideEstado = selectedEstado == null ||
                    "Todos".equals(selectedEstado) ||
                    selectedEstado.equalsIgnoreCase(usuario.getEstado());

            // Devuelve true solo si ambas condiciones son verdaderas
            return coincideTexto && coincideEstado;
        });
    }

    @FXML
    public void handleModificarUsuarioButton() {
        UsuarioEmpleadoTableView selectedUsuario = usuariosEditableView.getSelectionModel().getSelectedItem();
        if (selectedUsuario != null) {
            try {
                boolean exito = usuarioDAO.modificarUsuariosEmpleados(selectedUsuario);
                if (exito) {
                    mostrarAlerta("Éxito", "Usuario modificado exitosamente.", Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Error", "No se pudo modificar el usuario en la base de datos.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Error", "Ocurrió un error al intentar modificar el usuario.", Alert.AlertType.ERROR);
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
    @FXML
    private void handleVolverButton(ActionEvent event) {
        try {
            // Carga el FXML de la pantalla a la que quieres regresar.
            // Asegúrate de que la ruta sea correcta.
            Parent root = FXMLLoader.load(getClass().getResource("/MenuAdmin.fxml"));

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
