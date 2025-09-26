// Archivo: Usuario.java
package app.model;

import javafx.beans.value.ObservableValue;

public class Usuario {
    private String usuario;
    private String contrasena;
    private int idPersona; // <-- CAMBIO AGREGADO
    private int idDireccion;
    private int idUsuario;

    // Constructor para inicio de sesion (sin idPersona)
    public Usuario(String usuario, String contrasenia) {
        this.usuario = usuario;
        this.contrasena = contrasenia;
    }

    // Constructor para registro (con idPersona)
    public Usuario(String usuario, String contrasenia, int idPersona) {
        this.usuario = usuario;
        this.contrasena = contrasenia;
        this.idPersona = idPersona;
    }

    public Usuario(String usuario, String contrasenia, int idPersona, int idDireccion) {
        this.usuario = usuario;
        this.contrasena = contrasenia;
        this.idPersona = idPersona;
        this.idDireccion = idDireccion;
    }

    public Usuario(int idUsuario, String usuario, String contrasena, int idPersona, int idDireccion) {
        this.idUsuario = idUsuario;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.idPersona = idPersona;
        this.idDireccion = idDireccion;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getContrasenia() {
        return contrasena;
    }

    public int getIdPersona() {
        return idPersona;
    }

    public int getIdDireccion() { return idDireccion; }


    public void setIdPersona(int idPersona) {
        this.idPersona = idPersona;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

}