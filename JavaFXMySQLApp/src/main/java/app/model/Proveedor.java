package app.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Proveedor {

    private final IntegerProperty idProveedor;
    private final StringProperty nombre;
    private final StringProperty contacto;
    private final StringProperty mail;
    private final StringProperty estado;
    private final IntegerProperty idDireccion;
    private final IntegerProperty idTipoProveedor;

    // Nuevo campo para la descripción del tipo de proveedor
    private final StringProperty descripcionTipoProveedor;

    // Constructor vacío por si es necesario para el DAO
    public Proveedor() {
        this(0, null, null, null, null, 0, 0, null);
    }

    // Constructor original - no lo borramos por si se usa en otro lado
    public Proveedor(int idProveedor, String nombre, String contacto, String mail, String estado, int idDireccion, int idTipoProveedor) {
        this(idProveedor, nombre, contacto, mail, estado, idDireccion, idTipoProveedor, null);
    }

    // Constructor actualizado para incluir la descripción
    public Proveedor(int idProveedor, String nombre, String contacto, String mail, String estado, int idDireccion, int idTipoProveedor, String descripcionTipoProveedor) {
        this.idProveedor = new SimpleIntegerProperty(idProveedor);
        this.nombre = new SimpleStringProperty(nombre);
        this.contacto = new SimpleStringProperty(contacto);
        this.mail = new SimpleStringProperty(mail);
        this.estado = new SimpleStringProperty(estado);
        this.idDireccion = new SimpleIntegerProperty(idDireccion);
        this.idTipoProveedor = new SimpleIntegerProperty(idTipoProveedor);
        this.descripcionTipoProveedor = new SimpleStringProperty(descripcionTipoProveedor);
    }

    // Getters y Setters
    public int getIdProveedor() {
        return idProveedor.get();
    }

    public String getNombre() {
        return nombre.get();
    }

    public String getContacto() {
        return contacto.get();
    }

    public String getMail() {
        return mail.get();
    }

    public String getEstado() {
        return estado.get();
    }

    public int getIdDireccion() {
        return idDireccion.get();
    }

    public int getIdTipoProveedor() {
        return idTipoProveedor.get();
    }

    public String getDescripcionTipoProveedor() {
        return descripcionTipoProveedor.get();
    }

    // Propiedades
    public IntegerProperty idProveedorProperty() {
        return idProveedor;
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public StringProperty contactoProperty() {
        return contacto;
    }

    public StringProperty mailProperty() {
        return mail;
    }

    public StringProperty estadoProperty() {
        return estado;
    }

    public IntegerProperty idDireccionProperty() {
        return idDireccion;
    }

    public IntegerProperty idTipoProveedorProperty() {
        return idTipoProveedor;
    }

    public StringProperty descripcionTipoProveedorProperty() {
        return descripcionTipoProveedor;
    }

    // Setters
    public void setIdProveedor(int idProveedor) {
        this.idProveedor.set(idProveedor);
    }

    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public void setContacto(String contacto) {
        this.contacto.set(contacto);
    }

    public void setMail(String mail) {
        this.mail.set(mail);
    }

    public void setEstado(String estado) {
        this.estado.set(estado);
    }

    public void setIdDireccion(int idDireccion) {
        this.idDireccion.set(idDireccion);
    }

    public void setIdTipoProveedor(int idTipoProveedor) {
        this.idTipoProveedor.set(idTipoProveedor);
    }

    public void setDescripcionTipoProveedor(String descripcionTipoProveedor) {
        this.descripcionTipoProveedor.set(descripcionTipoProveedor);
    }
}