package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/menuAbms.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);

        stage.setTitle("Imprenta");
        stage.setScene(scene);

        // CLAVE: Inicia la ventana en modo maximizado nativo del sistema operativo.
        // Esto hace que la ventana ocupe el 100% de la pantalla.
        stage.setMaximized(true);

        // Permite al usuario redimensionar la ventana (para poder restaurarla si desmaximiza).
        // Si lo pones en 'false', el botón de restaurar/maximizar puede desaparecer o no funcionar como se espera.
        stage.setResizable(true);

        // No es necesario stage.centerOnScreen() para ventanas maximizadas.
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}