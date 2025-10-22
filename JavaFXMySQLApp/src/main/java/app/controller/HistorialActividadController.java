package app.controller;

import app.dao.HistorialActividadDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    private HistorialActividadDAO historialDAO;

    @FXML
    public void initialize() {
        historialDAO = new HistorialActividadDAO();

        // ==========================================================
        // === VINCULACIÓN DEL ANCHO DE COLUMNAS PORCENTUAL ========
        // ==========================================================
        fechaModificacionColumn.prefWidthProperty().bind(historialTableView.widthProperty().multiply(0.15));
        nombreUsuarioColumn.prefWidthProperty().bind(historialTableView.widthProperty().multiply(0.15));
        tablaAfectadaColumn.prefWidthProperty().bind(historialTableView.widthProperty().multiply(0.12));
        columnaAfectadaColumn.prefWidthProperty().bind(historialTableView.widthProperty().multiply(0.13));
        idRegistroModificadoColumn.prefWidthProperty().bind(historialTableView.widthProperty().multiply(0.10));
        datoPrevioColumn.prefWidthProperty().bind(historialTableView.widthProperty().multiply(0.175));
        datoModificadoColumn.prefWidthProperty().bind(historialTableView.widthProperty().multiply(0.175));
        // ==========================================================


        // 1. Configurar las Cell Value Factories
        // Usamos los nombres de los métodos 'propiedadProperty()' definidos en HistorialActividadTableView.
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
                    // Convierte el Timestamp a LocalDateTime y luego aplica el formato
                    LocalDateTime localDateTime = item.toLocalDateTime();
                    setText(localDateTime.format(formatter));
                }
            }
        });

        // 3. Cargar los datos
        cargarHistorial();
    }

    private void cargarHistorial() {
        try {
            // Llama al DAO para obtener la lista de registros
            ObservableList<HistorialActividadTableView> registros = FXCollections.observableArrayList(
                    historialDAO.obtenerTodosLosRegistros()
            );
            historialTableView.setItems(registros);
        } catch (Exception e) {
            e.printStackTrace();
            // Implementar una alerta o mensaje de error adecuado para el usuario
            System.err.println("Error al cargar el historial de actividad: " + e.getMessage());
        }
    }

    /**
     * Manejador del botón Volver. Cierra la ventana actual.
     */
    @FXML
    private void handleVolverButtonHistorial(ActionEvent event) {
        try {
            // Se asume que MenuController existe en el paquete app.controller
            Class.forName("app.controller.MenuController");
            loadScene((Node) event.getSource(), "/menuAdmin.fxml", "Menú Admin");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Clase MenuController no encontrada. No se puede volver.");
        }
    }
}