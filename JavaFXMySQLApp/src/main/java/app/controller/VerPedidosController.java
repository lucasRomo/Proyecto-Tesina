package app.controller;

import app.dao.HistorialActividadDAO;
import app.dao.PedidoDAO;
import app.model.Cliente;
import app.model.Pedido;
import app.util.ComprobanteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class VerPedidosController implements Initializable {

    // Formato para mostrar la fecha de creación (dd-MM-yyyy HH:mm)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // Tamaño de los iconos para los botones
    private static final int ICON_SIZE = 24;

    @FXML private TableView<Pedido> pedidosTable;
    @FXML private TableColumn<Pedido, Integer> idPedidoColumn;
    @FXML private TableColumn<Pedido, String> clienteColumn;
    @FXML private TableColumn<Pedido, String> empleadoColumn;
    @FXML private TableColumn<Pedido, String> estadoColumn;

    // 🚨 Declaración de la columna Tipo Pago
    @FXML private TableColumn<Pedido, String> tipoPagoColumn;

    @FXML private TableColumn<Pedido, Double> montoTotalColumn;
    @FXML private TableColumn<Pedido, Double> montoEntregadoColumn;
    @FXML private TableColumn<Pedido, LocalDateTime> fechaCreacionColumn;
    @FXML private TableColumn<Pedido, String> instruccionesColumn;

    // --- NUEVAS COLUMNAS AGREGADAS ---
    @FXML private TableColumn<Pedido, Void> contactoClienteColumn;
    // ---------------------------------

    @FXML private TableColumn<Pedido, Void> ticketColumn;
    @FXML private TableColumn<Pedido, Void> comprobantePagoColumn;

    @FXML private ComboBox<String> empleadoFilterComboBox;
    @FXML private ComboBox<String> estadoFilterComboBox;
    @FXML private ComboBox<String> metodoPagoFilterComboBox;

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ComprobanteService comprobanteService = new ComprobanteService();
    private final HistorialActividadDAO historialDAO = new HistorialActividadDAO();

    private int idEmpleadoFiltro = 0; // 0 significa 'Todos los Empleados'

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pedidosTable.setEditable(true);

        // 🚨 LÓGICA DE TAMAÑO Y POLÍTICA DE RESIZE (Configuración Proporcional / Porcentual)

        // Aplica la política de redimensionamiento que distribuye el ancho de la tabla entre todas las columnas.


        // Asignación de anchos preferidos para establecer proporciones basadas en el porcentaje
        // (La suma de los valores define el 100% de la tabla)
        // ... dentro del método initialize

        // Aplica la política de redimensionamiento que distribuye el ancho de la tabla entre todas las columnas.


        // --- ASIGNACIÓN DE ANCHOS CORREGIDOS (SUMA 99.8%) ---


        // Configuración para la columna Contacto (Botones fijos)
        // Se le asigna un ancho fijo de 75 píxeles. Esto anula la lógica proporcional para esta columna.
        contactoClienteColumn.setMinWidth(75.0);
        contactoClienteColumn.setMaxWidth(75.0);
        contactoClienteColumn.setPrefWidth(75.0); // Se mantiene prefWidth para compatibilidad, pero min/max es la clave.

        // --- ASIGNACIÓN DE ANCHOS PROPORCIONALES RESTANTES (Suman 92% Aprox.) ---
        // Ahora, el CONSTRAINED_RESIZE_POLICY distribuirá el espacio restante entre estas columnas:

        idPedidoColumn.setPrefWidth(4.5);           // ID (4.5%)
        clienteColumn.setPrefWidth(11.5);           // Cliente (11.5%)
        empleadoColumn.setPrefWidth(11.5);          // Empleado (11.5%)

        // ESTADO y TIPO PAGO son pequeñas
        estadoColumn.setPrefWidth(6.5);             // Estado (6.5%)
        if (tipoPagoColumn != null) {
            tipoPagoColumn.setPrefWidth(6.5);       // Tipo Pago (6.5%)
        }

        // MONTOS
        montoTotalColumn.setPrefWidth(7.0);         // Monto Total (7.0%)
        montoEntregadoColumn.setPrefWidth(8.5);     // Monto Entregado (8.5%)

        // FECHA (Ancho medio)
        fechaCreacionColumn.setPrefWidth(10.5);     // Fecha de Creación (10.5%)

        // INSTRUCCIONES (La más grande)
        instruccionesColumn.setPrefWidth(14.0);     // Instrucciones (14.0%)

        // BOTONES FINALES
        comprobantePagoColumn.setPrefWidth(50.0);    // Comprobante Pago
        comprobantePagoColumn.setMinWidth(50.0);    // Comprobante Pago (9.0%)
        ticketColumn.setPrefWidth(45.0);             // Detallar/Ticket
        ticketColumn.setMinWidth(45.0);
       // Aplica la política de redimensionamiento que distribuye el ancho de la tabla entre todas las columnas.
        // NOTA: Esta línea es redundante si está en el FXML, pero se mantiene como seguro.
                 // Detallar/Ticket (8.3%)
        // ------------------------------------------------------------------
        // ------------------------------------------------------------------
        // SUMA TOTAL: 4.5 + 11.5 + 11.5 + 7.5 + 6.5 + 6.5 + 7.0 + 8.5 + 10.5 + 14.0 + 9.0 + 8.3 = 99.8%

        // ...------------------------------

        // --- Configuración de Propiedades ---
        idPedidoColumn.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        clienteColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        empleadoColumn.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        montoTotalColumn.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));
        montoEntregadoColumn.setCellValueFactory(new PropertyValueFactory<>("montoEntregado"));
        instruccionesColumn.setCellValueFactory(new PropertyValueFactory<>("instrucciones"));

        // PropertyValueFactory para tipoPagoColumn
        if (tipoPagoColumn != null) {
            tipoPagoColumn.setCellValueFactory(new PropertyValueFactory<>("tipoPago"));
        }

        // Configurar la columna de Creación con el tipo LocalDateTime y formato personalizado
        fechaCreacionColumn.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        fechaCreacionColumn.setCellFactory(column -> new FormattedDateTableCell<>(DATE_FORMATTER));
        fechaCreacionColumn.setEditable(false);

        // Deshabilitar edición en columnas que no deben cambiar
        clienteColumn.setEditable(false);
        empleadoColumn.setEditable(false);

        // --- 1. Columna ESTADO (ComboBoxTableCell) ---
        ObservableList<String> estados = FXCollections.observableArrayList(
                "Pendiente", "En Proceso", "Finalizado", "Entregado", "Cancelado", "Retirado"
        );
        estadoColumn.setCellFactory(ComboBoxTableCell.forTableColumn(estados));
        estadoColumn.setOnEditCommit(event -> {
            Pedido pedido = event.getRowValue();
            String nuevoEstado = event.getNewValue();

            if (nuevoEstado.equalsIgnoreCase("Retirado")) {
                pedido.setFechaFinalizacion(LocalDateTime.now());
                mostrarAlerta("Éxito", "El pedido ha sido marcado como Retirado.", Alert.AlertType.INFORMATION);
            } else if (pedido.getEstado() != null && pedido.getEstado().equalsIgnoreCase("Retirado") && !nuevoEstado.equalsIgnoreCase("Retirado")) {
                pedido.setFechaFinalizacion(null);
            }

            pedido.setEstado(nuevoEstado);
            guardarCambiosEnBD(pedido, "Estado");
        });

        // --- 2. Columna CONTACTO (WhatsApp y Email) ---
        configurarColumnaContacto();

        // --- 3. Columna Ticket/Factura (Botón) ---
        configurarColumnaTicket();

        // --- 4. Columna Comprobante Pago (Botón) ---
        configurarColumnaComprobante();

        // --- 5 & 6. Columnas NUMÉRICAS (Double) ---
        montoTotalColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        montoTotalColumn.setOnEditCommit(event -> {
            if (event.getNewValue() != null && event.getNewValue() >= 0) {
                event.getRowValue().setMontoTotal(event.getNewValue());
                guardarCambiosEnBD(event.getRowValue(), "Monto Total");
            } else {
                mostrarAlerta("Advertencia", "El monto total debe ser un valor numérico positivo.", Alert.AlertType.WARNING);
                pedidosTable.refresh();
            }
        });

        montoEntregadoColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        montoEntregadoColumn.setOnEditCommit(event -> {
            if (event.getNewValue() != null && event.getNewValue() >= 0) {
                event.getRowValue().setMontoEntregado(event.getNewValue());
                guardarCambiosEnBD(event.getRowValue(), "Monto Entregado");
            } else {
                mostrarAlerta("Advertencia", "El monto entregado debe ser un valor numérico positivo.", Alert.AlertType.WARNING);
                pedidosTable.refresh();
            }
        });

        // --- 7. Columna INSTRUCCIONES (TextFieldTableCell) ---
        instruccionesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        instruccionesColumn.setOnEditCommit(event -> {
            event.getRowValue().setInstrucciones(event.getNewValue());
            guardarCambiosEnBD(event.getRowValue(), "Instrucciones");
        });

        // --- 8. Configuración del Filtro de Empleado ---
        cargarEmpleadosEnFiltro();
        empleadoFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            idEmpleadoFiltro = extractIdFromComboBox(newVal);
            cargarPedidos();
        });

        // --- 9. Configuración de Filtros Adicionales ---
        if (estadoFilterComboBox != null) {
            ObservableList<String> estadosConTodos = FXCollections.observableArrayList(estados);
            estadosConTodos.add(0, "Todos los Estados");
            estadoFilterComboBox.setItems(estadosConTodos);
            estadoFilterComboBox.getSelectionModel().selectFirst();
            estadoFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> cargarPedidos());
        }

        if (metodoPagoFilterComboBox != null) {
            List<String> tiposPago = pedidoDAO.getTiposPago();
            tiposPago.add(0, "Todos los Métodos");
            metodoPagoFilterComboBox.setItems(FXCollections.observableArrayList(tiposPago));
            metodoPagoFilterComboBox.getSelectionModel().selectFirst();
            metodoPagoFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> cargarPedidos());
        }

        // Carga inicial de pedidos
        cargarPedidos();
    }

    /**
     * Helper para cargar un ImageView desde un recurso.
     */
    private ImageView crearIcono(String nombreArchivo) {
        try {
            // La ruta en el classpath para 'resources/imagenes/whatsapp.png' es '/imagenes/whatsapp.png'
            URL imageUrl = getClass().getResource("/imagenes/" + nombreArchivo);
            if (imageUrl == null) {
                System.err.println("Advertencia: No se encontró el recurso de imagen: /imagenes/" + nombreArchivo);
                return new ImageView();
            }
            Image image = new Image(imageUrl.toExternalForm());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(ICON_SIZE);
            imageView.setFitHeight(ICON_SIZE);
            return imageView;
        } catch (Exception e) {
            System.err.println("Error al cargar icono " + nombreArchivo + ": " + e.getMessage());
            // En caso de error crítico, devuelve un placeholder vacío
            return new ImageView();
        }
    }


    /**
     * Configura la columna de contacto agregando botones con los iconos de WhatsApp y Gmail.
     */
    private void configurarColumnaContacto() {

        Callback<TableColumn<Pedido, Void>, TableCell<Pedido, Void>> contactoCellFactory = new Callback<>() {
            @Override
            public TableCell<Pedido, Void> call(final TableColumn<Pedido, Void> param) {

                return new TableCell<Pedido, Void>() {

                    private final Button waButton = new Button("", crearIcono("whatsapp.png"));
                    private final Button emailButton = new Button("", crearIcono("gmail.png"));
                    // HBox debe ser un campo para ser usado explícitamente en updateItem
                    private final HBox buttonBox;

                    {
                        // Configuración visual de los botones
                        waButton.getStyleClass().addAll("button-icon");
                        emailButton.getStyleClass().addAll("button-icon");
                        waButton.setStyle("-fx-padding: 2 4 2 4;");
                        emailButton.setStyle("-fx-padding: 2 4 2 4;");

                        // Inicializar el HBox
                        this.buttonBox = new HBox(5, waButton, emailButton);
                        this.buttonBox.setAlignment(Pos.CENTER);

                        // Solo definimos el tipo de contenido que muestra la celda, no asignamos el gráfico aquí
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                        // Lógica de acciones
                        waButton.setOnAction(event -> {
                            Pedido pedido = getTableView().getItems().get(getIndex());
                            abrirWhatsApp(pedido);
                        });

                        emailButton.setOnAction(event -> {
                            Pedido pedido = getTableView().getItems().get(getIndex());
                            abrirEmail(pedido);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // *** FIX CRÍTICO ***
                            // Siempre asignamos el HBox de botones al gráfico cuando la celda no está vacía.
                            // Esto asegura que los botones reaparezcan correctamente después de cualquier filtrado/recarga.
                            setGraphic(buttonBox);
                            // *******************

                            Pedido pedido = getTableView().getItems().get(getIndex());
                            // Opcional: Deshabilitar botones si no hay datos de contacto
                            waButton.setDisable(pedido.getTelefonoCliente() == null || pedido.getTelefonoCliente().isEmpty());
                            emailButton.setDisable(pedido.getEmailCliente() == null || pedido.getEmailCliente().isEmpty());
                            // Eliminado: setGraphic(getGraphic()); ya que buttonBox lo reemplaza
                        }
                    }
                };
            }
        };
        contactoClienteColumn.setCellFactory(contactoCellFactory);
    }

    /**
     * Abre el chat de WhatsApp en el navegador usando el formato wa.me.
     * @param pedido El pedido con el número del cliente.
     */
    private void abrirWhatsApp(Pedido pedido) {
        if (pedido.getTelefonoCliente() != null && !pedido.getTelefonoCliente().isEmpty()) {
            // Aseguramos que el número solo tenga dígitos y el '+' inicial
            String telefonoLimpio = pedido.getTelefonoCliente().replaceAll("[^0-9+]", "");

            // **IMPORTANTE: El número debe incluir el código de país (ej: +54911)**
            String url = "https://wa.me/" + telefonoLimpio;
            openUrl(url);
        } else {
            mostrarAlerta("Error de Contacto", "El cliente " + pedido.getNombreCliente() + " no tiene un número de WhatsApp válido.", Alert.AlertType.WARNING);
        }
    }

    /**
     * Abre un nuevo mensaje usando el esquema 'mailto:', que le indica al sistema
     * que abra el cliente de correo predeterminado (ya sea web o de escritorio).
     * @param pedido El pedido con el email del cliente.
     */
    private void abrirEmail(Pedido pedido) {
        if (pedido.getEmailCliente() != null && !pedido.getEmailCliente().isEmpty()) {
            // 1. Codificar asunto y cuerpo
            String subject = urlEncode("Consulta sobre Pedido #" + pedido.getIdPedido());
            String body = urlEncode(
                    "Hola " + pedido.getNombreCliente() + ",\n\n" +
                            "Con respecto a su pedido del " + DATE_FORMATTER.format(pedido.getFechaCreacion()) + ":\n\n" +
                            "[Escriba aquí su mensaje]"
            );

            // 2. Usar el esquema mailto: (más robusto)
            String mailtoUrl = String.format(
                    "mailto:%s?subject=%s&body=%s",
                    pedido.getEmailCliente(),
                    subject,
                    body
            );

            openUrl(mailtoUrl);
        } else {
            mostrarAlerta("Error de Contacto", "El cliente " + pedido.getNombreCliente() + " no tiene una dirección de email válida.", Alert.AlertType.WARNING);
        }
    }

    /**
     * Helper para codificar texto para URLs (WhatsApp, Mailto).
     */
    private String urlEncode(String text) {
        try {
            // Se usa java.net.URLEncoder.encode. Reemplazar '+' por '%20' es crucial para que
            // los espacios se interpreten bien en URLs de mailto/web.
            return java.net.URLEncoder.encode(text, "UTF-8").replaceAll("\\+", "%20");
        } catch (java.io.UnsupportedEncodingException e) {
            System.err.println("Error de codificación URL: " + e.getMessage());
            return text; // Fallback
        }
    }

    /**
     * Helper genérico para abrir URLs (sitios web o mailto).
     */
    private void openUrl(String url) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                // El navegador maneja bien los esquemas 'http', 'https', y 'mailto'
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                mostrarAlerta("Error al Abrir Enlace", "Hubo un problema al intentar abrir: " + url + "\nError: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Error de Sistema", "La función de abrir enlaces no es compatible con el sistema operativo actual.", Alert.AlertType.ERROR);
        }
    }

    private void cargarEmpleadosEnFiltro() {
        try {
            List<String> listaEmpleados = pedidoDAO.getAllEmpleadosDisplay();
            listaEmpleados.add(0, "0 - Todos los Empleados");
            empleadoFilterComboBox.setItems(FXCollections.observableArrayList(listaEmpleados));
            empleadoFilterComboBox.getSelectionModel().selectFirst();
        } catch (Exception e) {
            System.err.println("Error al cargar empleados para el filtro: " + e.getMessage());
            mostrarAlerta("Error de Carga", "No se pudieron cargar los empleados para el filtro.", Alert.AlertType.ERROR);
        }
    }

    private int extractIdFromComboBox(String selectedItem) {
        if (selectedItem == null || selectedItem.isEmpty()) {
            return 0;
        }
        try {
            String idString = selectedItem.split(" - ")[0].trim();
            return Integer.parseInt(idString);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    private void configurarColumnaTicket() {
        ticketColumn.setCellFactory(param -> new TableCell<Pedido, Void>() {
            private final Button btn = new Button("Detallar/Ticket");
            private final HBox pane = new HBox(btn);

            {
                btn.setOnAction((ActionEvent event) -> {
                    Pedido pedido = getTableView().getItems().get(getIndex());
                    handleGenerarTicket(pedido, (Stage) ((Node) event.getSource()).getScene().getWindow());
                });
                pane.setAlignment(Pos.CENTER);
                btn.getStyleClass().add("ticket-button");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void configurarColumnaComprobante() {
        Callback<TableColumn<Pedido, Void>, TableCell<Pedido, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Pedido, Void> call(final TableColumn<Pedido, Void> param) {
                final TableCell<Pedido, Void> cell = new TableCell<>() {

                    private final Button btn = new Button();
                    private final HBox pane = new HBox(btn);

                    {
                        btn.setMinWidth(110);
                        pane.setAlignment(Pos.CENTER);

                        btn.setOnAction(event -> {
                            Pedido pedido = getTableView().getItems().get(getIndex());
                            handleSubirComprobante(pedido);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // Asignación explícita del gráfico en el else (como en el FIX de contactoClienteColumn)
                            setGraphic(pane);
                            Pedido pedido = getTableView().getItems().get(getIndex());
                            String ruta = pedido.getRutaComprobante();

                            if (ruta != null && !ruta.isEmpty()) {
                                btn.setText("VER/Cambiar PDF");
                                btn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-cursor: hand;");
                            } else {
                                btn.setText("Subir Comprobante");
                                btn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand;");
                            }
                        }
                    }
                };
                return cell;
            }
        };

        comprobantePagoColumn.setCellFactory(cellFactory);
    }

    /**
     * Maneja la subida del archivo de comprobante de pago.
     * Muestra el archivo existente o abre el FileChooser para subir uno nuevo.
     * La lógica CRÍTICA para guardar y actualizar la DB se encuentra aquí.
     */
    private void handleSubirComprobante(Pedido pedido) {
        Stage stage = (Stage) pedidosTable.getScene().getWindow();

        // 1. Mostrar o Abrir existente
        if (pedido.getRutaComprobante() != null && !pedido.getRutaComprobante().isEmpty()) {
            File existingFile = new File(pedido.getRutaComprobante());
            if (existingFile.exists()) {
                try {
                    // Abrir el archivo existente para que el usuario lo vea
                    Desktop.getDesktop().open(existingFile);
                    System.out.println("Abriendo comprobante existente: " + pedido.getRutaComprobante());

                    // Preguntar si quiere reemplazarlo
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Reemplazar Comprobante");
                    confirmAlert.setHeaderText("Comprobante Existente Abierto");
                    confirmAlert.setContentText("¿Desea reemplazar el comprobante actual subiendo uno nuevo?");

                    if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                        return; // Si no confirma reemplazar, salimos.
                    }
                } catch (IOException e) {
                    mostrarAlerta("Error al Abrir", "No se pudo abrir el archivo de comprobante. Ruta: " + pedido.getRutaComprobante(), Alert.AlertType.ERROR);
                    // Continuamos para permitir subir uno nuevo si falla la apertura
                }
            } else {
                mostrarAlerta("Advertencia", "La ruta guardada ya no contiene el archivo físico. Se procederá a subir uno nuevo.", Alert.AlertType.WARNING);
            }
        }

        // 2. Abrir FileChooser para seleccionar archivo
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Comprobante de Pago (PDF o JPG)");
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
        FileChooser.ExtensionFilter imgFilter = new FileChooser.ExtensionFilter("Imágenes JPG (*.jpg, *.jpeg)", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().addAll(pdfFilter, imgFilter);

        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            // Lógica CRÍTICA: Llama al servicio para guardar el archivo y actualizar la DB
            System.out.println("Intentando guardar el comprobante en la carpeta y DB...");

            // Para el propósito de esta corrección, usamos el servicio que guarda el archivo físico y actualiza la ruta.
            boolean exitoGuardado = comprobanteService.guardarComprobante(pedido.getIdPedido(), file);

            if (exitoGuardado) {
                // Si el servicio tuvo éxito, recargamos los pedidos para obtener la ruta actualizada del modelo
                // y refrescar la tabla.
                cargarPedidos();
                mostrarAlerta("Éxito", "Comprobante subido y registrado correctamente.", Alert.AlertType.INFORMATION);
            } else {
                // El servicio ya muestra alertas detalladas, pero aseguramos el refresh
                pedidosTable.refresh();
            }
        }
    }


    private void handleGenerarTicket(Pedido pedido, Stage ownerStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detallePedidoView.fxml"));
            Parent root = loader.load();

            DetallePedidoController controller = loader.getController();
            controller.setPedido(pedido);

            Stage stage = new Stage();
            stage.setTitle("Detalle de Compra y Generación de Ticket - Pedido ID: " + pedido.getIdPedido());
            stage.setScene(new Scene(root, 1000, 700));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(ownerStage);
            stage.centerOnScreen();
            stage.showAndWait();

            cargarPedidos();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la ventana de Detalle de Pedido.\nVerifique que 'detallePedidoView.fxml' existe en el classpath.\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    /**
     * Guarda cambios en la tabla PEDIDO.
     * NOTA: Este método NO maneja la lógica de la tabla COMPROBANTEPAGO.
     * @param pedido El objeto Pedido modificado.
     * @param campoEditado Nombre del campo modificado para logging/alertas.
     */
    private void guardarCambiosEnBD(Pedido pedido, String campoEditado) {
        if (campoEditado.equalsIgnoreCase("Ruta Comprobante")) {
            System.out.println("Advertencia: El campo 'Ruta Comprobante' no debería actualizarse directamente desde PedidoDAO.");
            return;
        }

        Pedido originalPedido = null;
        try {
            // 🚨 PASO 1: OBTENER EL VALOR ORIGINAL DE LA BASE DE DATOS
            // Requiere que PedidoDAO tenga un método para obtener el Pedido completo por su ID.
            originalPedido = pedidoDAO.getPedidoById(pedido.getIdPedido());
            if (originalPedido == null) {
                mostrarAlerta("Error Interno", "No se encontraron datos originales para el pedido ID: " + pedido.getIdPedido() + ". No se pudo guardar.", Alert.AlertType.ERROR);
                pedidosTable.refresh();
                return;
            }
        } catch (Exception e) {
            mostrarAlerta("Error de BD", "Error al obtener datos originales del pedido: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
            pedidosTable.refresh();
            return;
        }

        // 🚨 PASO 2: REALIZAR EL UPDATE EN LA BASE DE DATOS
        boolean exitoUpdate = pedidoDAO.modificarPedido(pedido);

        if (exitoUpdate) {
            // 🚨 PASO 3: REGISTRAR CAMBIOS EN EL HISTORIAL DE FORMA DETALLADA
            try {
                int loggedInUserId = app.controller.SessionManager.getInstance().getLoggedInUserId();
                boolean exitoHistorial = true;

                // --- Comparar Estado ---
                if (!pedido.getEstado().equals(originalPedido.getEstado())) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Pedido", "estado", pedido.getIdPedido(),
                            originalPedido.getEstado(), pedido.getEstado()
                    );
                }

                // --- Comparar Monto Total ---
                // (Se usa Double.compare para manejar valores Double correctamente)
                if (Double.compare(pedido.getMontoTotal(), originalPedido.getMontoTotal()) != 0) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Pedido", "monto_total", pedido.getIdPedido(),
                            String.valueOf(originalPedido.getMontoTotal()), String.valueOf(pedido.getMontoTotal())
                    );
                }

                // --- Comparar Monto Entregado ---
                if (Double.compare(pedido.getMontoEntregado(), originalPedido.getMontoEntregado()) != 0) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Pedido", "monto_entregado", pedido.getIdPedido(),
                            String.valueOf(originalPedido.getMontoEntregado()), String.valueOf(pedido.getMontoEntregado())
                    );
                }

                // --- Comparar Instrucciones ---
                // Se normalizan nulos a cadenas vacías para una comparación segura
                String originalInstrucciones = originalPedido.getInstrucciones() != null ? originalPedido.getInstrucciones() : "";
                String nuevoInstrucciones = pedido.getInstrucciones() != null ? pedido.getInstrucciones() : "";

                if (!nuevoInstrucciones.equals(originalInstrucciones)) {
                    exitoHistorial = exitoHistorial && historialDAO.insertarRegistro(
                            loggedInUserId, "Pedido", "instrucciones", pedido.getIdPedido(),
                            originalInstrucciones, nuevoInstrucciones
                    );
                }

                // Nota: La columna 'tipoPago' no es editable en la tabla, por lo que no se incluye aquí.

                if (!exitoHistorial) {
                    System.err.println("Advertencia: No todos los registros de historial fueron insertados correctamente para Pedido ID: " + pedido.getIdPedido());
                }

            } catch (Exception e) {
                System.err.println("Error FATAL al registrar en el historial para Pedido ID: " + pedido.getIdPedido());
                e.printStackTrace();
            }
            // ------------------------------------------

            System.out.println("Pedido ID " + pedido.getIdPedido() + " actualizado. Campo modificado: " + campoEditado);
            if ("Retirado".equalsIgnoreCase(pedido.getEstado())) {
                cargarPedidos();
            } else {
                pedidosTable.refresh();
            }
        } else {
            mostrarAlerta("Error de Guardado", "No se pudo actualizar el " + campoEditado + " del pedido ID " + pedido.getIdPedido() + " en la base de datos.", Alert.AlertType.ERROR);
            pedidosTable.refresh();
        }
    }


    private void cargarPedidos() {
        // ... (Tu lógica existente para cargar y filtrar)
        List<Pedido> listaCompleta = pedidoDAO.getPedidosPorEmpleado(idEmpleadoFiltro);

        List<Pedido> listaFiltrada = listaCompleta.stream()
                .filter(p -> {
                    String estadoSeleccionado = estadoFilterComboBox != null ? estadoFilterComboBox.getSelectionModel().getSelectedItem() : null;
                    if (estadoSeleccionado != null && !estadoSeleccionado.equals("Todos los Estados")) {
                        return p.getEstado().equals(estadoSeleccionado);
                    }
                    return true;
                })
                .filter(p -> {
                    String metodoSeleccionado = metodoPagoFilterComboBox != null ? metodoPagoFilterComboBox.getSelectionModel().getSelectedItem() : null;
                    if (metodoSeleccionado != null && !metodoSeleccionado.equals("Todos los Métodos")) {
                        // Asumimos que Pedido tiene un getter getTipoPago() que recupera este dato
                        // de la tabla ComprobantePago o está poblado en el DTO Pedido.
                        return metodoSeleccionado.equals(p.getTipoPago());
                    }
                    return true;
                })
                .collect(Collectors.toList());

        pedidosTable.setItems(FXCollections.observableArrayList(listaFiltrada));
    }

    @FXML
    private void handleGuardarCambios(ActionEvent event) {
        Pedido pedidoSeleccionado = pedidosTable.getSelectionModel().getSelectedItem();

        if (pedidoSeleccionado != null) {
            boolean exito = pedidoDAO.modificarPedido(pedidoSeleccionado);

            if (exito) {
                mostrarAlerta("Éxito", "El Pedido ID " + pedidoSeleccionado.getIdPedido() + " ha sido modificado y guardado correctamente.", Alert.AlertType.INFORMATION);
                if ("Retirado".equalsIgnoreCase(pedidoSeleccionado.getEstado())) {
                    cargarPedidos();
                } else {
                    pedidosTable.refresh();
                }
            } else {
                mostrarAlerta("Error", "No se pudo modificar el pedido ID " + pedidoSeleccionado.getIdPedido() + " en la base de datos.", Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Advertencia", "Por favor, seleccione una fila antes de usar el botón 'Guardar Cambios'.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleLimpiarFiltros(ActionEvent event) {
        if (empleadoFilterComboBox != null) empleadoFilterComboBox.getSelectionModel().selectFirst();
        if (estadoFilterComboBox != null) estadoFilterComboBox.getSelectionModel().selectFirst();
        if (metodoPagoFilterComboBox != null) metodoPagoFilterComboBox.getSelectionModel().selectFirst();

        idEmpleadoFiltro = 0;
        cargarPedidos();
    }

    // ===============================================
    // *** MÉTODO VOLVER ***
    // ===============================================
    @FXML
    private void handleVolver(ActionEvent event) {
        try {
            // Usa el método unificado para cargar la escena en la ventana principal,
            // manteniendo el estado de maximización/tamaño de la aplicación.
            MenuController.loadScene(
                    (Node) event.getSource(),
                    "/PedidosPrimerMenu.fxml",
                    "Menú de Pedidos"
            );
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo volver al menú de pedidos. Verifique la ruta del FXML y la clase MenuController.", Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static class FormattedDateTableCell<S, T extends LocalDateTime> extends TableCell<S, T> {
        private final DateTimeFormatter formatter;

        public FormattedDateTableCell(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(formatter.format(item));
            }
        }
    }

    @FXML
    private void handleHelpButton() {
        // Creamos una nueva alerta de tipo INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Configuramos el título y los encabezados del mensaje
        alert.setTitle("Ayuda - Gestión de Pedidos Pendientes");
        alert.setHeaderText("Funcionalidades del Módulo");

        // Configuramos el contenido del mensaje
        alert.setContentText("Este módulo permite la administración completa de los Pedidos Pendientes Registrados en el Sistema:\n"
                + "\n"
                + "1. Visualización y Edición: Modifique directamente los campos de la tabla (Monto Total, Monto Entregado y Instrucciones) Al hacer doble click en la Columna.\n"
                + "----------------------------------------------------------------------\n"
                + "2. Filtros: Utilice el *ChoiceBox* para filtrar por El nombre del Empleado a Cargo, Estado (Pendiente, Finalizado, Etc) y por el Tipo de Pago (Efectivo, Tarjeta de Debito, Etc).\n"
                + "----------------------------------------------------------------------\n"
                + "3. Estado: Utilice el *ChoiceBox* para Seleccionar el Estado en el que se Encuentra el Pedido (Pendiente, Finalizado, Etc)\n"
                + "----------------------------------------------------------------------\n"
                + "4. Contacto: Seleccione el Icono de Whatsapp para Ser Redirigído al Chat de Contacto del Cliente o El Icono del Gmail para Ser Redirirgído al Buzón del Gmail de la Empresa).\n"
                + "----------------------------------------------------------------------\n"
                + "5. Subir Comprobante: Utilice este Boton para Vincular la Imagen del Comprobante de pago Recibído a su Pedido Correspondiente.\n"
                + "----------------------------------------------------------------------\n"
                + "6. Detallar Ticket: Utilice este Boton para Redactar, Finalizar el Pedido y Generar un PDF del Ticket del Pedido.\n"
                + "----------------------------------------------------------------------\n"
                + "7. Guardar Cambios: El botón 'Guardar Cambios' aplica todas las modificaciones realizadas en las celdas de la tabla a la base de datos.\n"
                + "----------------------------------------------------------------------\n"
                + "Para mas Información Visite el Manual de Usuario.\n");

        // Mostramos el mensaje y esperamos a que el usuario lo cierre
        alert.showAndWait();
    }
}