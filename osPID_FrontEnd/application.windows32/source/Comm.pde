String LastError="";
void Connect()
{
  if(ConnectButton==null)return;
  if(!madeContact)
  {
    try
    {
      LastError="";
      ConnectButton.setVisible(false);

      Connecting.setVisible(true);
      nPoints=0;
      startTime= millis();
      
      int selected=int(r1.getValue());
      println(selected);
      
      /*for(int i=0;i<CommPorts.length;i++)
      {
        if ( r1.getItem(i).getState())
        {*/
          myPort = new Serial(this, CommPorts[selected], 9600); 
          myPort.bufferUntil(10); 
          //immediately send a request for osPID type;
          byte[] typeReq = new byte[]{
            0,0                              };
          myPort.write(typeReq);
        /*  break;
        }
      }*/
    }
    catch (Exception ex)
    {
      LastError = ex.toString();
      //println(LastError);
      ConnectButton.setVisible(true);
      Connecting.setVisible(false);
      DisconnectButton.setVisible(false);
      commconfigLabel1.setVisible(true);
      commconfigLabel2.setVisible(true);
    } 
  }

}

void Disconnect()
{
  
  if(DisconnectButton==null)return;
  if(madeContact)
  {    
    myPort.stop();
    madeContact=false;
    ConnectButton.setVisible(true);
    Connecting.setVisible(false);
    DisconnectButton.setVisible(false);
    commconfigLabel1.setVisible(true);
    commconfigLabel2.setVisible(true);
    ClearInput();
    ClearOutput();
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
if(myPort==null)return;

  float[] toSend = new float[3];
  toSend[0] = float(SPField.getText());
  toSend[1] = float(InField.getText());
  toSend[2] = float(OutField.getText());

  Byte a = (AMLabel.getValueLabel().getText()=="Manual")?(byte)0:(byte)1;
  byte identifier = 1;
  myPort.write(identifier);
  myPort.write(a);
  myPort.write(floatArrayToByteArray(toSend));
} 

void Send_Tunings()
{
  if(myPort==null)return;
  float[] toSend = new float[3];
  Byte d = (DRLabel.getValueLabel().getText()=="Direct")?(byte)0:(byte)1;
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
  if(myPort==null)return;
  float[] toSend = new float[3];
  Byte d = (ATLabel.getValueLabel().getText()=="OFF")?(byte)0:(byte)1;
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
  if(myPort==null)return;
  float[] toSend = new float[4];
  toSend[0] = float(R0Field.getText());
  toSend[1] = float(BetaField.getText());
  toSend[2] = float(T0Field.getText());
  toSend[3] = float(oSecField.getText());

  Byte a =0;
  if(r2.getState(1)==true)a=1;
  else if(r2.getState(2)==true)a=2;

  byte o = r3.getState(0)==true ? (byte)0 : (byte)1;

  byte identifier = 5;
  myPort.write(identifier);
  myPort.write(a);
  myPort.write(o);
  myPort.write(floatArrayToByteArray(toSend));
} 
void Run_Profile()
{
  if(myPort==null)return;
  byte[] toSend = new byte[2];
  toSend[0]=8;
  toSend[1]=1;
  myPort.write(toSend);
}
void Stop_Profile()
{
  if(myPort==null)return;
  byte[] toSend = new byte[2];
  toSend[0]=8;
  toSend[1]=0;
  myPort.write(toSend);
}

void Send_Profile()
{
  println("send_profile1");
  if(myPort==null)return;
  currentxferStep=0;
  SendProfileStep(byte(currentxferStep));
  println("send_profile2");
}
int currentxferStep=-1;

void SendProfileStep(byte step)
{
  
  if(myPort==null)return;
  byte identifier=7;
  Profile p = profs[curProf];
  float[] temp = new float[2];
  temp[0] = p.vals[step];
  temp[1] = p.times[step];

  byte[] toSend = new byte[11];
  toSend[0]=identifier;
  toSend[1]=step;
  toSend[2]=p.types[step];
  arrayCopy(floatArrayToByteArray(temp),0,toSend,3,8);
  myPort.write(toSend);

}

void SendProfileName()
{
  if(myPort==null)return;
  byte identifier=7;
print("sendprofilename1");

  byte[] toSend = new byte[9];

  toSend[0] = identifier;
  toSend[1] = byte(currentxferStep);
print("sendprofilename2");
  try
  {
    byte[] n = profs[curProf].Name.getBytes();
    int copylen = n.length>7? 7:n.length;
    for(int i=0;i<7;i++) toSend[i+2] = i<copylen? n[i] : 32;

  }
  catch(Exception ex)
  {
    print(ex.toString());
  }
  myPort.write(toSend);
  print("sendprofilename3");
}

void Reset_Factory_Defaults()
{
  if(myPort==null)return;
  byte identifier = 4;
  myPort.write(identifier);
  myPort.write((byte)1); 
}

byte[] floatArrayToByteArray(float[] input)
{
  int len = 4*input.length;
  //int index=0;
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

byte[] intArrayToByteArray(int[] input)
{
  int len = 4*input.length;
  //int index=0;
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

String InputCreateReq="",OutputCreateReq="";
//take the string the arduino sends us and parse it
void serialEvent(Serial myPort)
{
  String read = myPort.readStringUntil(10);
  if(outputFileName!="") output.print(str(millis())+ " "+read);
  String[] s = split(read, " ");
  print(read);

  if(s.length==4 && s[0].equals("osPID"))
  {
    if(InputCard=="" || !InputCard.equals(trim(s[2]))) InputCreateReq=trim(s[2]);
    if(OutputCard=="" || !OutputCard.equals(trim(s[3]))) OutputCreateReq=trim(s[3]);
    ConnectButton.setVisible(false);
    Connecting.setVisible(false);
    DisconnectButton.setVisible(true);
    commconfigLabel1.setVisible(false);
    commconfigLabel2.setVisible(false);

    madeContact=true;
  }
  if(!madeContact) return;
  if(s.length==6 && s[0].equals("DASH"))
  {

    Setpoint = float(s[1]);
    Input = float(s[2]);
    Output = float(s[3]);  
    SPLabel.setValue(s[1]);           //   where it's needed
    InLabel.setValue(s[2]);           //
    OutLabel.setValue(s[3]);  
    AMCurrent.setValue(int(s[4]) == 1 ? "Automatic" : "Manual");    
    //if(SPField.valueLabel().equals("---"))
    if(dashNull || int(trim(s[5]))==1)
    {

      dashNull=false;
      SPField.setText(s[1]);    //   the arduino,  take the
      InField.setText(s[2]);    //   current values and put
      OutField.setText(s[3]);
      AMLabel.setValue(int(s[4]) == 1 ? "Automatic" : "Manual");   
    }
  }
  else if(s.length==10 && s[0].equals("TUNE"))
  {
    PLabel.setValue(s[1]);
    ILabel.setValue(s[2]);
    DLabel.setValue(s[3]);
    DRCurrent.setValue(int(s[4]) == 1 ? "Reverse" : "Direct");
    ATCurrent.setValue(int(s[5])==1? "ATune On" : "ATune Off");
    oSLabel.setValue(s[6]);
    nLabel.setValue(s[7]);
    lbLabel.setValue(trim(s[8]));
    if(tuneNull || int(trim(s[9]))==1)
    {
      tuneNull=false;
      PField.setText(s[1]);    //   the arduino,  take the
      IField.setText(s[2]);    //   current values and put
      DField.setText(s[3]);
      DRLabel.setValue(int(s[4]) == 1 ? "Reverse" : "Direct");  
      oSField.setText(s[6]);
      nField.setText(s[7]);
      lbField.setValue(s[8]);    
      ATLabel.setValue(int(s[5])==1? "ON" : "OFF");
    }

  }
  else if(s[0].equals("IPT") && InputCard != null)
  {
    PopulateCardFields(InputCard, s);
  }
  else if(s[0].equals("OPT") && OutputCard != null)  
  {
    PopulateCardFields(OutputCard, s);
  }
  else if( s.length>3 && s[0].equals("PROF"))
  {
    lastReceiptTime=millis();
    int curType = int(trim(s[2]));
    curProfStep = int(s[1]);
    ProfCmd.setVisible(false);
    ProfCmdStop.setVisible(true);
    String[] msg;
    switch(curType)
    {
    case 1: //ramp
      msg = new String[]{
        "Running Profile", "", "Step="+s[1]+", Ramping Setpoint", float(trim(s[3]))/1000+" Sec remaining"            };
      break;
    case 2: //wait
      float helper = float(trim(s[4]));
      msg = new String[]{
        "Running Profile", "","Step="+s[1]+", Waiting","Distance Away= "+s[3],(helper<0? "Waiting for cross" :("Time in band= "+helper/1000+" Sec" ))            };
      break;
    case 3: //step
      msg = new String[]{
        "Running Profile", "","Step="+s[1]+", Stepped Setpoint"," Waiting for "+ float(trim(s[3]))/1000+" Sec"            };
      break;

    default:
      msg = new String[0];
      break;
    }
    poulateStat(msg);
  }
  else if(trim(s[0]).equals("P_DN"))
  {
    lastReceiptTime = millis()-10000;
    ProfileRunTime();
  }

  if(s.length==5 && s[0].equals("ProfAck"))
  {
    lastReceiptTime=millis();
    String[] profInfo = new String[]{
      "Transferring Profile","Step "+s[1]+" successful"            };
    poulateStat(profInfo);
    currentxferStep = int(s[1])+1;
    if(currentxferStep<pSteps) SendProfileStep(byte(currentxferStep));
    else if(currentxferStep>=pSteps) SendProfileName();

  }
  else if(s[0].equals("ProfDone"))
  {
    lastReceiptTime=millis()+7000;//extra display time
    String[] profInfo = new String[]{
      "Profile Transfer","Profile Sent Successfully"        };
    poulateStat(profInfo);
    currentxferStep=0;
  }
  else if(s[0].equals("ProfError"))
  {
    lastReceiptTime=millis()+7000;//extra display time
    String[] profInfo = new String[]{
      "Profile Transfer","Error Sending Profile"            };
    poulateStat(profInfo);
  }
}

void poulateStat(String[] msg)
{
  for(int i=0;i<6;i++)
  {
    ((controlP5.Textlabel)controlP5.getController("dashstat"+i)).setValue(i<msg.length?msg[i]:"");
    ((controlP5.Textlabel)controlP5.getController("profstat"+i)).setValue(i<msg.length?msg[i]:"");
  }
}