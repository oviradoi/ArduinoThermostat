#include "SensorRelayCasa.h"
#include <Arduino.h>

SensorRelayCasa::SensorRelayCasa(
    const char *name, int idx, int pinSensor, int pinRelay, LiquidCrystal_I2C &lcd,
    const SensorRelay &sensorRelayPuffer) : SensorRelay(name, idx, pinSensor, pinRelay, lcd),
                                            _sensorRelayPuffer(sensorRelayPuffer)
{
}

RelayChange SensorRelayCasa::getRelayCondition()
{
  if (_sensorRelayPuffer.getCurrentTemp() > 95.)
  {
    return RelayChange::On;
  }
  if (!_sensorRelayPuffer.isRelayOn())
  {
    return RelayChange::Off;
  }
  else
  {
    if (_currentTemp < _targetTemp + _hysteresis)
    {
      return RelayChange::On;
    }
    else if (_currentTemp > _targetTemp - _hysteresis)
    {
      return RelayChange::Off;
    }
    else
    {
      return RelayChange::None;
    }
  }
}
