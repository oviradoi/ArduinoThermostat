#pragma once
#include <Arduino.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <LiquidCrystal_I2C.h>

class SensorRelay {
private:
  int _idx;
  int _pinSensor;
  int _pinRelay;

  float _currentTemp;
  bool _hasCurrentTemp;
  float _currentSobaTemp;
  bool _hasSobaTemp;

  int _targetTemp;
  int _editedTargetTemp;
  int _hysteresis;
  int _editedHysteresis;

  const char* _name;
  bool _isEditMode;
  bool _editType;

  const float _minTemp = 10;
  const float _maxTemp = 80;
  const float _minHyst = 0;
  const float _maxHyst = 9;

  OneWire _ow;
  DallasTemperature _ds;
  LiquidCrystal_I2C& _lcd;
public:
  SensorRelay(const char* name, int idx, int pinSensor, int pinRelay, LiquidCrystal_I2C& lcd);

  void init();
  void readTemps();
  void print();
  void editTemp(float dist);

  void setSobaTemp(float sobaTemp) { _currentSobaTemp = sobaTemp; _hasSobaTemp = true; }

  float getCurrentTemp() { return _currentTemp; }

  int getTargetTemp() { return _targetTemp; }
  void setTargetTemp(int target)
  {
    _targetTemp = min(max(target, _minTemp),_maxTemp);
  }

  int getHysteresis() { return _hysteresis; }
  void setHysteresis(int hyst)
  {
    _hysteresis = min(max(hyst, _minHyst),_maxHyst);
  }

  bool isEditMode(){return _isEditMode;}
  void setEditMode(bool editMode, bool editType);
};
