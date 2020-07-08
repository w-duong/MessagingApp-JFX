package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // instantiate loader to unpack .fxml layout
        FXMLLoader loader = new FXMLLoader (getClass().getResource("layout/messageMain.fxml"));
        // load current layout and designate as 'Parent'
        Parent root = loader.load();

        // staging
        primaryStage.setTitle("Messaging App");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
