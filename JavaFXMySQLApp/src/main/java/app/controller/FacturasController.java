package app.controller;

import app.dao.FacturaDAO; // Importa el DAO para obtener datos
import app.controller.FacturasAdminTableView; // Importa el modelo de vista con 7 propiedades
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Controlador para la interfaz de gestión de facturas.
 * Mapea las columnas del FXML a las propiedades del modelo FacturasAdminTableView.
 */
public class FacturasController {

    // --- Elementos de la Interfaz (FXML) ---
    @FXML
    private TableView<FacturasAdminTableView> facturasAdminTableView;
    @FXML
    private TableColumn<FacturasAdminTableView, Number> idFacturaColumn;
    @FXML
    private TableColumn<FacturasAdminTableView, Number> idPedidoColumn;
    @FXML
    private TableColumn<FacturasAdminTableView, String> nombreClienteColumn; // Mantenida: id_cliente
    @FXML
    private TableColumn<FacturasAdminTableView, String> numeroFacturaColumn;
    @FXML
    private TableColumn<FacturasAdminTableView, LocalDateTime> fechaEmisionColumn;
    @FXML
    private TableColumn<FacturasAdminTableView, Double> montoTotalColumn;
    @FXML
    private TableColumn<FacturasAdminTableView, String> estadoPagoColumn;
    @FXML
    private TableColumn<FacturasAdminTableView, Void> accionArchivoColumn; // Columna para el botón de acción
    @FXML
    private TextField filterField; // Campo de texto para el filtrado

    // --- Variables de Lógica ---
    private FacturaDAO facturaDAO;
    private ObservableList<FacturasAdminTableView> listaFacturas;



    /**
     * Método de inicialización llamado automáticamente después de que se cargan los elementos FXML.
     */
    @FXML
    public void initialize() {
        this.facturaDAO = new FacturaDAO();

        // 1. Mapeo de columnas a propiedades del modelo (7 propiedades)
        // El string entre comillas debe coincidir con el nombre del *Property() getter en el modelo.
        idFacturaColumn.setCellValueFactory(new PropertyValueFactory<>("idFactura"));
        idPedidoColumn.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        nombreClienteColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        numeroFacturaColumn.setCellValueFactory(new PropertyValueFactory<>("numeroFactura"));
        fechaEmisionColumn.setCellValueFactory(new PropertyValueFactory<>("fechaEmision"));
        montoTotalColumn.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));
        estadoPagoColumn.setCellValueFactory(new PropertyValueFactory<>("estadoPago"));

        // 2. Cargar los datos y configurar el filtrado
        cargarDatosFacturas();
        configurarFiltrado();

        // 3. Implementar el CellFactory para la columna de acción
        agregarBotonAccion();

        // (Opcional) Permite la edición en la tabla si se desea
        facturasAdminTableView.setEditable(true);
    }

    /**
     * Carga los datos de facturas desde la base de datos a la ObservableList.
     */
    private void cargarDatosFacturas() {
        listaFacturas = facturaDAO.obtenerFacturasAdmin();
        // NOTA: No asignamos directamente a facturasAdminTableView.setItems,
        // ya que el método configurarFiltrado lo hará a través de SortedList.
    }

    /**
     * Configura la lista filtrada y ordenada, enlazándola al campo de texto de filtrado.
     */
    private void configurarFiltrado() {
        // 1. Inicializa una lista filtrada a partir de los datos cargados
        FilteredList<FacturasAdminTableView> filteredData = new FilteredList<>(listaFacturas, p -> true);

        // 2. Listener para el campo de texto (filterField)
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(factura -> {
                // Si el filtro está vacío, muestra todos los resultados
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // *** LÓGICA DE BÚSQUEDA EXCLUSIVA POR NOMBRE DEL CLIENTE ***
                if (factura.getNombreCliente() != null && factura.getNombreCliente().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Coincidencia por Nombre del Cliente
                }

                return false; // No hay coincidencia
            });
        });

        // 4. Envuelve el FilteredList en un SortedList (Permite la ordenación al hacer clic en las cabeceras)
        SortedList<FacturasAdminTableView> sortedData = new SortedList<>(filteredData);

        // 5. Enlaza el comparador de SortedList con el comparador de TableView
        sortedData.comparatorProperty().bind(facturasAdminTableView.comparatorProperty());

        // 6. Aplica los datos ordenados y filtrados a la TableView
        facturasAdminTableView.setItems(sortedData);
    }

    /**
     * Agrega un botón de acción (e.g., "Ver Archivo") a la columna de acción.
     */
    private void agregarBotonAccion() {
        accionArchivoColumn.setCellFactory(column -> new TableCell<FacturasAdminTableView, Void>() {
            private final Button btn = new Button("Ver Archivo");

            {
                // Configuración de estilo básico para el botón


                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setMaxHeight(Double.MAX_VALUE);

                // Manejador de eventos al hacer clic en el botón
                btn.setOnAction(event -> {
                    FacturasAdminTableView factura = getTableView().getItems().get(getIndex());
                    // Lógica para abrir o descargar el archivo de la factura
                    System.out.println("Clic en 'Ver Link de la Factura' para Factura ID: " + factura.getIdFactura() +
                            " - Número: " + factura.getNumeroFactura());

                    // Aquí puedes implementar una ventana modal para ver el detalle o llamar a un servicio de descarga.
                    // ...
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setAlignment(null);
                } else {
                    setGraphic(btn);
                    setText(null);
                    setStyle("-fx-padding: 0;");
                }
            }
        });
    }

    @FXML
    private void handleVolverMenuInformesButton(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/InformesAdmin.fxml"));
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