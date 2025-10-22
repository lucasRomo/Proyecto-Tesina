package app.util;

import app.dao.ComprobantePagoDAO;
import javafx.scene.control.Alert;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Servicio encargado de gestionar el almacenamiento físico de los comprobantes
 * de pago y registrar su ruta en la base de datos.
 */
public class ComprobanteService {

    private final ComprobantePagoDAO comprobantePagoDAO;
    // Define la carpeta donde se guardarán todos los comprobantes.
    // Usamos la ruta relativa al directorio de ejecución del programa.
    private static final String COMPROBANTES_DIR_NAME = "comprobantes";
    private final Path storagePath;

    public ComprobanteService() {
        this.comprobantePagoDAO = new ComprobantePagoDAO();
        // Inicializa la ruta de almacenamiento y asegura que la carpeta exista.
        this.storagePath = Paths.get(COMPROBANTES_DIR_NAME);
        ensureStorageDirectoryExists();
    }

    /**
     * Asegura que la carpeta 'comprobantes' exista en el directorio de trabajo.
     */
    private void ensureStorageDirectoryExists() {
        try {
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
                System.out.println("Carpeta de comprobantes creada en: " + storagePath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error al crear el directorio de comprobantes: " + e.getMessage());
            e.printStackTrace();
            // Aquí podrías lanzar una excepción o mostrar una alerta crítica
        }
    }

    /**
     * Almacena el comprobante de pago subido por el cliente y registra su ruta.
     * * @param idPedido El ID del pedido asociado.
     * @param archivoOriginal El objeto File del comprobante subido (desde un FileChooser, por ejemplo).
     * @return true si el almacenamiento y la actualización en DB fueron exitosos.
     */
    public boolean guardarComprobante(int idPedido, File archivoOriginal) {
        if (archivoOriginal == null || !archivoOriginal.exists()) {
            mostrarAlerta("Error", "El archivo de comprobante es inválido.", Alert.AlertType.WARNING);
            return false;
        }

        // 1. Crear un nombre de archivo único y descriptivo (Ej: COMPROBANTE_123_timestamp.pdf)
        String extension = getFileExtension(archivoOriginal);
        String nuevoNombre = String.format("COMPROBANTE_%d_%d%s",
                idPedido,
                System.currentTimeMillis(),
                extension);

        Path destino = storagePath.resolve(nuevoNombre);

        try {
            // 2. Copiar el archivo subido a la carpeta 'comprobantes'
            Files.copy(archivoOriginal.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

            // 3. Obtener la ruta absoluta del archivo guardado para la DB
            String rutaAbsoluta = destino.toAbsolutePath().toString();

            // 4. Actualizar la ruta en la base de datos
            boolean dbActualizada = comprobantePagoDAO.actualizarRutaComprobante(idPedido, rutaAbsoluta);

            if (dbActualizada) {
                mostrarAlerta("Éxito", "Comprobante guardado y ruta registrada correctamente.", Alert.AlertType.INFORMATION);
                return true;
            } else {
                mostrarAlerta("Advertencia", "Comprobante guardado en disco, pero falló la actualización en la DB.", Alert.AlertType.WARNING);
                // Opcionalmente, aquí deberías borrar el archivo si falla la DB
                // Files.deleteIfExists(destino);
                return false;
            }

        } catch (IOException e) {
            mostrarAlerta("Error de E/S", "Error al copiar el archivo del comprobante.", Alert.AlertType.ERROR);
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            mostrarAlerta("Error", "Ocurrió un error inesperado al guardar el comprobante.", Alert.AlertType.ERROR);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene la extensión del archivo, incluyendo el punto.
     */
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // Sin extensión
        }
        return name.substring(lastIndexOf);
    }

    // Método auxiliar (simple) para mostrar alertas
    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
