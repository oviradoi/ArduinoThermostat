#include "RotaryEncoder.h"

// Pins for the encoder
#if BUILD_ESP32
const int encoder1 = 16;
const int encoder2 = 17;
#else
const int encoder1 = 2;
const int encoder2 = 3;
#endif

const unsigned long threshold = 5000;
volatile long rotaryHalfSteps = 0;
volatile unsigned long int0time = 0;
volatile unsigned long int1time = 0;
volatile uint8_t int0signal = 0;
volatile uint8_t int1signal = 0;
volatile uint8_t int0history = 0;
volatile uint8_t int1history = 0;

// Time when we entered the menu
volatile unsigned long lastMenuTime = 0;
// Time when we turned on the light
volatile unsigned long lastLightTime = 0;

void int0()
{  
  if (micros() - int0time < threshold)
    return;
  int0history = int0signal;
#if BUILD_ESP32
  int0signal = digitalRead(encoder1);
#else
  int0signal = bitRead(PIND, encoder1);
#endif
  if (int0history == int0signal)
    return;
  int0time = micros();
  lastMenuTime = millis();
  lastLightTime = millis();
  if (int0signal == int1signal)
  {
    rotaryHalfSteps++;
  }
  else
  {
    rotaryHalfSteps--;
  }
}

void int1()
{
  if (micros() - int1time < threshold)
    return;
  int1history = int1signal;
#if BUILD_ESP32
  int1signal = digitalRead(encoder2);
#else
  int1signal = bitRead(PIND, encoder2);
#endif
  if (int1history == int1signal)
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
