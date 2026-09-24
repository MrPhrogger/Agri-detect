#include <iostream>
#include <string>
#include <thread>
#include <chrono>
#include <cstdlib>
#include <ctime>

int sensorValue = 500;
bool gasDetected = false;
bool lastGasDetected = false;
bool firstRun = true;

// Simulates a fake MQ2 reading instead of analogRead()
int fakeAnalogRead()
{
    // Randomly walk the value so it sometimes crosses the 600 threshold
    static int value = 300; // starting baseline
    int step = (rand() % 101) - 50; // random change between -50 and +50
    value += step;

    if (value < 0) value = 0;
    if (value > 1023) value = 1023;

    return value;
}

// Simulates the Firebase push — just prints instead
void pushToFirebase(const std::string &status)
{
    std::cout << "[FIREBASE PUSH] " << status << std::endl;
}

void sendUptime()
{
    sensorValue = fakeAnalogRead();
    std::cout << "Sensor value: " << sensorValue << std::endl;

    gasDetected = (sensorValue > 600);

    if (gasDetected)
    {
        std::cout << "LED: ON  | LCD: Gaz detected" << std::endl;
    }
    else
    {
        std::cout << "LED: OFF | LCD: Normal air qlty" << std::endl;
    }

    if (gasDetected != lastGasDetected || firstRun)
    {
        firstRun = false;
        lastGasDetected = gasDetected;

        std::string status = gasDetected ? "gaz detected" : "normal air quality";
        pushToFirebase(status);
    }

    std::cout << "-----------------------------" << std::endl;
}

int main()
{
    srand(static_cast<unsigned int>(time(nullptr)));

    std::cout << "Starting gas sensor simulation (Ctrl+C to stop)..." << std::endl;

    while (true)
    {
        sendUptime();
        std::this_thread::sleep_for(std::chrono::seconds(1));
    }

    return 0;
}