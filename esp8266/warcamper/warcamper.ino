// WARCAMPING
/*
 * @author Simon Burkhardt
 * @date 2016/03/13
 * @brief wifi scanner that collects info about the scanned networks in an interval of ~15s
 * 
 * Schematics:
 * connect the SD logger to the SPI port of the esp8266
 *  chip select (optional) on pin 4
 * connect the RTC to the I2C port of the esp8266
 * connect the XPD pin to the DTR pin (detatchable, for programming)
 * see pinout for reference
 * 
 */
/*
 * Board:
 * https://www.sparkfun.com/products/13231
 * 
 * Pinout
 * https://cdn.sparkfun.com/assets/learn_tutorials/4/4/5/esp8266-thing-pinout.png
 * 
 * esp8266 Library for Arduino
 * https://github.com/esp8266/Arduino.git
 * 
 * esp8266 RTC libraries
 * https://github.com/Makuna/Rtc
 * 
 * Deep Sleep Guide
 * https://learn.sparkfun.com/tutorials/esp8266-thing-hookup-guide/example-sketch-goodnight-thing-sleep-mode
 * 
 */

// LIBRARIES
#include "ESP8266WiFi.h"
#include "SD.h"
#include "SPI.h"            // for SD card
#include <Wire.h>           // for RTC
#include <RtcDS1307.h>

// MAKROS
#define countof(a) (sizeof(a) / sizeof(a[0]))

// INSTANCES
RtcDS1307 Rtc;

// GOLBAL VARIABLES
int n_new = 0;
const int chipSelect = 4;
char datenow[20] = {};

// MAIN (setup)
void setup() {
  Serial.begin(115200);
  Serial.println();
  
  //--------RTC SETUP ------------------------------------------------------
  Rtc.Begin();
  
  RtcDateTime compiled = RtcDateTime(__DATE__, __TIME__);
  /*
  if (!Rtc.IsDateTimeValid()) 
  {
    // Common Cuases:
    //    1) the battery on the device is low or even missing and the power line was disconnected
    Serial.println("RTC lost confidence in the DateTime!");
  }
  */
  RtcDateTime now = Rtc.GetDateTime();
  
  if (now < compiled) 
  {
    // check if RTC has been reset or has wrong time
    Serial.println("RTC is older than compile time!  (Updating DateTime)");
    Rtc.SetDateTime(compiled);
  }
  
  delay(10);
  snprintf_P(datenow, 
            countof(datenow),
            PSTR("%04u/%02u/%02u %02u:%02u:%02u"),
            now.Year(),
            now.Month(),
            now.Day(),
            now.Hour(),
            now.Minute(),
            now.Second() );
  Serial.println(datenow);
  
  //--------SD SETUP --------------------------------------------------------
  if (!SD.begin(chipSelect)) {
    Serial.println("card failed, or not present");
    // reset device
    ESP.deepSleep(1*1000000);  // 1s standby
  }
  Serial.println("card initialized");
  //--------FILE SETUP ------------------------------------------------------
  if (SD.exists("wifi.txt")) {
    Serial.print("wifi.txt");
    Serial.println(" exists");
  } else {
    Serial.print("creating ");
    Serial.println("wifi.txt");
    File newFile = SD.open("wifi.txt", FILE_WRITE);
    newFile.close();
  }
  
  //--------WIFI SETUP ------------------------------------------------------
  // Set WiFi to station mode and disconnect from an AP if it was previously connected
  WiFi.mode(WIFI_STA);
  WiFi.disconnect();
  Serial.println("setup done");
  delay(10);

  //--------SCAN FOR NETWORKS -----------------------------------------------
  n_new = WiFi.scanNetworks();
  
  if(n_new == 0)
    Serial.println("no networks found");
  else
  {
    Serial.print(n_new);
    Serial.println(" networks found");
    File writer = SD.open("wifi.txt", FILE_WRITE);
    writer.println(datenow);          // save datetime to logfile
    for(int i=0; i<n_new; i++)
    {
      String logData = "";
      // Print SSID and RSSI for each network found
      logData += WiFi.SSID(i);
      logData += ";";
      switch(WiFi.encryptionType(i))
      {
        case ENC_TYPE_NONE :
          logData += "none";
          break;
        case ENC_TYPE_WEP :
          logData += "WEP";
          break;
        case ENC_TYPE_TKIP :
          logData += "WPA_PSK";
          break;
        case ENC_TYPE_CCMP :
          logData += "WPA_PSK2";
          break;
        case ENC_TYPE_AUTO :
          logData += "AUTO";
          break;
        default :
          logData += "err";
          break;
      }
      logData += ";";
      // logData += "(";
      logData += WiFi.RSSI(i);
      // logData += ")";
      Serial.println(logData);
      delay(10);
      writer.println(logData);
    }
    Serial.println("");
    writer.println("");
    writer.close();
  }
  WiFi.mode(WIFI_OFF);
  SPI.end();
  // deepSleep resets the device, at restart, the code begins at the top of void setup()
  ESP.deepSleep(12*1000000);  // 12s standby  + 3s scan time   --> 15s scan interval
  
}



// never reached with deep sleep
void loop() {
  
}





