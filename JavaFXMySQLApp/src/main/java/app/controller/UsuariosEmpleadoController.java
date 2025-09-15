package app.controller;
import app.controller.UsuarioEmpleadoTableView;
import app.model.UsuarioDAO;
import app.model.dao.PersonaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.sql.SQLException;

public class UsuariosEmpleadoController {

    // Se cambió el tipo genérico a UsuarioEmpleadoView
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
    private void initialize() {
        this.usuarioDAO = new UsuarioDAO();
        this.personaDAO = new PersonaDAO();

        // Se cambiaron los CellValueFactory para usar la nueva clase
        idUsuarioColumn.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        UsuarioColumn.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        ContrasenaColumn.setCellValueFactory(new PropertyValueFactory<>("contrasena"));
        idPersonaColumn.setCellValueFactory(new PropertyValueFactory<>("idPersona"));
        NombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        ApellidoColumn.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        SalarioColumn.setCellValueFactory(new PropertyValueFactory<>("salario"));
        EstadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // La tabla ahora es editable
        usuariosEditableView.setEditable(true);

        // Se configura la edición de las celdas
        NombreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        NombreColumn.setOnEditCommit(event -> {
            UsuarioEmpleadoTableView usuario = event.getRowValue();
            usuario.setNombre(event.getNewValue());
            // Lógica para guardar en la base de datos (requiere un método en el DAO)
        });

        ApellidoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        ApellidoColumn.setOnEditCommit(event -> {
            UsuarioEmpleadoTableView usuario = event.getRowValue();
            usuario.setApellido(event.getNewValue());
            // Lógica para guardar en la base de datos (requiere un método en el DAO)
        });

        // Configurar la columna de estado con un desplegable
        ObservableList<String> estados = FXCollections.observableArrayList("Activo", "Desactivado");
        EstadoColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn(estados));

        EstadoColumn.setOnEditCommit(event -> {
            UsuarioEmpleadoTableView usuario = event.getRowValue();
            String nuevoEstado = event.getNewValue();
            // Lógica para guardar el estado en la base de datos
            boolean exito = usuarioDAO.modificarUsuariosEmpleados(usuario);
            if (exito) {
                usuario.setEstado(nuevoEstado);
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el estado.", Alert.AlertType.ERROR);
                usuariosEditableView.refresh();
            }
        });

        // Llamada al método para cargar los datos en la tabla
        cargarDatos();
    }

    // Método para cargar los datos en la tabla
    private void cargarDatos() {
        try {
            // Se asume que UsuarioDAO tiene un método que devuelve una lista de UsuarioEmpleadoView
            listaUsuariosEmpleados = FXCollections.observableArrayList(usuarioDAO.obtenerUsuariosEmpleados());
            usuariosEditableView.setItems(listaUsuariosEmpleados);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los datos de los usuarios.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleModificarUsuarioButton() {
        UsuarioEmpleadoTableView selectedUsuario = usuariosEditableView.getSelectionModel().getSelectedItem();
        if (selectedUsuario != null) {
            try {
                // Lógica para guardar los cambios en la base de datos
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

    // Método de utilidad para mostrar alertas
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}