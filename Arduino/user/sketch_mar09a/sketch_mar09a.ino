#include <Wire.h>
#include <SoftwareSerial.h>
#define Addr 0x1E // 7-bit address of HMC5883 compass
SoftwareSerial mySerial(12,13);//RX TX
void setup() 
{
  //序列
  Serial.begin(9600);
  mySerial.begin(9600);
  delay(100); // Power up delay
  //電子羅盤
  Wire.begin();
  Wire.beginTransmission(Addr);
  Wire.write(0x02);
  Wire.write(0x00);
  Wire.endTransmission();   
  
//  String getword="";
//  bool getedH=0;
//  bool getedS=0;
//   while(1)
//   {
//      while (Serial.available())
//      {
//        getedS=1;
//        getword+=(char)Serial.read();
//        delay(5);
//      }
//      if(getedS)
//      {    
//        mySerial.print(getword);
//        getword="";
//        getedS=0;
//      }
//      
//    //get message from hardware serial to software serial.
//      while (mySerial.available())
//      {
//        getedH=1;
//        getword+=(char)mySerial.read();
//        delay(5);
//      }
//      if(getedH)
//      {    
//        Serial.println(getword);
//        getword="";
//        getedH=0;
//      }    
//   }
  //mySerial.write("AT+MODE2");
}

void loop() 
{
   int angle;
  // if(mySerial.available())
  // {
       //if(mySerial.read() == 'Z')
       //{
         angle = compassDirection();
         //Serial.println(angle);
         if(angle<10) mySerial.print("00" + String(angle)); 
         else if(angle<100) mySerial.print("0" + String(angle)); 
         else mySerial.print(String(angle)); 
      // }
  // }

}
//電子羅盤換算角度
int compassDirection()
{
   float x, y, z;
   double angle;
   float declinationAngle = -0.019;

   Wire.beginTransmission(Addr);
   Wire.write(0x03); 
   Wire.endTransmission();
   Wire.requestFrom(Addr, 6); 
   if(Wire.available() <=6) 
   { 
      x = Wire.read() << 8 | Wire.read();
      z = Wire.read() << 8 | Wire.read();
      y = Wire.read() << 8 | Wire.read(); 
   }
   angle=atan2((double)y,(double)x)*(180/3.1416)+180; 
   delay(100);  
   return (angle);
}
