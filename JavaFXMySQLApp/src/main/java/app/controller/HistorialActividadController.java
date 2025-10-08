package app.controller;

import app.dao.HistorialActividadDAO; // Asegúrate de que esta ruta sea correcta
import app.model.RegistroActividad; // Asegúrate de que esta ruta sea correcta
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.FilteredList;
import javafx.collections.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import java.sql.Timestamp;

public class HistorialActividadController {

    // --- Componentes FXML ---
    @FXML
    private TableView<RegistroActividad> actividadTableView;

    @FXML
    private TableColumn<RegistroActividad, String> colNombreEmpleado;

    @FXML
    private TableColumn<RegistroActividad, Timestamp> colFechaModificacion;

    @FXML
    private TableColumn<RegistroActividad, String> colTablaAfectada;

    @FXML
    private TableColumn<RegistroActividad, String> colColumnaAfectada;

    @FXML
    private TableColumn<RegistroActividad, Integer> colIdModificado;

    @FXML
    private TableColumn<RegistroActividad, String> colDatoPrevio;

    @FXML
    private TableColumn<RegistroActividad, String> colDatoModificado;

    @FXML
    private TextField filterField;

    @FXML
    private Button volverButton;

    // --- Atributos de Control ---
    private final RegistroActividadDAO registroActividadDAO = new RegistroActividadDAO();
    private ObservableList<RegistroActividad> masterData = FXCollections.observableArrayList();

    /**
     * Inicializa el controlador. Se llama automáticamente después de que el FXML
     * ha sido cargado.
     */
    @FXML
    public void initialize() {
        // 1. Configurar las columnas para el mapeo de datos
        configurarColumnas();

        // 2. Cargar los datos desde la base de datos
        cargarDatos();

        // 3. Configurar el filtrado dinámico (Búsqueda)
        configurarFiltro();
    }

    /**
     * Mapea cada columna de la TableView a la propiedad (getter) del modelo RegistroActividad.
     */
    private void configurarColumnas() {
        // Enlaza la columna con el método getNombreCompletoUsuario() del modelo
        colNombreEmpleado.setCellValueFactory(new PropertyValueFactory<>("nombreCompletoUsuario"));

        // Enlaza la columna con el método getFechaModificacion() del modelo
        colFechaModificacion.setCellValueFactory(new PropertyValueFactory<>("fechaModificacion"));

        // Enlaza la columna con el método getTablaAfectada() del modelo
        colTablaAfectada.setCellValueFactory(new PropertyValueFactory<>("tablaAfectada"));

        // Enlaza la columna con el método getColumnaAfectada() del modelo
        colColumnaAfectada.setCellValueFactory(new PropertyValueFactory<>("columnaAfectada"));

        // Enlaza la columna con el método getIdRegistroModificado() del modelo
        colIdModificado.setCellValueFactory(new PropertyValueFactory<>("idRegistroModificado"));

        // Enlaza la columna con el método getDatoPrevioModificacion() del modelo
        colDatoPrevio.setCellValueFactory(new PropertyValueFactory<>("datoPrevioModificacion"));

        // Enlaza la columna con el método getDatoModificado() del modelo
        colDatoModificado.setCellValueFactory(new PropertyValueFactory<>("datoModificado"));
    }

    /**
     * Carga los datos de la base de datos en la ObservableList.
     */
    private void cargarDatos() {
        // Limpiar la lista maestra para evitar duplicados
        masterData.clear();

        // Obtener la lista del DAO
        List<RegistroActividad> registros = registroActividadDAO.obtenerTodosLosRegistros();

        // Agregar los registros a la lista observable
        masterData.addAll(registros);
    }

    /**
     * Configura la funcionalidad de filtrado dinámico usando FilteredList y SortedList.
     */
    private void configurarFiltro() {
        // 1. Envolver la ObservableList en un FilteredList (filtro)
        FilteredList<RegistroActividad> filteredData = new FilteredList<>(masterData, p -> true);

        // 2. Establecer el predicado de filtro cuando el texto de búsqueda cambia
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(registro -> {
                // Si el campo de filtro está vacío, mostrar todos los registros
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Convertir el texto a minúsculas para una búsqueda sin distinción de mayúsculas
                String lowerCaseFilter = newValue.toLowerCase();

                // Criterios de búsqueda: por nombre de usuario o por tabla afectada
                if (registro.getNombreCompletoUsuario().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Coincidencia con el nombre del empleado
                } else if (registro.getTablaAfectada().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Coincidencia con la tabla afectada
                }
                return false; // No hay coincidencias
            });
        });

        // 3. Envolver la FilteredList en un SortedList (ordenamiento)
        SortedList<RegistroActividad> sortedData = new SortedList<>(filteredData);

        // 4. Enlazar la SortedList con la TableView para que el ordenamiento funcione
        sortedData.comparatorProperty().bind(actividadTableView.comparatorProperty());

        // 5. Aplicar la SortedList a la TableView
        actividadTableView.setItems(sortedData);
    }

    @FXML
    private void handleVolverMenuAdminButton(ActionEvent event) {
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
