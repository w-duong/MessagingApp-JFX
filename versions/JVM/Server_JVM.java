//package com.company;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server
{
    //private static ArrayList<Thread> onlineClients = new ArrayList<>();
    private static Vector<WorkerThread> onlineClients = new Vector<>();

    public static void main (String [] args) throws IOException // Sockets throw exceptions (necessary)
    {
        DataInputStream comingIn;
        String clientName = "DEFAULT";

        // server requires 2 different sockets, (1) ServerSocket listening for requests...
        ServerSocket serverSocket = new ServerSocket (8405);
        // ...and (2) Socket that allows back/forth communication between itself and client.
        Socket clientSocket;

        while (true)
        {
            // GET NEW CLIENT REQUEST TO ESTABLISH SOCKET
            clientSocket = serverSocket.accept ();
            comingIn = new DataInputStream(clientSocket.getInputStream()); // open Stream to receive Client name
            try
            {
                clientName = comingIn.readUTF();
            }
            catch (Exception e)
            {
                System.out.println (e.getMessage());
            }

            System.out.println (String.format ("New Client connected at ... [%s]", clientSocket) );

            // instantiate new Thread to handle incoming socket
            // NOTE: in C/C++, we passed a newly created Thread the function it will carry out, in Java, we extend
            // a Class to inherit from Thread instead
            WorkerThread match = new WorkerThread(serverSocket.toString(), clientSocket, clientName);
            Thread temp = new Thread(match);

            // add to list of 'onlineClients'
            onlineClients.add(match);

            // call WorkerThread.run
            temp.start();

            System.out.println ("New WorkerThread assigned.");
        }

        //<-- TO DO: probably have exit condition here (???) -->//
    }

    // if this class was not 'static', we would have to implement as external Class...
    static class WorkerThread implements Runnable
    {
        // MISCELLANEOUS:
        String serverParent = null;

        // NECESSITIES:
        String identifier;          // User will define identifier (i.e. Name@ipaddress)
        boolean online = false;
        Socket commsLine = null;    // WorkerThread has to know the Socket it is assigned
        DataInputStream comingIn;   // Stream coming in from client
        DataOutputStream goingOut;  // Stream going out to client

        public WorkerThread (String serverName, Socket assignedSocket, String identifier) throws IOException // Streams throw exceptions
        {
            this.serverParent = serverName;
            this.identifier = identifier;
            this.online = true;
            this.commsLine = assignedSocket;

            this.comingIn = new DataInputStream(commsLine.getInputStream());    // could also have used 'assignedSocket'
            this.goingOut = new DataOutputStream(commsLine.getOutputStream());  // to create either Streams

            try
            {
                goingOut.writeUTF ("CONNECTION ESTABLISHED...\n");
            }
            catch (Exception e)
            {
                System.out.println (e.getMessage());
            }
        }

        @Override
        public void run () // method run when 'WorkerThread.start()' invoked
        {
            while (true)
            {
                String fromClient;
                try
                {
                    fromClient = comingIn.readUTF();

                    // EXIT condition, NOTE: does not require @identifier suffix - server will not need it
                    if (fromClient.equals("EXIT"))
                    {
                        goingOut.writeUTF("LOGOUT"); // reciprocal 'command' to have client shut down

                        this.online = false;
                        comingIn.close();
                        goingOut.close();
                        commsLine.close();
                        break;
                    }

                    // Parse incoming String for actual message and recipient ID
                    String [] messageArray = fromClient.split("@");
                    String message = messageArray[0];
                    String recipient = messageArray[1];
                    String sender = messageArray[2];

                    // iterate through Vector for unique ID of recipient and pass message if they're online
                    for (WorkerThread client : onlineClients)
                        if (client.identifier.equals(recipient) && (client.online))
                        {
                            client.goingOut.writeUTF(fromClient);
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
}