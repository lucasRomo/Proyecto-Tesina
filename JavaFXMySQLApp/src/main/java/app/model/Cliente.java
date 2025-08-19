package app.model;

public class Cliente extends Persona {
    private int idCliente;
    private String razonSocial;
    private String personaContacto;
    private String condicionesPago;

    public Cliente(String nombre, String apellido, int idTipoDocumento, String numeroDocumento, int idDireccion, String telefono, String email, String razonSocial, String personaContacto, String condicionesPago) {
        super(nombre, apellido, idTipoDocumento, numeroDocumento, idDireccion, telefono, email);
        this.razonSocial = razonSocial;
        this.personaContacto = personaContacto;
        this.condicionesPago = condicionesPago;
    }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getRazonSocial() { return razonSocial; }
    public String getPersonaContacto() { return personaContacto; }
    public String getCondicionesPago() { return condicionesPago; }
}