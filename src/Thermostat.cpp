#include <Arduino.h>
#include <OneWire.h>
#include <LiquidCrystal_I2C.h>
#include <PciManager.h>
#include <PciListenerImp.h>
#include "SensorRelay.h"
#include "SensorRelayDepend.h"
#include "RotaryEncoder.h"
#include "WebUi.h"

// Pins
const int buton = 9;
const int senzor1 = A0;
const int senzor2 = A1;
const int senzor3 = A2;
const int senzor4 = A3;
const int relay1 = 5;
const int relay2 = 6;
const int relay3 = 7;
const int relay4 = 8;

// LCD
LiquidCrystal_I2C lcd(0x27, 20, 4);

// SensorRelays
SensorRelay sr1("Puffer", 1, senzor1, relay1, lcd);
SensorRelayDepend sr2("Soba", 2, senzor2, relay2, lcd, sr1, Formula2);
SensorRelayDepend sr3("Solar", 3, senzor3, relay3, lcd, sr1, Formula2);
SensorRelayDepend sr4("A.C.M.", 4, senzor4, relay4, lcd, sr1, Formula1);

SensorRelay *srs[] = {&sr1, &sr2, &sr3, &sr4};

// Web password (base64 encoded), max 36 characters
const char *webPassword = "VGVzdA==";

// Current target
int currentTarget = -1;
int prevTarget = -1;
volatile bool buttonPressed = false;

void onPinChange(byte changeKind)
{
  if (changeKind == 0)
  {
    buttonPressed = true;
    lastMenuTime = millis();
    lastLightTime = millis();
  }
}

// Interrupt listener
PciListenerImp listener(buton, onPinChange);

void readButton()
{
  if (buttonPressed)
  {
    currentTarget++;
    if (currentTarget > 7)
    {
      currentTarget = -1;
    }
    buttonPressed = false;
  }
  if (currentTarget != prevTarget)
  {
    // the button has changed
    if (prevTarget != -1)
    {
      int prevSensorIdx = prevTarget / 2;
      srs[prevSensorIdx]->setEditMode(false, false);
    }
    if (currentTarget != -1)
    {
      int sensorIdx = currentTarget / 2;
      int editType = currentTarget % 2;
      srs[sensorIdx]->setEditMode(true, editType == 1);
    }
    prevTarget = currentTarget;
    rotaryHalfSteps = 0;
  }
  else
  {
    unsigned long currentTime = millis();

    if (currentTime < lastMenuTime || currentTime - lastMenuTime > 5000)
    {

      // Exit menu
      srs[currentTarget / 2]->setEditMode(false, false);

      currentTarget = -1;
      prevTarget = -1;
    }
  }
}

void editTargetTemp()
{
  for (SensorRelay *sr : srs)
  {
    if (sr->isEditMode())
    {
      float dist = (float)rotaryHalfSteps / 2.;
      sr->editTemp(dist);
    }
  }
}

void setup(void)
{
  lcd.init();
  lcd.backlight();
  pinMode(buton, INPUT);

  InitWebUi(srs, 4, webPassword);

  for (SensorRelay *sr : srs)
  {
    sr->init();
  }

  PciManager.registerListener(buton, &listener);

  initEncoder();
}

void loop()
{
  for (SensorRelay *sr : srs)
  {
    sr->requestTemps();
  }

  readButton();
  editTargetTemp();

  for (SensorRelay *sr : srs)
  {
    sr->readTemps();
    sr->print();
  }

  HandleWebClient();

  unsigned long currentTime = millis();
  if (currentTime > lastLightTime && currentTime - lastLightTime < 60000)
  {
    lcd.backlight();
  }
  else
  {
    lcd.noBacklight();
  }
}
