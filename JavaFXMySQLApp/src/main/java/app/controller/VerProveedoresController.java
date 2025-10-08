package app.controller;

import app.dao.ProveedorDAO;
import app.model.Proveedor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.TableCell;

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
}