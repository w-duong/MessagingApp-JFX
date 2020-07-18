// package com.company;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client_JVM_simple
{
    private static Scanner cin = new Scanner (System.in);

    // NECESSITIES:
    private static Socket commsLine;           // Socket for communications
    private static Thread sendMessage, readMessage;
    // private static BufferedReader comingIn;    // Stream coming in from client
    // private static PrintWriter goingOut;       // Stream going out to client
    private static DataOutputStream goingOut;
    private static DataInputStream comingIn;

    public void sendFile () throws Exception
    {
        try
        {
            File outboundFile = new File ("GraphingFundamentals.png");
            FileInputStream fstream = new FileInputStream(outboundFile);
            BufferedInputStream bfstream = new BufferedInputStream (fstream);

            byte [] sendBuffer = new byte [4096];
            int remaining = 0; 
            int soFar = 0;

            while ((remaining = bfstream.read(sendBuffer, 0, sendBuffer.length)) != -1)
            {
                goingOut.write(sendBuffer, 0, remaining);
                soFar += remaining;
                System.out.println ("Amount of data sent > " + soFar);
            }

            goingOut.flush();
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
        String path = "/Desktop/" + filename;

        try
        {
            File incomingFile = new File (System.getProperty("user.home"), path);
            OutputStream writeToFile = new FileOutputStream (incomingFile);

            byte [] readBuffer = new byte [1024];
            int readEachTime = 0;
            int soFar = 0;

            while ((readEachTime = comingIn.read(readBuffer, 0, readBuffer.length)) != -1)
            {
                writeToFile.write(readBuffer, 0, readEachTime);
                soFar += readEachTime;

                System.out.println ("Read from Server > " + soFar);
            }

            writeToFile.close();
        }
        catch (Exception e)
        {
            System.out.println (e.getMessage());
        }
    }

    public static void main (String [] args) throws IOException
    {
        // RETRIEVE IP-ADDRESS BY (1) LOOKUP VS (2) MANUAL ASSIGNMENT
        String ip = "192.168.1.46"; // (2)

        // ESTABLISH CONNECTION USING IP AND PORT OF _SERVER_
        // NOTE: client also opens its own socket, but this is system assigned and
        // we do not need to know its value (???)
        commsLine = new Socket(ip, 8405);

        // ESTABLISH STREAMS USING NEW SOCKET
        // comingIn = new BufferedReader(new InputStreamReader(commsLine.getInputStream()));
        // goingOut = new PrintWriter(commsLine.getOutputStream());
        comingIn = new DataInputStream(commsLine.getInputStream());
        goingOut = new DataOutputStream(commsLine.getOutputStream());

        try
        {
            // goingOut.write(args[0]);
            // goingOut.flush();
            goingOut.writeUTF(args[0]);
        }
        catch (Exception e)
        {
            System.out.println (e.getMessage());
        }

        // SPAWN NEW THREAD TO HANDLE OUTGOING MESSAGES
        sendMessage = new Thread (new Runnable() {
            boolean exit = false;

            @Override
            public void run()
            {
                while (!exit)
                {
                    String messageOut = cin.nextLine();

                    try
                    {
                        // goingOut.write(messageOut);
                        // goingOut.flush();
                        goingOut.writeUTF(messageOut);
                    }
                    catch (Exception e)
                    {
                        System.out.println (e.getMessage());
                    }

                    if (messageOut.equals("EXIT"))
                    {
                        System.out.println ("CLOSING connection to server @ " + ip);
                        this.exit = true;
                    }
                }
            }

            public void stop () { this.exit = true; }
        });

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
                        // String messageIn = comingIn.readLine();
                        String messageIn = comingIn.readUTF();
                        if (messageIn.equals("LOGOUT"))
                        {
                            goingOut.close();
                            comingIn.close();

                            this.exit = true;
                        }
                        else if (messageIn.contains("ACKNSEND"))
                            sendFile();
                        else if (messageIn.contains("//FRIENDFILE"))
                        {
                            String [] packet = messageIn.split("@");
                            String [] prefix = packet[0].split(">>:");
                            String fileName = prefix[1];

                            recvFile(fileName);
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
        sendMessage.start();
    }
}
