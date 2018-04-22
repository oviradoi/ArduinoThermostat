#pragma once
#include <Arduino.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <LiquidCrystal_I2C.h>

enum RelayChange { None, On, Off };

class SensorRelay {
protected:
  int _idx;
  int _pinSensor;
  int _pinRelay;

  float _currentTemp;
  bool _hasCurrentTemp;

  int _targetTemp;
  int _editedTargetTemp;
  int _hysteresis;
  int _editedHysteresis;

  bool _isRelayOn;

  const char* _name;
  bool _isEditMode;
  bool _editType;

  const float _minTemp = 10;
  const float _maxTemp = 95;
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

  virtual RelayChange getRelayCondition();

  const char* getName() { return _name; }

  float getCurrentTemp() const { return _currentTemp; }

  int getTargetTemp() const { return _targetTemp; }
  void setTargetTemp(int target)
  {
    _targetTemp = min(max(target, _minTemp),_maxTemp);
  }

  int getHysteresis() const { return _hysteresis; }
  void setHysteresis(int hyst)
  {
    _hysteresis = min(max(hyst, _minHyst),_maxHyst);
  }

  bool isEditMode() const { return _isEditMode; }
  void setEditMode(bool editMode, bool editType);

  void saveData();
  void loadData();

  void turnRelayOn(){_isRelayOn = true; digitalWrite(_pinRelay, LOW);}
  void turnRelayOff(){_isRelayOn = false; digitalWrite(_pinRelay, HIGH);}
  bool isRelayOn(){return _isRelayOn;}
};
