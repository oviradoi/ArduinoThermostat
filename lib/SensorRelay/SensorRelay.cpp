#include "SensorRelay.h"
#include <EEPROM.h>

SensorRelay::SensorRelay(const char* name, int idx, int pinSensor, int pinRelay, LiquidCrystal_I2C& lcd) :
  _idx(idx), _pinSensor(pinSensor), _pinRelay(pinRelay),
  _ow(pinSensor), _ds(&_ow), _lcd(lcd)
{
  _currentTemp = 0.0;
  _hasCurrentTemp = false;
  _name = name;
}

void SensorRelay::init()
{
  pinMode(_pinRelay, OUTPUT);
  pinMode(_pinSensor, INPUT);
  _ds.begin();
  setTargetTemp(EEPROM.read(2*_idx));
  setHysteresis(EEPROM.read(2*_idx+1));
}

void SensorRelay::readTemps()
{
  if(_ds.requestTemperaturesByIndex(0))
  {
    _currentTemp = _ds.getTempCByIndex(0);
    _hasCurrentTemp = true;
    if(_currentTemp > _targetTemp + _hysteresis)
    {
      // Turn on relay with LOW
      digitalWrite(_pinRelay, LOW);
    }
    else if(_currentTemp < _targetTemp - _hysteresis)
    {
      // Turn off relay with HIGH
      digitalWrite(_pinRelay, HIGH);
    }
  }
  else
  {
    _hasCurrentTemp = false;
  }
}

void SensorRelay::editTemp(float dist)
{
  if(_editType == false)
  {
    float calcTarget = _targetTemp + round(dist);
    if(calcTarget < _minTemp)
    {
      calcTarget = _minTemp;
    }
    if(calcTarget > _maxTemp)
    {
      calcTarget = _maxTemp;
    }
    _editedTargetTemp = calcTarget;
  }
  else
  {
    int calcHyst = _hysteresis + round(dist);
    if(calcHyst < _minHyst)
    {
      calcHyst = _minHyst;
    }
    if(calcHyst > _maxHyst)
    {
      calcHyst = _maxHyst;
    }
    _editedHysteresis = calcHyst;
  }
}

void SensorRelay::setEditMode(bool editMode, bool editType)
{
  if(_isEditMode && !editMode)
  {
    setTargetTemp(_editedTargetTemp);
    setHysteresis(_editedHysteresis);
    EEPROM.write(2*_idx, _targetTemp);
    EEPROM.write(2*_idx+1, _hysteresis);
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
  _lcd.setCursor(0,_idx-1);
  _lcd.print(_name);

  char buffer[8];

  // Current temp
  if(_hasCurrentTemp)
  {
    dtostrf(_currentTemp, 5, 1, buffer);
  }
  else
  {
    sprintf(buffer, "%s", "---.-");
  }
  _lcd.setCursor(6,_idx-1);
  _lcd.print(buffer);
  _lcd.print("\xDF");

  // Target temp
  _lcd.setCursor(13,_idx-1);
  _lcd.print(_isEditMode ? _editedTargetTemp : _targetTemp);
  _lcd.print((_isEditMode && _editType == false) ? (char)255 : '\xDF');

  // Hysteresis
  _lcd.setCursor(17, _idx-1);
  _lcd.print(_isEditMode ? _editedHysteresis : _hysteresis);
  _lcd.print((_isEditMode && _editType == true) ? (char)255 : '\xDF');

  // Relay indicator
  _lcd.setCursor(19,_idx-1);
  if(_currentTemp > _targetTemp + _hysteresis)
  {
    _lcd.print("*");
  }
  else if(_currentTemp < _targetTemp - _hysteresis)
  {
    _lcd.print(" ");
  }
}
