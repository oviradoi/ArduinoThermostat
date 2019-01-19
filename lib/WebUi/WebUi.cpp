#include "WebUi.h"

#include <Arduino.h>
#include <SPI.h>
#include <Ethernet.h>

#define WEBDUINO_FAVICON_DATA ""
#include <WebServer.h>

static byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED };
static IPAddress ipadr(192,168,1,145);
WebServer server("", 80);

static SensorRelay** _srs;
static int _sensorCount;
static const char* _base64pwd = nullptr;

#define NAMELEN 32
#define VALUELEN 32
char name[NAMELEN];
char value[VALUELEN];

int getParam(WebServer&server, char* url_tail)
{
  URLPARAM_RESULT rc;
  rc = server.nextURLparam(&url_tail, name, NAMELEN, value, VALUELEN);
  if (rc != URLPARAM_EOS)
  {
    return (name[0] - '0');
  }

  return 0;
}

void getInfoCmd(WebServer &server, WebServer::ConnectionType type, char *url_tail, bool tail_complete)
{
  if (type == WebServer::GET && server.checkCredentials(_base64pwd))
  {
    server.httpSuccess();
    server.println(_sensorCount);
    for(int i=0;i<_sensorCount;i++)
    {
      // Print name
      server.print(_srs[i]->getName());
      if(_srs[i]->isRelayOn())
      {
        server.print("*");
      }
      server.println();
      // Print current temp
      server.println(_srs[i]->getCurrentTemp());
      // Print target temp
      server.println(_srs[i]->getTargetTemp());
      // Print hyst
      server.println(_srs[i]->getHysteresis());
    }
  }
  else
  {
    server.httpUnauthorized();
  }
}

void targetCmd(WebServer &server, WebServer::ConnectionType type, char *url_tail, bool tail_complete)
{
  if (type == WebServer::POST && server.checkCredentials(_base64pwd))
  {
    int param = getParam(server, url_tail);
    if (param >= 1 && param <=4)
    {
      while(server.readPOSTparam(name, NAMELEN, value, VALUELEN))
      {
        if(strcmp(name, "temp")==0)
        {
          String s(value);
          int target = s.toInt();
          _srs[param-1]->setTargetTemp(target);
          _srs[param-1]->saveData();
          server.httpSuccess();
          return;
        }
      }
    }
    server.httpFail();
  }
  else
  {
    server.httpUnauthorized();
  }
}

void hystCmd(WebServer &server, WebServer::ConnectionType type, char *url_tail, bool tail_complete)
{
  if (type == WebServer::POST && server.checkCredentials(_base64pwd))
  {
    int param = getParam(server, url_tail);
    if (param >= 1 && param <=4)
    {
      while(server.readPOSTparam(name, NAMELEN, value, VALUELEN))
      {
        if(strcmp(name, "hyst")==0)
        {
          String s(value);
          int hyst = s.toInt();
          _srs[param-1]->setHysteresis(hyst);
          _srs[param-1]->saveData();
          server.httpSuccess();
          return;
        }
      }
    }
    server.httpFail();
  }
  else
  {
    server.httpUnauthorized();
  }
}

void InitWebUi(SensorRelay** srs, int sensorCount, const char* base64Pwd)
{
  _srs = srs;
  _sensorCount = sensorCount;
  _base64pwd = base64Pwd;

  pinMode(4, OUTPUT);
  digitalWrite(4, HIGH);

  Ethernet.begin(mac, ipadr);

  server.setDefaultCommand(&getInfoCmd);
  server.addCommand("info", &getInfoCmd);
  server.addCommand("target", &targetCmd);
  server.addCommand("hyst", &hystCmd);
  server.begin();
}

void HandleWebClient()
{
  server.processConnection();
}
