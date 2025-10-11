package app.model;

import java.time.LocalDateTime;

public class Pedido {
    private int idPedido;
    private int idCliente;
    private int idEmpleado;
    private String nombreCliente;
    private String nombreEmpleado;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEntregaEstimada;
    private LocalDateTime fechaFinalizacion;
    private String estado;
    // La columna 'metodo_pago' ha sido eliminada de la base de datos (se usa 'tipoPago' de ComprobantePago)
    private String tipoPago; // <-- Viene de ComprobantePago, usado solo para mostrar en la UI
    private String instrucciones;
    private double montoTotal;
    private double montoEntregado;

    /**
     * Constructor para crear un NUEVO Pedido. No maneja información de pago.
     */
    public Pedido(int idCliente, int idEmpleado, LocalDateTime fechaCreacion, LocalDateTime fechaEntregaEstimada,
                  LocalDateTime fechaFinalizacion, String estado, String instrucciones,
                  double montoTotal, double montoEntregado) {
        this.idCliente = idCliente;
        this.idEmpleado = idEmpleado;
        this.fechaCreacion = fechaCreacion;
        this.fechaEntregaEstimada = fechaEntregaEstimada;
        this.fechaFinalizacion = fechaFinalizacion;
        this.estado = estado;
        this.instrucciones = instrucciones;
        this.montoTotal = montoTotal;
        this.montoEntregado = montoEntregado;
        this.tipoPago = "N/A"; // Inicializado para evitar NullPointer en la UI antes de la consulta
    }

    /**
     * Constructor COMPLETO para cuando se recuperan Pedidos del DAO.
     * Incluye 'tipoPago' recuperado del JOIN a ComprobantePago.
     */
    public Pedido(int idPedido, int idCliente, String nombreCliente, int idEmpleado, String nombreEmpleado,
                  String estado, String tipoPago, LocalDateTime fechaCreacion, LocalDateTime fechaEntregaEstimada,
                  LocalDateTime fechaFinalizacion, String instrucciones, double montoTotal, double montoEntregado) {
        this.idPedido = idPedido;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.idEmpleado = idEmpleado;
        this.nombreEmpleado = nombreEmpleado;
        this.estado = estado;
        this.tipoPago = tipoPago; // <-- Asignado desde el DAO (ComprobantePago)
        this.fechaCreacion = fechaCreacion;
        this.fechaEntregaEstimada = fechaEntregaEstimada;
        this.fechaFinalizacion = fechaFinalizacion;
        this.instrucciones = instrucciones;
        this.montoTotal = montoTotal;
        this.montoEntregado = montoEntregado;
    }

    // Getters y Setters
    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaEntregaEstimada() { return fechaEntregaEstimada; }
    public void setFechaEntregaEstimada(LocalDateTime fechaEntregaEstimada) { this.fechaEntregaEstimada = fechaEntregaEstimada; }

    public LocalDateTime getFechaFinalizacion() { return fechaFinalizacion; }
    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) { this.fechaFinalizacion = fechaFinalizacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // GETTER/SETTER para el tipo de pago (proveniente de ComprobantePago)
    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }

    public String getInstrucciones() { return instrucciones; }
    public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }

    public double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }

    public double getMontoEntregado() { return montoEntregado; }
    public void setMontoEntregado(double montoEntregado) { this.montoEntregado = montoEntregado; }
}