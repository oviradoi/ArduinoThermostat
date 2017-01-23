#pragma once
#include "SensorRelay.h"

class SensorRelayACM : public SensorRelay {
private:
  const SensorRelay& _sensorSoba;
public:
  SensorRelayACM(const char* name, int idx, int pinSensor, int pinRelay, LiquidCrystal_I2C& lcd, const SensorRelay& sensorSoba);
  RelayChange getRelayCondition() override;
};
