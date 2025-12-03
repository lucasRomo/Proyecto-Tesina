package app.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Cliente extends Persona {
    private IntegerProperty idCliente;
    private StringProperty razonSocial;
    private StringProperty personaContacto;
    private StringProperty condicionesPago;
    private StringProperty estado;

    // PROPIEDAD OBSERVABLE PARA EL NOMBRE DEL TIPO DE DOCUMENTO
    private final StringProperty tipoDocumentoNombre = new SimpleStringProperty(this, "tipoDocumentoNombre");

    // =========================================================================
    // === CONSTRUCTOR PRINCIPAL ===============================================
    // =========================================================================
    public Cliente(String nombre, String apellido, int idTipoDocumento, String numeroDocumento, int idDireccion,
                   String telefono, String email, String razonSocial, String personaContacto,
                   String condicionesPago, String estado) {
        super(nombre, apellido, idTipoDocumento, numeroDocumento, idDireccion, telefono, email);

        this.idCliente = new SimpleIntegerProperty();
        this.razonSocial = new SimpleStringProperty(razonSocial);
        this.personaContacto = new SimpleStringProperty(personaContacto);
        this.condicionesPago = new SimpleStringProperty(condicionesPago);
        this.estado = new SimpleStringProperty(estado);
        this.tipoDocumentoNombre.set(null); // Se asignará después
    }

    // =========================================================================
    // === CONSTRUCTOR DE COPIA ================================================
    // =========================================================================
    public Cliente(Cliente otroCliente) {
        super(otroCliente.getNombre(), otroCliente.getApellido(), otroCliente.getIdTipoDocumento(),
                otroCliente.getNumeroDocumento(), otroCliente.getIdDireccion(), otroCliente.getTelefono(), otroCliente.getEmail());

        this.idCliente = new SimpleIntegerProperty(otroCliente.getIdCliente());
        this.razonSocial = new SimpleStringProperty(otroCliente.getRazonSocial());
        this.personaContacto = new SimpleStringProperty(otroCliente.getPersonaContacto());
        this.condicionesPago = new SimpleStringProperty(otroCliente.getCondicionesPago());
        this.estado = new SimpleStringProperty(otroCliente.getEstado());
        this.tipoDocumentoNombre.set(otroCliente.getTipoDocumentoNombre());

        this.setIdPersona(otroCliente.getIdPersona());
    }

    // =========================================================================
    // === GETTERS Y SETTERS ===================================================
    // =========================================================================
    public int getIdCliente() { return idCliente.get(); }
    public IntegerProperty idClienteProperty() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente.set(idCliente); }

    public String getRazonSocial() { return razonSocial.get(); }
    public StringProperty razonSocialProperty() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial.set(razonSocial); }

    public String getPersonaContacto() { return personaContacto.get(); }
    public StringProperty personaContactoProperty() { return personaContacto; }
    public void setPersonaContacto(String personaContacto) { this.personaContacto.set(personaContacto); }

    public String getCondicionesPago() { return condicionesPago.get(); }
    public StringProperty condicionesPagoProperty() { return condicionesPago; }
    public void setCondicionesPago(String condicionesPago) { this.condicionesPago.set(condicionesPago); }

    public String getEstado() { return estado.get(); }
    public StringProperty estadoProperty() { return estado; }
    public void setEstado(String estado) { this.estado.set(estado); }

    // TIPO DOCUMENTO NOMBRE
    public String getTipoDocumentoNombre() { return tipoDocumentoNombre.get(); }
}