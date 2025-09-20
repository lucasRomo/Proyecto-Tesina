package app.controller;
import app.model.Cliente;
import app.model.UsuarioDAO;
import app.model.dao.PersonaDAO;
import app.controller.UsuarioEmpleadoTableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.util.StringConverter;

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
    @FXML private Button modificarUsuarioButton;
    @FXML private TextField filterField;

    private UsuarioDAO usuarioDAO;
    private PersonaDAO personaDAO;
    private ObservableList<UsuarioEmpleadoTableView> listaUsuariosEmpleados;


    @FXML
    private void initialize() throws SQLException {
        this.usuarioDAO = new UsuarioDAO();
        this.personaDAO = new PersonaDAO();

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


        // Configurar la columna de estado con un desplegable
        ObservableList<String> estados = FXCollections.observableArrayList("Activo", "Desactivado");
        EstadoColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn(estados));

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

        cargarDatos();
        cargarUsuariosyConfigurarBuscador();
    }

    private void cargarDatos() {
        try {
            listaUsuariosEmpleados = FXCollections.observableArrayList(usuarioDAO.obtenerUsuariosEmpleados());
            usuariosEditableView.setItems(listaUsuariosEmpleados);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los datos de los usuarios.", Alert.AlertType.ERROR);
        }
    }

    private void cargarUsuariosyConfigurarBuscador() throws SQLException {
        ObservableList<UsuarioEmpleadoTableView> masterData = usuarioDAO.obtenerUsuariosEmpleados();

        FilteredList<UsuarioEmpleadoTableView> filteredData = new FilteredList<>(masterData, p -> true);

        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(usuario -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return usuario.getUsuario().toLowerCase().contains(lowerCaseFilter) ||
                        usuario.getNombre().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<UsuarioEmpleadoTableView> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(usuariosEditableView.comparatorProperty());
        usuariosEditableView.setItems(sortedData);
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
}
