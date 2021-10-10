#include "SensorRelaySoba.h"
#include <Arduino.h>

SensorRelaySoba::SensorRelaySoba(const char *name, int idx,
                                 int pinSensor, int pinRelay,
                                 LiquidCrystal_I2C &lcd, const SensorRelay &sensorRelayPuffer) 
                                 : SensorRelay(name, idx, pinSensor, pinRelay, lcd), 
                                 _sensorRelayPuffer(sensorRelayPuffer)
{
}

RelayChange SensorRelaySoba::getRelayCondition()
{
    float setTemp = max(_sensorRelayPuffer.getCurrentTemp(), _targetTemp);
    if (_currentTemp > setTemp + _hysteresis)
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