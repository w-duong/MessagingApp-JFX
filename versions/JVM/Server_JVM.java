//package com.company;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server_JVM
{
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
        ArrayList<WorkerThread> ipcList;

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
            this.ipcList = new ArrayList<>();

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

        public void notifyFriends ()
        {
            String notification = "//FRIENDOUT@ALL@" + identifier;

            for (WorkerThread friend : ipcList)
            {
                try
                {
                    friend.goingOut.writeUTF(notification);
                    friend.removeOnlineFriend(this);
                }
                catch (Exception e)
                {
                    System.out.println (e.getMessage());
                }
            }
        }

        public void addOnlineFriend (WorkerThread secondPerson)
        {
            if (!ipcList.contains(secondPerson))
                ipcList.add(secondPerson);

            // secondPerson.addOnlineFriend(this);
        }

        public void removeOnlineFriend (WorkerThread secondPerson)
        {
            if (ipcList.contains(secondPerson))
                ipcList.remove(secondPerson);

            // secondPerson.removeOnlineFriend(this);
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
                        notifyFriends();
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
                    boolean isGoodTransmission = false;
                    for (WorkerThread toClient : onlineClients)
                        if (toClient.identifier.equals(recipient) && (toClient.online))
                        {
                            addOnlineFriend(toClient);

                            if (message.contains("//FRIENDFILE"))
                            {
                                goingOut.writeUTF("ACKNSEND");
                                toClient.goingOut.writeUTF(fromClient);

                                long sizeOfFile = comingIn.readLong();
                                fileTransfer(toClient, sizeOfFile);
                            }
                            else
                                toClient.goingOut.writeUTF(fromClient);
                            
                            isGoodTransmission = true;
                            break;
                        }
                    
                    if (!isGoodTransmission)
                        goingOut.writeUTF("//FRIENDOFF@SERVER@" + recipient);
                }
                catch (Exception e)
                {
                    System.out.println (e.getMessage());
                }
            }
        }

        public void fileTransfer (WorkerThread recipient, long sizeOfFile) throws Exception
        {
            try
            {
                recipient.goingOut.writeLong(sizeOfFile);

                byte [] transientBuffer = new byte [4096];
                int readEachTime = 0;
                int soFar = 0;

                while ((sizeOfFile > soFar) && ((readEachTime = comingIn.read(transientBuffer, 0, transientBuffer.length)) > 0))
                {
                    recipient.goingOut.write(transientBuffer, 0, readEachTime);
                    soFar += readEachTime;

                    System.out.println ("Read from Client 1 > " + soFar);
                }

                goingOut.flush();
            }
            catch (Exception e)
            {
                System.out.println (e.getMessage());
            }
        }

    }
}