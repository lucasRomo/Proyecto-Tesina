package app.controller;

import app.dao.HistorialActividadDAO; // Nuevo DAO
import app.model.HistorialActividadTableView; // Nuevo modelo

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * Controlador para la vista del historial de actividad (HistorialActividad.fxml).
 * Se encarga de cargar y mostrar todos los registros de actividad en una TableView.
 */
public class HistorialActividadController implements Initializable { // Nuevo nombre del controlador

    // Componentes FXML inyectados desde la vista (FXML)
    @FXML
    private TableView<HistorialActividadTableView> historialActividadTable;

    // Columnas de la tabla
    @FXML
    private TableColumn<HistorialActividadTableView, Integer> idColumna;
    @FXML
    private TableColumn<HistorialActividadTableView, Timestamp> fechaColumna;
    @FXML
    private TableColumn<HistorialActividadTableView, String> usuarioColumna;
    @FXML
    private TableColumn<HistorialActividadTableView, String> tablaColumna;
    @FXML
    private TableColumn<HistorialActividadTableView, String> columnaAfectadaColumna;
    @FXML
    private TableColumn<HistorialActividadTableView, Integer> idRegistroColumna;
    @FXML
    private TableColumn<HistorialActividadTableView, String> datoPrevioColumna;
    @FXML
    private TableColumn<HistorialActividadTableView, String> datoModificadoColumna;

    // DAO para acceder a los datos
    private HistorialActividadDAO historialActividadDAO;

    /**
     * Método de inicialización llamado automáticamente al cargar el FXML.
     * @param location URL de la ubicación.
     * @param resources ResourceBundle asociado.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        historialActividadDAO = new HistorialActividadDAO();
        configurarTabla();
        cargarDatosHistorial();
    }

    /**
     * Configura el enlace de las columnas de la TableView con las propiedades del modelo.
     */
    private void configurarTabla() {
        // Enlaza cada columna con la propiedad correspondiente del modelo HistorialActividadTableView
        idColumna.setCellValueFactory(cellData -> cellData.getValue().idRegActProperty().asObject());
        fechaColumna.setCellValueFactory(cellData -> cellData.getValue().fechaModificacionProperty());
        // 'nombreUsuario' viene del JOIN en el DAO
        usuarioColumna.setCellValueFactory(cellData -> cellData.getValue().nombreUsuarioProperty());
        tablaColumna.setCellValueFactory(cellData -> cellData.getValue().tablaAfectadaProperty());
        columnaAfectadaColumna.setCellValueFactory(cellData -> cellData.getValue().columnaAfectadaProperty());
        idRegistroColumna.setCellValueFactory(cellData -> cellData.getValue().idRegistroModificadoProperty().asObject());
        datoPrevioColumna.setCellValueFactory(cellData -> cellData.getValue().datoPrevioModificacionProperty());
        datoModificadoColumna.setCellValueFactory(cellData -> cellData.getValue().datoModificadoProperty());
    }

    /**
     * Carga los datos del historial de actividad desde la base de datos
     * y los muestra en la TableView.
     */
    private void cargarDatosHistorial() {
        try {
            // 1. Obtener la lista de registros usando el nuevo DAO
            List<HistorialActividadTableView> registros = historialActividadDAO.obtenerTodosLosRegistros();

            // 2. Convertir a ObservableList para la TableView
            ObservableList<HistorialActividadTableView> datos = FXCollections.observableArrayList(registros);

            // 3. Establecer los datos en la tabla
            historialActividadTable.setItems(datos);

            // Mensaje si no hay datos
            if (datos.isEmpty()) {
                historialActividadTable.setPlaceholder(new javafx.scene.control.Label("No hay registros de actividad para mostrar."));
            }

        } catch (Exception e) {
            System.err.println("Error al cargar los datos del historial: " + e.getMessage());
            // Se puede mostrar una alerta al usuario en caso de error grave
            historialActividadTable.setPlaceholder(new javafx.scene.control.Label("Error al cargar los datos. Consulte la consola."));
        }
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