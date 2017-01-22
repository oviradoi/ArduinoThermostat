#include "RotaryEncoder.h"

// Pins for the encoder
const int encoder1 = 2;
const int encoder2 = 3;

const unsigned long threshold = 5000;
volatile long rotaryHalfSteps = 0;
volatile unsigned long int0time = 0;
volatile unsigned long int1time = 0;
volatile uint8_t int0signal = 0;
volatile uint8_t int1signal = 0;
volatile uint8_t int0history = 0;
volatile uint8_t int1history = 0;
volatile unsigned long lastRotaryTime;

// Time when we entered the menu
volatile unsigned long menuTime = 0;

void int0()
{
  if ( micros() - int0time < threshold )
    return;
  int0history = int0signal;
  int0signal = bitRead(PIND,encoder1);
  if ( int0history==int0signal )
    return;
  int0time = micros();
  menuTime = millis();
  if ( int0signal == int1signal )
  {
    rotaryHalfSteps++;
    lastRotaryTime = millis();
  }
  else
  {
    rotaryHalfSteps--;
    lastRotaryTime = millis();
  }
}

void int1()
{
  if ( micros() - int1time < threshold )
    return;
  int1history = int1signal;
  int1signal = bitRead(PIND,encoder2);
  if ( int1history==int1signal )
    return;
  int1time = micros();
}

void initEncoder()
{
  pinMode(encoder1, INPUT);
  pinMode(encoder2, INPUT);

  attachInterrupt(digitalPinToInterrupt(encoder1), int0, CHANGE);
  attachInterrupt(digitalPinToInterrupt(encoder2), int1, CHANGE);
}
