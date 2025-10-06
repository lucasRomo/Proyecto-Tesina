package app.controller;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Clase de modelo para representar una fila en la tabla de Comprobantes.
 * Utiliza propiedades de JavaFX (SimpleXProperty) para permitir que la TableView
 * observe los cambios en los datos en tiempo real.
 */
public class ComprobantesAdminTableView {

    // Propiedades mapeadas a las columnas
    private final LongProperty idComprobante = new SimpleLongProperty(this, "idComprobante");
    private final LongProperty idPedido = new SimpleLongProperty(this, "idPedido");
    private final StringProperty tipoPago = new SimpleStringProperty(this, "tipoPago");
    private final DoubleProperty montoPago = new SimpleDoubleProperty(this, "montoPago");
    private final ObjectProperty<LocalDateTime> fechaCarga = new SimpleObjectProperty<>(this, "fechaCarga");
    private final ObjectProperty<LocalDateTime> fechaVerificacion = new SimpleObjectProperty<>(this, "fechaVerificacion");

    // Propiedad adicional para el estado de verificación
    private final StringProperty estadoVerificacion = new SimpleStringProperty(this, "estadoVerificacion");

    // Propiedad para el nombre completo (usado en la barra de búsqueda si aplica)
    private final StringProperty nombreCompletoCliente = new SimpleStringProperty(this, "nombreCompletoCliente");


    /**
     * Constructor para inicializar una instancia de ComprobanteData.
     */
    public ComprobantesAdminTableView(long idComprobante, long idPedido, String tipoPago,
                           double montoPago, LocalDateTime fechaCarga, LocalDateTime fechaVerificacion,
                           String estadoVerificacion, String nombreCompletoCliente) {

        this.idComprobante.set(idComprobante);
        this.idPedido.set(idPedido);
        this.tipoPago.set(tipoPago);
        this.montoPago.set(montoPago);
        this.fechaCarga.set(fechaCarga);
        this.fechaVerificacion.set(fechaVerificacion);
        this.estadoVerificacion.set(estadoVerificacion);
        this.nombreCompletoCliente.set(nombreCompletoCliente);
    }

    // --- Getters y Setters Estándar (opcional, pero buena práctica) ---
    // (JavaFX PropertyValueFactory busca directamente los métodos property)

    // --- Métodos Property (CRUCIALES para JavaFX TableView) ---

    public LongProperty idComprobanteProperty() { return idComprobante; }
    public LongProperty idPedidoProperty() { return idPedido; }
    public StringProperty tipoPagoProperty() { return tipoPago; }
    public DoubleProperty montoPagoProperty() { return montoPago; }
    public ObjectProperty<LocalDateTime> fechaCargaProperty() { return fechaCarga; }
    public ObjectProperty<LocalDateTime> fechaVerificacionProperty() { return fechaVerificacion; }
    public StringProperty estadoVerificacionProperty() { return estadoVerificacion; }
    public StringProperty nombreCompletoClienteProperty() { return nombreCompletoCliente; }

    // Puedes agregar aquí los métodos get/set si planeas modificar los valores programáticamente
    public long getIdComprobante() { return idComprobante.get(); }
    public String getTipoPago() { return tipoPago.get(); }
    public String getnombreCompletoCliente() { return nombreCompletoCliente.get(); }

}
