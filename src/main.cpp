#include <Arduino.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

#define MQ5 34

void sendUptime();

int sensorValue = 0;
unsigned long previousMillis = 0;
const long interval = 1000;

LiquidCrystal_I2C lcd(0x27, 16, 2); 

void setup()
{
  Serial.begin(115200);
  pinMode(MQ5, INPUT);

  lcd.init();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("Gas Sensor Init");
  delay(1500);
  lcd.clear();
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

  if (sensorValue < 500)
  {
    lcd.print("Gaz detected    "); 
  }
  else
  {
    lcd.print("Normal air qlty "); 
  }
}