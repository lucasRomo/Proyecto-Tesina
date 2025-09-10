package app.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.DoubleProperty;

// Esta clase combina la información de Persona, Empleado y Usuario
// para mostrarla en la vista de administración de usuarios.
public class UsuarioEmpleadoView {
    private final IntegerProperty idUsuario;
    private final StringProperty usuario;
    private final StringProperty nombre;
    private final StringProperty apellido;
    private final DoubleProperty salario;
    private final StringProperty estado;
    private final IntegerProperty idPersona;

    public UsuarioEmpleadoView(int idUsuario, String usuario, String nombre, String apellido, double salario, String estado, int idPersona) {
        this.idUsuario = new SimpleIntegerProperty(idUsuario);
        this.usuario = new SimpleStringProperty(usuario);
        this.nombre = new SimpleStringProperty(nombre);
        this.apellido = new SimpleStringProperty(apellido);
        this.salario = new SimpleDoubleProperty(salario);
        this.estado = new SimpleStringProperty(estado);
        this.idPersona = new SimpleIntegerProperty(idPersona);
    }

    // Getters
    public int getIdUsuario() { return idUsuario.get(); }
    public String getUsuario() { return usuario.get(); }
    public String getNombre() { return nombre.get(); }
    public String getApellido() { return apellido.get(); }
    public double getSalario() { return salario.get(); }
    public String getEstado() { return estado.get(); }
    public int getIdPersona() { return idPersona.get(); }

    // Setters
    public void setUsuario(String usuario) { this.usuario.set(usuario); }
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public void setApellido(String apellido) { this.apellido.set(apellido); }
    public void setSalario(double salario) { this.salario.set(salario); }
    public void setEstado(String estado) { this.estado.set(estado); }

    // Property getters para la TableView
    public IntegerProperty idUsuarioProperty() { return idUsuario; }
    public StringProperty usuarioProperty() { return usuario; }
    public StringProperty nombreProperty() { return nombre; }
    public StringProperty apellidoProperty() { return apellido; }
    public DoubleProperty salarioProperty() { return salario; }
    public StringProperty estadoProperty() { return estado; }
    public IntegerProperty idPersonaProperty() { return idPersona; }
}
