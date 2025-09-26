package app.model;

import java.time.LocalDateTime;

public class Pedido {

    private int idPedido;
    private int idCliente;
    private String nombreCliente;
    private int idEmpleado;
    private String nombreEmpleado;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEntregaEstimada;
    private LocalDateTime fechaFinalizacion;
    private String instrucciones;
    private double montoTotal;
    private double montoEntregado;

    /**
     * Constructor para crear un nuevo pedido que se va a insertar en la base de datos.
     * El idPedido se asignará automáticamente en la base de datos.
     *
     * @param idCliente El ID del cliente asociado al pedido.
     * @param idEmpleado El ID del empleado asignado al pedido.
     * @param fechaCreacion La fecha y hora de creación del pedido.
     * @param fechaEntregaEstimada La fecha y hora de entrega estimada.
     * @param fechaFinalizacion La fecha y hora de finalización del pedido.
     * @param estado El estado actual del pedido.
     * @param instrucciones Instrucciones adicionales para el pedido.
     * @param montoTotal El monto total del pedido.
     * @param montoEntregado El monto entregado por el cliente.
     */
    public Pedido(int idCliente, int idEmpleado, LocalDateTime fechaCreacion, LocalDateTime fechaEntregaEstimada,
                  LocalDateTime fechaFinalizacion, String estado, String instrucciones, double montoTotal, double montoEntregado) {
        this.idCliente = idCliente;
        this.idEmpleado = idEmpleado;
        this.fechaCreacion = fechaCreacion;
        this.fechaEntregaEstimada = fechaEntregaEstimada;
        this.fechaFinalizacion = fechaFinalizacion;
        this.estado = estado;
        this.instrucciones = instrucciones;
        this.montoTotal = montoTotal;
        this.montoEntregado = montoEntregado;
    }

    /**
     * Constructor para inicializar un objeto Pedido con todos los campos,
     * típicamente utilizado al recuperar datos de la base de datos.
     *
     * @param idPedido El ID del pedido.
     * @param idCliente El ID del cliente.
     * @param nombreCliente El nombre del cliente.
     * @param idEmpleado El ID del empleado.
     * @param nombreEmpleado El nombre del empleado.
     * @param estado El estado del pedido.
     * @param fechaCreacion La fecha de creación.
     * @param fechaEntregaEstimada La fecha de entrega estimada.
     * @param fechaFinalizacion La fecha de finalización.
     * @param instrucciones Las instrucciones.
     * @param montoTotal El monto total.
     * @param montoEntregado El monto entregado.
     */
    public Pedido(int idPedido, int idCliente, String nombreCliente, int idEmpleado, String nombreEmpleado, String estado, LocalDateTime fechaCreacion, LocalDateTime fechaEntregaEstimada, LocalDateTime fechaFinalizacion, String instrucciones, double montoTotal, double montoEntregado) {
        this.idPedido = idPedido;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.idEmpleado = idEmpleado;
        this.nombreEmpleado = nombreEmpleado;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaEntregaEstimada = fechaEntregaEstimada;
        this.fechaFinalizacion = fechaFinalizacion;
        this.instrucciones = instrucciones;
        this.montoTotal = montoTotal;
        this.montoEntregado = montoEntregado;
    }

    // Getters y setters (sin cambios)
    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaEntregaEstimada() {
        return fechaEntregaEstimada;
    }

    public void setFechaEntregaEstimada(LocalDateTime fechaEntregaEstimada) {
        this.fechaEntregaEstimada = fechaEntregaEstimada;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public String getInstrucciones() {
        return instrucciones;
    }

    public void setInstrucciones(String instrucciones) {
        this.instrucciones = instrucciones;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public double getMontoEntregado() {
        return montoEntregado;
    }

    public void setMontoEntregado(double montoEntregado) {
        this.montoEntregado = montoEntregado;
    }

    @Override
    public String toString() {
        // Formato para mostrar el objeto en el ComboBox
        return String.format("Pedido ID: %d, Cliente: %s, Estado: %s", idPedido, nombreCliente, estado);
    }
}