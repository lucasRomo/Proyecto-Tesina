package app.model;

import java.time.LocalDate;

public class Empleado {

    private int idEmpleado;
    private LocalDate fechaContratacion;
    private String cargo;
    private double salario;
    private String estado;
    private int idPersona;
    private String nombre;
    private String apellido;

    public Empleado() {
    }

    // Constructor completo (usado para lectura desde DB)
    public Empleado(int idEmpleado, LocalDate fechaContratacion, String cargo, double salario, String estado, int idPersona, String nombre, String apellido) {
        this.idEmpleado = idEmpleado;
        this.fechaContratacion = fechaContratacion;
        this.cargo = cargo;
        this.salario = salario;
        this.estado = estado;
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    // Constructor para inserción (sin id_empleado, con id_persona)
    public Empleado(LocalDate fechaContratacion, String cargo, double salario, String estado, int idPersona) {
        this.fechaContratacion = fechaContratacion;
        this.cargo = cargo;
        this.salario = salario;
        this.estado = estado;
        this.idPersona = idPersona;
    }

    public Empleado(int idPersona, double salario) {
        this.idPersona = idPersona;
        this.salario = salario;
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

    public String getCargo() {
        return cargo;
    }

    public double getSalario() {
        return salario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(int idPersona) {
        this.idPersona = idPersona;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }
}
