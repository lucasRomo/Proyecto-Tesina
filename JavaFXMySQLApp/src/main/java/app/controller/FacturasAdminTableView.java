package app.controller;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Modelo de vista simplificado para la tabla administrativa de facturas.
 * Corresponde a las 7 columnas de la tabla Factura.
 */
public class FacturasAdminTableView {
    // Propiedades de la Factura (7 campos)
    private final IntegerProperty idFactura;
    private final IntegerProperty idPedido;
    private final IntegerProperty idCliente;
    private final StringProperty numeroFactura;
    private final ObjectProperty<LocalDateTime> fechaEmision;
    private final DoubleProperty montoTotal;
    private final StringProperty estadoPago;
    private final StringProperty nombreCliente;

    /**
     * Constructor con 7 parámetros.
     */
    public FacturasAdminTableView(int idFactura, int idPedido, int idCliente, String numeroFactura, LocalDateTime fechaEmision, double montoTotal, String estadoPago, String nombreCliente) {
        this.idFactura = new SimpleIntegerProperty(idFactura);
        this.idPedido = new SimpleIntegerProperty(idPedido);
        this.idCliente = new SimpleIntegerProperty(idCliente);
        this.numeroFactura = new SimpleStringProperty(numeroFactura);
        this.fechaEmision = new SimpleObjectProperty<>(fechaEmision);
        this.montoTotal = new SimpleDoubleProperty(montoTotal);
        this.estadoPago = new SimpleStringProperty(estadoPago);
        this.nombreCliente = new SimpleStringProperty(nombreCliente);
    }

    // --- Getters de Propiedades (Necesarios para el mapeo de la TableView) ---
    public IntegerProperty idFacturaProperty() { return idFactura; }
    public IntegerProperty idPedidoProperty() { return idPedido; }
    public IntegerProperty idClienteProperty() { return idCliente; }
    public StringProperty numeroFacturaProperty() { return numeroFactura; }
    public ObjectProperty<LocalDateTime> fechaEmisionProperty() { return fechaEmision; }
    public DoubleProperty montoTotalProperty() { return montoTotal; }
    public StringProperty estadoPagoProperty() { return estadoPago; }
    public StringProperty nombreClienteProperty() { return nombreCliente; }
    public String getNombreCliente() { return nombreCliente.get(); }

    // --- Getters estándar (para lectura) ---
    public int getIdFactura() { return idFactura.get(); }
    public int getIdPedido() { return idPedido.get(); }
    public int getIdCliente() { return idCliente.get(); }
    public String getNumeroFactura() { return numeroFactura.get(); }
    public LocalDateTime getFechaEmision() { return fechaEmision.get(); }
    public double getMontoTotal() { return montoTotal.get(); }
    public String getEstadoPago() { return estadoPago.get(); }

    public void setNombreCliente(String nombreCliente) { this.nombreCliente.set(nombreCliente); }
}
