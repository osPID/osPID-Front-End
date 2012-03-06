String LastError="";
void Connect()
{

  if(!madeContact)
  {
    try
    {
      LastError="";
      ConnectButton.setVisible(false);
      Connecting.setVisible(true);
      nPoints=0;
      startTime= millis();
      for(int i=0;i<CommPorts.length;i++)
      {
        if ( r1.getItem(i).getState())
        {
          myPort = new Serial(this, CommPorts[i], 115200); 
          myPort.bufferUntil(10); 
          break;
        }
      }
    }
    catch (Exception ex)
    {
      LastError = ex.toString();
      //println(LastError);
      ConnectButton.setVisible(true);
      Connecting.setVisible(false);
      DisconnectButton.setVisible(false);
    } 

  }

}

void Disconnect()
{
  if(madeContact)
  {

    myPort.stop();
    madeContact=false;
    ConnectButton.setVisible(true);
    Connecting.setVisible(false);
    DisconnectButton.setVisible(false);
    Nullify();
  } 
}

// Sending Floating point values to the arduino
// is a huge pain.  if anyone knows an easier
// way please let know.  the way I'm doing it:
// - Take the 6 floats we need to send and
//   put them in a 6 member float array.
// - using the java ByteBuffer class, convert
//   that array to a 24 member byte array
// - send those bytes to the arduino
void Send_Dash()//To_Controller()
{


  float[] toSend = new float[3];
  toSend[0] = float(SPField.getText());
  toSend[1] = float(InField.getText());
  toSend[2] = float(OutField.getText());

  Byte a = (AMLabel.valueLabel().getText()=="Manual")?(byte)0:(byte)1;
  byte identifier = 1;
  myPort.write(identifier);
  myPort.write(a);
  myPort.write(floatArrayToByteArray(toSend));
} 

void Send_Tunings()
{
  float[] toSend = new float[3];
  Byte d = (DRLabel.valueLabel().getText()=="Direct")?(byte)0:(byte)1;
  toSend[0] = float(PField.getText());
  toSend[1] = float(IField.getText());
  toSend[2] = float(DField.getText());
  byte identifier = 2;
  myPort.write(identifier);
  myPort.write(d);
  myPort.write(floatArrayToByteArray(toSend));
}

void Send_Auto_Tune()
{
  float[] toSend = new float[3];
  Byte d = (ATLabel.valueLabel().getText()=="OFF")?(byte)0:(byte)1;
  toSend[0] = float(oSField.getText());
  toSend[1] = float(nField.getText());
  toSend[2] = float(lbField.getText());
  byte identifier = 3;
  myPort.write(identifier);
  myPort.write(d);
  myPort.write(floatArrayToByteArray(toSend));
}

void Send_Configuration()//To_Controller()
{

  float[] toSend = new float[4];
  toSend[0] = float(R0Field.getText());
  toSend[1] = float(BetaField.getText());
  toSend[2] = float(T0Field.getText());
  toSend[3] = float(oSecField.getText());
  
  Byte a =0;
  if(r2.getState(1)==true)a=1;
  else if(r2.getState(2)==true)a=2;
  
  byte o = r3.getState(0)==true ? (byte)0 : (byte)1;
  
println(o);
  byte identifier = 5;
  myPort.write(identifier);
  myPort.write(a);
  myPort.write(o);
  myPort.write(floatArrayToByteArray(toSend));
} 

void Reset_Factory_Defaults()
{
  byte identifier = 4;
  myPort.write(identifier);
  myPort.write((byte)1); 
}

byte[] floatArrayToByteArray(float[] input)
{
  int len = 4*input.length;
  int index=0;
  byte[] b = new byte[4];
  byte[] out = new byte[len];
  ByteBuffer buf = ByteBuffer.wrap(b);
  for(int i=0;i<input.length;i++) 
  {
    buf.position(0);
    buf.putFloat(input[i]);
    for(int j=0;j<4;j++) out[j+i*4]=b[3-j];
  }
  return out;
}

float unflip(float thisguy)
{
  byte[] b = new byte[4];
  ByteBuffer buf = ByteBuffer.wrap(b);  
  buf.position(0);
  buf.putFloat(thisguy);
  byte temp = b[0]; 
  b[0] = b[3]; 
  b[3] = temp;
  temp = b[1]; 
  b[1] = b[2]; 
  b[2] = temp;
  return buf.getFloat(0);

}

//take the string the arduino sends us and parse it
void serialEvent(Serial myPort)
{
  String read = myPort.readStringUntil(10);
  if(outputFileName!="") output.print(str(millis())+ " "+read);
  String[] s = split(read, " ");
  print(read);
  
  if(s.length==20)
  {
   
    Setpoint = float(s[1]);
    Input = float(s[2]);
    Output = float(s[3]);

    SPLabel.setValue(s[1]);           //   where it's needed
    InLabel.setValue(s[2]);           //
    OutLabel.setValue(s[3]);          //
    AMCurrent.setValue(int(s[16]) == 1 ? "Automatic" : "Manual");

    PLabel.setValue(s[4]);
    ILabel.setValue(s[5]);
    DLabel.setValue(s[6]);
    DRCurrent.setValue(int(s[17]) == 1 ? "Reverse" : "Direct");

    String oS, n;
    oSLabel.setValue(s[7]);
    nLabel.setValue(s[8]);
    lbLabel.setValue(s[9]);
    ATCurrent.setValue(int(s[18])==1? "ATune On" : "ATune Off");

    int ack = int(trim(s[19]));
  
    if(!madeContact || ack==1)
    {
      SPField.setText(s[1]);    //   the arduino,  take the
      InField.setText(s[2]);    //   current values and put
      OutField.setText(s[3]);
      AMLabel.setValue(int(s[16]) == 1 ? "Automatic" : "Manual");   
    }
    if(!madeContact || ack==2)
    {
      PField.setText(s[4]);    //   the arduino,  take the
      IField.setText(s[5]);    //   current values and put
      DField.setText(s[6]);
      DRLabel.setValue(int(s[17]) == 1 ? "Reverse" : "Direct");   
    }
    if(!madeContact || ack==3)
    {
      
      oSField.setText(s[7]);
      nField.setText(s[8]);
      lbField.setValue(s[9]);    
      ATLabel.setValue(int(s[18])==1? "ON" : "OFF");
    }
    if(!madeContact || ack==5)
    {
      int v = int(s[14]); 
      if(v==0) r2.getItem(0).setState(true);
      else if(v==1) r2.getItem(1).setState(true);
      else r2.getItem(2).setState(true);
      
      v = int(s[15]);
      if(v==0) r3.getItem(0).setState(true);
      else r3.getItem(1).setState(true);

      R0Field.setText(s[10]);    //   the arduino,  take the
      BetaField.setText(s[11]);    //   current values and put
      T0Field.setText(s[12]);
      oSecField.setText(s[13]);   
    }
    if(!madeContact)
    {
      ConnectButton.setVisible(false);
      Connecting.setVisible(false);
      DisconnectButton.setVisible(true);
      madeContact=true;    
    }
  }
}

