package app.controller;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HasheadorController {

    public static String hashPassword(String Contrasenia) {
        try {
            // Obtener una instancia de MessageDigest para SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Convertir la contraseña a bytes y generar el hash
            byte[] hashedBytes = md.digest(Contrasenia.getBytes());

            // Convertir el hash de bytes a un formato hexadecimal legible
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            System.err.println("Algoritmo de hashing no encontrado.");
            return null;
        }
    }
}
