# Warcamper

> wardriving (v.) to drive around with a laptop with a wireless card, and an antenna, looking for accessible wireless networks

Hence a Warcamper would be a stationary device to log Wifi networks in a public area (eg. smartphone access points).

Tested on a WeMos D1 mini Pro + Data Logger Shield (DS1307). But should work on any ESP8266 board with Arduino support. Connect the SD card to the SPI pins and the DS1307 to the I2C port. In order for the deep sleep mode to work, the D0 pin must be connected to the RST pin.

The filename is `WIFI.TXT` but is in json format. Filenames cannot be longer than 3 characters. Before parsing the json file you have to delete the last `,` and add a `]`. The default scanning interval is ~15 seconds. You can use a power bank or a battery shield and install the warcamper in a hidden place.

## Libraries
You need the esp8266 libraries for Arduino installed via board manager.
Get the libraries here [github]

License

MIT

   [github]: <https://github.com/esp8266/Arduino>
   