#include <ozone.h>
#include <Servo.h>
#include <Wire.h>
#include <SoftwareSerial.h>
#include <TimerOne.h>
#include <time.h>
#define Addr 0x1E
//超音波
SonarA mySonar1(1);
SonarA mySonar2(2);
SonarA mySonar3(3);
SonarA mySonar4(4);
SonarA mySonar5(5);
SonarA mySonar6(6);
SonarA mySonar7(7);
SonarA mySonar8(8);
//馬達
MotorRunnerC myMotorL(9);       //  設定左輪馬達模組編號為9
MotorRunnerC myMotorR(10);      //  設定右輪馬達模組編號為10
//語音
SR1 mySR1(15); //設定模組編號為 15
char MyString[100];
int sr1Sign=0;
//藍芽
bool getedS = 0;
bool getedH = 0;
String getword = "";
boolean connectionCheck = false;
//兩端羅盤角度
int robotAngle, intUserAngle;
//
int aaa = 0;
int val = 0;
//
unsigned long timeCount = 0;
unsigned long currentTime = millis();
bool isPing = false;
//
void setup()
{
    Serial.begin(9600);
    //while (!Serial);
    //
    Serial1.begin(9600);
    //超音波
    mySonar1.SetRepeatTime(10);  //設定重複偵測間隔時間
    mySonar2.SetRepeatTime(10);
    mySonar3.SetRepeatTime(10);
    mySonar4.SetRepeatTime(10);
    mySonar5.SetRepeatTime(10);
    mySonar6.SetRepeatTime(10);
    mySonar7.SetRepeatTime(10);
    mySonar8.SetRepeatTime(10);
    mySonar1.RepeatRanging();   //重複(週期性)執行超音波偵測
    mySonar2.RepeatRanging();
    mySonar3.RepeatRanging();
    mySonar4.RepeatRanging();
    mySonar5.RepeatRanging();
    mySonar6.RepeatRanging();
    mySonar7.RepeatRanging();
    mySonar8.RepeatRanging();
    //語音
    mySR1.DeleteAllSentence();
    delay(100); // 延遲 100 ms
    sprintf(MyString, "jie ting"); // 接聽
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(1);
    sprintf(MyString, "jie tying"); // 接聽
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(2);
    sprintf(MyString, "jie tieng"); // 接聽
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(3);
    sprintf(MyString, "jie tyieng"); // 接聽
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(4);
    sprintf(MyString, "jye ting"); // 接聽
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(5);
    sprintf(MyString, "jye tying"); // 接聽
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(6);
    sprintf(MyString, "jye tieng"); // 接聽
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(7);
    sprintf(MyString, "jye tyieng"); // 接聽
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(8);
    sprintf(MyString, "jiê ting"); // 接聽
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(9);
    sprintf(MyString, "jiê tying"); // 接聽
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(10);
    sprintf(MyString, "jiê tieng"); // 接聽
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(11);
    sprintf(MyString, "jiê tyieng"); // 接聽
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(12);
    sprintf(MyString, "jyiê ting"); // 接聽
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(13);
    sprintf(MyString, "jyiê tying"); // 接聽
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(14);
    sprintf(MyString, "jyiê tieng"); // 接聽
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(15);
    sprintf(MyString, "jyiê tyieng"); // 接聽
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(16);
    
    sprintf(MyString, "gua duan"); // 掛斷
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(17);
    sprintf(MyString, "gua dwan"); // 掛斷
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(18);
    sprintf(MyString, "gua dwuan"); // 掛斷
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(19);
    sprintf(MyString, "gwa duan"); // 掛斷
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(20);
    sprintf(MyString, "gwa dwan"); // 掛斷
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(21);
    sprintf(MyString, "gwa dwuan"); // 掛斷
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(22);
    sprintf(MyString, "gwua duan"); // 掛斷
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(23);
    sprintf(MyString, "gwua dwan"); // 掛斷
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(24);
    sprintf(MyString, "gwua dwuan"); // 掛斷
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(25);
    
    sprintf(MyString, "da dian hua"); // 打電話
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(26);
    sprintf(MyString, "da dyan hua"); // 打電話
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(27);
    sprintf(MyString, "da dian hwua"); // 打電話
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(28);
    sprintf(MyString, "da dyan hwua"); // 打電話
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(29);

    sprintf(MyString, "gen"); // 跟
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(30);

    sprintf(MyString, "ting xia"); // 停下
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(31);
    mySR1.GoRecognition();
    sprintf(MyString, "ting xya"); // 停下
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(32);
    mySR1.GoRecognition();
    sprintf(MyString, "ting xyia"); // 停下
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(33);
    mySR1.GoRecognition();
    sprintf(MyString, "tying xia"); // 停下
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(34);
    mySR1.GoRecognition();
    sprintf(MyString, "tying xya"); // 停下
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(35);
    mySR1.GoRecognition();
    sprintf(MyString, "tying xyia"); // 停下
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(36);
    mySR1.GoRecognition();
    mySR1.GoRecognition();
    //IO
    pinMode(A0, INPUT);
    pinMode(4, OUTPUT);
    pinMode(6, OUTPUT);
    pinMode(7, OUTPUT);
    pinMode(8, OUTPUT);
    pinMode(9, OUTPUT);
    pinMode(10, OUTPUT);
    pinMode(11, OUTPUT);
    pinMode(12, OUTPUT);
    pinMode(13, INPUT);
    //
    digitalWrite(4, HIGH);
    digitalWrite(6, LOW);
    digitalWrite(7, LOW);
    digitalWrite(8, LOW);
    digitalWrite(9, LOW);
    digitalWrite(10, LOW);
    digitalWrite(11, LOW);
    digitalWrite(12, LOW);
    //藍芽連線
    bleConnection();
    delay(500);
    //電子羅盤
    Wire.begin();
    Wire.beginTransmission(Addr);
    Wire.write(0x02);
    Wire.write(0x00);
    Wire.endTransmission();
   
//    while(1)
//    {
//      while (Serial1.available())
//      {
//        getedS=1;
//        getword+=(char)Serial1.read();
//        delay(5);
//      }
//      if(getedS)
//      {    
//        Serial.println(getword);
//        getword="";
//        getedS=0;
//      }
//      
//    //get message from hardware serial to software serial.
//      while (Serial.available())
//      {
//        getedH=1;
//        getword+=(char)Serial.read();
//        delay(5);
//      }
//      if(getedH)
//      {    
//        Serial1.print(getword);
//        getword="";
//        getedH=0;
//      }    
//    }
//  while(1)
//  {
//        Serial.println("Sonar1 :" + String(Sonar1()));
//        Serial.println("Sonar2 :" + String(Sonar2()));
//        Serial.println("Sonar3 :" + String(Sonar3()));
//        Serial.println("Sonar4 :" + String(Sonar4()));
//        Serial.println("Sonar5 :" + String(Sonar5()));
//        Serial.println("Sonar6 :" + String(Sonar6()));
//        Serial.println("Sonar7 :" + String(Sonar7()));
//        Serial.println("Sonar8 :" + String(Sonar8()));
//  }
}
void loop()
{
  sr1Sign = SR1();
  if(isPing) 
  { 
    timeCount += millis() - currentTime;
    if(timeCount > 1500) 
    {
      timeCount = 0;
      isPing = false;
      digitalWrite(10, LOW);
      digitalWrite(11, LOW);
      digitalWrite(12, LOW);
    }
  }
  currentTime = millis();
  if(sr1Sign >=1 && sr1Sign<=16  )//接聽  && digitalRead(13)==1
  {
     digitalWrite(10, HIGH);
     digitalWrite(11, LOW);
     digitalWrite(12, LOW);
     Serial.println("start");
     sr1Sign = 0;
     isPing = true;
  }
  else if(sr1Sign >=17 && sr1Sign<=25  )//掛斷 
  {
      digitalWrite(10, LOW);
      digitalWrite(11, HIGH);
      digitalWrite(12, LOW);
      Serial.println("over");
      sr1Sign = 0;
      isPing = true;
  }
  else if(sr1Sign >=26 && sr1Sign<=29 )//打電話 && digitalRead(13)==0
  {
       digitalWrite(10, LOW);
       digitalWrite(11, LOW);
       digitalWrite(12, HIGH);
       Serial.println("take phone");
       sr1Sign = 0;
       isPing = true;
  }
  else if(sr1Sign == 30 )//跟 
  {
     //digitalWrite(8, HIGH);
     digitalWrite(4, HIGH);
     Serial.println("follow");
  }
  else if(sr1Sign >=31 && sr1Sign<=36 )//停下 
  {
     //digitalWrite(8, LOW);
     digitalWrite(4, LOW);
     Serial.println("stop");
  }
  val = analogRead(A0);
  //主:341513879CAC  從:3415138796E6

  robotAngle = -1;
  intUserAngle = -1;
  intUserAngle = manualBle();
  Serial.println("robotAngle :" + String(robotAngle));
  Serial.println("UserAngle :" + String(intUserAngle)); 
  robotAngle /= 30;
  if(val <= 100)
  {
     //hideBlock();
     Stop();
  }
  
  if(val >= 800)
  {
      //hideBlock();
      switch (robotAngle)
      {
                  //0-29
                  case 0 :  // >310
                      if( (intUserAngle>350 || intUserAngle<35) && intUserAngle!=-1 )
                      {
                          //Serial.println("ok");
                          digitalWrite(8, HIGH);
                          if(aaa==0)
                          {
                              Stop();
                              aaa++;
                          }
                          getBleDistance();
                      }
                      else if( intUserAngle<175 && intUserAngle>35 && intUserAngle!=-1 )
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Right(90);
                          //Serial.println("R");
                      }
                      else if( (intUserAngle>175 || intUserAngle<35) && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Left(90);
                          //Serial.println("L");
                      }
                      else
                      {
                          aaa=0;
                          Stop();
                      }
                      break;
                  //30-59
                  case 1 :  // >30
                      if( (intUserAngle>35 && intUserAngle<70) && intUserAngle!=-1 )
                      {
                          digitalWrite(8, HIGH);
                          if(aaa==0)
                          {
                              Stop();
                              aaa++;
                          }
                          getBleDistance();
                      }
                      else if( intUserAngle<200 && intUserAngle>35 && intUserAngle!=-1 )
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Right(90);
                          //Serial.println("R");
                      }
                     else if((intUserAngle>200 || intUserAngle<35) && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Left(90);
                          //Serial.println("L");
                      }
                      else
                      {
                          aaa=0;
                          Stop();
                      }
                      break;
                  //60-89
                  case 2 :  
                      if( (intUserAngle>70 && intUserAngle<110) && intUserAngle!=-1 )
                      {
                          Serial.println("ok");
                          digitalWrite(8, HIGH);
                          if(aaa==0)
                          {
                              Stop();
                              aaa++;
                          }
                          getBleDistance();
                      }
                      else if( intUserAngle<250 && intUserAngle>70 && intUserAngle!=-1 )
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Right(90);
                          //Serial.println("R");
                      }
                     else if( (intUserAngle>250 || intUserAngle<70) && intUserAngle!=-1 )
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Left(90);
                          //Serial.println("L");
                      }
                      else
                      {
                          aaa=0;
                          Stop();
                      }
                      break;
                  //90-119
                  case 3 :  
                      if( (intUserAngle>110 && intUserAngle<155) && intUserAngle!=-1 )
                      {
                          Serial.println("OK");
                          digitalWrite(8, HIGH);
                          if(aaa==0)
                          {
                              Stop();
                              aaa++;
                          }
                          getBleDistance();
                      }
                      else if( intUserAngle>155 && intUserAngle<270 && intUserAngle!=-1 )
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Right(90);
                          //Serial.println("R");
                      }
                     else if( (intUserAngle<155 || intUserAngle>270) && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Left(90);
                          //Serial.println("L");
                      }
                      else
                      {
                          aaa=0;
                          Stop();
                      }
                      break;
                  //120-149
                  case 4 :  
                      if( (intUserAngle>155 && intUserAngle<180) && intUserAngle!=-1 )
                      {
                          digitalWrite(8, HIGH);
                          if(aaa==0)
                          {
                              Stop();
                              aaa++;
                          }
                          getBleDistance();
                      }
                      else if( intUserAngle>180 && intUserAngle<330 && intUserAngle!=-1 )
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Right(90);
                          //Serial.println("R");
                      }
                     else if( (intUserAngle<180 || intUserAngle>330) && intUserAngle!=-1 )
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Left(90);
                          //Serial.println("L");
                      }
                      else
                      {
                          aaa=0;
                          Stop();
                      }
                      break;
                   //150-179 **
                   case 5 :  
                      if( intUserAngle>180 && intUserAngle<230 && intUserAngle!=-1 )
                      {
                          digitalWrite(8, HIGH);
                          if(aaa==0)
                          {
                              Stop();
                              aaa++;
                          }
                          getBleDistance();
                      }
                      else if(intUserAngle>70 && intUserAngle<230 && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Left(90);
                          Serial.println("L");
                      }
                      else if( (intUserAngle<70 || intUserAngle>230) && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Right(90);
                          Serial.println("R");
                      }
                      else
                      {
                          aaa=0;
                          Stop();
                      }
                      break;
                   //180-209 **
                   case 6 :  
                      if( intUserAngle>180 && intUserAngle<230 && intUserAngle!=-1 )
                      {
                          digitalWrite(8, HIGH);
                          if(aaa==0)
                          {
                              Stop();
                              aaa++;
                          }
                          getBleDistance();
                      }
                      else if(intUserAngle>70 && intUserAngle<230 && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Left(90);
                          Serial.println("L");
                      }
                      else if( (intUserAngle<70 || intUserAngle>230) && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Right(90);
                          Serial.println("R");
                      }
                      else
                      {
                          aaa=0;
                          Stop();
                      }
                      break;
                   //210-239 **
                   case 7 :  
                      if( (intUserAngle>180 && intUserAngle<230) && intUserAngle!=-1 )
                      {
                          digitalWrite(8, HIGH);
                          if(aaa==0)
                          {
                              Stop();
                              aaa++;
                          }
                          getBleDistance();
                      }
                      else if(intUserAngle>70 && intUserAngle<230 && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Left(90);
                          Serial.println("L");
                      }
                      else if( (intUserAngle<70 || intUserAngle>230) && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Right(90);
                          Serial.println("R");
                      }
                      else
                      {
                          aaa=0;
                          Stop();
                      }
                      break;
                   //240-269 **
                   case 8 :  
                      if( (intUserAngle>180 && intUserAngle<255) && intUserAngle!=-1 )
                      {
                          digitalWrite(8, HIGH);
                          if(aaa==0)
                          {
                              Stop();
                              aaa++;
                          }
                          getBleDistance();
                      }
                      else if(intUserAngle>70 && intUserAngle<255 && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Left(90);
                          Serial.println("L");
                      }
                      else if( (intUserAngle<70 || intUserAngle>255) && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Right(90);
                          Serial.println("R");
                      }
                      else
                      {
                          aaa=0;
                          Stop();
                      }
                      break;
                   //270-299
                   case 9 :  
                      if( (intUserAngle>230 && intUserAngle<=265) && intUserAngle!=-1 )
                      {
                          digitalWrite(8, HIGH);
                           if(aaa==0)
                          {
                              Stop();
                              aaa++;
                          }
                          getBleDistance();
                      }
                      else if(intUserAngle>115 && intUserAngle<230 && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Left(90);
                          Serial.println("L");
                      }
                      else if( (intUserAngle<115 || intUserAngle>230) && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Right(90);
                          Serial.println("R");
                      }
                      else
                      {
                          aaa=0;
                          Stop();
                      }
                      break;
                   //300-329
                   case 10 :  
                      if( (intUserAngle>265 && intUserAngle<=287) && intUserAngle!=-1 )
                      {
                          digitalWrite(8, HIGH);
                           if(aaa==0)
                          {
                              Stop();
                              aaa++;
                          }
                          getBleDistance();
                      }
                      else if(intUserAngle>115 && intUserAngle<265 && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Left(90);
                          Serial.println("L");
                      }
                      else if( (intUserAngle<115 || intUserAngle>265) && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Right(90);
                          Serial.println("R");
                      }
                      else
                      {
                          aaa=0;
                          Stop();
                      }
                      break;
                   //330-359
                   case 11 :  
                      if( (intUserAngle>287 && intUserAngle<350) && intUserAngle!=-1 )
                      {
                          digitalWrite(8, HIGH);
                          if(aaa==0)
                          {
                              Stop();
                              aaa++;
                          }
                          getBleDistance();
                      }
                      else if(intUserAngle>170 && intUserAngle<350 && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Left(90);
                          Serial.println("L");
                      }
                      else if( (intUserAngle<170 || intUserAngle>350) && intUserAngle!=-1)
                      {
                          aaa=0;
                          digitalWrite(8, LOW);
                          Right(90);
                          Serial.println("R");
                      }
                      else
                      {
                          aaa=0;
                          Stop();
                      }
                      break;
                 //
                 default :
                     aaa=0;
                     Stop();
                     break;
      }
  }
}
//八顆超音波副程式
int Sonar1()
{
  uint8_t Status1; // 儲存取得的偵測結果狀態
  uint16_t Distance1; // 儲存取得的偵測結果
  do
  {
    mySonar1.Ranging(); //執行偵測
    delay(10); //等待 10ms
    Status1 = mySonar1.GetDistance(1, Distance1); //取得偵測結果
    if (Status1 == 2) 
    {
        Distance1 = 1000;
        break;
    }
  } while (Status1 != 1);
  return Distance1;
}
int Sonar2()
{
  uint8_t Status2;    // 儲存取得的偵測結果狀態
  uint16_t Distance2; // 儲存取得的偵測結果
  do
  {
    mySonar2.Ranging(); //執行偵測
    delay(10); //等待 10ms
    Status2 = mySonar2.GetDistance(1, Distance2); //取得偵測結果
    if (Status2 == 2)
    {
        Distance2 = 1000;
        break;
    }
  } while (Status2 != 1);
  return Distance2;
}
int Sonar3()
{
    uint8_t Status3; // 儲存取得的偵測結果狀態
    uint16_t Distance3; // 儲存取得的偵測結果
    do
    {
      mySonar3.Ranging(); //執行偵測
      delay(10); //等待 10ms
      Status3 = mySonar3.GetDistance(1, Distance3); //取得偵測結果
      if (Status3 == 2)
      {
          Distance3 = 1000;
          break;
      }
    } while (Status3 != 1);
    return Distance3;
}
int Sonar4()
{
    uint8_t Status4; // 儲存取得的偵測結果狀態
    uint16_t Distance4; // 儲存取得的偵測結果
    do
    {
      mySonar4.Ranging(); //執行偵測
      delay(10); //等待 10ms
      Status4 = mySonar4.GetDistance(1, Distance4); //取得偵測結果
      if (Status4 == 2)
      {
          Distance4 = 1000;
          break;
      }
    } while (Status4 != 1);
    return Distance4;
}
int Sonar5()
{
    uint8_t Status5; // 儲存取得的偵測結果狀態
    uint16_t Distance5; // 儲存取得的偵測結果
    do
    {
      mySonar5.Ranging(); //執行偵測
      delay(10); //等待 10ms
      Status5 = mySonar5.GetDistance(1, Distance5); //取得偵測結果
      if (Status5 == 2)
      {
          Distance5 = 1000;
          break;
      }
    } while (Status5 != 1);
    return Distance5;
}
int Sonar6()
{
    uint8_t Status6; // 儲存取得的偵測結果狀態
    uint16_t Distance6; // 儲存取得的偵測結果
    do
    {
      mySonar6.Ranging(); //執行偵測
      delay(10); //等待 10ms
      Status6 = mySonar6.GetDistance(1, Distance6); //取得偵測結果
      if (Status6 == 2)
      {
          Distance6 = 1000;
          break;
      }
    } while (Status6 != 1);
    return Distance6;
}
int Sonar7()
{
    uint8_t Status7; // 儲存取得的偵測結果狀態
    uint16_t Distance7; // 儲存取得的偵測結果
    do
    {
      mySonar7.Ranging(); //執行偵測
      delay(10); //等待 10ms
      Status7 = mySonar7.GetDistance(1, Distance7); //取得偵測結果
      if (Status7 == 2)
      {
          Distance7 = 1000;
          break;
      }
    } while (Status7 != 1);
    return Distance7;
}
int Sonar8()
{
    uint8_t Status8; // 儲存取得的偵測結果狀態
    uint16_t Distance8; // 儲存取得的偵測結果
    do
    {
      mySonar8.Ranging(); //執行偵測
      delay(10); //等待 10ms
      Status8 = mySonar8.GetDistance(1, Distance8); //取得偵測結果
      if (Status8 == 2)
      {
          Distance8 = 1000;
          break;
      }
    } while (Status8 != 1);
    return Distance8;
}
//馬達
void Forward(int Speed)
{
    myMotorL.Forward(Speed);   //  讓馬達左輪以speed的設定向前轉動
    myMotorR.Forward(Speed);   //  讓馬達右輪以speed的設定向前轉動
}
void Backward(int Speed)
{
    myMotorL.Backward(Speed);   //  讓馬達左輪以speed的設定向後轉動
    myMotorR.Backward(Speed);   //  讓馬達左輪以speed的設定向後轉動
}
void Left(int Speed)
{
    myMotorL.Backward(Speed);   //  讓馬達左輪以speed的設定向後轉動
    myMotorR.Forward(Speed);    //  讓馬達右輪以speed的設定向前轉動
}
void Right(int Speed)
{
    myMotorR.Backward(Speed);   //  讓馬達右輪以speed的設定向後轉動
    myMotorL.Forward(Speed);    //  讓馬達左輪以speed的設定向前轉動
}
void Stop()
{
    myMotorL.Brake();
    myMotorR.Brake();
}
//避障副程式
void hideBlock()
{
  //前面超音波避障
  if(Sonar6()<40 && Sonar2()>40)
  {
      Backward(170);
      delay(300);
      Stop();
  }
  //後面超音波避障
  if(Sonar2()<40 && Sonar6()>40)
  {
      Forward(170);
      delay(300);
      Stop();
  }
  //左前超音波避障
  if (Sonar7() < 40)
  {
    Right(150);
    delay(400);
    Stop();
    if (Sonar6() > 40)
    {
      Forward(170);
      delay(500);
      Stop();
    }
  }
  //右前超音波避障
  if (Sonar5() < 40)
  {
    Left(150);
    delay(400);
    Stop();
    if (Sonar6() > 40)
    {
      Forward(170);
      delay(500);
      Stop();
    }
  }
  //左後超音波避障
  if(Sonar1()<30)
  {
      Right(150);
      delay(400);
      Stop();
      if(Sonar6()>40)
      {
          Forward(170);
          delay(500);
          Stop();
      }
  }
  //右後超音波避障
  if(Sonar3()<30)
  {
      Left(150);
      delay(400);
      Stop();
      if(Sonar6()>40)
      {
          Forward(170);
          delay(500);
          Stop();
      }
  }
  //左方超音波避障
  if(Sonar8()<30)
  {
      Right(150);
      delay(700);
      Stop();
      if(Sonar6()>40)
      {
          Forward(170);
          delay(500);
          Stop();
      }
  }
  //右方超音波避障
  if(Sonar4()<30)
  {
      Left(150);
      delay(700);
      Stop();
      if(Sonar6()>40)
      {
          Forward(170);
          delay(500);
          Stop();
      }
  }
}
//繞過右方避障副程式
void hideBlockRight()
{
    Forward(170);
    delay(670);
    Stop();
//    for (int i = 0; i < 2000; i++)
//    {
//      if (Sonar6() < 40 || Sonar7() < 20 || Sonar5() < 20) break;
//    }
    //
    Right(150);
    delay(670);
    Stop();
    //
    Forward(170);
    while (Sonar8() < 60 && Sonar6()>60){hideBlock();}
    Stop();
    //
    hideBlock();
    Forward(170);
    delay(800);
    Stop();
    //
    Left(150);
    delay(700);
    Stop();
    //
    hideBlock();
    Forward(170);
    delay(1000);
    Stop();
    for(int i = 0; i<3000;i++)
    {
      Forward(170);
      delay(1);
      //Stop();
      if(Sonar6()<60) 
      {
          Stop();
          break;
      }
      
    }
    Stop();
}
//繞過左方避障副程式
void hideBlockLeft()
{
    Forward(170);
    delay(400);
    Stop();
//    for (int i = 0; i < 2000; i++)
//    {
//      if (Sonar6() < 40 || Sonar7() < 20 || Sonar5() < 20) break;
//    }
    //
    Left(150);
    delay(770);
    Stop();
    //
    Forward(170);
    while (Sonar4() < 60 && Sonar6()>60){hideBlock();}
    Stop();
    //
    hideBlock();
    Forward(170);
    delay(800);
    Stop();
    //
    Right(150);
    delay(700);
    Stop();
    //
    hideBlock();
    Forward(170);
    delay(1000);
    Stop();
    for(int i = 0; i<3000;i++)
    {
      Forward(170);
      delay(1);
      //Stop();
      if(Sonar6()<60) 
      {
          Stop();
          break;
      }
      
    }
    Stop();
}
//藍牙連線副程式
void bleConnection()
{
    delay(100);
    Serial1.write("AT+CON3415138796E6");  //AT+CON(MAC)　3415138796E6
    delay(100);
    connectionCheck = true;
    delay(100);
}
//RSSI轉成距離副程式
float getDist(int rssi)
{
    int iRssi = abs(rssi);
    float power = (iRssi - 59) / (10 * 2.0); //59
    return pow(10, power);
}
//藍牙直線追隨副程式
void getBleDistance()
{
    return ;
    float bleDistance;
    String strRssi;
    int strNum = 0;
  
    Serial1.write("AT+RSSI?");
    delay(200);
    if (Serial1.available() > 0)
    {
      digitalWrite(6, HIGH);
      while (Serial1.available())
      {
        getedS = 1;
        getword += (char)Serial1.read();
        delay(1);
      }
      if (getedS)
      {
        //Serial.println(getword);
        for (int i = 0; i < getword.length(); i++)
        {
            strNum++;
            if (getword[i] == '-') break;
        }
        strRssi = getword.substring(strNum, strNum + 2);
        Serial.println(strRssi);
        bleDistance = getDist(strRssi.toInt());
        bleDistance *= 100;
        
//        Serial.println(bleDistance);
//        Serial.println("Sonar6 :" + String(Sonar6()));
//        Serial.println("Sonar2 :" + String(Sonar2()));
        
        hideBlock();
        if (bleDistance > 100 && Sonar5() > 20  && Sonar6() > 70 && Sonar7() > 20)
        {
            Forward(170);
            Serial.println("F");
        }
        else if ( ( (Sonar5() < 30 || Sonar6() < 45 || Sonar7() < 30) && (Sonar1() > 30 && Sonar2() > 30 && Sonar3() > 30))
                   || bleDistance < 80)
        {
            Backward(170);
            Serial.println("B");
        }
//        else if(bleDistance > 400 && Sonar6() > 45 && Sonar6() < 80)
//        {
//            if(Sonar4()>Sonar8()) hideBlockRight();
//            else if(Sonar4()<Sonar8()) hideBlockLeft();    
//            else hideBlockRight();
//            Serial.print("H");
//        }
        else
        {
            Stop();
        }
        getword = "";
        getedS = 0;
      }
    }
    else
    {
      digitalWrite(6, LOW);
      Stop();
      bleConnection();
    }
}
//電子羅盤換算角度副程式
int compassDirection()
{
    float x, y, z;
    int angle;
  
    Wire.beginTransmission(Addr);
    Wire.write(0x03);
    Wire.endTransmission();
    Wire.requestFrom(Addr, 6);
    if (Wire.available() <= 6)
    {
      x = Wire.read() << 8 | Wire.read();
      z = Wire.read() << 8 | Wire.read();
      y = Wire.read() << 8 | Wire.read();
    }
    angle = atan2((double)y, (double)x) * (180 / 3.1416) + 180;
    delay(100);
    return (angle);
}
//讀取使用者羅盤角度副程式
int manualBle()
{
    String val = "F";
    int strNum = 0;
    int angle = -1;
    //Serial1.println('Z');
    if (Serial1.available() > 0)
    {
      digitalWrite(6, HIGH);
      while (Serial1.available())
      {
        strNum++;
        getedS = 1;
        getword += (char)Serial1.read();
        delay(1);
      }
      if (getedS)
      {
        val = getword;
        getword = "";
        getedS = 0;
      }
      robotAngle = compassDirection();
      val = val.substring(strNum - 3, strNum);
      angle = val.toInt();
    }
    else
    {
      digitalWrite(6, LOW);
      Stop();
      robotAngle = -1;
      angle = -1;
      bleConnection();
    }
    return angle;
}
//語音副程式(接聽 掛斷)
int SR1()
{
    uint8_t Result, State;
    State = mySR1.GetRecognition(Result); //取得偵測結果
    if(State == 0) 
    {
        return Result;
    }
    else
    {
        return 0;
    }
}

