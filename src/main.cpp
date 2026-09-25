#include <Arduino.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <WiFi.h>
#include <FirebaseESP32.h>

#define MQ2 34

void sendUptime(); // forward declaration

int sensorValue = 0;
unsigned long previousMillis = 0;
const long interval = 1000;

bool gasDetected = false;
bool lastGasDetected = false;
bool firstRun = true;

LiquidCrystal_I2C lcd(0x27, 16, 2);

#define WIFI_SSID "SSID"
#define WIFI_PASSWORD "password"

#define FIREBASE_HOST "firebasehost"
#define FIREBASE_AUTH "firebaseauth"

FirebaseData firebaseData;
FirebaseConfig config;
FirebaseAuth auth;

void setup()
{
  Serial.begin(115200);
  pinMode(MQ2, INPUT);

  lcd.init();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("Gas Sensor Init");
  delay(1500);
  lcd.clear();

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  lcd.setCursor(0, 0);
  lcd.print("Connecting WiFi ");
  while (WiFi.status() != WL_CONNECTED)
  {
    delay(300);
    Serial.print(".");
  }
  Serial.println();
  Serial.println("WiFi connected");
  lcd.clear();

  config.host = FIREBASE_HOST;
  config.signer.tokens.legacy_token = FIREBASE_AUTH;
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
}

void loop()
{
  unsigned long currentMillis = millis();

  if (currentMillis - previousMillis >= interval)
  {
    previousMillis = currentMillis;
    sendUptime();
  }
}

void sendUptime()
{
  sensorValue = analogRead(MQ2);
  Serial.println(sensorValue);

  lcd.setCursor(0, 0);

  gasDetected = (sensorValue < 600);

  if (gasDetected)
  {
    lcd.print("Gaz detected    ");
  }
  else
  {

    lcd.print("Normal air qlty ");
  }

  if (gasDetected != lastGasDetected || firstRun)
  {
    firstRun = false;
    lastGasDetected = gasDetected;

    String status = gasDetected ? "gaz detected" : "normal air quality";

    if (Firebase.pushString(firebaseData, "/air_quality_log", status))
    {
      Serial.println("Pushed to Firebase: " + status);
    }
    else
    {
      Serial.println("Firebase push failed: " + firebaseData.errorReason());
    }
  }
}