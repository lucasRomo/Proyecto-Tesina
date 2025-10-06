package app.controller;

import app.dao.ComprobanteDAO;
import app.controller.ComprobantesAdminTableView; // Asegúrate de que esta clase exista y esté correctamente definida
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Controlador para la vista de Comprobantes (Administración).
 * Gestiona la carga de datos desde el ComprobanteDAO, el filtrado y las acciones
 * de verificación en la tabla.
 */
public class ComprobantesController {

    // --- Elementos FXML Inyectados ---
    @FXML private VBox ComprobantesVBox;
    // Usamos el modelo que viene del DAO
    @FXML private TableView<ComprobantesAdminTableView> facturasAdminTableView;

    // Columnas (Mapeo de FXML ID a propiedades del modelo ComprobantesAdminTableView)
    @FXML private TableColumn<ComprobantesAdminTableView, Long> idFacturaColumn;     // -> idComprobante
    @FXML private TableColumn<ComprobantesAdminTableView, Long> idPedidoColumn;      // -> idPedido
    @FXML private TableColumn<ComprobantesAdminTableView, String> nombreClienteColumn; // -> nombreCompletoCliente
    @FXML private TableColumn<ComprobantesAdminTableView, String> numeroFacturaColumn; // -> tipoPago (Mostraremos el tipo de pago aquí)
    @FXML private TableColumn<ComprobantesAdminTableView, Double> fechaEmisionColumn;  // -> montoPago (Mostraremos el monto)
    @FXML private TableColumn<ComprobantesAdminTableView, LocalDateTime> montoTotalColumn; // -> fechaCarga (Mostraremos la fecha de carga)
    @FXML private TableColumn<ComprobantesAdminTableView, String> estadoPagoColumn;   // -> estadoVerificacion
    @FXML private TableColumn<ComprobantesAdminTableView, LocalDateTime> fechaVerificacionColumn;
    @FXML private TableColumn<ComprobantesAdminTableView, ComprobantesAdminTableView> accionArchivoColumn; // Columna para botones

    @FXML private TextField filterField;
    @FXML private Button VolverButton;

    // Dependencias
    private final ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
    private FilteredList<ComprobantesAdminTableView> filteredData;
    private ObservableList<ComprobantesAdminTableView> masterData;

    /**
     * Inicializa el controlador: Carga datos, configura columnas y filtros.
     */
    @FXML
    public void initialize() {

        // 1. Mapeo de Columnas
        setupColumnMapping();

        // 2. Cargar Datos desde el DAO
        cargarDatosDesdeDB();

        // 3. Configurar la Columna de Acción (Botón "Ver Archivo" y "Verificar")
        configurarColumnaAccion();

        // 4. Configurar el Filtro de Búsqueda
        configurarFiltroBusqueda();

    }

    /**
     * Define cómo se mapean las propiedades del modelo (ComprobantesAdminTableView) a las columnas de la tabla.
     */
    private void setupColumnMapping() {
        // Los strings deben coincidir con los métodos Property/Getter del modelo
        idFacturaColumn.setCellValueFactory(new PropertyValueFactory<>("idComprobante"));
        idPedidoColumn.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        nombreClienteColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCompletoCliente"));
        numeroFacturaColumn.setCellValueFactory(new PropertyValueFactory<>("tipoPago"));
        fechaEmisionColumn.setCellValueFactory(new PropertyValueFactory<>("montoPago"));
        montoTotalColumn.setCellValueFactory(new PropertyValueFactory<>("fechaCarga"));
        estadoPagoColumn.setCellValueFactory(new PropertyValueFactory<>("estadoVerificacion"));
        fechaVerificacionColumn.setCellValueFactory(new PropertyValueFactory<>("fechaVerificacion"));
    }

    /**
     * Carga los datos reales de los comprobantes desde la base de datos usando el DAO.
     */
    private void cargarDatosDesdeDB() {
        try {
            // Utilizamos el DAO para obtener la lista
            masterData = comprobanteDAO.getAllComprobantes();

            if (masterData.isEmpty()) {
                System.out.println("No se encontraron comprobantes en la base de datos.");
            }
        } catch (Exception e) {
            System.err.println("Error fatal al inicializar la carga de datos: " + e.getMessage());
            Alert error = new Alert(Alert.AlertType.ERROR, "No se pudo conectar a la base de datos o el modelo 'ComprobantesAdminTableView' es incorrecto.");
            error.showAndWait();
            masterData = FXCollections.observableArrayList();
        }
    }

    /**
     * Configura el comportamiento de la columna 'Acción' para mostrar botones.
     */
    private void configurarColumnaAccion() {

        // 1. Necesario si la columna está tipada como <T, T>:
        // La ValueFactory debe devolver el objeto de la fila.
        accionArchivoColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));

        // 2. Corregimos el tipo de TableCell para que coincida con lo que el compilador espera:
        accionArchivoColumn.setCellFactory(column -> new TableCell<ComprobantesAdminTableView, ComprobantesAdminTableView>() {

            private final Button btnVerArchivo = new Button("Ver Archivo");

            {
                // Configuración de estilo INLINE: azul vibrante y texto blanco
                btnVerArchivo.setStyle("-fx-background-color: #5d5dff; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6px;");

                // Propiedades para que el botón se expanda dentro de la celda
                btnVerArchivo.setMaxWidth(Double.MAX_VALUE);
                btnVerArchivo.setMaxHeight(Double.MAX_VALUE);

                // Manejador de eventos al hacer clic en el botón (Solo log)
                btnVerArchivo.setOnAction(event -> {
                    // Obtener el objeto de la fila que se ha clickeado
                    ComprobantesAdminTableView comprobante = getTableView().getItems().get(getIndex());

                    // Solo se imprime en la consola, no realiza ninguna acción funcional.
                    System.out.println("Clic en 'Ver Archivo' para Comprobante ID: " + comprobante.getIdComprobante() +
                            ". Lógica pendiente de implementación.");
                });
            }

            @Override
            // 'item' ahora es de tipo ComprobantesAdminTableView (aunque no lo usamos, debemos manejarlo)
            protected void updateItem(ComprobantesAdminTableView item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    // Solo se muestra el botón si la celda no está vacía
                    setGraphic(btnVerArchivo);
                }
            }
        });

        // Aseguramos un ancho suficiente para el botón
        accionArchivoColumn.setPrefWidth(99);
    }


    /**
     * Muestra una alerta con la ruta del archivo.
     * Requiere que ComprobantesAdminTableView tenga el método getArchivo().
     *

    /**
     * Maneja la lógica para cambiar el estado del comprobante a "Verificado" en DB y UI.
     */

    /**
     * Configura el listener de texto en el campo de filtro para aplicar la búsqueda por nombre/razón social o tipo de pago.
     */
    private void configurarFiltroBusqueda() {
        // Envolver la lista observable en una FilteredList
        filteredData = new FilteredList<>(masterData, p -> true);

        // Conectar el FilteredList a la TableView
        facturasAdminTableView.setItems(filteredData);

        // Establecer el Listener para el campo de búsqueda
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(createSearchPredicate(newValue));
        });
    }

    /**
     * Crea el predicado (regla de filtrado) basado en el texto de búsqueda.
     */
    private Predicate<ComprobantesAdminTableView> createSearchPredicate(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return p -> true;
        }

        String lowerCaseFilter = searchText.toLowerCase();

        return comprobante -> {
            String nombreCliente = comprobante.getnombreCompletoCliente();
            String tipoPago = comprobante.getTipoPago();

            // Buscar por nombre de cliente/razón social
            if (nombreCliente != null && nombreCliente.toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            // Buscar por tipo de pago
            else if (tipoPago != null && tipoPago.toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            return false;
        };
    }

    /**
     * Maneja el evento de clic en el botón "Volver".
     */
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