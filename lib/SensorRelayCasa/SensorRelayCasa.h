#pragma once
#include "SensorRelay.h"

class SensorRelayCasa : public SensorRelay {
private:
  const SensorRelay& _sensorRelayPuffer;
public:
  SensorRelayCasa(const char* name, int idx,
    int pinSensor, int pinRelay,
    LiquidCrystal_I2C& lcd, const SensorRelay& sensorRelayPuffer);
  RelayChange getRelayCondition() override;
};
