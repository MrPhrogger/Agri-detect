#include <Arduino.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <DHT.h>
#include <WiFi.h>
#include <FirebaseESP32.h>

// ---- MQ5 Gas Sensor ----
#define MQ5 34
int sensorValue = 0;
bool gasDetected = false;
bool lastGasDetected = false;
bool firstRun = true;

// ---- DHT11 ----
#define DHTPIN 4
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);
float humidity = 0;
float tempC = 0;
float lastTempC = -1000;      // sentinel value, guarantees first read always "changes"
float lastHumidity = -1000;
const float changeThreshold = 0.3; // minimum change to count as "different"

// ---- LCD ----
LiquidCrystal_I2C lcd(0x27, 16, 2);

// ---- Timing ----
unsigned long previousReadMillis = 0;
const long readInterval = 2000; // read sensors every 2 seconds (DHT11 minimum)

unsigned long previousDisplayMillis = 0;
const long displayInterval = 3000; // switch LCD screen every 3 seconds
int displayScreen = 0; // 0 = air quality, 1 = temp/humidity

// ---- WiFi credentials ----
#define WIFI_SSID "SSID"
#define WIFI_PASSWORD "password"

// ---- Firebase credentials ----
#define FIREBASE_HOST "firebaseURL.firebaseio.com"
#define FIREBASE_AUTH "Firebase secret"

FirebaseData firebaseData;
FirebaseConfig config;
FirebaseAuth auth;

void readSensors();
void updateDisplay();
void pushStatusToFirebase();

void setup()
{
  Serial.begin(115200);
  pinMode(MQ5, INPUT);

  dht.begin();

  lcd.init();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("System Init...");
  delay(1500);
  lcd.clear();

  // --- WiFi connect ---
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

  // --- Firebase connect ---
  config.host = FIREBASE_HOST;
  config.signer.tokens.legacy_token = FIREBASE_AUTH;
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
}

void loop()
{
  unsigned long currentMillis = millis();

  if (currentMillis - previousReadMillis >= readInterval)
  {
    previousReadMillis = currentMillis;
    readSensors();
  }

  if (currentMillis - previousDisplayMillis >= displayInterval)
  {
    previousDisplayMillis = currentMillis;
    displayScreen = (displayScreen + 1) % 2;
    updateDisplay();
  }
}

void readSensors()
{
  // --- Gas sensor ---
  sensorValue = analogRead(MQ5);
  gasDetected = (sensorValue < 800);

  // --- DHT11 ---
  float h = dht.readHumidity();
  float t = dht.readTemperature();

  if (!isnan(h) && !isnan(t))
  {
    humidity = h;
    tempC = t;
  }
  else
  {
    Serial.println("Failed to read from DHT11 sensor!");
  }

  // --- Serial debug ---
  Serial.print("Gas value: ");
  Serial.print(sensorValue);
  Serial.print(" | Status: ");
  Serial.print(gasDetected ? "Gaz detected" : "Normal air quality");
  Serial.print(" | Temp: ");
  Serial.print(tempC);
  Serial.print("C | Humidity: ");
  Serial.print(humidity);
  Serial.println("%");

  pushStatusToFirebase();
}

void pushStatusToFirebase()
{
  bool gasChanged = (gasDetected != lastGasDetected);
  bool tempChanged = (fabs(tempC - lastTempC) >= changeThreshold);
  bool humidityChanged = (fabs(humidity - lastHumidity) >= changeThreshold);

  if (gasChanged || tempChanged || humidityChanged || firstRun)
  {
    firstRun = false;
    lastGasDetected = gasDetected;
    lastTempC = tempC;
    lastHumidity = humidity;

    String status = gasDetected ? "gaz detected" : "normal air quality";

    FirebaseJson json;
    json.set("status", status);
    json.set("temperature", tempC);
    json.set("humidity", humidity);

    if (Firebase.pushJSON(firebaseData, "/air_quality_log", json))
    {
      Serial.println("Pushed to Firebase: " + status +
                      " | Temp: " + String(tempC) +
                      " | Humidity: " + String(humidity));
    }
    else
    {
      Serial.println("Firebase push failed: " + firebaseData.errorReason());
    }
  }
}

void updateDisplay()
{
  lcd.clear();
  lcd.setCursor(0, 0);

  if (displayScreen == 0)
  {
    lcd.print("Air Quality:");
    lcd.setCursor(0, 1);
    lcd.print(gasDetected ? "Gaz detected" : "Normal air qlty");
  }
  else
  {
    lcd.print("Temp: ");
    lcd.print(tempC, 1);
    lcd.print((char)223); // degree symbol
    lcd.print("C");

    lcd.setCursor(0, 1);
    lcd.print("Humidity: ");
    lcd.print(humidity, 1);
    lcd.print("%");
  }
}