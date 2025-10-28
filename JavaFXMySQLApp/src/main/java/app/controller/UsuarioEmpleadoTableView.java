package app.controller;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UsuarioEmpleadoTableView {
    private final IntegerProperty idUsuario;
    private final StringProperty usuario;
    private final StringProperty contrasena;
    private final StringProperty nombre;
    private final StringProperty apellido;
    private final DoubleProperty salario;
    private final StringProperty estado;
    private final IntegerProperty idPersona;
    private final IntegerProperty idDireccion;
    private StringProperty numeroDocumento;
    private final IntegerProperty idTipoUsuario;

    public UsuarioEmpleadoTableView(int idUsuario, String usuario, String contrasena, String nombre, String apellido, double salario, String estado, int idPersona, int idDireccion, int idTipoUsuario) {
        this.idUsuario = new SimpleIntegerProperty(idUsuario);
        this.usuario = new SimpleStringProperty(usuario);
        this.contrasena = new SimpleStringProperty(contrasena);
        this.nombre = new SimpleStringProperty(nombre);
        this.apellido = new SimpleStringProperty(apellido);
        this.salario = new SimpleDoubleProperty(salario);
        this.estado = new SimpleStringProperty(estado);
        this.idPersona = new SimpleIntegerProperty(idPersona);
        this.idDireccion = new SimpleIntegerProperty(idDireccion);
        this.idTipoUsuario = new SimpleIntegerProperty(idTipoUsuario); // <-- Inicialización
    }

    public UsuarioEmpleadoTableView(int idUsuario, String usuario, String contrasena, String nombre, String apellido,
                                    String numeroDocumento, double salario, String estado, int idPersona, int idDireccion, int idTipoUsuario) {
        // ... inicialización de campos existentes
        this.idUsuario = new SimpleIntegerProperty(idUsuario);
        this.usuario = new SimpleStringProperty(usuario);
        this.contrasena = new SimpleStringProperty(contrasena);
        this.nombre = new SimpleStringProperty(nombre);
        this.apellido = new SimpleStringProperty(apellido);
        this.salario = new SimpleDoubleProperty(salario);
        this.estado = new SimpleStringProperty(estado);
        this.idPersona = new SimpleIntegerProperty(idPersona);
        this.idDireccion = new SimpleIntegerProperty(idDireccion);
        this.numeroDocumento = new SimpleStringProperty(numeroDocumento); // Inicializa el nuevo campo
        this.idTipoUsuario = new SimpleIntegerProperty(idTipoUsuario);
    }

    public int getIdUsuario() {
        return this.idUsuario.get();
    }

    public String getUsuario() {
        return (String)this.usuario.get();
    }

    public String getContrasena() {
        return (String)this.contrasena.get();
    }

    public String getNombre() {
        return (String)this.nombre.get();
    }

    public String getApellido() {
        return (String)this.apellido.get();
    }

    public double getSalario() {
        return this.salario.get();
    }

    public String getEstado() {
        return (String)this.estado.get();
    }

    public int getIdPersona() {
        return this.idPersona.get();
    }

    public int getIdDireccion() {return this.idDireccion.get();}

    public void setUsuario(String usuario) {
        this.usuario.set(usuario);
    }

    public void setContrasena(String contrasena) {
        this.contrasena.set(contrasena);
    }

    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public void setApellido(String apellido) {
        this.apellido.set(apellido);
    }

    public void setSalario(double salario) {
        this.salario.set(salario);
    }

    public void setEstado(String estado) {
        this.estado.set(estado);
    }

    public void setIdDireccion (int idDireccion) {this.idDireccion.set(idDireccion);}

    public String getNumeroDocumento() { return numeroDocumento.get(); }

    public StringProperty numeroDocumentoProperty() { return numeroDocumento; }

    public IntegerProperty idUsuarioProperty() {
        return this.idUsuario;
    }

    public StringProperty usuarioProperty() {
        return this.usuario;
    }

    public StringProperty contrasenaProperty() {
        return this.contrasena;
    }

    public StringProperty nombreProperty() {
        return this.nombre;
    }

    public StringProperty apellidoProperty() {
        return this.apellido;
    }

    public DoubleProperty salarioProperty() {
        return this.salario;
    }

    public StringProperty estadoProperty() {
        return this.estado;
    }

    public IntegerProperty idPersonaProperty() {
        return this.idPersona;
    }

    public int getIdTipoUsuario() {
        return this.idTipoUsuario.get();
    }

    public void setIdTipoUsuario(int idTipoUsuario) {
        this.idTipoUsuario.set(idTipoUsuario);
    }

    public IntegerProperty idTipoUsuarioProperty() {
        return this.idTipoUsuario;
    }

}