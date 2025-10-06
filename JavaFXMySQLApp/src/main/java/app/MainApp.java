package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {



    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/menuCliente.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);

        stage.setTitle("Imprenta");
        stage.setScene(scene);


        stage.setWidth(1400);
        stage.setHeight(800);

        // Centra la ventana en la pantalla (opcional)
        stage.centerOnScreen();

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
