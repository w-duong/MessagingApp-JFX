/*
 Multi Client Chat Server
 
 created 7/09/2020
 by William N. Duong
 modified 7/11/2020
 by William N. Duong
 
 */
#include <chatServerFN.h>

WiFiServer server(8405);

void setup() 
{
  pinMode(DATA_LED, OUTPUT);
  pinMode(DISC_LED, OUTPUT);
  
  //Initialize serial and wait for port to open:
  Serial.begin(115200); 
  
  // attempt to connect to Wifi network:
  while ( status != WL_CONNECTED) { 
    Serial.printf("Attempting to connect to SSID: %s\n", ssid);

    // Connect to WPA/WPA2 network. Change this line if using open or WEP network:    
    status = WiFi.begin(ssid, pass);

    // wait 10 seconds for connection:
    delay(5000);
  } 

  // start the server:
  server.begin();
  
  // you're connected now, so print out the status:
  printWifiStatus();
 }

void loop() {  
  // IF NEW CLIENT IS SIGNALING FOR CONNECTION TO BE MADE //
  WiFiClient client = server.available();

  if (client)
  {
    // (0) WAIT FOR CLIENT TO SEND PREAMBLE //
    while (!client.available())
      delay(50);
    
    // (1) GET IDENTIFIER-PREAMBLE SENT BY CLIENT. NOTE: may not always be IP address //
    string identifier;
    while (client.available())
    {
      char newChar = client.read();
      identifier += newChar;
    }

    const char *startOfMessage = identifier.c_str();
    Serial.printf("Checkpoint 0: New Client connected at ...  [%s]\n", startOfMessage);

    // (2) CREATE NEW 'WorkerThread' USING INCOMING CLIENT //
    // AND INSERT INTO ARRAY OF AVAILABLE SLOTS //
    for (int i = 0; i < MAX_CLIENTS; ++i)
      if (onlineClients[i] == NULL) // i.e. if this slot is available
      {
        onlineClients[i] = new WorkerThread (client, identifier);
        Serial.printf ("New Client assigned to slot ... [%d]\n",i);
        break;
      }
  }

  // (3) PROCEED TO LOOP THROUGH ANY ONLINE CLIENTS THAT HAVE PACKETS AVAILABLE TO SEND //
  for (int i = 0; i < MAX_CLIENTS; ++i)
    if (onlineClients[i] != NULL && onlineClients[i]->getClient().available()) // if present + available
    {
      Serial.printf ("Checkpoint 1: Message from Client [%d]\n", i);
      
      // (3a) COLLECT MESSAGE FROM CLIENT //
      string message;
      while (onlineClients[i]->getClient().available())
      {
        digitalWrite(DATA_LED, HIGH);
        char newChar = onlineClients[i]->getClient().read();
        message += newChar;
        digitalWrite(DATA_LED, LOW);
      }

      // EXIT CONDITION, CLIENT LOGS OUT GRACEFULLY //
      if (message == "EXIT")
      {
        onlineClients[i]->getClient().print(LOGOUT);
        
        disconnectClient(onlineClients[i], i);
        break;
      }

      // (3b) IF NOT EXIT CONDITION, PARSE MESSAGE INTO PACKETS OF DATA //
      string packets [3];
      packetParser(message, "@", packets);
      const char *startOfMessage = message.c_str();
      bool wasGoodReroute = false;

      // (4) FIND APPROPRIATE RECIPIENT (based on packet[1]) OF MESSAGE AND RE-ROUTE //
      for(int j = 0; j < MAX_CLIENTS; ++j)
        // VERIFY CORRECT RECIPIENT AND CHECK ONLINE STATUS (redundant???) //
        if (onlineClients[j] != NULL && onlineClients[j]->getIdentifier() == packets[1] && onlineClients[j]->getStatus())
        {
          onlineClients[j]->getClient().println(startOfMessage);
          wasGoodReroute = true;
          break;
        }

      if (!wasGoodReroute)
        Serial.println("Checkpoint 2: No recipient found");

      Serial.printf("Checkpoint 3: '%s'\n", startOfMessage);
    }
    else if (onlineClients[i] != NULL && !onlineClients[i]->getClient().connected())
      disconnectClient(onlineClients[i], i); // client disconnected without sending LOGOUT
}
