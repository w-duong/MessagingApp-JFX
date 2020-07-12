package sample.model;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client implements Runnable
{
    // MISC:
    TabPane tabPane;

    // CLIENT VARIABLES:
    private String serverName;
    private String selfIdentifier;
    private Socket commsLine;           // Socket for communications
    private DataInputStream comingIn;   // Stream coming in externally
    private DataOutputStream goingOut;  // Stream going out to server

    public Client (String selfIdentifier, String serverName, int serverPort) throws IOException
    {
        // RETRIEVE IP-ADDRESS BY (1) LOOKUP VS (2) MANUAL ASSIGNMENT
        // InetAddress ip = InetAddress.getByName("localhost"); // (1)

        commsLine = new Socket(serverName, serverPort);
        setSelfIdentifier(selfIdentifier);
        setServerName(serverName);
        comingIn = new DataInputStream(commsLine.getInputStream());
        goingOut = new DataOutputStream(commsLine.getOutputStream());

        /* Need to write out identifier of Client to Server to join list of online Clients (kept Server side) */
        try
        {
            goingOut.writeUTF(selfIdentifier);
        }
        catch (Exception e)
        {
            System.out.println (e.getMessage());
        }
    }

    public void setSelfIdentifier (String selfIdentifier) { this.selfIdentifier = selfIdentifier; }
    public void setServerName (String serverName) { this.serverName = serverName; }


    /* previously, we needed another background Thread to handle sending messages but with JavaFX as the main thread,
       we can simply treat the 'Client' model as its own Thread */
    public void sendMessage (String packet)
    {
        try
        {
            goingOut.writeUTF(packet);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public String getSelfIdentifier () {return this.selfIdentifier; }
    public String getServerName () { return this.serverName; }
    public void setTabPane (TabPane tabPane) { this.tabPane = tabPane; }

    @Override
    public void run ()
    {
        boolean exit = false;
        while (!exit)
        {
            try
            {
                String messageIn = comingIn.readUTF();
                if (messageIn.equals("LOGOUT"))
                {
                    goingOut.close();
                    comingIn.close();
                    commsLine.close();

                    exit = true;
                }

                /* parse message for body-recipient-sender */
                String [] array = messageIn.split("@");

                /* sort through Tab group within TabPane and identify correct sender */
                for (Tab tab : tabPane.getTabs())
                    if (tab.getId().equals(array[2]))
                    {
                        Text chatLine = new Text(tab.getText() + " > " + array[0] + "\n");
                        chatLine.setFill(Paint.valueOf("Red"));

                        Node node = tab.getContent();
                        TextFlow chatLog = (TextFlow) node.lookup("#ChatLog");

                        /* Any changes in JavaFX GUI must be done by JavaFX Thread. Since Client is its own background
                        *  thread, we should use Platform.runLater and lambda function (the latter for brevity) */
                        Platform.runLater(() -> chatLog.getChildren().add(chatLine));

                        break;
                    }
            }
            catch (Exception e)
            {
                System.out.println (e.getMessage());
            }
        }
    }
}
