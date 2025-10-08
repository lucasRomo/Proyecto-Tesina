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
    private final StringProperty nombreCliente;
    private final StringProperty numeroFactura;
    private final ObjectProperty<LocalDateTime> fechaEmision;
    private final DoubleProperty montoTotal;
    private final StringProperty estadoPago;

    /**
     * Constructor con 7 parámetros.
     */
    public FacturasAdminTableView(int idFactura, int idPedido, String nombreCliente, String numeroFactura, LocalDateTime fechaEmision, double montoTotal, String estadoPago) {
        this.idFactura = new SimpleIntegerProperty(idFactura);
        this.idPedido = new SimpleIntegerProperty(idPedido);
        this.nombreCliente = new SimpleStringProperty(nombreCliente);
        this.numeroFactura = new SimpleStringProperty(numeroFactura);
        this.fechaEmision = new SimpleObjectProperty<>(fechaEmision);
        this.montoTotal = new SimpleDoubleProperty(montoTotal);
        this.estadoPago = new SimpleStringProperty(estadoPago);
    }

    // --- Getters de Propiedades (Necesarios para el mapeo de la TableView) ---
    public IntegerProperty idFacturaProperty() { return idFactura; }
    public IntegerProperty idPedidoProperty() { return idPedido; }
    public StringProperty numeroFacturaProperty() { return numeroFactura; }
    public ObjectProperty<LocalDateTime> fechaEmisionProperty() { return fechaEmision; }
    public DoubleProperty montoTotalProperty() { return montoTotal; }
    public StringProperty estadoPagoProperty() { return estadoPago; }
    public StringProperty nombreClienteProperty() { return nombreCliente; }
    public String getNombreCliente() { return nombreCliente.get(); }

    // --- Getters estándar (para lectura) ---
    public int getIdFactura() { return idFactura.get(); }
    public int getIdPedido() { return idPedido.get(); }
    public String getnombreCliente() { return nombreCliente.get(); }
    public String getNumeroFactura() { return numeroFactura.get(); }
    public LocalDateTime getFechaEmision() { return fechaEmision.get(); }
    public double getMontoTotal() { return montoTotal.get(); }
    public String getEstadoPago() { return estadoPago.get(); }
    public void setnombreCliente(String nombreCliente) { this.nombreCliente.set(nombreCliente); }
}
