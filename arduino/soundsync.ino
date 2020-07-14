char sw=0,bsw='j';
int i;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  for(i=2;i<15;i++)pinMode(i,INPUT);//2-14
}

void loop() {
  // put your main code here, to run repeatedly:
  sw=0;

  if(digitalRead(2)){
    sw='a';
    goto A;
    
  }
  if(digitalRead(3)){
    sw='b';
    goto A;
  }
  if(digitalRead(4)){
    sw='c';
    goto A;
  }
  if(digitalRead(5)){
    sw='d';
    goto A;
  }
  if(digitalRead(6)){
    sw='e';
    goto A;
  }
  
  if(digitalRead(7)){
    sw='f';
    goto A;
  }
  
  if(digitalRead(8)){
    sw='g';
    goto A;
  }
  if(digitalRead(9)){
    sw='h';
    goto A;
  }
  if(digitalRead(10)){
    sw='i';
    goto A;
  }
  if(digitalRead(11)){
    sw='j';
    goto A;
  }
  if(digitalRead(12)){
    sw='k';
    goto A;
  }
  if(digitalRead(13)){
    sw='l';
    goto A;
  }
  if(digitalRead(14)){
    sw='m';
    goto A;
  }
  

  A:
  
  if(sw!=bsw){
    if(sw!=0) Serial.write(sw);
    bsw=sw;
  }
  delay(50);
}
