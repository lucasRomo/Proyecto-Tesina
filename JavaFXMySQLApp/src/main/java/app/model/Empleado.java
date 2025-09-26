package app.model;

import java.time.LocalDate;

public class Empleado {

    private int idEmpleado;
    private LocalDate fechaContratacion;
    private String cargo;
    private double salario;
    private int idPersona;
    private String nombre;
    private String apellido;

    public Empleado() {
    }

    // Constructor para cuando se obtienen datos desde la DB
    public Empleado(int idEmpleado, LocalDate fechaContratacion, String cargo, double salario, int idPersona, String nombre, String apellido) {
        this.idEmpleado = idEmpleado;
        this.fechaContratacion = fechaContratacion;
        this.cargo = cargo;
        this.salario = salario;
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    // Constructor sin id_empleado, útil para la inserción
    public Empleado(LocalDate fechaContratacion, String cargo, double salario, int idPersona) {
        this.fechaContratacion = fechaContratacion;
        this.cargo = cargo;
        this.salario = salario;
        this.idPersona = idPersona;
    }

    @Override
    public String toString() {
        return nombre + " " + apellido;
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

    // Métodos setNombre y setApellido
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    // Métodos getNombre y getApellido
    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }
}