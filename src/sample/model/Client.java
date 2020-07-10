package sample.model;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client implements Runnable
{
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

                System.out.println (messageIn);
            }
            catch (Exception e)
            {
                System.out.println (e.getMessage());
            }
        }
    }
}
