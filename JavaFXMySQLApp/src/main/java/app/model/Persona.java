package app.model;

import javafx.beans.property.*;

public class Persona {
    private final IntegerProperty idPersona = new SimpleIntegerProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty apellido = new SimpleStringProperty();
    private final IntegerProperty idTipoDocumento = new SimpleIntegerProperty();
    private final StringProperty numeroDocumento = new SimpleStringProperty();
    private final IntegerProperty idDireccion = new SimpleIntegerProperty();
    private final StringProperty telefono = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();

    public Persona(String nombre, String apellido, int idTipoDocumento, String numeroDocumento, int idDireccion, String telefono, String email) {
        this.nombre.set(nombre);
        this.apellido.set(apellido);
        this.idTipoDocumento.set(idTipoDocumento);
        this.numeroDocumento.set(numeroDocumento);
        this.idDireccion.set(idDireccion);
        this.telefono.set(telefono);
        this.email.set(email);
    }

    public Persona(String nombre, String apellido) {
        this.nombre.set(nombre);
        this.apellido.set(apellido);
        this.idTipoDocumento.set(0);
        this.numeroDocumento.set("");
        this.idDireccion.set(0);
        this.telefono.set("");
        this.email.set("");
    }

    // Getters y Setters con sus respectivos métodos Property()
    public int getIdPersona() { return idPersona.get(); }
    public void setIdPersona(int idPersona) { this.idPersona.set(idPersona); }
    public IntegerProperty idPersonaProperty() { return idPersona; }

    public String getNombre() { return nombre.get(); }
    public StringProperty nombreProperty() { return nombre; }
    public void setNombre(String nombre) { this.nombre.set(nombre); }

    public String getApellido() { return apellido.get(); }
    public StringProperty apellidoProperty() { return apellido; }
    public void setApellido(String apellido) { this.apellido.set(apellido); }

    public int getIdTipoDocumento() { return idTipoDocumento.get(); }
    public IntegerProperty idTipoDocumentoProperty() { return idTipoDocumento; }
    public void setIdTipoDocumento(int idTipoDocumento) { this.idTipoDocumento.set(idTipoDocumento); }

    public String getNumeroDocumento() { return numeroDocumento.get(); }
    public StringProperty numeroDocumentoProperty() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento.set(numeroDocumento); }

    public int getIdDireccion() { return idDireccion.get(); }
    public IntegerProperty idDireccionProperty() { return idDireccion; }
    public void setIdDireccion(int idDireccion) { this.idDireccion.set(idDireccion); }

    public String getTelefono() { return telefono.get(); }
    public StringProperty telefonoProperty() { return telefono; }
    public void setTelefono(String telefono) { this.telefono.set(telefono); }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }
    public void setEmail(String email) { this.email.set(email); }
}