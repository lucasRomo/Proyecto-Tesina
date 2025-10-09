// Archivo: Usuario.java
package app.model;

import javafx.beans.value.ObservableValue;

public class Usuario {
    private String usuario;
    private String contrasena;
    private int idPersona; // <-- CAMBIO AGREGADO
    private int idDireccion;
    private int idUsuario;
    private int idTipoUsuario;
    private String estado;

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

    public Usuario(int idUsuario, String usuario, String contrasena, int idPersona, int idDireccion, int idTipoUsuario) {
        this.idUsuario = idUsuario;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.idPersona = idPersona;
        this.idDireccion = idDireccion;
        this.idTipoUsuario = idTipoUsuario;
    }

    public Usuario(int idPersona, String usuario, String contrasenia) {
        this.idPersona = idPersona;
        this.usuario = usuario;
        this.contrasena = contrasenia;
        // El estado se puede inicializar a null o se establecerá con el setter
        this.estado = null;
    }



    public Usuario() {
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

    public int getIdTipoUsuario() {
        return idTipoUsuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public void setIdPersona(int idPersona) {
        this.idPersona = idPersona;
    }


    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setIdTipoUsuario(int idTipoUsuario) {
        this.idTipoUsuario = idTipoUsuario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}