package app.controller;

import app.dao.PedidoDAO;
import app.model.Pedido;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser; // Importado para selección de archivo
import javafx.util.converter.DoubleStringConverter;
import javafx.util.Callback; // Importado para configurar la celda del botón

import java.io.File; // Importado para manejo de archivos
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.awt.Desktop; // Importado para abrir el archivo (funcionalidad de escritorio)

// IMPORTACIÓN AÑADIDA
import app.controller.DetallePedidoController;

public class VerPedidosController implements Initializable {

    // Formato para mostrar la fecha de creación (dd-MM-yyyy HH:mm)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML
    private TableView<Pedido> pedidosTable;
    @FXML
    private TableColumn<Pedido, Integer> idPedidoColumn;
    @FXML
    private TableColumn<Pedido, String> clienteColumn;
    @FXML
    private TableColumn<Pedido, String> empleadoColumn;
    @FXML
    private TableColumn<Pedido, String> estadoColumn;

    @FXML
    private TableColumn<Pedido, Double> montoTotalColumn;
    @FXML
    private TableColumn<Pedido, Double> montoEntregadoColumn;

    // Columna de Fecha de Creación
    @FXML
    private TableColumn<Pedido, LocalDateTime> fechaCreacionColumn;

    @FXML
    private TableColumn<Pedido, String> instruccionesColumn;

    // Columna para el botón de Ticket/Factura
    @FXML
    private TableColumn<Pedido, Void> ticketColumn;

    // NUEVA COLUMNA para el comprobante de pago
    @FXML
    private TableColumn<Pedido, Void> comprobantePagoColumn;

    // ComboBox para filtrar por empleado
    @FXML
    private ComboBox<String> empleadoFilterComboBox;

    // ComboBox para filtrar por estado
    @FXML
    private ComboBox<String> estadoFilterComboBox;

    // ComboBox para filtrar por método de pago
    @FXML
    private ComboBox<String> metodoPagoFilterComboBox; // Se mantiene el nombre, pero filtra por 'tipo_pago'

    private PedidoDAO pedidoDAO;
    private int idEmpleadoFiltro = 0; // 0 significa 'Todos los Empleados'

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pedidoDAO = new PedidoDAO();
        pedidosTable.setEditable(true);

        // --- Configuración de Propiedades ---
        idPedidoColumn.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        clienteColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        empleadoColumn.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        montoTotalColumn.setCellValueFactory(new PropertyValueFactory<>("montoTotal"));
        montoEntregadoColumn.setCellValueFactory(new PropertyValueFactory<>("montoEntregado"));
        instruccionesColumn.setCellValueFactory(new PropertyValueFactory<>("instrucciones"));

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

            // Lógica para establecer la fecha de finalización al cambiar a 'Retirado'
            if (nuevoEstado.equalsIgnoreCase("Retirado")) {
                // Si se marca como Retirado, se le pone la fecha actual
                pedido.setFechaFinalizacion(LocalDateTime.now());
                mostrarAlerta("Éxito", "El pedido ha sido marcado como Retirado.", Alert.AlertType.INFORMATION);
            } else if (pedido.getEstado() != null && pedido.getEstado().equalsIgnoreCase("Retirado") && !nuevoEstado.equalsIgnoreCase("Retirado")) {
                // Si se cambia de 'Retirado' a otro estado, limpiamos la fecha de finalización
                pedido.setFechaFinalizacion(null);
            }

            pedido.setEstado(nuevoEstado);
            guardarCambiosEnBD(pedido, "Estado");
        });

        // --- 2. Columna Ticket/Factura (Botón) ---
        configurarColumnaTicket();

        // --- 3. Columna Comprobante Pago (Botón) ---
        configurarColumnaComprobante(); // NUEVA CONFIGURACIÓN

        // --- 4 & 5. Columnas NUMÉRICAS (Double) ---
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

        // --- 6. Columna INSTRUCCIONES (TextFieldTableCell) ---
        instruccionesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        instruccionesColumn.setOnEditCommit(event -> {
            event.getRowValue().setInstrucciones(event.getNewValue());
            guardarCambiosEnBD(event.getRowValue(), "Instrucciones");
        });

        // --- 7. Configuración del Filtro de Empleado ---
        cargarEmpleadosEnFiltro();
        empleadoFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            idEmpleadoFiltro = extractIdFromComboBox(newVal);
            cargarPedidos(); // Recarga los pedidos con el nuevo filtro
        });

        // --- 8. Configuración de Filtros Adicionales ---
        // Se asume que estos ComboBox existen en el FXML, por lo que los configuramos.
        if (estadoFilterComboBox != null) {
            estadoFilterComboBox.setItems(FXCollections.observableArrayList(estados));
            estadoFilterComboBox.getItems().add(0, "Todos los Estados");
            estadoFilterComboBox.getSelectionModel().selectFirst();
            estadoFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> cargarPedidos());
        }

        if (metodoPagoFilterComboBox != null) {
            // Se asume que PedidoDAO.getTiposPago() existe y retorna una List<String>
            List<String> tiposPago = pedidoDAO.getTiposPago();
            tiposPago.add(0, "Todos los Métodos");
            metodoPagoFilterComboBox.setItems(FXCollections.observableArrayList(tiposPago));
            metodoPagoFilterComboBox.getSelectionModel().selectFirst();
            metodoPagoFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> cargarPedidos());
        }


        // Carga inicial de pedidos
        cargarPedidos();
    } // CIERRE DEL MÉTODO initialize

    /**
     * Carga la lista de empleados para el ComboBox de filtro, corrigiendo el problema de visualización.
     */
    private void cargarEmpleadosEnFiltro() {

        try {
            // Asumiendo que PedidoDAO.getAllEmpleadosDisplay() existe
            List<String> listaEmpleados = pedidoDAO.getAllEmpleadosDisplay();
            // Añadir la opción para ver todos
            listaEmpleados.add(0, "0 - Todos los Empleados");
            empleadoFilterComboBox.setItems(FXCollections.observableArrayList(listaEmpleados));
            empleadoFilterComboBox.getSelectionModel().selectFirst();
        } catch (Exception e) {
            System.err.println("Error al cargar empleados para el filtro: " + e.getMessage());
            mostrarAlerta("Error de Carga", "No se pudieron cargar los empleados para el filtro.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Método auxiliar para extraer el ID de un String con formato "ID - Nombre".
     */
    private int extractIdFromComboBox(String selectedItem) {
        if (selectedItem == null || selectedItem.isEmpty()) {
            return 0;
        }
        try {
            // El formato es "ID - Nombre"
            String idString = selectedItem.split(" - ")[0].trim();
            return Integer.parseInt(idString);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Si no se puede parsear, retorna 0 (Todos)
            return 0;
        }
    }

    /**
     * Configura la columna para incluir un botón "Detallar/Ticket".
     */
    private void configurarColumnaTicket() {
        // Aseguramos que la columna es de tipo Void
        ticketColumn.setCellFactory(param -> new TableCell<Pedido, Void>() {
            private final Button btn = new Button("Detallar/Ticket");
            private final HBox pane = new HBox(btn);

            {
                // Manejador de evento al hacer clic en el botón
                btn.setOnAction((ActionEvent event) -> {
                    // Obtiene el objeto Pedido de la fila actual
                    Pedido pedido = getTableView().getItems().get(getIndex());
                    // Pasa el Stage de la ventana principal para que la nueva sea modal a ella
                    handleGenerarTicket(pedido, (Stage) ((Node) event.getSource()).getScene().getWindow());
                });
                pane.setAlignment(Pos.CENTER);
                btn.getStyleClass().add("ticket-button"); // Estilo CSS opcional
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

    /**
     * Configura la columna para incluir un botón de "Subir/Ver Comprobante".
     */
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
                            Pedido pedido = getTableView().getItems().get(getIndex());
                            String ruta = pedido.getRutaComprobante(); // Se asume que este getter existe

                            // Cambia el texto y estilo del botón basado en si ya hay un comprobante
                            if (ruta != null && !ruta.isEmpty()) {
                                btn.setText("VER/Cambiar PDF");
                                btn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-cursor: hand;"); // Azul
                            } else {
                                btn.setText("Subir Comprobante");
                                btn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand;"); // Verde
                            }
                            setGraphic(pane);
                        }
                    }
                };
                return cell;
            }
        };

        comprobantePagoColumn.setCellFactory(cellFactory);
    }

    /**
     * Lógica para subir un archivo PDF de comprobante y guardar la ruta en el pedido.
     */
    private void handleSubirComprobante(Pedido pedido) {
        // Determinar la ventana principal (Stage)
        Stage stage = (Stage) pedidosTable.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Comprobante de Pago (PDF)");
        // Filtro para solo mostrar archivos PDF
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);

        // Si ya existe una ruta, intenta abrir el archivo o pide confirmación para reemplazar
        if (pedido.getRutaComprobante() != null && !pedido.getRutaComprobante().isEmpty()) {
            File existingFile = new File(pedido.getRutaComprobante());
            if (existingFile.exists()) {
                // Si el archivo existe, primero lo abre (comportamiento de "Ver")
                try {
                    Desktop.getDesktop().open(existingFile);
                    System.out.println("Abriendo comprobante existente: " + pedido.getRutaComprobante());
                    // Luego, preguntar si desea cambiarlo
                    // Opcionalmente, puedes añadir una alerta de confirmación aquí si quieres que el "VER" sea un botón separado.
                } catch (IOException e) {
                    mostrarAlerta("Error al Abrir", "No se pudo abrir el archivo PDF. Ruta: " + pedido.getRutaComprobante(), Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Advertencia", "La ruta guardada ya no contiene el archivo PDF. Proceda a subir uno nuevo.", Alert.AlertType.WARNING);
            }
        }

        // Mostrar el diálogo de selección de archivo (siempre disponible para cambiar/subir)
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            String newPath = file.getAbsolutePath();

            // 1. Guardar la nueva ruta en el objeto Pedido
            pedido.setRutaComprobante(newPath); // Se asume que este setter existe
            System.out.println("Comprobante subido para Pedido " + pedido.getIdPedido() + ". Ruta: " + newPath);

            // 2. Persistir esta nueva ruta en la base de datos
            guardarCambiosEnBD(pedido, "Ruta Comprobante");

            // 3. Refrescar la tabla para que cambie el texto del botón
            pedidosTable.refresh();
        }
    }


    /**
     * Manejador del evento de clic en el botón Detallar/Ticket.
     * Abre una nueva ventana modal para detallar la compra y generar el ticket.
     * @param pedido El pedido para el cual generar el detalle.
     * @param ownerStage La ventana principal (dueña) del pedido.
     */
    private void handleGenerarTicket(Pedido pedido, Stage ownerStage) {
        try {
            // Cargar el FXML de la ventana de detalle (detallePedidoView.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detallePedidoView.fxml"));
            Parent root = loader.load();

            // Obtener el controlador de la ventana de detalle
            DetallePedidoController controller = loader.getController();

            // Inicializa el controlador con el pedido seleccionado
            controller.setPedido(pedido);

            // Crear y configurar la nueva Stage (ventana modal)
            Stage stage = new Stage();
            stage.setTitle("Detalle de Compra y Generación de Ticket - Pedido ID: " + pedido.getIdPedido());
            stage.setScene(new Scene(root, 1000, 700)); // Tamaño ajustado para la tabla de detalles
            stage.initModality(Modality.WINDOW_MODAL); // Lo hace modal
            stage.initOwner(ownerStage); // Lo hace dependiente de la ventana principal
            stage.centerOnScreen();
            stage.showAndWait();

            // Después de cerrar la ventana de detalle, se refresca la tabla por si el monto total cambió
            cargarPedidos();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la ventana de Detalle de Pedido.\nVerifique que 'detallePedidoView.fxml' existe en el classpath.\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    /**
     * Llama al DAO para sobrescribir los datos del pedido en la base de datos.
     * @param pedido El objeto Pedido actualizado.
     * @param campoEditado El nombre del campo que fue modificado.
     */
    private void guardarCambiosEnBD(Pedido pedido, String campoEditado) {
        // IMPORTANTE: Aquí se asume que PedidoDAO.modificarPedido puede manejar la actualización del campo rutaComprobante
        boolean exito = pedidoDAO.modificarPedido(pedido);

        if (exito) {
            System.out.println("Pedido ID " + pedido.getIdPedido() + " actualizado. Campo modificado: " + campoEditado);

            // Si el estado es "Retirado", recargar la lista para que desaparezca
            if ("Retirado".equalsIgnoreCase(pedido.getEstado()) && !campoEditado.equalsIgnoreCase("Ruta Comprobante")) {
                cargarPedidos();
            } else {
                pedidosTable.refresh();
            }
        } else {
            mostrarAlerta("Error de Guardado", "No se pudo actualizar el " + campoEditado + " del pedido ID " + pedido.getIdPedido() + " en la base de datos.", Alert.AlertType.ERROR);
            pedidosTable.refresh(); // Refresca para restaurar el valor antiguo en caso de error
        }
    }


    /**
     * Carga los pedidos activos (no Retirado), aplicando el filtro de Empleado si existe,
     * y los filtros de Estado y Método de Pago.
     */
    private void cargarPedidos() {
        // Obtiene la lista de pedidos del DAO
        List<Pedido> listaCompleta = pedidoDAO.getPedidosPorEmpleado(idEmpleadoFiltro);

        // --- Aplicar filtros de UI adicionales ---
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
                        // El método de pago puede ser nulo o "N/A" si no hay comprobante
                        // CAMBIO CLAVE: Usamos getTipoPago() que es lo que mapea el DAO desde ComprobantePago
                        return metodoSeleccionado.equals(p.getTipoPago());
                    }
                    return true;
                })
                .collect(Collectors.toList());

        pedidosTable.setItems(FXCollections.observableArrayList(listaFiltrada));
    }

    /**
     * Maneja el clic en el botón "Guardar Modificación".
     */
    @FXML
    private void handleGuardarCambios(ActionEvent event) {
        // Se asume que el guardado principal ocurre en onEditCommit, este es un guardado explícito.
        Pedido pedidoSeleccionado = pedidosTable.getSelectionModel().getSelectedItem();

        if (pedidoSeleccionado != null) {
            boolean exito = pedidoDAO.modificarPedido(pedidoSeleccionado);

            if (exito) {
                mostrarAlerta("Éxito", "El Pedido ID " + pedidoSeleccionado.getIdPedido() + " ha sido modificado y guardado correctamente.", Alert.AlertType.INFORMATION);
                if ("Retirado".equalsIgnoreCase(pedidoSeleccionado.getEstado())) {
                    cargarPedidos(); // Recarga si se marcó como Retirado para que desaparezca
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

    /**
     * Maneja el clic en el botón Limpiar Filtros.
     */
    @FXML
    private void handleLimpiarFiltros(ActionEvent event) {
        // Resetea todos los ComboBox a su primera opción ("Todos...")
        if (empleadoFilterComboBox != null) empleadoFilterComboBox.getSelectionModel().selectFirst();
        if (estadoFilterComboBox != null) estadoFilterComboBox.getSelectionModel().selectFirst();
        if (metodoPagoFilterComboBox != null) metodoPagoFilterComboBox.getSelectionModel().selectFirst();

        // El listener de valor del ComboBox de Empleado ya llama a cargarPedidos(), pero lo aseguramos
        idEmpleadoFiltro = 0;
        cargarPedidos();
    }

    @FXML
    private void handleVolver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PedidosPrimerMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root, 1800, 1000));
            stage.setTitle("Menú de Pedidos");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo volver al menú de pedidos.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Muestra una alerta simple.
     * @param titulo El título de la ventana.
     * @param mensaje El contenido del mensaje.
     * @param tipo El tipo de alerta (ERROR, INFORMATION, WARNING, etc.).
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null); // Eliminamos la cabecera ya que casi siempre se usa null.
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Clase auxiliar para formatear columnas de tipo LocalDateTime a String.
     */
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
}
