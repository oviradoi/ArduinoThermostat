#include "SensorRelay.h"
#include <EEPROM.h>

SensorRelay::SensorRelay(const char *name, int idx, int pinSensor, int pinRelay, LiquidCrystal_I2C &lcd) : _idx(idx), _pinSensor(pinSensor), _pinRelay(pinRelay),
                                                                                                           _ow(pinSensor), _ds(&_ow), _lcd(lcd)
{
  _currentTemp = 0.0;
  _hasCurrentTemp = false;
  _name = name;
  _isRelayOn = true;
}

void SensorRelay::init()
{
  pinMode(_pinRelay, OUTPUT);
  pinMode(_pinSensor, INPUT);
  _ds.begin();
  loadData();
}

void SensorRelay::requestTemps()
{
  _ds.setWaitForConversion(false);
  if (_ds.requestTemperaturesByIndex(0))
  {
    _hasCurrentTemp = true;
  }
  else
  {
    _hasCurrentTemp = false;
  }
}

void SensorRelay::readTemps()
{
  if (_hasCurrentTemp)
  {
    // wait until the conversion is complete
    int maxTime = _ds.millisToWaitForConversion(_ds.getResolution());
    unsigned long startTime = millis();
    while (!_ds.isConversionComplete() || (startTime + maxTime > millis() || millis() < startTime))
    {
      delay(10);
    }

    _currentTemp = _ds.getTempCByIndex(0);
    _hasCurrentTemp = true;

    RelayChange change = getRelayCondition();
    if (change == RelayChange::On)
    {
      turnRelayOn();
    }
    else if (change == RelayChange::Off)
    {
      turnRelayOff();
    }
  }
  else
  {
    turnRelayOn();
  }
}

RelayChange SensorRelay::getRelayCondition()
{
  if (_currentTemp > _targetTemp + _hysteresis)
  {
    return RelayChange::On;
  }
  else if (_currentTemp < _targetTemp - _hysteresis)
  {
    return RelayChange::Off;
  }
  else
  {
    return RelayChange::None;
  }
}

void SensorRelay::editTemp(float dist)
{
  if (_editType == false)
  {
    float calcTarget = _targetTemp + round(dist);
    if (calcTarget < _minTemp)
    {
      calcTarget = _minTemp;
    }
    if (calcTarget > _maxTemp)
    {
      calcTarget = _maxTemp;
    }
    _editedTargetTemp = calcTarget;
  }
  else
  {
    int calcHyst = _hysteresis + round(dist);
    if (calcHyst < _minHyst)
    {
      calcHyst = _minHyst;
    }
    if (calcHyst > _maxHyst)
    {
      calcHyst = _maxHyst;
    }
    _editedHysteresis = calcHyst;
  }
}

void SensorRelay::setEditMode(bool editMode, bool editType)
{
  if (_isEditMode && !editMode)
  {
    setTargetTemp(_editedTargetTemp);
    setHysteresis(_editedHysteresis);
    saveData();
  }
  else if (!_isEditMode && editMode)
  {
    _editedTargetTemp = _targetTemp;
    _editedHysteresis = _hysteresis;
  }
  _isEditMode = editMode;
  _editType = editType;
}

void SensorRelay::print()
{
  _lcd.setCursor(0, _idx - 1);
  _lcd.print(_name);

  static char buffer[8];

  // Current temp
  if (_hasCurrentTemp)
  {
    dtostrf(_currentTemp, 5, 1, buffer);
  }
  else
  {
    sprintf(buffer, "%s", "---.-");
  }
  _lcd.setCursor(6, _idx - 1);
  _lcd.print(buffer);
  _lcd.print("\xDF");
  _lcd.print(" ");

  // Target temp
  _lcd.setCursor(13, _idx - 1);
  _lcd.print(_isEditMode ? _editedTargetTemp : _targetTemp);
  _lcd.print((_isEditMode && _editType == false) ? (char)255 : '\xDF');
  _lcd.print(" ");

  // Hysteresis
  _lcd.setCursor(17, _idx - 1);
  _lcd.print(_isEditMode ? _editedHysteresis : _hysteresis);
  _lcd.print((_isEditMode && _editType == true) ? (char)255 : '\xDF');

  // Relay indicator
  _lcd.setCursor(19, _idx - 1);
  _lcd.print(isRelayOn() ? "*" : " ");
}

void SensorRelay::saveData()
{
  EEPROM.write(2 * _idx, _targetTemp);
  EEPROM.write(2 * _idx + 1, _hysteresis);
}

void SensorRelay::loadData()
{
  setTargetTemp(EEPROM.read(2 * _idx));
  setHysteresis(EEPROM.read(2 * _idx + 1));
}
