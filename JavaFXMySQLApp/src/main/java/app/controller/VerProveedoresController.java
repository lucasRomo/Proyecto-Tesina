package app.controller;

import app.dao.ProveedorDAO;
import app.model.Proveedor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.Node;

public class VerProveedoresController {

    @FXML private TableView<Proveedor> proveedoresTableView;
    @FXML private TableColumn<Proveedor, String> nombreColumn;
    @FXML private TableColumn<Proveedor, String> contactoColumn;
    @FXML private TableColumn<Proveedor, String> mailColumn;
    @FXML private TableColumn<Proveedor, String> estadoColumn;
    @FXML private ChoiceBox<String> estadoChoiceBox;

    private ProveedorDAO proveedorDAO;
    private ObservableList<Proveedor> masterData;
    private FilteredList<Proveedor> filteredData;

    public VerProveedoresController() {
        this.proveedorDAO = new ProveedorDAO();
    }

    @FXML
    private void initialize() {

        // ==========================================================
        // === VINCULACIÓN DEL ANCHO DE COLUMNAS PORCENTUAL ========
        // ==========================================================
        nombreColumn.prefWidthProperty().bind(proveedoresTableView.widthProperty().multiply(0.25));
        contactoColumn.prefWidthProperty().bind(proveedoresTableView.widthProperty().multiply(0.25));
        mailColumn.prefWidthProperty().bind(proveedoresTableView.widthProperty().multiply(0.35));
        estadoColumn.prefWidthProperty().bind(proveedoresTableView.widthProperty().multiply(0.15));
        // ==========================================================

        nombreColumn.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        contactoColumn.setCellValueFactory(cellData -> cellData.getValue().contactoProperty());
        mailColumn.setCellValueFactory(cellData -> cellData.getValue().mailProperty());
        estadoColumn.setCellValueFactory(cellData -> cellData.getValue().estadoProperty());

        // Estilo de la celda de estado
        estadoColumn.setCellFactory(column -> new TableCell<Proveedor, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // 1. Limpiar clases de estilo y el estilo en línea
                getStyleClass().removeAll("activo-cell", "desactivado-cell");
                setStyle(null); // Limpiar estilos en línea anteriores

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if ("Activo".equalsIgnoreCase(item)) {
                        getStyleClass().add("activo-cell");
                        // **SOLUCIÓN FINAL:** Forzar el color de texto a negro con estilo en línea.
                        setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
                    } else if ("Desactivado".equalsIgnoreCase(item)) {
                        getStyleClass().add("desactivado-cell");
                        // **SOLUCIÓN FINAL:** Forzar el color de texto a negro con estilo en línea.
                        setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Inicializa el ChoiceBox de estado
        estadoChoiceBox.setItems(FXCollections.observableArrayList("Todos", "Activo", "Desactivado"));
        estadoChoiceBox.getSelectionModel().select("Todos");

        // Listener para el filtro por estado
        estadoChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateFilteredList();
        });
    }

    public void setTipoProveedor(int idTipoProveedor) {
        masterData = proveedorDAO.getProveedoresByTipo(idTipoProveedor);
        filteredData = new FilteredList<>(masterData, p -> true);

        SortedList<Proveedor> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(proveedoresTableView.comparatorProperty());
        proveedoresTableView.setItems(sortedData);
    }

    private void updateFilteredList() {
        filteredData.setPredicate(proveedor -> {
            String selectedStatus = estadoChoiceBox.getSelectionModel().getSelectedItem();
            return selectedStatus.equals("Todos") || selectedStatus.equalsIgnoreCase(proveedor.getEstado());
        });
    }

    @FXML
    private void handleCerrarButton(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleHelpButton() {
        // Creamos una nueva alerta de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuramos el título y los encabezados del mensaje
        alert.setTitle("Ayuda - Menu De Visualizacion de Proveedores Registrados");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite la Visualizacion de Proveedores Registrados Vinculados al Tipo de Insumo Seleccionado: \n"
                + "\n"
                + "1. Filtros: Utilice el *ChoiceBox* para filtrar por Estado (Activo o Desactivado).\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();
    }
}