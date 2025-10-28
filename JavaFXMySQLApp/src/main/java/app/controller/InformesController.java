package app.controller;

import app.dao.*;
import app.model.Empleado;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controlador para la vista de Informes (Reportes) de la aplicación.
 * Gestiona la carga de datos estadísticos y su visualización en gráficos.
 */
public class InformesController {

    // DAOs
    private final FacturaDAO facturaDAO = new FacturaDAO();
    private final ComprobantePagoDAO comprobantePagoDAO = new ComprobantePagoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO(); // Ya existe

    // Variable para almacenar el conteo de pagos (Necesario para el Tooltip del PieChart)
    private Map<String, Integer> conteoPagosPorRango = new HashMap<>();

    // Formato para la moneda
    private final DecimalFormat currencyFormatter = new DecimalFormat("$ #,##0.00");

    // FXML fields para la Fila Superior
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private LineChart<String, Number> lineChartVentas;
    @FXML private CategoryAxis xAxisVentas;
    @FXML private NumberAxis yAxisVentas;
    @FXML private Label lblTotalVentas;
    @FXML private Label lblFacturasEmitidas; // Muestra el total de pedidos finalizados

    // Componentes del BarChart Ventas por Empleado
    @FXML private BarChart<String, Number> barChartVentasEmpleado;
    @FXML private CategoryAxis xAxisVentasEmpleado;
    @FXML private NumberAxis yAxisVentasEmpleado;


    // FXML fields para la Fila Inferior
    @FXML private PieChart pieChartVentas;
    @FXML private BarChart<String, Number> barChartCategorias;
    @FXML private CategoryAxis xAxisCategorias;
    @FXML private NumberAxis yAxisUnidades;

    // Constante para el archivo de menú principal
    private static final String MENU_PRINCIPAL_FXML = "/MenuAdmin.fxml";

    @FXML
    public void initialize() {
        // 1. Configurar fechas iniciales
        LocalDate hoy = LocalDate.now();
        dpFechaFin.setValue(hoy);
        // Rango por defecto (últimos 7 días)
        dpFechaInicio.setValue(hoy.minusDays(7));

        // 2. Configurar ejes de gráficos
        xAxisVentas.setLabel("Fecha");
        yAxisVentas.setLabel("Monto Vendido ($)");
        xAxisCategorias.setLabel("Categoría");
        yAxisUnidades.setLabel("Unidades Vendidas");

        if (xAxisVentasEmpleado != null) {
            xAxisVentasEmpleado.setLabel("Empleado");
        }
        if (yAxisVentasEmpleado != null) {
            // Se actualiza la etiqueta del eje para reflejar la cantidad de pedidos
            yAxisVentasEmpleado.setLabel("Pedidos Completados");
            // Asegura que el eje Y se trata como un número entero
            yAxisVentasEmpleado.setTickUnit(1.0);
            yAxisVentasEmpleado.setMinorTickVisible(false);
        }

        // 3. Ocultar leyendas innecesarias
        if (barChartVentasEmpleado != null) {
            barChartVentasEmpleado.setLegendVisible(false);
        }
        if (barChartCategorias != null) {
            barChartCategorias.setLegendVisible(false);
        }
        if (lineChartVentas != null) {
            lineChartVentas.setLegendVisible(false);
        }

        // 4. Deshabilitar edición de DatePickers
        if (dpFechaInicio != null) {
            dpFechaInicio.setEditable(false);
        }
        if (dpFechaFin != null) {
            dpFechaFin.setEditable(false);
        }

        // 5. Cargar datos iniciales
        handleGenerarGraficoButton(null);
    }

    /**
     * Carga la cantidad de pedidos entregados por cada empleado en el BarChart.
     * En producción, esto llama al método del DAO que devuelve Map<String, Integer>.
     */
    private void loadVentasEmpleadoData(LocalDate inicio, LocalDate fin) {
        if (barChartVentasEmpleado == null) return;

        barChartVentasEmpleado.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pedidos Completados");

        try {
            // 1. Obtener los datos del DAO (incluye la lógica de conexión)
            // Se asume que el método en PedidoDAO se llama getPedidosCompletadosPorEmpleado
            Map<String, Integer> pedidosPorEmpleado = pedidoDAO.getPedidosCompletadosPorEmpleado(inicio, fin);

            // 2. Ordenar el mapa por la cantidad de pedidos (descendente)
            pedidosPorEmpleado.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEach(entry -> {
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    });

            barChartVentasEmpleado.getData().add(series);
            barChartVentasEmpleado.setTitle("Pedidos Entregados por Empleado");

        } catch (RuntimeException e) {
            // Manejar la excepción de conexión (propagada desde el DAO)
            System.err.println("Error al cargar el gráfico de pedidos por empleado: " + e.getMessage());
            mostrarAlerta("Error de Conexión", "No se pudieron cargar los datos de pedidos por empleado de la base de datos. Se mostrarán datos de prueba.", Alert.AlertType.ERROR);

            // Fallback a datos de prueba
            loadVentasEmpleadoTestData();
        }

        setupBarChartVentasEmpleadoTooltips();
    }

    /**
     * Carga datos de prueba para el gráfico de pedidos por empleado en caso de fallo.
     */
    private void loadVentasEmpleadoTestData() {
        if (barChartVentasEmpleado == null) return;

        barChartVentasEmpleado.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pedidos Completados (Datos de Prueba)");

        Map<String, Integer> testData = new HashMap<>();
        testData.put("Juan Pérez", 25);
        testData.put("Ana Gómez", 18);
        testData.put("Carlos Ruiz", 32);
        testData.put("Elena Castro", 15);

        // Ordenar los datos de prueba
        testData.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                });

        barChartVentasEmpleado.getData().add(series);
        barChartVentasEmpleado.setTitle("Pedidos Entregados por Empleado (DATOS DE PRUEBA)");
    }


    /**
     * Agrega tooltips al BarChart de Pedidos por Empleado para mostrar la cantidad.
     */
    private void setupBarChartVentasEmpleadoTooltips() {
        if (barChartVentasEmpleado == null || barChartVentasEmpleado.getData().isEmpty()) {
            return;
        }

        for (XYChart.Series<String, Number> series : barChartVentasEmpleado.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    String employeeName = data.getXValue();
                    Number count = data.getYValue();

                    String tooltipText = String.format(
                            "%s:\n" +
                                    "Pedidos Entregados: %d",
                            employeeName,
                            count.intValue() // Se usa intValue para mostrar la cantidad
                    );

                    Tooltip tooltip = new Tooltip(tooltipText);
                    Tooltip.install(node, tooltip);
                }
            }
        }
    }


    /**
     * Carga datos de ventas en el LineChart y actualiza las Métricas Clave.
     */
    private void loadVentasDataReal(LocalDate inicio, LocalDate fin) {
        // 1. OBTENER DATOS DESDE PEDIDODAO
        double totalVentas = 0.0;
        int totalPedidos = 0;
        Map<LocalDate, Double> ventasPorDia = new HashMap<>();

        try {
            totalVentas = pedidoDAO.getTotalVentasPorRango(inicio, fin);
            totalPedidos = pedidoDAO.getTotalPedidosPorRango(inicio, fin);
            ventasPorDia = pedidoDAO.getVentasDiariasPorRango(inicio, fin);
        } catch (RuntimeException e) {
            System.err.println("Error al cargar datos de ventas (KPIs y Gráfico de Líneas): " + e.getMessage());
            mostrarAlerta("Error de Conexión", "No se pudieron cargar los datos de ventas de la base de datos.", Alert.AlertType.ERROR);
            // Si falla la conexión, los valores permanecen en 0 o vacíos.
        }


        // 2. Actualizar Métricas Clave
        if (lblTotalVentas != null) {
            lblTotalVentas.setText(currencyFormatter.format(totalVentas));
        }
        if (lblFacturasEmitidas != null) {
            lblFacturasEmitidas.setText(String.valueOf(totalPedidos));
        }

        // 3. Cargar LineChart (Ventas Diarias)
        if (lineChartVentas != null) {
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
            lineChartVentas.setTitle("Tendencia de Ventas (Pedidos) (" + inicio + " al " + fin + ")");
        }
    }

    /**
     * Carga la distribución de ingresos por método de pago en el PieChart.
     */
    private void loadDistribucionDataReal(LocalDate inicio, LocalDate fin) {
        // 1. Obtener la distribución de MONTOS (Monto Total por cada Método de Pago)
        Map<String, Double> distribucionMontos = new HashMap<>();
        // 2. OBTENER el CONTEO de transacciones REAL (cantidad de veces usado cada método)
        this.conteoPagosPorRango = new HashMap<>();

        try {
            distribucionMontos = comprobantePagoDAO.getDistribucionPagosTotalCorrecto(inicio, fin);
            this.conteoPagosPorRango = comprobantePagoDAO.getConteoPagosPorRango(inicio, fin);
        } catch (RuntimeException e) {
            System.err.println("Error al cargar datos de distribución (PieChart): " + e.getMessage());
            mostrarAlerta("Error de Conexión", "No se pudieron cargar los datos de distribución de pagos de la base de datos.", Alert.AlertType.ERROR);
        }


        if (pieChartVentas != null) {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            for (Map.Entry<String, Double> entry : distribucionMontos.entrySet()) {
                if (entry.getValue() > 0) {
                    pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                }
            }

            pieChartVentas.setData(pieChartData);
            pieChartVentas.setTitle("Distribución por Método de Pago");
        }
    }

    /**
     * Carga las unidades vendidas por categoría en el BarChart.
     */
    private void loadCategoriasDataReal(LocalDate inicio, LocalDate fin) {
        Map<String, Integer> unidadesVendidas = new HashMap<>();

        try {
            unidadesVendidas = productoDAO.getUnidadesVendidasPorCategoria(inicio, fin);
        } catch (RuntimeException e) {
            System.err.println("Error al cargar datos de categorías (BarChart): " + e.getMessage());
            mostrarAlerta("Error de Conexión", "No se pudieron cargar los datos de categorías de la base de datos.", Alert.AlertType.ERROR);
        }


        if (barChartCategorias != null) {
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
    }

    /**
     * Agrega tooltips a cada barra del BarChartCategorias para mostrar las unidades vendidas.
     */
    private void setupBarChartTooltips() {
        if (barChartCategorias == null || barChartCategorias.getData().isEmpty()) {
            return;
        }

        for (XYChart.Series<String, Number> series : barChartCategorias.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();

                if (node != null) {
                    String category = data.getXValue();
                    Number unitsSold = data.getYValue();

                    String tooltipText = String.format("%s: %d unidades vendidas", category, unitsSold.intValue());

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
        if (pieChartVentas == null || pieChartVentas.getData().isEmpty()) {
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
        if (lineChartVentas != null) {
            lineChartVentas.getData().clear();
        }
        if (pieChartVentas != null) {
            pieChartVentas.getData().clear();
        }
        if (barChartCategorias != null) {
            barChartCategorias.getData().clear();
        }

        if (barChartVentasEmpleado != null) {
            barChartVentasEmpleado.getData().clear();
        }


        // Llamada a la capa DAO con los datos reales
        loadVentasDataReal(inicio, fin);
        loadDistribucionDataReal(inicio, fin);
        loadCategoriasDataReal(inicio, fin);
        loadVentasEmpleadoData(inicio, fin); // Carga el BarChart de empleados


        // Llamada a los métodos de tooltips, justo después de cargar los datos
        setupBarChartTooltips();
        setupPieChartTooltips();
        setupBarChartVentasEmpleadoTooltips();

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

    @FXML
    private void handleHelpButton() {
        // Creamos una nueva alerta de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuramos el título y los encabezados del mensaje
        alert.setTitle("Ayuda - Menu de Generación de Estadisticas de Las Ventas Registradas en el Sistema");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite la Visualizacion de los informes y Estadisticas de las Ventas Registradas en el Sistema:\n"
                + "\n"
                + "Paso 1: Seleccione La Fecha Desde donde quiere que calcule el Grafico a la Izquierda.\n"
                + "----------------------------------------------------------------------\n"
                + "Paso 2: Seleccione La Fecha Hasta donde quiere que calcule el Grafico a la Derecha.\n"
                + "----------------------------------------------------------------------\n"
                + "Paso 3: Haga Click en el Boton Generar Grafico para Continuar.\n"
                + "----------------------------------------------------------------------\n"
                + "Si alguna Estadistica o Grafico está fuera de lugar, Vuelva a Hacer Click en Generar Grafico.\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();
    }
}
