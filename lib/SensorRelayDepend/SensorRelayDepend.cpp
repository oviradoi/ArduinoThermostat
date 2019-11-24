#include "SensorRelayDepend.h"
#include <Arduino.h>

SensorRelayDepend::SensorRelayDepend(
  const char* name, int idx, int pinSensor, int pinRelay, LiquidCrystal_I2C& lcd,
  const SensorRelay& sensorOther, FormulaType formulaType):
  SensorRelay(name, idx, pinSensor, pinRelay, lcd),
  _sensorOther(sensorOther),
  _formulaType(formulaType)
{

}

RelayChange SensorRelayDepend::getRelayCondition()
{
  if(_formulaType == Formula1)
  {
    float currentOtherTemp = _sensorOther.getCurrentTemp();
    float setTemp = max(_currentTemp, _targetTemp);
    if(currentOtherTemp > setTemp + _hysteresis)
    {
      return RelayChange::On;
    }
    else if (currentOtherTemp < setTemp - _hysteresis)
    {
      return RelayChange::Off;
    }
    else
    {
      return RelayChange::None;
    }
  }
  else if (_formulaType == Formula2)
  {
    float setTemp = (_currentTemp > 70.0) ? _targetTemp : max(_sensorOther.getCurrentTemp(), _targetTemp);
    if(_currentTemp > setTemp + _hysteresis)
    {
      return RelayChange::On;
    }
    else if (_currentTemp < setTemp - _hysteresis)
    {
      return RelayChange::Off;
    }
    else
    {
      return RelayChange::None;
    }
  }
  else
  {
    return RelayChange::None;
  }
}
