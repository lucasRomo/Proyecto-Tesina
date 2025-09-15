package app.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Pedido {
    private final IntegerProperty idPedido = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> fecha = new SimpleObjectProperty<>();
    private final StringProperty estado = new SimpleStringProperty();
    private final IntegerProperty idCliente = new SimpleIntegerProperty();
    private final DoubleProperty montoTotal = new SimpleDoubleProperty();

    public Pedido(int idPedido, LocalDate fecha, String estado, int idCliente, double montoTotal) {
        this.idPedido.set(idPedido);
        this.fecha.set(fecha);
        this.estado.set(estado);
        this.idCliente.set(idCliente);
        this.montoTotal.set(montoTotal);
    }

    // Getters
    public int getIdPedido() { return idPedido.get(); }
    public LocalDate getFecha() { return fecha.get(); }
    public String getEstado() { return estado.get(); }
    public int getIdCliente() { return idCliente.get(); }
    public double getMontoTotal() { return montoTotal.get(); }

    // Propiedades para la TableView
    public IntegerProperty idPedidoProperty() { return idPedido; }
    public ObjectProperty<LocalDate> fechaProperty() { return fecha; }
    public StringProperty estadoProperty() { return estado; }
    public IntegerProperty idClienteProperty() { return idCliente; }
    public DoubleProperty montoTotalProperty() { return montoTotal; }
}