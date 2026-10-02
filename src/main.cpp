#include <Arduino.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <DHT.h>

// ---- MQ5 Gas Sensor ----
#define MQ5 34
int sensorValue = 0;
bool gasDetected = false;

// ---- DHT11 ----
#define DHTPIN 4
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);
float humidity = 0;
float tempC = 0;

// ---- LCD ----
LiquidCrystal_I2C lcd(0x27, 16, 2);

// ---- Timing ----
unsigned long previousReadMillis = 0;
const long readInterval = 2000; // read sensors every 2 seconds (DHT11 needs this minimum)

unsigned long previousDisplayMillis = 0;
const long displayInterval = 3000; // switch LCD screen every 3 seconds
int displayScreen = 0; // 0 = air quality, 1 = temp/humidity

void readSensors();
void updateDisplay();

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
}

void loop()
{
  unsigned long currentMillis = millis();

  // Read sensors every 2 seconds
  if (currentMillis - previousReadMillis >= readInterval)
  {
    previousReadMillis = currentMillis;
    readSensors();
  }

  // Switch LCD screen every 3 seconds
  if (currentMillis - previousDisplayMillis >= displayInterval)
  {
    previousDisplayMillis = currentMillis;
    displayScreen = (displayScreen + 1) % 2; // cycles between 0 and 1
    updateDisplay();
  }
}

void readSensors()
{
  // --- Gas sensor ---
  sensorValue = analogRead(MQ5);
  gasDetected = (sensorValue < 600);

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
}

void updateDisplay()
{
  lcd.clear();
  lcd.setCursor(0, 0);

  if (displayScreen == 0)
  {
    // Screen 1: Air quality status
    lcd.print("Air Quality:");
    lcd.setCursor(0, 1);
    lcd.print(gasDetected ? "Gaz detected" : "Normal air qlty");
  }
  else
  {
    // Screen 2: Temp & Humidity
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