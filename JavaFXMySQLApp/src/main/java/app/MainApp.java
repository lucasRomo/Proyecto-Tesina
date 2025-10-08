package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    // Define el tamaño fijo deseado para toda la aplicación
    // Tamaño inicial deseado (prudencial)
    private static final double DEFAULT_WIDTH = 1200;
    private static final double DEFAULT_HEIGHT = 700;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/menuinicial.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);

        stage.setTitle("Imprenta");
        stage.setScene(scene);

        // 1. Inicia en el tamaño prudencial
        stage.setWidth(DEFAULT_WIDTH);
        stage.setHeight(DEFAULT_HEIGHT);

        // 2. CLAVE: La ventana NO es redimensionable por el usuario
        stage.setResizable(false);

        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
