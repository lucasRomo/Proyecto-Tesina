package app.model;

public class Persona {
    private int idPersona;
    private String nombre;
    private String apellido;
    private int idTipoDocumento;
    private String numeroDocumento;
    private int idDireccion;
    private String telefono;
    private String email;

    public Persona(String nombre, String apellido, int idTipoDocumento, String numeroDocumento, int idDireccion, String telefono, String email) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.idTipoDocumento = idTipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.idDireccion = idDireccion;
        this.telefono = telefono;
        this.email = email;
    }

    public int getIdPersona() { return idPersona; }
    public void setIdPersona(int idPersona) { this.idPersona = idPersona; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public int getIdTipoDocumento() { return idTipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public int getIdDireccion() { return idDireccion; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
}