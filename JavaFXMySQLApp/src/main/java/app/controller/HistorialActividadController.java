package app.controller;

import app.controller.HistorialActividadTableView;
import app.dao.HistorialActividadDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox; // *** IMPORTAR ChoiceBox ***
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static app.controller.MenuController.loadScene;

public class HistorialActividadController {

    @FXML private TableView<HistorialActividadTableView> historialTableView;
    @FXML private TableColumn<HistorialActividadTableView, Timestamp> fechaModificacionColumn;
    @FXML private TableColumn<HistorialActividadTableView, String> nombreUsuarioColumn;
    @FXML private TableColumn<HistorialActividadTableView, String> tablaAfectadaColumn;
    @FXML private TableColumn<HistorialActividadTableView, String> columnaAfectadaColumn;
    @FXML private TableColumn<HistorialActividadTableView, Number> idRegistroModificadoColumn;
    @FXML private TableColumn<HistorialActividadTableView, String> datoPrevioColumn;
    @FXML private TableColumn<HistorialActividadTableView, String> datoModificadoColumn;

    // *** NUEVOS ELEMENTOS FXML ***
    @FXML private ChoiceBox<String> cmbFiltroUsuario;

    private HistorialActividadDAO historialDAO;
    // *** NUEVAS LISTAS PARA FILTRADO ***
    private ObservableList<HistorialActividadTableView> masterData;
    private FilteredList<HistorialActividadTableView> filteredData;


    @FXML
    public void initialize() {
        historialDAO = new HistorialActividadDAO();

        // (Configuración del ancho de columnas permanece igual)

        // 1. Configurar las Cell Value Factories
        fechaModificacionColumn.setCellValueFactory(new PropertyValueFactory<>("fechaModificacion"));
        nombreUsuarioColumn.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        tablaAfectadaColumn.setCellValueFactory(new PropertyValueFactory<>("tablaAfectada"));
        columnaAfectadaColumn.setCellValueFactory(new PropertyValueFactory<>("columnaAfectada"));
        idRegistroModificadoColumn.setCellValueFactory(new PropertyValueFactory<>("idRegistroModificado"));
        datoPrevioColumn.setCellValueFactory(new PropertyValueFactory<>("datoPrevio"));
        datoModificadoColumn.setCellValueFactory(new PropertyValueFactory<>("datoModificado"));


        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        fechaModificacionColumn.setCellFactory(column -> new TableCell<HistorialActividadTableView, Timestamp>() {
            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    LocalDateTime localDateTime = item.toLocalDateTime();
                    setText(localDateTime.format(formatter));
                }
            }
        });

        // 3. Cargar los datos y configurar el filtro
        cargarHistorialYConfigurarFiltro();
    }

    // -------------------------------------------------------------
    // === MÉTODOS DE FILTRADO Y CARGA (NUEVOS/MODIFICADOS) ========
    // -------------------------------------------------------------

    private void cargarHistorialYConfigurarFiltro() {
        try {
            // Cargar TODOS los registros (Master Data)
            masterData = FXCollections.observableArrayList(
                    historialDAO.obtenerTodosLosRegistros()
            );

            // Inicializar FilteredList con todos los datos
            filteredData = new FilteredList<>(masterData, p -> true);
            historialTableView.setItems(filteredData);

            // Cargar los nombres únicos para el ChoiceBox
            cargarNombresUsuario();

            // Configurar el Listener para el ChoiceBox
            cmbFiltroUsuario.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> aplicarFiltro()
            );

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al cargar el historial de actividad: " + e.getMessage());
        }
    }

    private void cargarNombresUsuario() {
        // 1. Obtener la lista de nombres únicos de la masterData
        List<String> nombresUnicos = masterData.stream()
                .map(HistorialActividadTableView::getnombreUsuario)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // 2. Agregar la opción "Todos"
        ObservableList<String> itemsFiltro = FXCollections.observableArrayList();
        itemsFiltro.add("Todos");
        itemsFiltro.addAll(nombresUnicos);

        // 3. Establecer los items y seleccionar "Todos" por defecto
        cmbFiltroUsuario.setItems(itemsFiltro);
        cmbFiltroUsuario.getSelectionModel().select("Todos");
    }

    private void aplicarFiltro() {
        String usuarioSeleccionado = cmbFiltroUsuario.getSelectionModel().getSelectedItem();

        filteredData.setPredicate(registro -> {
            // Si "Todos" está seleccionado, mostrar todos los registros
            if (usuarioSeleccionado == null || usuarioSeleccionado.equals("Todos")) {
                return true;
            }

            // Si se selecciona un usuario específico, filtrar por nombre de usuario
            return registro.getnombreUsuario().equals(usuarioSeleccionado);
        });
    }

    // (El método handleVolverButtonHistorial permanece igual)
    @FXML
    private void handleVolverButtonHistorial(ActionEvent event) {
        try {
            Class.forName("app.controller.MenuController");
            loadScene((Node) event.getSource(), "/menuAdmin.fxml", "Menú Admin");
        } catch (Exception e) {
            System.err.println("Error de navegación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}