#pragma once
#include "SensorRelay.h"

void InitWebUi(SensorRelay** srs, int sensorCount, const char* base64pwd);
void HandleWebClient();
