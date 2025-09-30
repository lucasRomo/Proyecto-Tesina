package app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class FacturasController {
    @FXML
    private TableView<UsuarioEmpleadoTableView> usuariosEditableView;
    @FXML private TableColumn<FacturasAdminTableView, Number> IdFacturaColumN;
    @FXML private TableColumn<FacturasAdminTableView, Number> IdPedidoColumn;
    @FXML private TableColumn<FacturasAdminTableView, Number> IdClienteColumn;
    @FXML private TableColumn<FacturasAdminTableView, Number> NumeroFacturaColumn;
    @FXML private TableColumn<FacturasAdminTableView, Number> FechaEmisionColumn;
    @FXML private TableColumn<FacturasAdminTableView, Number> MontoTotalColumn;
    @FXML private TableColumn<FacturasAdminTableView, String> EstadoPagoColumn;
    @FXML private TableColumn<FacturasAdminTableView, Void> AccionArchivoColumn;
    @FXML private Button modificarUsuarioButton;
    @FXML private TextField filterField;
}
