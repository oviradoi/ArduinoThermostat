#pragma once
#include <Arduino.h>

// Rotary encoder half steps
extern volatile long rotaryHalfSteps;
// Last time we entered the menu
extern volatile unsigned long lastMenuTime;
// Last time we turned on the light
extern volatile unsigned long lastLightTime;

void int0();
void int1();

void initEncoder();
