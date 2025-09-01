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

    public Cliente(String nombre, String apellido, int idTipoDocumento, String numeroDocumento, int idDireccion, String telefono, String email, String razonSocial, String personaContacto, String condicionesPago, String estado) {
        super(nombre, apellido, idTipoDocumento, numeroDocumento, idDireccion, telefono, email);
        this.idCliente = new SimpleIntegerProperty();
        this.razonSocial = new SimpleStringProperty(razonSocial);
        this.personaContacto = new SimpleStringProperty(personaContacto);
        this.condicionesPago = new SimpleStringProperty(condicionesPago);
        this.estado = new SimpleStringProperty(estado);
    }

    // Getters y Setters para idCliente
    public int getIdCliente() {
        return idCliente.get();
    }

    public IntegerProperty idClienteProperty() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente.set(idCliente);
    }

    // Getters y Setters para razonSocial
    public String getRazonSocial() {
        return razonSocial.get();
    }

    public StringProperty razonSocialProperty() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial.set(razonSocial);
    }

    // Getters y Setters para personaContacto
    public String getPersonaContacto() {
        return personaContacto.get();
    }

    public StringProperty personaContactoProperty() {
        return personaContacto;
    }

    public void setPersonaContacto(String personaContacto) {
        this.personaContacto.set(personaContacto);
    }

    // Getters y Setters para condicionesPago
    public String getCondicionesPago() {
        return condicionesPago.get();
    }

    public StringProperty condicionesPagoProperty() {
        return condicionesPago;
    }

    public void setCondicionesPago(String condicionesPago) {
        this.condicionesPago.set(condicionesPago);
    }

    // Getters y Setters para estado
    public String getEstado() {
        return estado.get();
    }

    public StringProperty estadoProperty() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado.set(estado);
    }
}