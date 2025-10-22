package app.controller;

import app.dao.ComprobantePagoDAO;
import app.dao.FacturaDAO;
import app.dao.ProductoDAO;
import app.dao.EmpleadoDAO;
import app.model.Empleado;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip; // <-- Importación necesaria para Tooltip
import javafx.scene.input.MouseEvent; // <-- Importación necesaria para manejar eventos del mouse

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap; // <-- Importación necesaria para el mapa de conteos

public class InformesController {

    // DAOs
    private final FacturaDAO facturaDAO = new FacturaDAO();
    private final ComprobantePagoDAO comprobantePagoDAO = new ComprobantePagoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    // Variable para almacenar el conteo de pagos (Necesario para el Tooltip)
    private Map<String, Integer> conteoPagosPorRango = new HashMap<>(); // <-- Nuevo mapa para el conteo

    // Formato para la moneda
    private final DecimalFormat currencyFormatter = new DecimalFormat("$ #,##0.00");

    // FXML fields para la Fila Superior
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private LineChart<String, Number> lineChartVentas;
    @FXML private CategoryAxis xAxisVentas;
    @FXML private NumberAxis yAxisVentas;
    @FXML private Label lblTotalVentas;
    @FXML private Label lblFacturasEmitidas;

    // FXML fields para la métrica de Empleados (Nuevos campos para el filtro)
    @FXML private ChoiceBox<Empleado> cbEmpleado;
    @FXML private Label lblFacturasEmpleado;

    // FXML fields para la Fila Inferior
    @FXML private PieChart pieChartVentas;
    @FXML private BarChart<String, Number> barChartCategorias;
    @FXML private CategoryAxis xAxisCategorias;
    @FXML private NumberAxis yAxisUnidades;

    // Constante para el archivo de menú principal
    private static final String MENU_PRINCIPAL_FXML = "/MenuAdmin.fxml";

    @FXML
    public void initialize() {
        // 1. Configurar ejes y rangos iniciales
        LocalDate hoy = LocalDate.now();
        dpFechaFin.setValue(hoy);
        // Rango por defecto (últimos 30 días)
        dpFechaInicio.setValue(hoy.minusDays(30));

        xAxisVentas.setLabel("Fecha");
        yAxisVentas.setLabel("Monto Vendido ($)");
        xAxisCategorias.setLabel("Categoría");
        yAxisUnidades.setLabel("Unidades Vendidas");

        if (barChartCategorias != null) {
            barChartCategorias.setLegendVisible(false); // <-- ¡ESTE ES EL CAMBIO!
        }

        if (lineChartVentas != null) {
            lineChartVentas.setLegendVisible(false); // <-- ¡ESTE ES EL CAMBIO!
        }

        // Listener que actualiza la métrica del empleado al cambiar la fecha
        dpFechaInicio.valueProperty().addListener((obs, oldV, newV) -> handleDateChange());
        dpFechaFin.valueProperty().addListener((obs, oldV, newV) -> handleDateChange());

        // 2. Cargar empleados en el ChoiceBox
        cargarEmpleados();

        // 3. Configurar Listener para ChoiceBox de Empleados
        if (cbEmpleado != null) {
            cbEmpleado.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                // Al seleccionar un empleado o al cambiar el rango de fechas, actualizamos la métrica
                if (newValue != null && dpFechaInicio.getValue() != null && dpFechaFin.getValue() != null) {
                    actualizarFacturasPorEmpleado(newValue, dpFechaInicio.getValue(), dpFechaFin.getValue());
                } else {
                    lblFacturasEmpleado.setText("0");
                }
            });
        }

        // 4. Cargar datos iniciales
        handleGenerarGraficoButton(null);
    }

    /**
     * Manejador de eventos para cambios en DatePicker.
     * Actualiza la métrica del empleado seleccionado.
     */
    private void handleDateChange() {
        Empleado selectedEmpleado = cbEmpleado.getSelectionModel().getSelectedItem();
        LocalDate inicio = dpFechaInicio.getValue();
        LocalDate fin = dpFechaFin.getValue();

        if (selectedEmpleado != null && inicio != null && fin != null) {
            actualizarFacturasPorEmpleado(selectedEmpleado, inicio, fin);
        } else if (lblFacturasEmpleado != null) {
            // Si falta algún valor, resetear el contador
            lblFacturasEmpleado.setText("0");
        }
    }

    /**
     * Carga la lista de empleados activos en el ChoiceBox.
     */
    private void cargarEmpleados() {
        if (cbEmpleado == null) return;

        List<Empleado> empleados = empleadoDAO.getAllEmpleados();

        if (!empleados.isEmpty()) {
            cbEmpleado.setItems(FXCollections.observableArrayList(empleados));
            // Selecciona el primer empleado para inicializar la métrica
            cbEmpleado.getSelectionModel().selectFirst();
        } else {
            cbEmpleado.setItems(FXCollections.observableArrayList());
            lblFacturasEmpleado.setText("0");
        }
    }

    /**
     * Lógica para contar facturas emitidas por un empleado en un rango de fechas.
     */
    private void actualizarFacturasPorEmpleado(Empleado empleado, LocalDate inicio, LocalDate fin) {
        if (lblFacturasEmpleado == null) return;

        int idEmpleado = empleado.getIdEmpleado();
        int count = facturaDAO.contarFacturasPorEmpleado(idEmpleado, inicio, fin);

        lblFacturasEmpleado.setText(String.valueOf(count));
    }


    /**
     * Carga datos de ventas en el LineChart y actualiza las Métricas Clave.
     */
    private void loadVentasDataReal(LocalDate inicio, LocalDate fin) {
        double totalVentas = facturaDAO.getTotalVentasPorRango(inicio, fin);
        int totalFacturas = facturaDAO.getTotalFacturasPorRango(inicio, fin);
        Map<LocalDate, Double> ventasPorDia = facturaDAO.getVentasDiariasPorRango(inicio, fin);

        // 1. Actualizar Métricas Clave
        if (lblTotalVentas != null) {
            lblTotalVentas.setText(currencyFormatter.format(totalVentas));
        }
        if (lblFacturasEmitidas != null) {
            lblFacturasEmitidas.setText(String.valueOf(totalFacturas));
        }

        // 2. Cargar LineChart (Ventas Diarias)
        XYChart.Series<String, Number> seriesVentas = new XYChart.Series<>();
        seriesVentas.setName("Ventas Diarias");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        ventasPorDia.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    seriesVentas.getData().add(new XYChart.Data<>(
                            entry.getKey().format(formatter),
                            entry.getValue()
                    ));
                });

        lineChartVentas.getData().clear();
        lineChartVentas.getData().add(seriesVentas);
        lineChartVentas.setTitle("Tendencia de Ventas (" + inicio + " al " + fin + ")");
    }

    /**
     * Carga la distribución de ingresos por método de pago en el PieChart.
     */
    private void loadDistribucionDataReal(LocalDate inicio, LocalDate fin) {
        Map<String, Double> distribucion = comprobantePagoDAO.getDistribucionPagosPorRango(inicio, fin);

        // *******************************************************************
        // Lógica de acceso a datos que DEBES implementar en ComprobantePagoDAO
        // Simulación: Asumimos que podemos obtener el conteo
        // *******************************************************************
        // DEBES implementar un método en tu DAO como este:
        // this.conteoPagosPorRango = comprobantePagoDAO.getConteoPagosPorRango(inicio, fin);
        // Para este ejemplo, simularé datos fijos.
        this.conteoPagosPorRango = new HashMap<>();
        // Esto solo funciona si los métodos de pago son 'Efectivo' y 'Cheque'
        this.conteoPagosPorRango.put("Efectivo", 55);
        this.conteoPagosPorRango.put("Cheque", 12);

        // Si tu DAO soporta esto, la línea real sería:
        // this.conteoPagosPorRango = comprobantePagoDAO.getConteoPagosPorRango(inicio, fin);
        // *******************************************************************

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Double> entry : distribucion.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        pieChartVentas.setData(pieChartData);
        pieChartVentas.setTitle("Distribución por Método de Pago");
    }

    /**
     * Carga las unidades vendidas por categoría en el BarChart.
     */
    private void loadCategoriasDataReal(LocalDate inicio, LocalDate fin) {
        Map<String, Integer> unidadesVendidas = productoDAO.getUnidadesVendidasPorCategoria(inicio, fin);

        XYChart.Series<String, Number> seriesUnidades = new XYChart.Series<>();
        seriesUnidades.setName("Unidades Vendidas");

        unidadesVendidas.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> {
                    seriesUnidades.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                });

        barChartCategorias.getData().clear();
        barChartCategorias.getData().add(seriesUnidades);
        barChartCategorias.setTitle("Volumen por Categoría");
    }

    /**
     * Agrega tooltips a cada barra del BarChartCategorias para mostrar las unidades vendidas.
     * Este método debe ser llamado después de poblar el BarChart con datos.
     */
    private void setupBarChartTooltips() {
        // Asegúrate de que el BarChart tenga datos antes de intentar iterar
        if (barChartCategorias.getData().isEmpty()) {
            return;
        }

        // Iterar sobre todas las series (en este caso, solo "Unidades Vendidas")
        for (XYChart.Series<String, Number> series : barChartCategorias.getData()) {
            // Iterar sobre cada punto de dato (cada barra) dentro de la serie
            for (XYChart.Data<String, Number> data : series.getData()) {
                // El 'Node' representa la barra visual en el gráfico
                Node node = data.getNode();

                if (node != null) {
                    // Obtener la categoría (XValue) y las unidades vendidas (YValue)
                    String category = data.getXValue();
                    Number unitsSold = data.getYValue();

                    // Formatear el texto del tooltip
                    // Usamos intValue() porque las unidades son números enteros
                    String tooltipText = String.format("%s: %d unidades vendidas", category, unitsSold.intValue());

                    // Crear e instalar el Tooltip en el Node (la barra)
                    Tooltip tooltip = new Tooltip(tooltipText);
                    Tooltip.install(node, tooltip);
                }
            }
        }
    }

    /**
     * Agrega tooltips a cada porción del PieChart para mostrar el monto de ingreso
     * Y la cantidad total de ventas (transacciones) de ese método.
     */
    private void setupPieChartTooltips() {
        if (pieChartVentas.getData().isEmpty()) {
            return;
        }

        for (PieChart.Data data : pieChartVentas.getData()) {
            // Se usa setOnMouseEntered para que se actualice el tooltip
            data.getNode().setOnMouseEntered((MouseEvent event) -> {
                // Obtener el nombre del método de pago y el monto
                String paymentMethod = data.getName();
                double amount = data.getPieValue();

                // Obtener el conteo de ventas de la variable global (mapa)
                int count = conteoPagosPorRango.getOrDefault(paymentMethod, 0);

                // Formatear el texto del tooltip con el monto y el conteo
                String tooltipText = String.format(
                        "%s:\n" +
                                "Monto: %s\n" +
                                "Transacciones: %d",
                        paymentMethod,
                        currencyFormatter.format(amount),
                        count
                );

                // Crear el Tooltip y instalarlo
                Tooltip tooltip = new Tooltip(tooltipText);
                Tooltip.install(data.getNode(), tooltip);

                // Opcional: Para resaltar la porción al pasar el mouse
                data.getNode().setStyle("-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );");
            });

            // Opcional: Restaurar el estilo al salir del mouse
            data.getNode().setOnMouseExited((MouseEvent event) -> {
                data.getNode().setStyle(""); // Restaura el estilo CSS por defecto
            });
        }
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

        // Limpiar gráficos antes de cargar nuevos datos
        lineChartVentas.getData().clear();
        pieChartVentas.getData().clear();
        barChartCategorias.getData().clear();

        // Llamada a la capa DAO con los datos reales
        loadVentasDataReal(inicio, fin);
        // loadDistribucionDataReal ahora carga el mapa de conteos también (simulado)
        loadDistribucionDataReal(inicio, fin);
        loadCategoriasDataReal(inicio, fin);

        // Llamada a los nuevos métodos de tooltips, justo después de cargar los datos
        setupBarChartTooltips();
        setupPieChartTooltips();

        // Actualizar métrica de empleado si hay uno seleccionado
        Empleado selectedEmpleado = cbEmpleado.getSelectionModel().getSelectedItem();
        if (selectedEmpleado != null) {
            actualizarFacturasPorEmpleado(selectedEmpleado, inicio, fin);
        }

        // Solo mostrar alerta si fue disparado por el botón (no en initialize)
        if (event != null) {
            mostrarAlerta("Informe Generado",
                    "Datos cargados para el período: " + inicio + " hasta " + fin,
                    Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleVolverButtonInformes(ActionEvent event) {
        try {

            MenuController.loadScene((Node) event.getSource(), MENU_PRINCIPAL_FXML, "Menú de Administración");
        } catch (Exception e) {
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
