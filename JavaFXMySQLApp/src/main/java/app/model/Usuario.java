package app.model;

public class Usuario {
    private String Usuario;
    private String Contrasenia;

    public Usuario(String Usuario, String Contrasenia) {
        this.Usuario = Usuario;
        this.Contrasenia = Contrasenia;
    }

    public String getUsuario() { return Usuario; }
    public String getContrasenia() { return Contrasenia; }
}
