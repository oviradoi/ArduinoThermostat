#include <Arduino.h>
#include <OneWire.h>
#include <LiquidCrystal_I2C.h>
#include <PciManager.h>
#include <PciListenerImp.h>
#include "SensorRelay.h"
#include "RotaryEncoder.h"

// Pins
const int buton = 4;
const int senzor1 = 5;
const int senzor2 = 6;
const int senzor3 = 7;
const int senzor4 = 8;
const int relay1 = 9;
const int relay2 = 10;
const int relay3 = 11;
const int relay4 = 12;

// LCD
LiquidCrystal_I2C lcd(0x27,20,4);

// SensorRelays
SensorRelay sr1("Soba", 1, senzor1, relay1, lcd);
SensorRelay sr2("Boiler", 2, senzor2, relay2, lcd);
SensorRelay sr3("Apa", 3, senzor3, relay3, lcd);
SensorRelay sr4("Retur", 4, senzor4, relay4, lcd);
SensorRelay srs[] = {sr1, sr2, sr3, sr4};

// Current target
int currentTarget = -1;
int prevTarget = -1;
volatile bool buttonPressed = false;

void onPinChange(byte changeKind)
{
  if(changeKind==0)
  {
    buttonPressed = true;
    menuTime = millis();
  }
}

// Interrupt listener
PciListenerImp listener(buton, onPinChange);

void readButton()
{
  if(buttonPressed)
  {
    currentTarget++;
    if(currentTarget > 7)
    {
      currentTarget = -1;
    }
    buttonPressed = false;
  }
  if(currentTarget != prevTarget)
  {
    // the button has changed
    if(prevTarget != -1)
    {
      int prevSensorIdx = prevTarget / 2;
      srs[prevSensorIdx].setEditMode(false, false);
    }
    if(currentTarget != -1)
    {
      int sensorIdx = currentTarget / 2;
      int editType = currentTarget % 2;
      srs[sensorIdx].setEditMode(true, editType==1);
    }
    prevTarget = currentTarget;
    rotaryHalfSteps = 0;
  }
  else
  {
    unsigned long currentTime = millis();

    if(currentTime < menuTime || currentTime - menuTime > 5000){

      // Exit menu
      srs[currentTarget / 2].setEditMode(false, false);

      currentTarget = -1;
      prevTarget = -1;
    }
  }
}

void editTargetTemp(){
  for(SensorRelay& sr : srs){
    if(sr.isEditMode()){
      float dist = (float)rotaryHalfSteps / 2.;
      sr.editTemp(dist);
    }
  }
}

void setup(void) {
  Serial.begin(9600);
  Serial.println("Start");

  lcd.init();
  lcd.backlight();
  pinMode(buton, INPUT);

  for(SensorRelay& sr : srs)
  {
    sr.init();
  }

  PciManager.registerListener(buton, &listener);

  initEncoder();
}

void loop(){
  for(SensorRelay& sr : srs)
  {
    sr.readTemps();
  }
  readButton();
  editTargetTemp();
  for(SensorRelay& sr : srs)
  {
    sr.print();
  }
  delay(500);
}
