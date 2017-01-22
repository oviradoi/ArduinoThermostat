#pragma once
#include <Arduino.h>

// Rotary encoder
extern volatile long rotaryHalfSteps;
extern volatile unsigned long int0time;
extern volatile unsigned long int1time;
extern volatile uint8_t int0signal;
extern volatile uint8_t int1signal;
extern volatile uint8_t int0history;
extern volatile uint8_t int1history;
extern volatile unsigned long menuTime;
extern volatile unsigned long lastRotaryTime;

void int0();
void int1();

void initEncoder();
