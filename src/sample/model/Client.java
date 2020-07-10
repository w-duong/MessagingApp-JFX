package sample.model;

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
    private Socket commsLine;           // Socket for communications
    private DataInputStream comingIn;   // Stream coming in externally
    private DataOutputStream goingOut;  // Stream going out to server

    public Client (String selfIdentifier, String serverName, int serverPort) throws IOException
    {
        // RETRIEVE IP-ADDRESS BY (1) LOOKUP VS (2) MANUAL ASSIGNMENT
        // InetAddress ip = InetAddress.getByName("localhost"); // (1)

        commsLine = new Socket(serverName, serverPort);
        setServerName(serverName);
        comingIn = new DataInputStream(commsLine.getInputStream());
        goingOut = new DataOutputStream(commsLine.getOutputStream());
        try
        {
            goingOut.writeUTF(selfIdentifier);
        }
        catch (Exception e)
        {
            System.out.println (e.getMessage());
        }
    }

    public void setServerName (String serverName) { this.serverName = serverName; }

    // previously, we needed another background Thread to handle sending messages but with JavaFX as the main thread,
    // we can simply treat the 'Client' model as its own Thread
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

                String [] array = messageIn.split("@");

                for (Tab tab : tabPane.getTabs())
                    if (tab.getId() == array[1])
                    {
                        Text chatLine = new Text(tab.getText() + " > " + array[0] + "\n");
                        chatLine.setFill(Paint.valueOf("Red"));

                        Node node = tab.getContent();
                        TextFlow chatLog = (TextFlow) node.lookup("ChatLog");
                        chatLog.getChildren().add(chatLine);

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
