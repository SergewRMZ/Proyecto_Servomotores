#include <Servo.h>
#include <SoftwareSerial.h>

const int SERVO1_PIN = 9;
const int SERVO2_PIN = 10;

const int BT_RX = 2;
const int BT_TX = 3;

SoftwareSerial bluetooth(BT_RX, BT_RX);

Servo servos[2];
String inputString = "";      
bool stringComplete = false; 

void setup() {
  Serial.begin(9600);
  bluetooth.begin(9600);
  
  servos[0].attach(SERVO1_PIN);
  servos[1].attach(SERVO2_PIN);
  Serial.println("Listo para recibir datos...");
}

void loop() {
  readSerial();

  if (stringComplete) {
    parsingInput();     
    inputString = "";
    stringComplete = false;
  }
}

void readSerial() {
  while (bluetooth.available()) {
    char c = bluetooth.read();

    if (c == '\n') {     
      stringComplete = true;
      break;
    } else {
      inputString += c;    
    }
  }
}

void parsingInput() {
  int coma = inputString.indexOf(',');
  int angles[2];
  
  if(coma != -1) {
    String ang1Str = inputString.substring(0, coma);
    String ang2Str = inputString.substring(coma + 1);

    angles[0] = constrain(ang1Str.toInt(), 0, 180);
    angles[1] = constrain(ang2Str.toInt(), 0, 180);

    for (int i = 0; i < 2; i++) {
      servos[i].write(angles[i]);
      delay(100);
    }
    

    Serial.print("Servo 1 - Ángulo: ");
    Serial.print(angles[0]);
    Serial.print("°, Servo 2 - Ángulo: ");
    Serial.print(angles[1]);
    Serial.print("°\n");
  }
}
