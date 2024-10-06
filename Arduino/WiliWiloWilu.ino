#include "BluetoothSerial.h"
#include <ArduinoJson.h>

int soilMoisture = 0;
int soilMoisturePercent = 0;

// Valores típicos para los sensores de humedad de suelo
const int AIRVALUE = 4095;  // Valor cuando el suelo está seco
const int WATERVALUE = 0;   // Valor cuando el suelo está totalmente húmedo

BluetoothSerial SerialBT;
bool connected = false;

void setup() {
  Serial.begin(115200);
  SerialBT.begin("YBOX_N01");
  Serial.println("El Bluetooth ha comenzado. Conéctate al dispositivo.");

  pinMode(34, INPUT);  // Configuramos el pin como entrada para leer el sensor
}

void loop() {
  conexionBL();
  int soilHumidity = readHumSoil();  // Corrige el nombre de la función
  float temperature = 0;
  int humidity = 0;

  // Solo enviar datos si está conectado
  if (connected) {
    enviardato(temperature, humidity, soilHumidity);
  } else {
    Serial.println("Esperando conexion...");
  }

  delay(5000);
}

void enviardato(float temperature, int humidity, int soilHumidity) {
  StaticJsonDocument<200> doc;
  doc["temperature"] = 27.5;
  doc["airHumidity"] = 32;
  doc["soilHumidity"] = soilHumidity;
  doc["luminosity"] = 800;
  doc["pressure"] = 987.4;

  String jsonString;
  serializeJson(doc, jsonString);

  SerialBT.println(jsonString);
  Serial.println("JSON enviado: " + jsonString);
}

void conexionBL() {
  if (SerialBT.hasClient()) {
    if (!connected) {
      Serial.println("Cliente conectado.");
      connected = true;
    }
  } else {
    if (connected) {
      Serial.println("Cliente desconectado.");
      connected = false;
    }
    Serial.println("Esperando conexión...");
  }
}

int readHumSoil() {
  soilMoisture = analogRead(34);  // Leer el valor del sensor de humedad del suelo
  soilMoisturePercent = map(soilMoisture, AIRVALUE, WATERVALUE, 0, 100);  // Mapea los valores a porcentaje
  soilMoisturePercent = constrain(soilMoisturePercent, 0, 100);  // Asegura que el porcentaje esté entre 0 y 100
  return soilMoisturePercent;
}