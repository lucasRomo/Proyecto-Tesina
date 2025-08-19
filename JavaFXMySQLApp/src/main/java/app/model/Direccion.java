package app.model;

public class Direccion {
    private int idDireccion;
    private String calle;
    private String numero;
    private String piso;
    private String departamento;
    private String codigoPostal;
    private String ciudad;
    private String provincia;
    private String pais;

    public Direccion(String calle, String numero, String piso, String departamento, String codigoPostal, String ciudad, String provincia, String pais) {
        this.calle = calle;
        this.numero = numero;
        this.piso = piso;
        this.departamento = departamento;
        this.codigoPostal = codigoPostal;
        this.ciudad = ciudad;
        this.provincia = provincia;
        this.pais = pais;
    }

    public int getIdDireccion() { return idDireccion; }
    public void setIdDireccion(int idDireccion) { this.idDireccion = idDireccion; }
    public String getCalle() { return calle; }
    public String getNumero() { return numero; }
    public String getPiso() { return piso; }
    public String getDepartamento() { return departamento; }
    public String getCodigoPostal() { return codigoPostal; }
    public String getCiudad() { return ciudad; }
    public String getProvincia() { return provincia; }
    public String getPais() { return pais; }
}