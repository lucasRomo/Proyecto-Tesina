package app.model;

import java.time.LocalDateTime;

public class AsignacionPedido {
    private int idAsignacion;
    private int idPedido;
    private int idEmpleado;
    private LocalDateTime fechaAsignacion;

    public AsignacionPedido(int idPedido, int idEmpleado, LocalDateTime fechaAsignacion) {
        this.idPedido = idPedido;
        this.idEmpleado = idEmpleado;
        this.fechaAsignacion = fechaAsignacion;
    }

    public AsignacionPedido(int idAsignacion, int idPedido, int idEmpleado, LocalDateTime fechaAsignacion) {
        this.idAsignacion = idAsignacion;
        this.idPedido = idPedido;
        this.idEmpleado = idEmpleado;
        this.fechaAsignacion = fechaAsignacion;
    }

    // Getters y Setters
    public int getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(int idAsignacion) {
        this.idAsignacion = idAsignacion;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }
}