# Warcamper

> wardriving (v.) to drive around with a laptop with a wireless card, and an antenna, looking for accessible wireless networks

This is a little project I came up with. Using an esp8266 thing from [Sparkfun].
Here is the full story on tumblr [part 1] & [part 2]

## Libraries
You need the esp8266 libraries for Arduino installed via board manager.
Get the libraries here [github]

## RTC fix
I used a cheap RTC which did not provide accurate time feedback.
In the Java application there is a function called RTC fix.
This can be used optional. It takes following arguments:
- path to the input file (usually "wifi.txt" from the warcamper.ino)
- name of the output file
- (part of) the date that is listed in the input file
- three numbers when the scan started (hh,mm,ss)
- three numbers when the scan was stopped (hh,mm,ss)

````sh
fixRtc ( "wifi.txt", "wifi_fix.txt", "2016/03", 5, 56, 6, 15, 58, 2 );
````
input file is "wifi.txt", output changes to "wifi_fix.txt", scanned: March 2016, started on 05:56:15 (am), stoped on 15:58:02 (pm).

License

MIT

   [Sparkfun]: <https://www.sparkfun.com/products/13231>
   [part 1]: <http://mnemocron.tumblr.com/post/140440549084/my-warcamping-adventure>
   [part 2]: <http://mnemocron.tumblr.com/post/141019080869/my-warcamping-adventure>
   [github]: <https://github.com/esp8266/Arduino>
   