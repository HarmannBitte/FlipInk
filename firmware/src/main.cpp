#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <GxEPD2_BW.h>
#include <esp_sleep.h>

/*
 * FlipInk Firmware - ESP32-S3 E-Paper Secondary Display Controller
 * Target Display: Good Display 4.2" or 4.26" Black/White E-Ink Panel
 * Communication: BLE 5.0 (High Throughput 2M PHY or Chunked GATT)
 */

#define EPD_CS    10
#define EPD_DC    9
#define EPD_RST   8
#define EPD_BUSY  7

#define SERVICE_UUID           "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHAR_IMAGE_DATA_UUID   "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define CHAR_CONTROL_UUID      "d0e1a420-7210-4822-b2f6-34d287102e1c"
#define CHAR_BATTERY_UUID      "2a19"

// Screen configuration: 400x300 (or 800x480 for 4.26" HD)
#define EPD_WIDTH  400
#define EPD_HEIGHT 300
#define FRAME_BUFFER_SIZE ((EPD_WIDTH * EPD_HEIGHT) / 8) // 15,000 bytes

uint8_t currentFramebuffer[FRAME_BUFFER_SIZE];
uint8_t previousFramebuffer[FRAME_BUFFER_SIZE];
volatile size_t receivedBytes = 0;
volatile bool frameReady = false;
int refreshCounter = 0;
const int FULL_REFRESH_INTERVAL = 15; // Full refresh every 15 pages to purge ghosting

BLEServer *pServer = NULL;
BLECharacteristic *pImageChar = NULL;
BLECharacteristic *pControlChar = NULL;
BLECharacteristic *pBatteryChar = NULL;
bool deviceConnected = false;

// Mock / GxEPD2 driver instance placeholder
// GxEPD2_BW<GxEPD2_420, GxEPD2_420::HEIGHT> display(GxEPD2_420(EPD_CS, EPD_DC, EPD_RST, EPD_BUSY));

class ServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) override {
        deviceConnected = true;
    }
    void onDisconnect(BLEServer* pServer) override {
        deviceConnected = false;
        pServer->startAdvertising();
    }
};

class ImageTransferCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) override {
        String data = pCharacteristic->getValue();
        size_t len = data.length();
        if (len == 0) return;

        const uint8_t* pData = (const uint8_t*)data.c_str();

        // Packet format: [Packet_Index_Hi, Packet_Index_Lo, ...Payload...]
        if (receivedBytes + len <= FRAME_BUFFER_SIZE) {
            memcpy(currentFramebuffer + receivedBytes, pData, len);
            receivedBytes += len;
        }

        if (receivedBytes >= FRAME_BUFFER_SIZE) {
            frameReady = true;
            receivedBytes = 0;
        }
    }
};

class ControlCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) override {
        String value = pCharacteristic->getValue();
        if (value.length() > 0) {
            uint8_t cmd = value[0];
            switch (cmd) {
                case 0x01: // Trigger Full Refresh
                    refreshCounter = FULL_REFRESH_INTERVAL;
                    break;
                case 0x02: // Clear to White
                    memset(currentFramebuffer, 0xFF, FRAME_BUFFER_SIZE);
                    frameReady = true;
                    break;
                case 0x03: // Sleep immediately
                    esp_deep_sleep_start();
                    break;
            }
        }
    }
};

void setup() {
    Serial.begin(115200);
    memset(currentFramebuffer, 0xFF, FRAME_BUFFER_SIZE);
    memset(previousFramebuffer, 0xFF, FRAME_BUFFER_SIZE);

    // Initialize BLE
    BLEDevice::init("FlipInk-Display");
    pServer = BLEDevice::createServer();
    pServer->setCallbacks(new ServerCallbacks());

    BLEService *pService = pServer->createService(SERVICE_UUID);

    pImageChar = pService->createCharacteristic(
        CHAR_IMAGE_DATA_UUID,
        BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_WRITE_NR
    );
    pImageChar->setCallbacks(new ImageTransferCallbacks());

    pControlChar = pService->createCharacteristic(
        CHAR_CONTROL_UUID,
        BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_READ
    );
    pControlChar->setCallbacks(new ControlCallbacks());

    pBatteryChar = pService->createCharacteristic(
        CHAR_BATTERY_UUID,
        BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY
    );

    pService->start();
    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID);
    pAdvertising->setScanResponse(true);
    pAdvertising->setMinPreferred(0x06);
    BLEDevice::startAdvertising();

    Serial.println("FlipInk initialized and advertising.");
}

void loop() {
    if (frameReady) {
        frameReady = false;
        refreshCounter++;

        if (refreshCounter >= FULL_REFRESH_INTERVAL) {
            Serial.println("Performing Full E-Paper Refresh (De-ghosting)...");
            // display.init(115200, true);
            // display.drawImage(currentFramebuffer, 0, 0, EPD_WIDTH, EPD_HEIGHT, false, false, true);
            refreshCounter = 0;
        } else {
            Serial.println("Performing Fast Partial E-Paper Refresh (~300ms)...");
            // display.init(115200, false);
            // display.writeImage(currentFramebuffer, 0, 0, EPD_WIDTH, EPD_HEIGHT);
            // display.refresh(true); // Partial update
        }

        memcpy(previousFramebuffer, currentFramebuffer, FRAME_BUFFER_SIZE);
    }

    delay(20);
}
