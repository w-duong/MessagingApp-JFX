package sample.model;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client
{
    private static final Scanner cin = new Scanner (System.in);

    // CLIENT VARIABLES:
    private String serverName;
    private Socket commsLine;           // Socket for communications
    private DataInputStream comingIn;   // Stream coming in externally
    private DataOutputStream goingOut;  // Stream going out to server
    private Thread readMessage;

    public Client (String selfIdentifier, String serverName, int serverPort) throws IOException
    {
        // RETRIEVE IP-ADDRESS BY (1) LOOKUP VS (2) MANUAL ASSIGNMENT
        // InetAddress ip = InetAddress.getByName("localhost"); // (1)

        commsLine = new Socket(serverName, serverPort);
        try
        {
            goingOut.writeUTF(selfIdentifier);
        }
        catch (Exception e)
        {
            System.out.println (e.getMessage());
        }

        setServerName(serverName);
        setComingIn((DataInputStream)commsLine.getInputStream());
        setGoingOut((DataOutputStream)commsLine.getOutputStream());

        start();
    }

    public void setCommsLine (Socket commsLine) { this.commsLine = commsLine; }
    public void setServerName (String serverName) { this.serverName = serverName; }
    public void setComingIn (DataInputStream comingIn) { this.comingIn = comingIn; }
    public void setGoingOut (DataOutputStream goingOut) { this.goingOut = goingOut; }
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

    public void start () throws IOException
    {
        // SPAWN NEW THREAD TO HANDLE INCOMING MESSAGES
        readMessage = new Thread (new Runnable() {
            boolean exit = false;

            @Override
            public void run()
            {
                while (!exit)
                {
                    try
                    {
                        String messageIn = comingIn.readUTF();
                        if (messageIn.equals("LOGOUT"))
                        {
                            goingOut.close();
                            comingIn.close();

                            this.exit = true;
                        }

                        System.out.println (messageIn);
                    }
                    catch (Exception e)
                    {
                        System.out.println (e.getMessage());
                    }
                }
            }

            public void stop () { this.exit = true; }
        });

        readMessage.start();
    }
}
