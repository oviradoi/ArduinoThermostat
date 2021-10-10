#pragma once
#include "SensorRelay.h"

class SensorRelaySoba : public SensorRelay {
private:
  const SensorRelay& _sensorRelayPuffer;
public:
  SensorRelaySoba(const char* name, int idx,
    int pinSensor, int pinRelay,
    LiquidCrystal_I2C& lcd, const SensorRelay& sensorRelayPuffer);
  RelayChange getRelayCondition() override;
};