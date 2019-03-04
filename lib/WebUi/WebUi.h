#pragma once
//#if BUILD_WEBUI
#include "SensorRelay.h"

void InitWebUi(SensorRelay **srs, int sensorCount, const char *base64pwd);
void HandleWebClient();
//#endif