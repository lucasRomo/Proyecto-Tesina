package app.model;

import java.time.LocalDateTime;

public class Pedido {

    private int idPedido;
    private int idCliente;
    private String nombreCliente; // Para la vista
    private int idEmpleado;
    private String nombreEmpleado; // Para la vista
    private String estado;
    private String tipoPago;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEntregaEstimada;
    private LocalDateTime fechaFinalizacion; // Puede ser null
    private String instrucciones;
    private double montoTotal;
    private double montoEntregado;
    private String rutaComprobante; // Puede ser null
    private String telefonoCliente;
    private String emailCliente;
    // =========================================================================
    // CONSTRUCTOR COMPLETO (14 Parámetros) - CRUCIAL PARA LA AUDITORÍA
    // =========================================================================

    /**
     * Constructor completo utilizado para obtener datos de la BD y para crear
     * la copia original del pedido para auditoría (crearCopiaPedido).
     */
    public Pedido(
            int idPedido,
            int idCliente,
            String nombreCliente,
            int idEmpleado,
            String nombreEmpleado,
            String estado,
            String tipoPago,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaEntregaEstimada,
            LocalDateTime fechaFinalizacion,
            String instrucciones,
            double montoTotal,
            double montoEntregado,
            String rutaComprobante
    ) {
        this.idPedido = idPedido;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.idEmpleado = idEmpleado;
        this.nombreEmpleado = nombreEmpleado;
        this.estado = estado;
        this.tipoPago = tipoPago;
        this.fechaCreacion = fechaCreacion;
        this.fechaEntregaEstimada = fechaEntregaEstimada;
        this.fechaFinalizacion = fechaFinalizacion;
        this.instrucciones = instrucciones;
        this.montoTotal = montoTotal;
        this.montoEntregado = montoEntregado;
        this.rutaComprobante = rutaComprobante;
    }


    // =========================================================================
    // CONSTRUCTOR BÁSICO (Para la creación inicial del pedido)
    // =========================================================================

    public Pedido(int idCliente, int idEmpleado, String tipoPago, LocalDateTime fechaEntregaEstimada, String instrucciones, double montoTotal) {
        this.idCliente = idCliente;
        this.idEmpleado = idEmpleado;
        this.tipoPago = tipoPago;
        this.fechaEntregaEstimada = fechaEntregaEstimada;
        this.instrucciones = instrucciones;
        this.montoTotal = montoTotal;

        // Valores por defecto al crear
        this.estado = "Pendiente";
        this.fechaCreacion = LocalDateTime.now();
        this.montoEntregado = 0.0;
        this.fechaFinalizacion = null;
        this.rutaComprobante = null;
    }

    public Pedido(
            int idPedido,
            int idCliente,
            String nombreCliente,
            // <-- AÑADIDOS LOS 2 NUEVOS CAMPOS -->
            String telefonoCliente,
            String emailCliente,
            // <-- AÑADIDOS LOS 2 NUEVOS CAMPOS -->
            int idEmpleado,
            String nombreEmpleado,
            String estado,
            String tipoPago,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaEntregaEstimada,
            LocalDateTime fechaFinalizacion,
            String instrucciones,
            double montoTotal,
            double montoEntregado,
            String rutaComprobante
    ) {
        this.idPedido = idPedido;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        // Inicialización de los nuevos campos
        this.telefonoCliente = telefonoCliente;
        this.emailCliente = emailCliente;
        this.idEmpleado = idEmpleado;
        this.nombreEmpleado = nombreEmpleado;
        this.estado = estado;
        this.tipoPago = tipoPago;
        this.fechaCreacion = fechaCreacion;
        this.fechaEntregaEstimada = fechaEntregaEstimada;
        this.fechaFinalizacion = fechaFinalizacion;
        this.instrucciones = instrucciones;
        this.montoTotal = montoTotal;
        this.montoEntregado = montoEntregado;
        this.rutaComprobante = rutaComprobante;
    }

    // =========================================================================
    // GETTERS Y SETTERS
    // =========================================================================

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

    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
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

    public String getRutaComprobante() {
        return rutaComprobante;
    }

    public void setRutaComprobante(String rutaComprobante) {
        this.rutaComprobante = rutaComprobante;
    }

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }

    public String getEmailCliente() {
        return emailCliente;
    }

    public void setEmailCliente(String emailCliente) {
        this.emailCliente = emailCliente;
    }
}