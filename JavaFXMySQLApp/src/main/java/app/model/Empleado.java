package app.model;

import java.time.LocalDate;

public class Empleado {

    private int idEmpleado;
    private LocalDate fechaContratacion;
    private String cargo;
    private double salario;
    private int idPersona;

    public Empleado() {
    }

    // Constructor sin id_empleado, útil para la inserción
    public Empleado(LocalDate fechaContratacion, String cargo, double salario, int idPersona) {
        this.fechaContratacion = fechaContratacion;
        this.cargo = cargo;
        this.salario = salario;
        this.idPersona = idPersona;
    }

    // Constructor con todos los atributos
    public Empleado(int idEmpleado, LocalDate fechaContratacion, String cargo, double salario, int idPersona) {
        this.idEmpleado = idEmpleado;
        this.fechaContratacion = fechaContratacion;
        this.cargo = cargo;
        this.salario = salario;
        this.idPersona = idPersona;
    }

    // Getters y Setters
    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    public int getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(int idPersona) {
        this.idPersona = idPersona;
    }
}