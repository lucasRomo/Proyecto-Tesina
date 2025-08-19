package app.model;

public class TipoDocumento {
    private int idTipoDocumento;
    private String nombreTipo;

    public TipoDocumento(int idTipoDocumento, String nombreTipo) {
        this.idTipoDocumento = idTipoDocumento;
        this.nombreTipo = nombreTipo;
    }

    public int getIdTipoDocumento() { return idTipoDocumento; }
    public String getNombreTipo() { return nombreTipo; }
}