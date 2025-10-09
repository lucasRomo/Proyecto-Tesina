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

    // 1. AÑADIR LA PROPIEDAD idTipoPersona
    private final IntegerProperty idTipoPersona = new SimpleIntegerProperty();

    // Constructor de 7 argumentos (Original)
    public Persona(String nombre, String apellido, int idTipoDocumento, String numeroDocumento, int idDireccion, String telefono, String email) {
        this.nombre.set(nombre);
        this.apellido.set(apellido);
        this.idTipoDocumento.set(idTipoDocumento);
        this.numeroDocumento.set(numeroDocumento);
        this.idDireccion.set(idDireccion);
        this.telefono.set(telefono);
        this.email.set(email);
        // Inicializar idTipoPersona por defecto si es necesario (ej: a 0 o 1 para Cliente)
        this.idTipoPersona.set(0);
    }

    // 2. AÑADIR EL CONSTRUCTOR DE 8 ARGUMENTOS (Requerido por EmpleadoController)
    public Persona(String nombre, String apellido, int idTipoDocumento, String numeroDocumento, int idDireccion, String telefono, String email, int idTipoPersona) {
        this.nombre.set(nombre);
        this.apellido.set(apellido);
        this.idTipoDocumento.set(idTipoDocumento);
        this.numeroDocumento.set(numeroDocumento);
        this.idDireccion.set(idDireccion);
        this.telefono.set(telefono);
        this.email.set(email);
        this.idTipoPersona.set(idTipoPersona); // <--- ESTE ES EL NUEVO VALOR
    }

    // Constructor de 2 argumentos (Original)
    public Persona(String nombre, String apellido) {
        this.nombre.set(nombre);
        this.apellido.set(apellido);
        this.idTipoDocumento.set(0);
        this.numeroDocumento.set("");
        this.idDireccion.set(0);
        this.telefono.set("");
        this.email.set("");
        this.idTipoPersona.set(0); // Inicializar idTipoPersona por defecto
    }

    public Persona() {}

    public Persona(int idPersona, String nombre, String apellido) {
        this.idPersona.set(idPersona);
        this.nombre.set(nombre);
        this.apellido.set(apellido);
    }

    // Getters y Setters con sus respectivos métodos Property() (EXISTENTES)
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

    // --- NUEVOS MÉTODOS PARA idTipoPersona ---
    public int getIdTipoPersona() { return idTipoPersona.get(); }
    public IntegerProperty idTipoPersonaProperty() { return idTipoPersona; }
    public void setIdTipoPersona(int idTipoPersona) { this.idTipoPersona.set(idTipoPersona); }
}