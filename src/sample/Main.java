package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.controller.messageMainController;
import sample.model.Client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // instantiate loader to unpack .fxml layout
        FXMLLoader loader = new FXMLLoader (getClass().getResource("controller/messageMain.fxml"));
        // load current layout and designate as 'Parent'
        Parent root = loader.load();

        String clientAddress = getAddressInfo();

        // instantiate 'Client' model to handle Socket transactions
        Client client = new Client (clientAddress, "192.168.1.61", 8405);
        // Client must be given its own thread or it will end the JavaFX 'Main' thread beyond this point
        Thread background = new Thread (client);
        // Client now runs in background and takes care of receiving messages from server
        background.start();

        // Passing Client to controller allows 'SendMessage' functionality
        messageMainController controller = loader.getController();
        controller.setClient(client);

        // staging
        primaryStage.setOnCloseRequest( event -> {
            System.out.println("Closing Stage");
            client.sendMessage("EXIT");

            Platform.exit();
            System.exit(0);
        });
        primaryStage.setTitle("Messaging App");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public String getAddressInfo () throws IOException // connecting through Socket may throw Exception
    {
        Socket socket = new Socket ();
        socket.connect(new InetSocketAddress("google.com", 80));
        String IP = socket.getLocalAddress().toString();

        return IP.trim().replaceAll("[^.0-9]","");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
