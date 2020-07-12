#ifndef CHATSERVERFN_H
#define CHATSERVERFN_H

#include <SPI.h>
#include <WiFi.h>
#include <pthread.h>

using namespace std;

#define DATA_LED 19
#define DISC_LED 2
#define MAX_CLIENTS 10

const char LOGOUT [] = "LOGOUT";
int status = WL_IDLE_STATUS;

char ssid[] = "BadKarmaAP_N"; //  your network SSID (name) 
char pass[] = "36Burningman!1";    // your network password (use for WPA, or use as key for WEP)

class WorkerThread
{
  private:
  WiFiClient client;
  string identifier;
  bool online = false;

  public:
  // CONSTRUCTORS //
  WorkerThread () {}
  WorkerThread (WiFiClient client, string identifier)
  {
    setClient (client);
    setIdentifier (identifier);
    setStatus (true);

    client.print("CONNECTION ESTABLISHED...\n");
  }

  // ACCESSORS //
  WiFiClient getClient () { return this->client; }
  string getIdentifier () { return this->identifier; }
  bool getStatus () { return this->online; }

  // MUTATORS //
  void setClient (WiFiClient copy) { this->client = copy; }
  void setIdentifier (string identifier) { this->identifier = identifier; }
  void setStatus (bool online) { this->online = online; }

  // MISC //
  bool equals (WorkerThread secondObj)
  {
    return (this->identifier == secondObj.getIdentifier());
  }
};

WorkerThread *onlineClients [MAX_CLIENTS] = { NULL };

void printWifiStatus() 
{
  // print the SSID of the network you're attached to:
  Serial.print("SSID: ");      // for some reason, cannot use templated strings
  Serial.println(WiFi.SSID()); // with WiFi.SSID() or .RSSI() (???)

  // print your WiFi shield's IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  // print the received signal strength:
  long rssi = WiFi.RSSI();
  Serial.print("signal strength (RSSI):");
  Serial.print(rssi);
  Serial.println(" dBm");
}

void packetParser (string packet, string delimiters, string tokens [3])
{
  int pos = 0;
  int index = 0;

  while ((pos = packet.find(delimiters)) != string::npos)
  {
    tokens [index] = packet.substr(0, pos);
    index++;
    packet.erase(0, pos + delimiters.length());
  }
  
  tokens[index] = packet;
}

void disconnectClient (WorkerThread* workerThread, int index)
{
  digitalWrite(DISC_LED, HIGH);

  workerThread->setStatus(false);
  workerThread->getClient().stop();
  delete onlineClients[index];
  onlineClients[index] = NULL;
  delay(250);

  Serial.println("Checkpoint EXIT");

  digitalWrite(DISC_LED, LOW);
}

#endif