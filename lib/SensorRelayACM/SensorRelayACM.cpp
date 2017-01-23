#include "SensorRelayACM.h"
#include <Arduino.h>

SensorRelayACM::SensorRelayACM(
  const char* name, int idx, int pinSensor, int pinRelay, LiquidCrystal_I2C& lcd,
  const SensorRelay& sensorSoba):
  SensorRelay(name, idx, pinSensor, pinRelay, lcd),
  _sensorSoba(sensorSoba)
{

}

RelayChange SensorRelayACM::getRelayCondition()
{
  float currentSobaTemp = _sensorSoba.getCurrentTemp();

  if(currentSobaTemp > max(_currentTemp, _targetTemp) + _hysteresis)
  {
    return RelayChange::On;
  }
  else if (currentSobaTemp < max(_currentTemp, _targetTemp) - _hysteresis)
  {
    return RelayChange::Off;
  }
  else
  {
    return RelayChange::None;
  }
}
