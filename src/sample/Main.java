package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.controller.messageMainController;
import sample.model.Client;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // instantiate loader to unpack .fxml layout
        FXMLLoader loader = new FXMLLoader (getClass().getResource("layout/messageMain.fxml"));
        // load current layout and designate as 'Parent'
        Parent root = loader.load();

        // instantiate 'Client' model to handle Socket transactions
        Client client = new Client ("192.168.1.10", "192.168.1.41", 8405);
        // Client must be given its own thread or it will end the JavaFX 'Main' thread beyond this point
        Thread background = new Thread (client);
        // Client now runs in background and takes care of receiving messages from server
        background.start();

        // Passing Client to controller allows 'SendMessage' functionality
        messageMainController controller = loader.getController();
        controller.setClient(client);

        // staging
        primaryStage.setTitle("Messaging App");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
