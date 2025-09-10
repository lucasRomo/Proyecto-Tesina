// Archivo: Usuario.java
package app.model;

import javafx.beans.value.ObservableValue;

public class Usuario {
    private String usuario;
    private String contrasenia;
    private int idPersona; // <-- CAMBIO AGREGADO

    // Constructor para inicio de sesion (sin idPersona)
    public Usuario(String usuario, String contrasenia) {
        this.usuario = usuario;
        this.contrasenia = contrasenia;
    }

    // Constructor para registro (con idPersona)
    public Usuario(String usuario, String contrasenia, int idPersona) {
        this.usuario = usuario;
        this.contrasenia = contrasenia;
        this.idPersona = idPersona;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    // <-- METODO AGREGADO
    public void setIdPersona(int idPersona) {
        this.idPersona = idPersona;
    }

    // <-- METODO AGREGADO
    public int getIdPersona() {
        return idPersona;
    }
}