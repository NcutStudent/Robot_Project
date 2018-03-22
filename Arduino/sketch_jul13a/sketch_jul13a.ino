#include <ozone.h>
#include <SoftwareSerial.h>
//SoftwareSerial myBT(10,9); //RX TX
//超音波
SonarA mySonar1(1);
SonarA mySonar2(2);
SonarA mySonar3(3);
SonarA mySonar6(6);
uint8_t Status1; // 儲存取得的偵測結果狀態
uint8_t Status2; // 儲存取得的偵測結果狀態
uint8_t Status3; // 儲存取得的偵測結果狀態
uint8_t Status6; // 儲存取得的偵測結果狀態
uint16_t Distance1; // 儲存取得的偵測結果
uint16_t Distance2; // 儲存取得的偵測結果
uint16_t Distance3; // 儲存取得的偵測結果
uint16_t Distance6; // 儲存取得的偵測結果
//馬達
MotorRunnerC myMotorL(9);       //  設定左輪馬達模組編號為9
MotorRunnerC myMotorR(10);      //  設定右輪馬達模組編號為10
//語音
SR1 mySR1(8); //設定模組編號為 31 
uint8_t Result, State;
char MyString[32];
//籃牙
String my_ibeacon = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
String my_Mac = "508CB165FD1B";


void setup()
{
//     Serial.begin(230400);
//     while (!Serial);
     //
     Serial1.begin(230400);
     //超音波
     mySonar1.SetRepeatTime(10);
     mySonar2.SetRepeatTime(10);
     mySonar3.SetRepeatTime(10);
     mySonar6.SetRepeatTime(10);
     mySonar1.RepeatRanging();
     mySonar2.RepeatRanging();
     mySonar3.RepeatRanging();   
     mySonar6.RepeatRanging();  
    //語音
    mySR1.DeleteAllSentence();
    delay(100); // 延遲 100 ms
    sprintf(MyString, "jie dian hua"); // 接電話
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(1);
    sprintf(MyString, "gua dian hua"); // 掛電話
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(2);
    sprintf(MyString, "gen"); // 跟
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(3);
    sprintf(MyString, "ting"); // 停
    mySR1.PutSentence(MyString);
    mySR1.SaveSentence(4);
    mySR1.GoRecognition();
    //Led
    pinMode(6, OUTPUT);  
    pinMode(7, OUTPUT);  
    pinMode(8, OUTPUT);  
    pinMode(9, OUTPUT);  
    digitalWrite(6, LOW);
    digitalWrite(7, LOW);
    digitalWrite(8, LOW);
    digitalWrite(9, LOW);

}
void loop()
{

    String ibeacon_list;
    String strUUID, strRssi, strMac;
    float bleDistance;
    int startUUID = 25, endUUID = 57;
    int startRssi = 84, endRssi = 86;
    int startMac = 69, endMac = 81;
    boolean ibeaconCheck = false;
    //508CB165FD1B


    ibeacon_list = getIbeaconList();
    //Serial.println(ibeacon_list);
    while(ibeaconCheck == false)
    {
        strUUID = ibeacon_list.substring(startUUID,endUUID);   
        strRssi = ibeacon_list.substring(startRssi,endRssi); 
        strMac = ibeacon_list.substring(startMac,endMac);  
        if(strUUID == "")
        {
            digitalWrite(6, HIGH);
            digitalWrite(8, HIGH);    
            ibeaconCheck = true;
        }     
        else if(strUUID == my_ibeacon && strMac == my_Mac)
        {
            Sonar6();
            bleDistance =getDist(strRssi.toInt()); 
            bleDistance*=100;        
            if(bleDistance>55 || (Distance6>350 && Distance6>0))
            {
               while(1)
               {
                    Forward(150);
                    Sonar6();
                    digitalWrite(6, HIGH);
                    digitalWrite(8, LOW); 
                    if(Distance6<200 && Distance6>0)
                    {
                        Stop();
                        //delay(1000);
                        break;
                    }
                            
               }
                 Stop();      
            }
            else if( bleDistance<30 || (Distance6<100 && Distance6>0) )
            {
                 digitalWrite(8, HIGH);
                 digitalWrite(6, LOW);   
                 Backward(150);
                 for(int backTime=0; backTime<20; backTime++)
                 {
                      //Serial.println(backTime);
                      Sonar2();
                      if(Distance2<10 && Distance2>0)
                      {
                           backTime=70;
                           Stop();
                           break;
                      }
                      else
                      {
                          delay(1);
                      }
                 }
                 Stop();
            }
            else
            {
                  digitalWrite(6, LOW);
                  digitalWrite(8, LOW);              
            }
            ibeaconCheck = true;
        }
        else
        {
            startUUID += 78;
            endUUID += 78;
            startRssi += 78;
            endRssi += 78;
            startMac += 78;
            endMac += 78;
        }
        Stop();
    }

    
//     Sonar2();
//     Sonar6();
//     Serial.print("Distance2 :");
//     Serial.println(Distance2);
//     Serial.print("Distance6 :");
//     Serial.println(Distance6);
//     delay(500);
     
//   manualBle();
  
   /* 語音
   State = mySR1.GetRecognition(Result); //取得偵測結果
  if(State == 0) 
  {
      Serial.println(Result);
      if(Result==1) digitalWrite(8, HIGH); 
      else if(Result==2)digitalWrite(8, LOW); 
      else if(Result==3)digitalWrite(7, HIGH); 
      else if(Result==4)digitalWrite(7, LOW); 
  }
  delay(10);

      /*超音波
      Sonar(); 
      if(Distance1 < Distance2)
      {
           Right(50); 
           delay(700);        
      }
      else if(Distance3 < Distance2)
      {
          Left(50);
          delay(700);        
      }
      else if(Distance2>25)
      {
         Backward(50);
      }  
      else if(Distance2<10)
      {
         Forward(50);
      } 
      else
      { 
         Stop();
      }
 */     

}
float getDist(int rssi)
{
     int iRssi = abs(rssi);
     float power = (iRssi-59)/(10*2.0);//59
     return pow(10, power);
}
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
    myMotorR.Forward(Speed);   //  讓馬達右輪以speed的設定向前轉動
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
void Sonar2()
{
    do 
    {
        mySonar2.Ranging(); //執行偵測
        delay(10); //等待 10ms
        Status2= mySonar2.GetDistance(1, Distance2); //取得偵測結果
        if(Status2==2) break;
    } while(Status2 != 1); 
}
void Sonar6()
{
    do 
    {
        mySonar6.Ranging(); //執行偵測
        delay(10); //等待 10ms
        Status6= mySonar6.GetDistance(1, Distance6); //取得偵測結果
        if(Status6==2) break;
    } while(Status6 != 1); 
}
void Sonar1()
{

  /*do 
  {
      Status1 = mySonar1.GetDistance(1, Distance1); //取得偵測結果
  } while (Status1 != 1); //迴圈停止條件(Status = 1)
*/
  

  /*
  do 
  {
      Status3 = mySonar3.GetDistance(1, Distance3); //取得偵測結果
  } while (Status3 != 1); //迴圈停止條件(Status = 1)*/
  
}
void manualBle()
{
    char val;
    if (Serial.available()) 
    {
        val = Serial.read();
        Serial1.print(val);
    }
    if (Serial1.available()) 
    {
       val = Serial1.read();
       Serial.print(val);   
    }
 
}
String getIbeaconList()
{
  String response;
  Serial1.print("AT+DISI?");
  delay(10);
  //OK+DISISOK+DISC:4C000215:74278BDAB64445208F0C720EAF059935:FFE0FFE1C5:78A5048CECAC:-053OK+DISC:4C000215:74278BDAB64445208F0C720EAF059935:FFE0FFE1C5:78A50485AF2D:-050OK+DISCE
  response = getBt();
  
  while(response.startsWith(String("OK+DISIS"))==true && response.endsWith(String("OK+DISCE"))==false) 
  {                                                                              
    response += getBt();
    //delay(1);
  }
  return response;
}
String getBt()
{
  String response;
  char val;

  if (Serial1.available())
  {
    while(Serial1.available())
    {
      val = Serial1.read();    
      response += val;
      delayMicroseconds(100);
    }
  }
  return response;
}
