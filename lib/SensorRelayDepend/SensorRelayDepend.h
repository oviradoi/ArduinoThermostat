#pragma once
#include "SensorRelay.h"

enum FormulaType
{
  Formula1,
  Formula2
};

class SensorRelayDepend : public SensorRelay {
private:
  const SensorRelay& _sensorOther;
  FormulaType _formulaType;
public:
  SensorRelayDepend(const char* name, int idx,
    int pinSensor, int pinRelay,
    LiquidCrystal_I2C& lcd, const SensorRelay& sensorOther,
    FormulaType formulaType);
  RelayChange getRelayCondition() override;
};
