package app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.io.IOException;
import java.time.LocalDate;

public class InformesController { // Clase renombrada

    // FXML fields para la Fila Superior
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private LineChart<String, Number> lineChartVentas;
    @FXML private CategoryAxis xAxisVentas;
    @FXML private NumberAxis yAxisVentas;
    @FXML private Label lblTotalVentas;
    @FXML private Label lblFacturasEmitidas;

    // FXML fields para la Fila Inferior
    @FXML private PieChart pieChartVentas;
    @FXML private BarChart<String, Number> barChartCategorias;
    @FXML private CategoryAxis xAxisCategorias;
    @FXML private NumberAxis yAxisUnidades;

    // Constante para el archivo de menú principal
    private static final String MENU_PRINCIPAL_FXML = "/MenuAbms.fxml";

    @FXML
    public void initialize() {
        // Establecer las fechas por defecto (ej. los últimos 7 días)
        dpFechaFin.setValue(LocalDate.now());
        dpFechaInicio.setValue(LocalDate.now().minusDays(6));

        // Inicializar los gráficos con datos de prueba
        // NOTA: Si el FXMLLoader no logra inyectar las variables @FXML (ej. lblTotalVentas),
        // esta llamada fallará con NullPointerException. Asegúrate de que el FXML tiene
        // la referencia correcta al controlador: fx:controller="app.controller.InformesController"
        loadVentasData();
        loadDistribucionData();
        loadCategoriasData();
    }

    /**
     * Simula la carga de datos de ventas en el LineChart y las Métricas Clave.
     */
    private void loadVentasData() {
        // Datos de prueba (Series del LineChart)
        XYChart.Series<String, Number> seriesVentas = new XYChart.Series<>();
        seriesVentas.setName("Ventas Diarias");

        // Datos simulados (Día, Monto)
        seriesVentas.getData().add(new XYChart.Data<>("01-Oct", 850.00));
        seriesVentas.getData().add(new XYChart.Data<>("02-Oct", 1200.50));
        seriesVentas.getData().add(new XYChart.Data<>("03-Oct", 950.00));
        seriesVentas.getData().add(new XYChart.Data<>("04-Oct", 1500.25));
        seriesVentas.getData().add(new XYChart.Data<>("05-Oct", 1100.00));
        seriesVentas.getData().add(new XYChart.Data<>("06-Oct", 1850.00));
        seriesVentas.getData().add(new XYChart.Data<>("07-Oct", 2000.75));

        // Limpiar y añadir datos al LineChart
        lineChartVentas.getData().clear();
        lineChartVentas.getData().add(seriesVentas);
        lineChartVentas.setTitle("Tendencia de Ventas");

        // Actualizar Métricas Clave (simuladas)
        // **Esta línea es la que falló en tu traza si lblTotalVentas es nulo.**
        if (lblTotalVentas != null) {
            lblTotalVentas.setText(String.format("$ %,.2f", 9451.50));
        }
        if (lblFacturasEmitidas != null) {
            lblFacturasEmitidas.setText("185");
        }
    }

    /**
     * Simula la carga de datos de distribución de ingresos en el PieChart.
     */
    private void loadDistribucionData() {
        // Datos de prueba (PieChart)
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Efectivo", 4000),
                        new PieChart.Data("Tarjeta de Crédito", 6500),
                        new PieChart.Data("Transferencia", 2000)
                );

        pieChartVentas.setData(pieChartData);
        pieChartVentas.setTitle("Métodos de Pago");
    }

    /**
     * Simula la carga de ventas por categoría en el BarChart.
     */
    private void loadCategoriasData() {
        // Datos de prueba (BarChart)
        XYChart.Series<String, Number> seriesUnidades = new XYChart.Series<>();
        seriesUnidades.setName("Unidades Vendidas");

        seriesUnidades.getData().add(new XYChart.Data<>("Alimentos", 550));
        seriesUnidades.getData().add(new XYChart.Data<>("Bebidas", 720));
        seriesUnidades.getData().add(new XYChart.Data<>("Limpieza", 300));
        seriesUnidades.getData().add(new XYChart.Data<>("Electrónica", 150));

        // Limpiar y añadir datos al BarChart
        barChartCategorias.getData().clear();
        barChartCategorias.getData().add(seriesUnidades);
        barChartCategorias.setTitle("Volumen por Categoría");
    }

    @FXML
    private void handleGenerarGraficoButton(ActionEvent event) {
        LocalDate inicio = dpFechaInicio.getValue();
        LocalDate fin = dpFechaFin.getValue();

        if (inicio == null || fin == null) {
            mostrarAlerta("Error de Fecha", "Debe seleccionar una fecha de inicio y una fecha de fin válidas.", Alert.AlertType.WARNING);
            return;
        }

        if (inicio.isAfter(fin)) {
            mostrarAlerta("Error de Rango", "La fecha de inicio no puede ser posterior a la fecha de fin.", Alert.AlertType.WARNING);
            return;
        }

        // Aquí iría la llamada a la capa DAO para obtener datos reales
        // Por ahora, solo recargamos los datos de prueba
        loadVentasData();
        loadDistribucionData();
        loadCategoriasData();

        mostrarAlerta("Gráfico Generado",
                "Datos simulados cargados para el período: " + inicio + " hasta " + fin,
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleVolverButtonInformes(ActionEvent event) {
        try {
            // Asume que MenuController tiene el método estático loadScene
            MenuController.loadScene(
                    (Node) event.getSource(),
                    MENU_PRINCIPAL_FXML,
                    "Menú de Administración"
            );
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Navegación", "No se pudo cargar el menú principal.", Alert.AlertType.ERROR);
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
