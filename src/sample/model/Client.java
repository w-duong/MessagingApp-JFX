package sample.model;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private String filePathToSend;
    private TabPane tabPane;
    private ObservableList<Contact> contacts = FXCollections.observableArrayList();
    private ObservableList<Contact> onlineNow = FXCollections.observableArrayList();

    // CLIENT VARIABLES:
    private String serverName;
    private String selfIdentifier;
    private Socket commsLine;           // Socket for communications
//    private BufferedReader comingIn;    // Stream coming in externally
//    private PrintWriter goingOut;       // Stream going out to server
    private DataInputStream comingIn;
    private DataOutputStream goingOut;

    public Client (String selfIdentifier, String serverName, int serverPort) throws IOException
    {
        // RETRIEVE IP-ADDRESS BY (1) LOOKUP VS (2) MANUAL ASSIGNMENT
        // InetAddress ip = InetAddress.getByName("localhost"); // (1)

        commsLine = new Socket(serverName, serverPort);
        setSelfIdentifier(selfIdentifier);
        setServerName(serverName);
//        comingIn = new BufferedReader(new InputStreamReader(commsLine.getInputStream()));
//        goingOut = new PrintWriter(commsLine.getOutputStream());
        comingIn = new DataInputStream(commsLine.getInputStream());
        goingOut = new DataOutputStream(commsLine.getOutputStream());

        /* Need to write out identifier of Client to Server to join list of online Clients (kept Server side) */
        try
        {
//            goingOut.write(selfIdentifier);
//            goingOut.flush();
            goingOut.writeUTF(selfIdentifier);
        }
        catch (Exception e)
        {
            System.out.println (e.getMessage());
        }
    }

    /* ACCESSORS AND MUTATORS */
    public void setSelfIdentifier (String selfIdentifier) { this.selfIdentifier = selfIdentifier; }
    public void setServerName (String serverName) { this.serverName = serverName; }
    public void setContacts (ObservableList<Contact> contacts) { this.contacts = contacts; }
    public void setOnlineNow(ObservableList<Contact> onlineNow){ this.onlineNow= onlineNow;}
    public void setTabPane (TabPane tabPane) { this.tabPane = tabPane; }
    public void setFilePathToSend (String filePathToSend) { this.filePathToSend = filePathToSend; }

    public String getSelfIdentifier () {return this.selfIdentifier; }
    public String getServerName () { return this.serverName; }

    /* previously, we needed another background Thread to handle sending messages but with JavaFX as the main thread,
       we can simply treat the 'Client' model as its own Thread */
    public void sendMessage (String packet)
    {
        try
        {
//            goingOut.write(packet);
//            goingOut.flush();
            goingOut.writeUTF(packet);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void sendFile (String absolutePath) throws Exception
    {
        try
        {
            /* Prepare File object and associated Stream to read to buffer */
            File outboundFile = new File (absolutePath);
            FileInputStream fstream = new FileInputStream(outboundFile);
            BufferedInputStream bfstream = new BufferedInputStream (fstream);

            /* Internal buffering prior to sending data to wire */
            byte [] sendBuffer = new byte [4096];
            int remaining = 0; // number of bytes read from File to internal buffer
            long soFar = 0;     // counter to keep track of elapsed size (OPTIONAL)

            /* Send size of File out to Server FIRST */
            long sizeOfFile = outboundFile.length();
            goingOut.writeLong(sizeOfFile);

            /* Continue to read bytes from File until '0' bytes remaining */
            while ((remaining = bfstream.read(sendBuffer, 0, sendBuffer.length)) > 0)
            {
                goingOut.write(sendBuffer, 0, remaining);
                soFar += remaining;
                System.out.println ("Amount of data sent > " + soFar);
            }

            /* Clean up open Streams */
            goingOut.flush(); // (OPTIONAL???)
            bfstream.close();
            fstream.close();
        }
        catch (Exception e)
        {
            System.out.println (e.getMessage());
        }
    }

    public void recvFile (String fileName) throws Exception
    {
        /* DEFAULT location for receiving file is user's Desktop */
        String path = "/Desktop/" + fileName;

        try
        {
            /* Prepare File object and Stream for writing out data */
            File incomingFile = new File (System.getProperty("user.home"), path);
            OutputStream writeToFile = new FileOutputStream (incomingFile);

            /* Internal buffering prior to writing to File stream */
            byte [] readBuffer = new byte [1024];
            int readEachTime = 0;
            long soFar = 0;          // bytes received for current File thus far

            /* Receive size of File PRIOR to receiving actual data */
            long sizeOfFile = comingIn.readLong();

            /* Continue receiving data until 'sizeOfFile' reached */
            while ((sizeOfFile > soFar) && ((readEachTime = comingIn.read(readBuffer, 0, readBuffer.length)) > 0))
            {
                writeToFile.write(readBuffer, 0, readEachTime);
                soFar += readEachTime;

                System.out.println ("Read from Server > " + soFar);
            }

            /* Close open Streams */
            writeToFile.close();
        }
        catch (Exception e)
        {
            System.out.println (e.getMessage());
        }
    }

    /* Switches Contacts from currently online to offline in their respective ListViews */
    public void refreshList ()
    {
        for (Contact person : contacts)
            if (person.isOnline() && !onlineNow.contains(person))
                onlineNow.add(person);
            else if (!person.isOnline() && onlineNow.contains(person))
                onlineNow.remove(person);
    }

    @Override
    public void run ()
    {
        boolean exit = false;
        while (!exit)
        {
            try
            {
                /* Check for Server specific messages first. */
//                String messageIn = comingIn.readLine();
                String messageIn = comingIn.readUTF();
                if (messageIn.equals("LOGOUT"))
                {
                    goingOut.close();
                    comingIn.close();
                    commsLine.close();

                    exit = true;
                    break;
                }
                else if (messageIn.contains("ACKNSEND")) // if recipient for File transfer request found, initiate transfer
                    sendFile(filePathToSend);

                /* parse message for body-recipient-sender */
                String [] array = messageIn.split("@");

                /* sort through Tab group within TabPane and identify correct sender */
                for (Tab tab : tabPane.getTabs())
                    if (tab.getId().equals(array[2]))
                    {
                        /* Prepare Text element to add to TextFlow node */
                        Text chatLine = null;
                        boolean isGoodTransmission = false;

                        /* If message is a Server level notification... */
                        if (array[0].equals("//FRIENDOUT") || array[0].equals("//FRIENDOFF"))
                        {
                            if (array[1].equals("ALL"))
                                chatLine = new Text(tab.getText() + " has logged off.\n");
                            else if (array[1].equals("SERVER"))
                                chatLine = new Text(tab.getText() + " is not currently logged on.\n");

                            /* Removes appropriate Contact from currently online ListView */
                            for (Contact person : contacts)
                                if (person.getContactNumber().equals(array[2]))
                                    person.setOnline(false);

                            Platform.runLater(() -> refreshList());
                        }
                        else
                        {
                            if (array[0].contains("//FRIENDFILE")) // this client was found to be receptive of a File transfer
                            {
                                String [] prefix = array[0].split(">>:");
                                String fileName = prefix[1];

                                recvFile(fileName);
                                chatLine = new Text(tab.getText() + " has sent you a file.\n");
                            }
                            else
                                chatLine = new Text(tab.getText() + " > " + array[0] + "\n");

                            isGoodTransmission = true;
                        }

                        chatLine.setFill(Paint.valueOf("Red"));

                        Node node = tab.getContent();
                        TextFlow chatLog = (TextFlow) node.lookup("#ChatLog");

                        /* Any changes in JavaFX GUI must be done by JavaFX Thread. Since Client is its own background
                        *  thread, we should use Platform.runLater and lambda function (the latter for brevity).
                        *  Also, any local variables referenced above and used in lambda expression should be 'final' or
                        *  converted to 'final'
                        */
                        Text finalChatLine = chatLine;
                        Platform.runLater(() -> chatLog.getChildren().add(finalChatLine));

                        /* In the event that this Client is receiving a message from a Contact who was PREVIOUSLY
                        *  offline, refresh the ListView to update their status
                        * */
                        if (isGoodTransmission)
                        {
                            for (Contact person : contacts)
                                if (person.getContactNumber().equals(array[2]) && !person.isOnline())
                                    person.setOnline(true);

                            Platform.runLater(() -> refreshList());
                        }
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
