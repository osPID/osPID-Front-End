import processing.core.*; 
import processing.xml.*; 

import java.nio.ByteBuffer; 
import processing.serial.*; 
import controlP5.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class osPID_FrontEnd extends PApplet {





/***********************************************
 * User spcification section
 **********************************************/
int windowWidth = 900;      // set the size of the 
int windowHeight = 600;     // form

float InScaleMin = 0;       // set the Y-Axis Min
float InScaleMax = 1024;    // and Max for both
float OutScaleMin = 0;      // the top and 
float OutScaleMax = 100;    // bottom trends


int windowSpan = 300000;    // number of mS into the past you want to display
int refreshRate = 100;      // how often you want the graph to be reDrawn;

//float displayFactor = 1; //display Time as Milliseconds
//float displayFactor = 1000; //display Time as Seconds
float displayFactor = 60000; //display Time as Minutes

String outputFileName = ""; // if you'd like to output data to 
// a file, specify the path here

/***********************************************
 * end user spec
 **********************************************/

int nextRefresh;
int arrayLength = windowSpan / refreshRate+1;
float[] InputData = new float[arrayLength];     //we might not need them this big, but
float[] SetpointData = new float[arrayLength];  // this is worst case
float[] OutputData = new float[arrayLength];

int startTime;

float inputTop = 25;
float inputHeight = (windowHeight-70)*2/3;
float outputTop = inputHeight+50;
float outputHeight = (windowHeight-70)*1/3;

float ioLeft = 180, ioWidth = windowWidth-ioLeft-50;
float ioRight = ioLeft+ioWidth;
float pointWidth= (ioWidth)/PApplet.parseFloat(arrayLength-1);

int vertCount = 10;
int nPoints = 0;
float Input, Setpoint, Output;

boolean madeContact =false;

Serial myPort;

ControlP5 controlP5;
controlP5.Button AMButton, DRButton, ATButton, ConnectButton, DisconnectButton, ProfButton, ProfCmd, ProfCmdStop;
controlP5.Textlabel AMLabel, AMCurrent, InLabel, 
OutLabel, SPLabel, PLabel, 
ILabel, DLabel,DRLabel, DRCurrent, ATLabel,
oSLabel,nLabel, ATCurrent, Connecting,lbLabel,
profSelLabel, commconfigLabel1, commconfigLabel2;
RadioButton r1,r2,r3; 
ListBox LBPref;
String[] CommPorts;
String[] prefs;
float[] prefVals;
controlP5.Textfield SPField, InField, OutField, 
PField, IField, DField, oSField, nField,T0Field,
R0Field,BetaField,lbField, oSecField;
String pHold="", iHold="", dHold="";
PrintWriter output;
PFont AxisFont, TitleFont, ProfileFont; 

int dashTop = 200, dashLeft = 10, dashW=160, dashH=180; 
int tuneTop = 30, tuneLeft = 10, tuneW=160, tuneH=180;
int ATTop = 230, ATLeft = 10, ATW=160, ATH=180;
int commTop = 30, commLeft = 10, commW=160, commH=180; 
int configTop = 30, configLeft = 10, configW=160, configH=200;
int RsTop = configTop+2*configH+30, RsLeft = 10, RsW=160, RsH=30;

BufferedReader reader;



public void setup()
{
  size(100, 100);
  frameRate(30);

  //read in preferences
  prefs = new String[] {
    "Form Width", "Form Height", "Input Scale Minimum","Input Scale Maximum","Output Scale Minimum","Output Scale Maximum", "Time Span (Min)"        };   
  prefVals = new float[] {
    windowWidth, windowHeight, InScaleMin, InScaleMax, OutScaleMin, OutScaleMax, windowSpan / 1000 / 60        };
  try
  {
    reader = createReader("prefs.txt");
    if(reader!=null)
    {
      for(int i=0;i<prefVals.length;i++)prefVals[i] = PApplet.parseFloat(reader.readLine());
    } 
  }
  catch(FileNotFoundException  ex)  {    
    println("here2");   
  }
  catch(IOException ex)  {    
    println("here3");   
  }

  PrefsToVals(); //read pref array into global variables

    String curDir = System.getProperty("user.dir");
  ReadProfiles(curDir+"\\profiles");



  controlP5 = new ControlP5(this);                                  // * Initialize the various

    //initialize UI
  createTabs();
  populateDashTab();
  populateTuneTab();
  populateConfigTab();
  populatePrefTab();
  populateProfileTab();

  AxisFont = loadFont("axis.vlw");
  TitleFont = loadFont("Titles.vlw");
  ProfileFont = loadFont("profilestep.vlw");

  //blank out data fields since we're not connected
  Nullify();
  nextRefresh=millis();
  if (outputFileName!="") output = createWriter(outputFileName);

  /*
  CreateUI("IID1", "Tab2",configTop);
    CreateUI("OID1", "Tab2",configTop+configH+15);
*/

}

public void draw()
{
  if(InputCreateReq!="" && InputCard!= InputCreateReq)
  {
    CreateUI(InputCreateReq, "Tab2",configTop);
    InputCreateReq="";
  }
  if(OutputCreateReq!="" && OutputCard!= OutputCreateReq)
  {
    CreateUI(OutputCreateReq,"Tab2",configTop+configH+15);
    OutputCreateReq="";
  }

  ProfileRunTime();

  background(200);

  strokeWeight(1);

  drawButtonArea();
  AdvanceData();
  if(currentTab==5 && curProf>-1)DrawProfile(profs[curProf], ioLeft+4, inputTop, ioWidth-1 , inputHeight);
  else drawGraph();

}


//keeps track of which tab is selected so we know 
//which bounding rectangles to draw
int currentTab=1;
public void controlEvent(ControlEvent theControlEvent) {
  if (theControlEvent.isTab()) { 
    currentTab = theControlEvent.tab().id();
  }
  else if(theControlEvent.isGroup() && theControlEvent.group().name()=="Available Profiles")
  {// a list item was clicked
    curProf=(int)theControlEvent.group().value();
    profSelLabel.setValue(profs[curProf].Name);
  }
}

//puts preference array into the correct fields
public void PopulatePrefVals()
{
  for(int i=0;i<prefs.length;i++)controlP5.controller(prefs[i]).setValueLabel(prefVals[i]+""); 
}

//translates the preferebce array in the corresponding local variables
//and makes any required UI changes
public void PrefsToVals()
{
  windowWidth = PApplet.parseInt(prefVals[0]);
  windowHeight = PApplet.parseInt(prefVals[1]);
  InScaleMin = prefVals[2];
  InScaleMax = prefVals[3];
  OutScaleMin = prefVals[4];
  OutScaleMax = prefVals[5];    
  windowSpan = PApplet.parseInt(prefVals[6] * 1000 * 60);

  inputTop = 25;
  inputHeight = (windowHeight-70)*2/3;
  outputTop = inputHeight+50;
  outputHeight = (windowHeight-70)*1/3;


  ioWidth = windowWidth-ioLeft-50;
  ioRight = ioLeft+ioWidth;

  arrayLength = windowSpan / refreshRate+1;
  InputData = (float[])resizeArray(InputData,arrayLength);
  SetpointData = (float[])resizeArray(SetpointData,arrayLength);
  OutputData = (float[])resizeArray(OutputData,arrayLength);   

  pointWidth= (ioWidth)/PApplet.parseFloat(arrayLength-1);
  resizer(windowWidth, windowHeight);
}


private static Object resizeArray (Object oldArray, int newSize) {
  int oldSize = java.lang.reflect.Array.getLength(oldArray);
  Class elementType = oldArray.getClass().getComponentType();
  Object newArray = java.lang.reflect.Array.newInstance(
  elementType,newSize);
  int preserveLength = Math.min(oldSize,newSize);
  if (preserveLength > 0)
    System.arraycopy (oldArray,0,newArray,0,preserveLength);
  return newArray; 
}

//resizes the form
public void resizer(int w, int h)
{
  size(w,h);
  frame.setSize(w,h+25);
}

public void Save_Preferences()
{
  for(int i=0;i<prefs.length;i++)
  {
    try
    {
      prefVals[i] = PApplet.parseFloat(controlP5.controller(prefs[i]).valueLabel().getText()); 
    }
    catch(Exception ex){
    }
  }
  PrefsToVals();
  PopulatePrefVals(); //in case there was an error we want to put the good values back in

  PrintWriter output;
  try
  {
    output = createWriter("prefs.txt");
    for(int i=0;i<prefVals.length;i++) output.println(prefVals[i]);
    output.flush();
    output.close();
  }
  catch(Exception ex){
  }
}

//puts a "---" into all live fields when we're not connected
boolean dashNull=false, tuneNull=false;
public void Nullify()
{

  String[] names = {
    "AM", "Setpoint", "Input", "Output", "AMCurrent", "SP", "In", "Out", "Kp (Proportional)",
    "Ki (Integral)","Kd (Derivative)","DR","P","I","D","DRCurrent",
    "Noise Band","ATune","oStep","noise","ATuneCurrent","Look Back","lback"  }; //,"  "," ","","Output Step","   ",
  for(int i=0;i<names.length;i++)controlP5.controller(names[i]).setValueLabel("---");
  dashNull=true;tuneNull=true;
}

//draws bounding rectangles based on the selected tab
public void drawButtonArea()
{

  stroke(0);
  fill(100);
  rect(0, 0, ioLeft, windowHeight);
  if(currentTab==1) //dash
  {
    rect(commLeft-5, commTop-5, commW+10, commH+10);
    fill(100,220,100);
    rect(dashLeft-5, dashTop-5, dashW+10, dashH+10);
    
    fill(140);
    rect(configLeft-5, configTop+485, configW+10, 82);
    rect(configLeft+5, configTop+479, 35, 12);

  }
  else if(currentTab==2) //tune
  {
    fill(140);
    rect(tuneLeft-5, tuneTop-5, tuneW+10, tuneH+10);
    fill(120);
    rect(ATLeft-5, ATTop-5, ATW+10, ATH+10);
  }
  else if(currentTab==3) //config
  {
    fill(140);
    if(madeContact)
    {
    rect(configLeft-5, configTop-5, configW+10, configH+10);
    rect(configLeft-5, configTop+configH+10, configW+10, configH+10);
    }
    else rect(configLeft-5, configTop-5, configW+10, 2*configH+20);

  }
  else if(currentTab==5) //profile
  {
      fill(140);
    rect(configLeft-5, configTop+485, configW+10, 82);
    rect(configLeft+5, configTop+479, 35, 12);
    
  }

}

public void Toggle_AM() {
  if(AMLabel.valueLabel().getText()=="Manual") 
  {
    AMLabel.setValue("Automatic");
  }
  else
  {
    AMLabel.setValue("Manual");   
  }
}

public void Toggle_DR() {
  if(DRLabel.valueLabel().getText()=="Direct") 
  {
    DRLabel.setValue("Reverse");
  }
  else
  {
    DRLabel.setValue("Direct");   
  }
}

public void ATune_CMD() {
  if(ATLabel.valueLabel().getText()=="OFF") 
  {
    ATLabel.setValue("ON");
  }
  else
  {
    ATLabel.setValue("OFF");   
  }
}













String LastError="";
public void Connect()
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
          myPort = new Serial(this, CommPorts[i], 9600); 
          myPort.bufferUntil(10); 
          //immediately send a request for osPID type;
          byte[] typeReq = new byte[]{
            0,0                              };
          myPort.write(typeReq);
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
      commconfigLabel1.setVisible(true);
      commconfigLabel2.setVisible(true);
    } 

  }

}

public void Disconnect()
{
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
public void Send_Dash()//To_Controller()
{


  float[] toSend = new float[3];
  toSend[0] = PApplet.parseFloat(SPField.getText());
  toSend[1] = PApplet.parseFloat(InField.getText());
  toSend[2] = PApplet.parseFloat(OutField.getText());

  Byte a = (AMLabel.valueLabel().getText()=="Manual")?(byte)0:(byte)1;
  byte identifier = 1;
  myPort.write(identifier);
  myPort.write(a);
  myPort.write(floatArrayToByteArray(toSend));
} 

public void Send_Tunings()
{
  float[] toSend = new float[3];
  Byte d = (DRLabel.valueLabel().getText()=="Direct")?(byte)0:(byte)1;
  toSend[0] = PApplet.parseFloat(PField.getText());
  toSend[1] = PApplet.parseFloat(IField.getText());
  toSend[2] = PApplet.parseFloat(DField.getText());
  byte identifier = 2;
  myPort.write(identifier);
  myPort.write(d);
  myPort.write(floatArrayToByteArray(toSend));
}

public void Send_Auto_Tune()
{
  float[] toSend = new float[3];
  Byte d = (ATLabel.valueLabel().getText()=="OFF")?(byte)0:(byte)1;
  toSend[0] = PApplet.parseFloat(oSField.getText());
  toSend[1] = PApplet.parseFloat(nField.getText());
  toSend[2] = PApplet.parseFloat(lbField.getText());
  byte identifier = 3;
  myPort.write(identifier);
  myPort.write(d);
  myPort.write(floatArrayToByteArray(toSend));
}

public void Send_Configuration()//To_Controller()
{

  float[] toSend = new float[4];
  toSend[0] = PApplet.parseFloat(R0Field.getText());
  toSend[1] = PApplet.parseFloat(BetaField.getText());
  toSend[2] = PApplet.parseFloat(T0Field.getText());
  toSend[3] = PApplet.parseFloat(oSecField.getText());

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
public void Run_Profile()
{

  byte[] toSend = new byte[2];
  toSend[0]=8;
  toSend[1]=1;
  myPort.write(toSend);
}
public void Stop_Profile()
{
  byte[] toSend = new byte[2];
  toSend[0]=8;
  toSend[1]=0;
  myPort.write(toSend);
}

public void Send_Profile()
{
  currentxferStep=0;
  SendProfileStep(PApplet.parseByte(currentxferStep));
}
int currentxferStep=-1;

public void SendProfileStep(byte step)
{
  byte identifier=7;
  Profile p = profs[curProf];
  float[] temp = new float[2];
  temp[0] = p.vals[step];
  temp[1] = p.times[step];

  byte[] toSend = new byte[11];
  toSend[0]=identifier;
  toSend[1]=step;
  toSend[2]=p.types[step];
  arraycopy(floatArrayToByteArray(temp),0,toSend,3,8);
  myPort.write(toSend);

}

public void SendProfileName()
{
  byte identifier=7;


  byte[] toSend = new byte[9];

  toSend[0] = identifier;
  toSend[1] = PApplet.parseByte(currentxferStep);
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
}

public void Reset_Factory_Defaults()
{
  byte identifier = 4;
  myPort.write(identifier);
  myPort.write((byte)1); 
}

public byte[] floatArrayToByteArray(float[] input)
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

public byte[] intArrayToByteArray(int[] input)
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

public float unflip(float thisguy)
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
public void serialEvent(Serial myPort)
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

    Setpoint = PApplet.parseFloat(s[1]);
    Input = PApplet.parseFloat(s[2]);
    Output = PApplet.parseFloat(s[3]);  
    SPLabel.setValue(s[1]);           //   where it's needed
    InLabel.setValue(s[2]);           //
    OutLabel.setValue(s[3]);  
    AMCurrent.setValue(PApplet.parseInt(s[4]) == 1 ? "Automatic" : "Manual");    
    //if(SPField.valueLabel().equals("---"))
    if(dashNull || PApplet.parseInt(trim(s[5]))==1)
    {

      dashNull=false;
      SPField.setText(s[1]);    //   the arduino,  take the
      InField.setText(s[2]);    //   current values and put
      OutField.setText(s[3]);
      AMLabel.setValue(PApplet.parseInt(s[4]) == 1 ? "Automatic" : "Manual");   
    }
  }
  else if(s.length==10 && s[0].equals("TUNE"))
  {
    PLabel.setValue(s[1]);
    ILabel.setValue(s[2]);
    DLabel.setValue(s[3]);
    DRCurrent.setValue(PApplet.parseInt(s[4]) == 1 ? "Reverse" : "Direct");
    ATCurrent.setValue(PApplet.parseInt(s[5])==1? "ATune On" : "ATune Off");
    oSLabel.setValue(s[6]);
    nLabel.setValue(s[7]);
    lbLabel.setValue(trim(s[8]));
    if(tuneNull || PApplet.parseInt(trim(s[9]))==1)
    {
      tuneNull=false;
      PField.setText(s[1]);    //   the arduino,  take the
      IField.setText(s[2]);    //   current values and put
      DField.setText(s[3]);
      DRLabel.setValue(PApplet.parseInt(s[4]) == 1 ? "Reverse" : "Direct");  
      oSField.setText(s[6]);
      nField.setText(s[7]);
      lbField.setValue(s[8]);    
      ATLabel.setValue(PApplet.parseInt(s[5])==1? "ON" : "OFF");
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
    int curType = PApplet.parseInt(trim(s[2]));
    curProfStep = PApplet.parseInt(s[1]);
    ProfCmd.setVisible(false);
    ProfCmdStop.setVisible(true);
    String[] msg;
    switch(curType)
    {
    case 1: //ramp
      msg = new String[]{
        "Running Profile", "", "Step="+s[1]+", Ramping Setpoint", PApplet.parseFloat(trim(s[3]))/1000+" Sec remaining"            };
      break;
    case 2: //wait
      float helper = PApplet.parseFloat(trim(s[4]));
      msg = new String[]{
        "Running Profile", "","Step="+s[1]+", Waiting","Distance Away= "+s[3],(helper<0? "Waiting for cross" :("Time in band= "+helper/1000+" Sec" ))            };
      break;
    case 3: //step
      msg = new String[]{
        "Running Profile", "","Step="+s[1]+", Stepped Setpoint"," Waiting for "+ PApplet.parseFloat(trim(s[3]))/1000+" Sec"            };
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
    currentxferStep = PApplet.parseInt(s[1])+1;
    if(currentxferStep<pSteps) SendProfileStep(PApplet.parseByte(currentxferStep));
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

public void poulateStat(String[] msg)
{
  for(int i=0;i<6;i++)
  {
    ((controlP5.Textlabel)controlP5.controller("dashstat"+i)).setValue(i<msg.length?msg[i]:"");
    ((controlP5.Textlabel)controlP5.controller("profstat"+i)).setValue(i<msg.length?msg[i]:"");
  }
}



public void AdvanceData()
{
  // add the latest data to the data Arrays.  
  if(millis() > nextRefresh && madeContact)
  {
    nextRefresh  = millis()+ refreshRate;

    for(int i=nPoints-1;i>0;i--)
    {
      InputData[i]=InputData[i-1];
      SetpointData[i]=SetpointData[i-1];
      OutputData[i]=OutputData[i-1];
    }
    if (nPoints < arrayLength) nPoints++;

    InputData[0] = Input;
    SetpointData[0] = Setpoint;
    OutputData[0] = Output;
  }  
}


public void drawGraph()
{
  //draw Base, gridlines
  stroke(0);
  fill(230);
  rect(ioLeft, inputTop,ioWidth-1 , inputHeight);
  rect(ioLeft, outputTop, ioWidth-1, outputHeight);
  stroke(210);

  //Section Titles
  textFont(TitleFont);
  fill(255);
  text("PID Input / Setpoint",(int)ioLeft+10,(int)inputTop-5);
  text("PID Output",(int)ioLeft+10,(int)outputTop-5);

  if(!madeContact) return;

  //GridLines and Titles
  textFont(AxisFont);

  //horizontal grid lines
  int interval = (int)inputHeight/5;
  for(int i=0;i<6;i++)
  {
    if(i>0&&i<5) line(ioLeft+1,inputTop+i*interval,ioRight-2,inputTop+i*interval);
    text(str((InScaleMax-InScaleMin)/5*(float)(5-i)+InScaleMin),ioRight+5,inputTop+i*interval+4);

  }
  interval = (int)outputHeight/5;
  for(int i=0;i<6;i++)
  {
    if(i>0&&i<5) line(ioLeft+1,outputTop+i*interval,ioRight-2,outputTop+i*interval);
    text(str((OutScaleMax-OutScaleMin)/5*(float)(5-i)+OutScaleMin),ioRight+5,outputTop+i*interval+4);
  }

  //vertical grid lines and TimeStamps
  int elapsedTime = millis()-startTime;
  interval = (int)ioWidth / vertCount;
  int shift = elapsedTime*(int)ioWidth / windowSpan;
  shift %=interval;

  int iTimeInterval = windowSpan/vertCount;
  float firstDisplay = (float)(iTimeInterval*(elapsedTime/iTimeInterval))/displayFactor;
  float timeInterval = (float)(iTimeInterval)/displayFactor;
  for(int i=0;i<vertCount;i++)
  {
    int x = (int)ioRight-shift-2-i*interval;

    line(x,inputTop+1,x,inputTop+inputHeight-1);
    line(x,outputTop+1,x,outputTop+outputHeight-1);    

    float t = firstDisplay-(float)i*timeInterval;
    if(t>=0)  text(str(t),x,outputTop+outputHeight+10);
  }



  
  AdvanceData();
  
  //draw lines for the input, setpoint, and output
  strokeWeight(4);
  for(int i=0; i<nPoints-2; i++)
  {
    int X1 = PApplet.parseInt(ioRight-2-PApplet.parseFloat(i)*pointWidth);
    int X2 = PApplet.parseInt(ioRight-2-PApplet.parseFloat(i+1)*pointWidth);
    boolean y1Above, y1Below, y2Above, y2Below;


    //DRAW THE INPUT
    boolean drawLine=true;
    stroke(255,0,0);
    int Y1 = PApplet.parseInt(inputHeight)-PApplet.parseInt(inputHeight*(InputData[i]-InScaleMin)/(InScaleMax-InScaleMin)); //InputData[i];
    int Y2 = PApplet.parseInt(inputHeight)-PApplet.parseInt(inputHeight*(InputData[i+1]-InScaleMin)/(InScaleMax-InScaleMin)); //InputData[i+1];

    y1Above = (Y1>inputHeight);                     // if both points are outside 
    y1Below = (Y1<0);                               // the min or max, don't draw the 
    y2Above = (Y2>inputHeight);                     // line.  if only one point is 
    y2Below = (Y2<0);                               // outside constrain it to the limit, 
    if(y1Above)                                     // and leave the other one untouched.
    {                                               //
      if(y2Above) drawLine=false;                   //
      else if(y2Below) {                            //
        Y1 = (int)inputHeight;                      //
        Y2 = 0;                                     //
      }                                             //
      else Y1 = (int)inputHeight;                   //
    }                                               //
    else if(y1Below)                                //
    {                                               //
      if(y2Below) drawLine=false;                   //
      else if(y2Above) {                            //
        Y1 = 0;                                     //
        Y2 = (int)inputHeight;                      //
      }                                             //
      else Y1 = 0;                                  //
    }                                               //
    else                                            //
    {                                               //
      if(y2Below) Y2 = 0;                           //
      else if(y2Above) Y2 = (int)inputHeight;       //
    }                                               //

    if(drawLine)
    {
      line(X1,Y1+inputTop, X2, Y2+inputTop);
    }

    //DRAW THE SETPOINT
    drawLine=true;
    stroke(0,255,0);
    Y1 = PApplet.parseInt(inputHeight)-PApplet.parseInt(inputHeight*(SetpointData[i]-InScaleMin)/(InScaleMax-InScaleMin));// SetpointData[i];
    Y2 = PApplet.parseInt(inputHeight)-PApplet.parseInt(inputHeight*(SetpointData[i+1]-InScaleMin)/(InScaleMax-InScaleMin)); //SetpointData[i+1];

    y1Above = (Y1>(int)inputHeight);                // if both points are outside 
    y1Below = (Y1<0);                               // the min or max, don't draw the 
    y2Above = (Y2>(int)inputHeight);                // line.  if only one point is 
    y2Below = (Y2<0);                               // outside constrain it to the limit, 
    if(y1Above)                                     // and leave the other one untouched.
    {                                               //
      if(y2Above) drawLine=false;                   //
      else if(y2Below) {                            //
        Y1 = (int)(inputHeight);                    //
        Y2 = 0;                                     //
      }                                             //
      else Y1 = (int)(inputHeight);                 //
    }                                               //
    else if(y1Below)                                //
    {                                               //
      if(y2Below) drawLine=false;                   //
      else if(y2Above) {                            //
        Y1 = 0;                                     //
        Y2 = (int)(inputHeight);                    //
      }                                             //
      else Y1 = 0;                                  //
    }                                               //
    else                                            //
    {                                               //
      if(y2Below) Y2 = 0;                           //
      else if(y2Above) Y2 = (int)(inputHeight);     //
    }                                               //

    if(drawLine)
    {
      line(X1, Y1+inputTop, X2, Y2+inputTop);
    }

    //DRAW THE OUTPUT
    drawLine=true;
    stroke(0,0,255);
    Y1 = PApplet.parseInt(outputHeight)-PApplet.parseInt(outputHeight*(OutputData[i]-OutScaleMin)/(OutScaleMax-OutScaleMin));// OutputData[i];
    Y2 = PApplet.parseInt(outputHeight)-PApplet.parseInt(outputHeight*(OutputData[i+1]-OutScaleMin)/(OutScaleMax-OutScaleMin));//OutputData[i+1];

    y1Above = (Y1>outputHeight);                   // if both points are outside 
    y1Below = (Y1<0);                              // the min or max, don't draw the 
    y2Above = (Y2>outputHeight);                   // line.  if only one point is 
    y2Below = (Y2<0);                              // outside constrain it to the limit, 
    if(y1Above)                                    // and leave the other one untouched.
    {                                              //
      if(y2Above) drawLine=false;                  //
      else if(y2Below) {                           //
        Y1 = (int)outputHeight;                    //
        Y2 = 0;                                    //
      }                                            //
      else Y1 = (int)outputHeight;                 //
    }                                              //
    else if(y1Below)                               //
    {                                              //
      if(y2Below) drawLine=false;                  //
      else if(y2Above) {                           //
        Y1 = 0;                                    //
        Y2 = (int)outputHeight;                    //
      }                                            //  
      else Y1 = 0;                                 //
    }                                              //
    else                                           //
    {                                              //
      if(y2Below) Y2 = 0;                          //
      else if(y2Above) Y2 = (int)outputHeight;     //
    }                                              //

    if(drawLine)
    {
      line(X1, outputTop + Y1, X2, outputTop + Y2);
    }
  }
  strokeWeight(1);
}
RadioButton protIr1, protIr2, protIr3, protIr4;
Textfield protItxt1, protItxt2, protItxt3, protItxt4;

RadioButton protOr1, protOr2, protOr3, protOr4;
Textfield protOtxt1, protOtxt2, protOtxt3, protOtxt4;

String InputCard="", OutputCard="";
ArrayList InputControls = new ArrayList(), OutputControls=new ArrayList(); //in case we need to kill them mid-run

public void ClearInput()
{
  for(int i=0;i<InputControls.size();i++)
  {
    if(InputControls.get(i).getClass().equals(controlP5.RadioButton.class))
    {
      ((ControllerGroup)InputControls.get(i)).remove();
    }
    else ((Controller)InputControls.get(i)).remove();
  }  
    InputCard = "";
}
public void ClearOutput()
{
  for(int i=0;i<OutputControls.size();i++)
  {
    if(OutputControls.get(i).getClass().equals(controlP5.RadioButton.class))
    {
      ((ControllerGroup)OutputControls.get(i)).remove();
    }
    else ((Controller)OutputControls.get(i)).remove();
  }
    OutputCard = "";
}

public void CreateUI(String cardID, String tab, int top)
{
  if(cardID.equals("IID1"))
  {

    ClearInput();
    InputCard = "IID1";
    InputControls.clear();


    controlP5.addTextlabel("spec0","Specify which input to use: ", configLeft,top);

    r2 = controlP5.addRadioButton("radioButton2",configLeft,top+22);
    r2.setColorForeground(color(120));
    r2.setColorActive(color(255));
    r2.setColorLabel(color(255));
    r2.setItemsPerRow(1);
    r2.setSpacingColumn(75);
    addToRadioButton(r2,"Thermocouple",1);
    addToRadioButton(r2,"Thermistor",2);
    r2.getItem(0).setState(true);

    controlP5.addTextlabel("spec1","Thermistor Coefficients: ", configLeft,top+70);
    controlP5.addTextlabel("T","T    =", configLeft+5,top+90);
    controlP5.addTextlabel("00","0", configLeft+10,top+95);
    controlP5.addTextlabel("R","R    =", configLeft+5,top+115);
    controlP5.addTextlabel("01","0", configLeft+12,top+120);
    controlP5.addTextlabel("Beta","Beta =", configLeft+5,top+140);
    T0Field= controlP5.addTextfield("",configLeft+45,top+84,60,20);         //   Buttons, Labels, and
    R0Field = controlP5.addTextfield(" ",configLeft+45,top+109,60,20);           //   Text Fields we'll be
    BetaField = controlP5.addTextfield("  ",configLeft+45,top+136,60,20);         //   using
    controlP5.addButton("Send_Input_Config",0.0f,configLeft,top+180,160,20);

    String[] names = {
      "spec1", "T", "00", "R", "01", "Beta", "spec0", "Send_Input_Config"                              };
    for(int i=0;i<names.length;i++)
    {
      controlP5.controller(names[i]).moveTo(tab);
      InputControls.add(controlP5.controller(names[i])); 
    }

    r2.moveTo(tab); 
    T0Field.moveTo(tab); 
    R0Field.moveTo(tab); 
    BetaField.moveTo(tab);  
    InputControls.add(r2); 
    InputControls.add(T0Field); 
    InputControls.add(R0Field); 
    InputControls.add(BetaField); 

  }
  else if(cardID.equals("OID1"))
  {
 
    ClearOutput();
    OutputCard = "OID1";   
    controlP5.addTextlabel("spec3","Specify which output to use: ", configLeft,top);                  

    r3 = controlP5.addRadioButton("radioButton3",configLeft,top+22);
    r3.setColorForeground(color(120));
    r3.setColorActive(color(255));
    r3.setColorLabel(color(255));
    r3.setItemsPerRow(1);
    r3.setSpacingColumn(75);

    addToRadioButton(r3,"Onboard Relay",1);
    addToRadioButton(r3,"Digital Output",2);
    r3.getItem(0).setState(true);

    controlP5.addTextlabel("spec2","Relay Output Window: ", configLeft,top+56);  
    controlP5.addTextlabel("sec","Seconds = ", configLeft+5,top+76);  
    oSecField = controlP5.addTextfield("   ",configLeft+55,top+70,50,20);
    controlP5.addButton("Send_Output_Config",0.0f,configLeft,top+180,160,20);         //    
    String[] names = {
      "spec2","spec3","sec","   ", "Send_Output_Config"                              };
    for(int i=0;i<names.length;i++)
    {
      controlP5.controller(names[i]).moveTo(tab);
      OutputControls.add(controlP5.controller(names[i]));
    }
    r3.moveTo(tab);  
    OutputControls.add(r3);
  }
  else if(cardID.equals("IID0"))
  {

    ClearInput();
    InputCard = "IID0";
    InputControls.clear();

    protIr1 = controlP5.addRadioButton("protIr1",configLeft,top);
    protIr1.setColorForeground(color(120));
    protIr1.setColorActive(color(255));
    protIr1.setColorLabel(color(255));
    protIr1.setItemsPerRow(2);
    protIr1.setSpacingColumn(75);
    addToRadioButton(protIr1,"On 1",1);
    addToRadioButton(protIr1,"Off 1",2);
    protIr1.getItem(0).setState(true);
       protIr1.getItem(0).captionLabel().style().backgroundWidth = 45;
       protIr1.getItem(1).captionLabel().style().backgroundWidth = 45;
       
    protIr2 = controlP5.addRadioButton("protIr2",configLeft,top+18);
    protIr2.setColorForeground(color(120));
    protIr2.setColorActive(color(255));
    protIr2.setColorLabel(color(255));
    protIr2.setItemsPerRow(2);
    protIr2.setSpacingColumn(75);
    addToRadioButton(protIr2,"On 2",1);
    addToRadioButton(protIr2,"Off 2",2);
    protIr2.getItem(0).setState(true);
       protIr2.getItem(0).captionLabel().style().backgroundWidth = 45;
       protIr2.getItem(1).captionLabel().style().backgroundWidth = 45;

    
    protIr3 = controlP5.addRadioButton("protIr3",configLeft,top+36);
    protIr3.setColorForeground(color(120));
    protIr3.setColorActive(color(255));
    protIr3.setColorLabel(color(255));
    protIr3.setItemsPerRow(2);
    protIr3.setSpacingColumn(75);
    addToRadioButton(protIr3,"On 3",1);
    addToRadioButton(protIr3,"Off 3",2);
    protIr3.getItem(0).setState(true);
       protIr3.getItem(0).captionLabel().style().backgroundWidth = 45;
       protIr3.getItem(1).captionLabel().style().backgroundWidth = 45;
       
    protIr4 = controlP5.addRadioButton("protIr4",configLeft,top+54);
    protIr4.setColorForeground(color(120));
    protIr4.setColorActive(color(255));
    protIr4.setColorLabel(color(255));
    protIr4.setItemsPerRow(2);
    protIr4.setSpacingColumn(75);
    addToRadioButton(protIr4,"On 4",1);
    addToRadioButton(protIr4,"Off 4",2);
    protIr4.getItem(0).setState(true);
           protIr4.getItem(0).captionLabel().style().backgroundWidth = 45;
       protIr4.getItem(1).captionLabel().style().backgroundWidth = 45;
       
       controlP5.addButton("Send_Input_Config",0.0f,configLeft,top+180,160,20);
   
   protItxt1 = controlP5.addTextfield("      ",configLeft+85,top+74,60,20);
   protItxt2 = controlP5.addTextfield("       ",configLeft+85,top+99,60,20);
   protItxt3 = controlP5.addTextfield("        ",configLeft+85,top+124,60,20);
   protItxt4 = controlP5.addTextfield("         ",configLeft+85,top+149,60,20);
       
    controlP5.addTextlabel("protIlbl1", "Float 1", configLeft, top+79);
    controlP5.addTextlabel("protIlbl2", "Float 2", configLeft, top+104);
    controlP5.addTextlabel("protIlbl3", "Float 3", configLeft, top+129);
    controlP5.addTextlabel("protIlbl4", "Float 4", configLeft, top+154);    

    String[] names = {"Send_Input_Config", "protIlbl1"  
, "protIlbl2", "protIlbl3", "protIlbl4"      };
    for(int i=0;i<names.length;i++)
    {
      controlP5.controller(names[i]).moveTo(tab);
      InputControls.add(controlP5.controller(names[i])); 
    }

    protIr1.moveTo(tab); 
    protIr2.moveTo(tab); 
    protIr3.moveTo(tab); 
    protIr4.moveTo(tab);  
    protItxt1.moveTo(tab);
    protItxt2.moveTo(tab);
    protItxt3.moveTo(tab);
    protItxt4.moveTo(tab);
    InputControls.add(protIr1); 
    InputControls.add(protIr2); 
    InputControls.add(protIr3); 
    InputControls.add(protIr4); 
    InputControls.add(protItxt1); 
    InputControls.add(protItxt2);
    InputControls.add(protItxt3);
    InputControls.add(protItxt4);

  }
else if(cardID.equals("OID0"))
  {

    ClearOutput();
    OutputCard = "OID0";
    OutputControls.clear();

    protOr1 = controlP5.addRadioButton("protOr1",configLeft,top);
    protOr1.setColorForeground(color(120));
    protOr1.setColorActive(color(255));
    protOr1.setColorLabel(color(255));
    protOr1.setItemsPerRow(2);
    protOr1.setSpacingColumn(75);
    addToRadioButton(protOr1,"On 1",1);
    addToRadioButton(protOr1,"Off 1",2);
    protOr1.getItem(0).setState(true);
       protOr1.getItem(0).captionLabel().style().backgroundWidth = 45;
       protOr1.getItem(1).captionLabel().style().backgroundWidth = 45;
       
    protOr2 = controlP5.addRadioButton("protOr2",configLeft,top+18);
    protOr2.setColorForeground(color(120));
    protOr2.setColorActive(color(255));
    protOr2.setColorLabel(color(255));
    protOr2.setItemsPerRow(2);
    protOr2.setSpacingColumn(75);
    addToRadioButton(protOr2,"On 2",1);
    addToRadioButton(protOr2,"Off 2",2);
    protOr2.getItem(0).setState(true);
       protOr2.getItem(0).captionLabel().style().backgroundWidth = 45;
       protOr2.getItem(1).captionLabel().style().backgroundWidth = 45;

    
    protOr3 = controlP5.addRadioButton("protOr3",configLeft,top+36);
    protOr3.setColorForeground(color(120));
    protOr3.setColorActive(color(255));
    protOr3.setColorLabel(color(255));
    protOr3.setItemsPerRow(2);
    protOr3.setSpacingColumn(75);
    addToRadioButton(protOr3,"On 3",1);
    addToRadioButton(protOr3,"Off 3",2);
    protOr3.getItem(0).setState(true);
       protOr3.getItem(0).captionLabel().style().backgroundWidth = 45;
       protOr3.getItem(1).captionLabel().style().backgroundWidth = 45;
       
    protOr4 = controlP5.addRadioButton("protOr4",configLeft,top+54);
    protOr4.setColorForeground(color(120));
    protOr4.setColorActive(color(255));
    protOr4.setColorLabel(color(255));
    protOr4.setItemsPerRow(2);
    protOr4.setSpacingColumn(75);
    addToRadioButton(protOr4,"On 4",1);
    addToRadioButton(protOr4,"Off 4",2);
    protOr4.getItem(0).setState(true);
           protOr4.getItem(0).captionLabel().style().backgroundWidth = 45;
       protOr4.getItem(1).captionLabel().style().backgroundWidth = 45;
       
       controlP5.addButton("Send_Output_Config",0.0f,configLeft,top+180,160,20);
   
   protOtxt1 = controlP5.addTextfield("          ",configLeft+85,top+74,60,20);
   protOtxt2 = controlP5.addTextfield("           ",configLeft+85,top+99,60,20);
   protOtxt3 = controlP5.addTextfield("            ",configLeft+85,top+124,60,20);
   protOtxt4 = controlP5.addTextfield("             ",configLeft+85,top+149,60,20);
       
    controlP5.addTextlabel("protOlbl1", "Float 1", configLeft, top+79);
    controlP5.addTextlabel("protOlbl2", "Float 2", configLeft, top+104);
    controlP5.addTextlabel("protOlbl3", "Float 3", configLeft, top+129);
    controlP5.addTextlabel("protOlbl4", "Float 4", configLeft, top+154);    

    String[] names = {"Send_Output_Config", "protOlbl1"  
, "protOlbl2", "protOlbl3", "protOlbl4"      };
    for(int i=0;i<names.length;i++)
    {
      controlP5.controller(names[i]).moveTo(tab);
      OutputControls.add(controlP5.controller(names[i])); 
    }

    protOr1.moveTo(tab); 
    protOr2.moveTo(tab); 
    protOr3.moveTo(tab); 
    protOr4.moveTo(tab);  
    protOtxt1.moveTo(tab);
    protOtxt2.moveTo(tab);
    protOtxt3.moveTo(tab);
    protOtxt4.moveTo(tab);
    OutputControls.add(protOr1); 
    OutputControls.add(protOr2); 
    OutputControls.add(protOr3); 
    OutputControls.add(protOr4); 
    OutputControls.add(protOtxt1); 
    OutputControls.add(protOtxt2);
    OutputControls.add(protOtxt3);
    OutputControls.add(protOtxt4);
                
  }
}

public void PopulateCardFields(String cardName, String[] fields)
{
  if(cardName.equals("IID1"))
  {
    int v = PApplet.parseInt(fields[1]); 
    if(v==0) r2.getItem(0).setState(true);
    else if(v==1) r2.getItem(1).setState(true);

    R0Field.setText(fields[2]);    //   the arduino,  take the
    BetaField.setText(fields[3]);    //   current values and put
    T0Field.setText(fields[4]);

  }
  else if(cardName.equals("IID0"))
  {
    int v1 = PApplet.parseInt(fields[1]); 
    int v2 = PApplet.parseInt(fields[2]); 
    int v3 = PApplet.parseInt(fields[3]); 
    int v4 = PApplet.parseInt(fields[4]); 
    protIr1.getItem(v1==1?0:1).setState(true);
    protIr2.getItem(v2==1?0:1).setState(true);
    protIr3.getItem(v3==1?0:1).setState(true);
    protIr4.getItem(v4==1?0:1).setState(true);
  
    protItxt1.setText(fields[5]);
    protItxt2.setText(fields[6]);
    protItxt3.setText(fields[7]);
    protItxt4.setText(fields[8]);    

  }
  else if(cardName.equals("OID1"))
  {
    int v = PApplet.parseInt(fields[1]);
    if(v==0) r3.getItem(0).setState(true);
    else r3.getItem(1).setState(true);
    oSecField.setText(fields[2]);
  }
  else if(cardName.equals("OID0"))
  {
    int v1 = PApplet.parseInt(fields[1]); 
    int v2 = PApplet.parseInt(fields[2]); 
    int v3 = PApplet.parseInt(fields[3]); 
    int v4 = PApplet.parseInt(fields[4]); 
    protOr1.getItem(v1==1?0:1).setState(true);
    protOr2.getItem(v2==1?0:1).setState(true);
    protOr3.getItem(v3==1?0:1).setState(true);
    protOr4.getItem(v4==1?0:1).setState(true);
  
    protOtxt1.setText(fields[5]);
    protOtxt2.setText(fields[6]);
    protOtxt3.setText(fields[7]);
    protOtxt4.setText(fields[8]);    

  }
}


public void Send_Input_Config()
{  //build the send string for the appropriate input card

  if(InputCard.equals("IID1"))
  {
    myPort.write(PApplet.parseByte(5));
    Byte a =0;
    if(r2.getState(1)==true)a=1;
    myPort.write(a);

    myPort.write(floatArrayToByteArray(new float[]{
      PApplet.parseFloat(R0Field.getText()),
      PApplet.parseFloat(BetaField.getText()), 
      PApplet.parseFloat(T0Field.getText()),
      PApplet.parseFloat(R0Field.getText()),//hidden reference resistance
    }
    ));
  }
  else if(InputCard.equals("IID0"))
  {
    myPort.write(PApplet.parseByte(5));
    Byte a =0;
    myPort.write( protIr1.getState(0)==true? (byte) 0 : (byte)1);
    myPort.write( protIr2.getState(0)==true? (byte) 0 : (byte)1);
    myPort.write( protIr3.getState(0)==true? (byte) 0 : (byte)1);
    myPort.write( protIr4.getState(0)==true? (byte) 0 : (byte)1);    
    
    myPort.write(floatArrayToByteArray(new float[]{
      PApplet.parseFloat(protItxt1.getText()),
      PApplet.parseFloat(protItxt2.getText()), 
      PApplet.parseFloat(protItxt3.getText()),
      PApplet.parseFloat(protItxt4.getText()),
    }
    ));
  }
}

public void Send_Output_Config()
{
  byte[] toSend;
  if(OutputCard.equals("OID1"))
  {
    myPort.write(PApplet.parseByte(6));
    byte o = r3.getState(0)==true ? (byte)0 : (byte)1;

    myPort.write(o);
    myPort.write(floatArrayToByteArray(new float[]{
      PApplet.parseFloat(oSecField.getText())                }
    ));
  }
  else if(OutputCard.equals("OID0"))
  {
    myPort.write(PApplet.parseByte(6));

    myPort.write( protOr1.getState(0)==true? (byte) 0 : (byte)1);
    myPort.write( protOr2.getState(0)==true? (byte) 0 : (byte)1);
    myPort.write( protOr3.getState(0)==true? (byte) 0 : (byte)1);
    myPort.write( protOr4.getState(0)==true? (byte) 0 : (byte)1);    
    
    myPort.write(floatArrayToByteArray(new float[]{
      PApplet.parseFloat(protOtxt1.getText()),
      PApplet.parseFloat(protOtxt2.getText()), 
      PApplet.parseFloat(protOtxt3.getText()),
      PApplet.parseFloat(protOtxt4.getText()),
    }
    ));
  }
}








int pSteps=15;

class Profile
{
  public float times[] = new float[pSteps];
  public float vals[] = new float[pSteps];
  public byte types[] = new byte[pSteps];
  public String errorMsg="";
  public String Name="";
}
Profile profs[];
int curProf=-1;

int lastReceiptTime=-1000;
String profname="";
int curProfStep=-1;


public void ProfileRunTime()
{

  if (lastReceiptTime+3000<millis())
  {
    for(int i=0;i<6;i++)
    { 
      ((controlP5.Textlabel)controlP5.controller("profstat"+i)).setValue("");
      ((controlP5.Textlabel)controlP5.controller("dashstat"+i)).setValue("");
      curProfStep=-1;
          ProfCmd.setVisible(true);
    ProfCmdStop.setVisible(false);
    } 

  }
}


public void ReadProfiles(String directory)
{
  //get all text files in the directory 
  String[] files = listFileNames(directory);
  profs = new Profile[files.length];
  for(int i=0;i<files.length;i++)
  {
    profs[i] = CreateProfile(directory+"\\"+files[i]); 
  }
  if(profs.length>0)curProf=0;
}

public String[] listFileNames(String dir) {
  File file = new File(dir);
  if (file.isDirectory()) {
    String names[] = file.list();
    return names;
  } 
  else {
    // If it's not a directory
    return null;
  }
}


public Profile CreateProfile(String filename)
{
  BufferedReader reader = createReader(filename);
  String ln=null;
  int count=0;
  Profile ret = new Profile();
  while (count==0 || (ln!=null && count-1<pSteps))
  {
    try {
      ln = reader.readLine();
    } 
    catch (IOException e) {
      e.printStackTrace();
      ln = null;
    }
    if (ln != null) {
      //pull the commands from this line.  if there's an error, record and leave
      try
      {
        int ind = ln.indexOf("//");
        if(ind>0) ln = trim(ln.substring(0,ind));
        
        if(count==0) ret.Name = (ln.length()<7)? ln : ln.substring(0,7);
        else
        {

          String s[] = split(ln, ','); 
          byte t = (byte)PApplet.parseInt(trim(s[0]));
          float v = PApplet.parseFloat(trim(s[1]));
          float time = PApplet.parseFloat(trim(s[2]));
          //int time = int(trim(s[2]));
          ret.types[count-1] = t;
          ret.vals[count-1] = v;
          ret.times[count-1] = time;
          if(time<0)ret.errorMsg = "Time cannot be negative";
          else if(t==2 && v<0)ret.errorMsg = "Wait Band cannot be negative";
          else if(t<0 || t>3)ret.errorMsg = "Unrecognized step type";

          if(ret.errorMsg!="") ret.errorMsg = "Error on line "+ (count+1)+". "+ret.errorMsg;
        }
      }
      catch(Exception ex)
      {
        if(ret.times[count]<0)ret.errorMsg = "Error on line "+ (count+1)+ ". "+ex.getMessage();      
      }
      println(ret.errorMsg);
      if(ret.errorMsg!="") return ret;
    }
    count++;
  } 
  return ret;
}


public void DrawProfile(Profile p, float x, float y, float w, float h)
{
  //if(p==null)return; 
  float step = w/(float)pSteps;
textFont(AxisFont);
  //scan for the minimum and maximum
  float minimum = 100000000,maximum=-10000000;
  for(int i=0;i<pSteps;i++)
  {
    byte t = p.types[i];
    if(t==1 || t== 3)
    {
      float v = p.vals[i];
      if(v<minimum)minimum=v;
      if(v>maximum)maximum=v;
    }
  }
  if (minimum==maximum)
  {
    minimum-=1;
    maximum+=1;
  }

  float bottom = y + h;
  
  strokeWeight(4);
  float lasty = bottom-h/2;
  for(int i=0;i<pSteps;i++)
  {

    if(i==curProfStep && (millis() % 2000<1000))stroke(255,0,0);
    else stroke(255);
    
    byte t = p.types[i];
    float v = bottom - (p.vals[i]-minimum)/(maximum-minimum) * h;
    float x1 = x+step*(float)i;
    float x2 = x+step*(float)(i+1);
    if(t==1)//Ramp
    {
      line(x1,lasty, x2,v);
      text(p.vals[i],x2,v);
      lasty=v;
    }
    else if(t==2)//Wait
    {    
      strokeWeight(8);
      line(x1,lasty, x2,lasty);        
      strokeWeight(4);
    }
    else if(t==3)//Step
    {
      line(x1,lasty, x1,v);
      line(x1,v, x2,v);
      lasty=v;
      text(p.vals[i],x1,lasty);
    }
    else if(t==0)
    {
      //if 0 do nothing
    }
    else
    { //unrecognized, this is a problem
      break;
    }
  }

  fill(0);
   
  rotate(-90*PI/180);
  float lastv = 999;
  for(int i=0;i<pSteps; i++)
  {
    byte t = p.types[i];
    float v = p.vals[i];
    String s1="",s2="", s3="";

    if(t==0)//end
    {
break;
    }
    if(t==1)//Ramp
    {
      s1 = "Ramp SP to " + v; 
      s2 = "Over " + p.times[i] + " Sec";
    }
    else if (t==2 && v==0) //Wait cross
    {
      s1 = "Wait until Input"; 
      s2 = "Crosses " + lastv;
    }
    else if(t==2)
    {
      s1 = "Wait until Input is"; 
      s2="Within "+v+ " of " + lastv;
      s3= "for "+p.times[i] +" Sec";
    }
    else if(t==3)
    {
      s1 = "Step SP to "+ v +" then"; 
      s2="wait " + p.times[i] + " Sec";
    }
    else
    { //unrecognized
      break;
    }

    if(s1!="")
    {
      text(s1, -(outputTop+outputHeight-30), x+i*step+10);
      text(s2, -(outputTop+outputHeight-30), x+i*step+20);
      text(s3, -(outputTop+outputHeight-30), x+i*step+30);
    }
    lastv = v;
  }

  rotate(90*PI/180);

  textFont(ProfileFont);
  for(int i=0;i<pSteps; i++)
  {
    byte t = p.types[i];
    if(t==0)//end
    {
      break;
    }
    text(i, x+i*step+5, outputTop+outputHeight);
  }


  if(p.errorMsg!="")
  {

    fill(255,0,0);
    text(p.errorMsg, ioLeft, inputTop);
    fill(0);

  }
  strokeWeight(1);
}








public void createTabs()
{
  controlP5.tab("Tab1").activateEvent(true);
  controlP5.tab("Tab1").setId(2);
  controlP5.tab("Tab1").setLabel("Tune");

  // in case you want to receive a controlEvent when
  // a  tab is clicked, use activeEvent(true)
  controlP5.tab("Tab2").activateEvent(true);
  controlP5.tab("Tab2").setId(3);
  controlP5.tab("Tab2").setLabel("Config");

  // in case you want to receive a controlEvent when
  // a  tab is clicked, use activeEvent(true)
  controlP5.tab("Tab3").activateEvent(true);
  controlP5.tab("Tab3").setId(4);
  controlP5.tab("Tab3").setLabel("Prefs");
  
   // in case you want to receive a controlEvent when
  // a  tab is clicked, use activeEvent(true)
  controlP5.tab("Tab4").activateEvent(true);
  controlP5.tab("Tab4").setId(5);
  controlP5.tab("Tab4").setLabel("Profile");

  controlP5.tab("default").activateEvent(true);
  // to rename the label of a tab, use setLabe("..."),
  // the name of the tab will remain as given when initialized.
  controlP5.tab("default").setLabel("Run");
  controlP5.tab("default").setId(1);

}

public void populateDashTab()
{

  ConnectButton = controlP5.addButton("Connect",0.0f,commLeft,commTop,60,20);
  DisconnectButton = controlP5.addButton("Disconnect",0.0f,commLeft,commTop,60, 20);
  Connecting = controlP5.addTextlabel("Connecting","Connecting...",commLeft,commTop+3);

  //RadioButtons for available CommPorts
  r1 = controlP5.addRadioButton("radioButton",commLeft,commTop+27);
  r1.setColorForeground(color(120));
  r1.setColorActive(color(255));
  r1.setColorLabel(color(255));
  r1.setItemsPerRow(1);
  r1.setSpacingColumn(75);

  CommPorts = Serial.list();
  for(int i=0;i<CommPorts.length;i++)
  {
    addToRadioButton(r1,CommPorts[i],i); 
  }
  if(CommPorts.length>0) r1.getItem(0).setState(true);
  commH = 27+12*CommPorts.length;
  dashTop = commTop+commH+20;

  DisconnectButton.setVisible(false);
  Connecting.setVisible(false);

  //dasboard

  AMButton = controlP5.addButton("Toggle_AM",0.0f,dashLeft,dashTop,60,20);      //
  AMLabel = controlP5.addTextlabel("AM","Manual",dashLeft+2,dashTop+22);            //
  SPField= controlP5.addTextfield("Setpoint",dashLeft,dashTop+40,60,20);         //   Buttons, Labels, and
  InField = controlP5.addTextfield("Input",dashLeft,dashTop+80,60,20);           //   Text Fields we'll be
  OutField = controlP5.addTextfield("Output",dashLeft,dashTop+120,60,20);         //   using

  AMCurrent = controlP5.addTextlabel("AMCurrent","Manual",dashLeft+70,dashTop+15);   //
  SPLabel=controlP5.addTextlabel("SP","3",dashLeft+70,dashTop+43);                  //
  InLabel=controlP5.addTextlabel("In","1",dashLeft+70,dashTop+83);                  //
  OutLabel=controlP5.addTextlabel("Out","2",dashLeft+70,dashTop+123);                // 
  controlP5.addButton("Send_Dash",0.0f,dashLeft,dashTop+160,160,20);         //
  int dashStatTop = configTop+490;
  for(int i=0;i<6;i++)
 { 
   controlP5.addTextlabel("dashstat"+i, "", configLeft, dashStatTop+12*i+5);
 }
  controlP5.addTextlabel("dashstatus", "Status", configLeft+9, dashStatTop-8);
}

public void populateTuneTab()
{
  //tunings
  PField = controlP5.addTextfield("Kp (Proportional)",tuneLeft,tuneTop,60,20);          //
  IField = controlP5.addTextfield("Ki (Integral)",tuneLeft,tuneTop+40,60,20);          //
  DField = controlP5.addTextfield("Kd (Derivative)",tuneLeft,tuneTop+80,60,20);          //
  DRButton = controlP5.addButton("Toggle_DR",0.0f,tuneLeft,tuneTop+120,60,20);      //
  DRLabel = controlP5.addTextlabel("DR","Direct",tuneLeft+2,tuneTop+144);            //

  PLabel=controlP5.addTextlabel("P","4",tuneLeft+70,tuneTop+3);                    //
  ILabel=controlP5.addTextlabel("I","5",tuneLeft+70,tuneTop+43);                    //
  DLabel=controlP5.addTextlabel("D","6",tuneLeft+70,tuneTop+83);                    //
  DRCurrent = controlP5.addTextlabel("DRCurrent","Direct",tuneLeft+70,tuneTop+123);   //
  controlP5.addButton("Send_Tunings",0.0f,tuneLeft,tuneTop+160,160,20);         //  

  PField.moveTo("Tab1"); 
  IField.moveTo("Tab1"); 
  DField.moveTo("Tab1");
  DRButton.moveTo("Tab1");  
  DRLabel.moveTo("Tab1"); 
  PLabel.moveTo("Tab1");
  ILabel.moveTo("Tab1"); 
  DLabel.moveTo("Tab1"); 
  DRCurrent.moveTo("Tab1");
  controlP5.controller("Send_Tunings").moveTo("Tab1");



  //Autotune
  oSField = controlP5.addTextfield("Output Step",ATLeft,ATTop,60,20);          //
  nField = controlP5.addTextfield("Noise Band",ATLeft,ATTop+40,60,20);          //
  lbField = controlP5.addTextfield("Look Back",ATLeft,ATTop+80,60,20);          //
  ATButton = controlP5.addButton("ATune_CMD",0.0f,ATLeft,ATTop+120,60,20);      //
  ATLabel = controlP5.addTextlabel("ATune","OFF",ATLeft+2,ATTop+142);            //

  oSLabel=controlP5.addTextlabel("oStep","4",ATLeft+70,ATTop+3);                    //
  nLabel=controlP5.addTextlabel("noise","5",ATLeft+70,ATTop+43); 
  lbLabel=controlP5.addTextlabel("lback","5",ATLeft+70,ATTop+83);   //
  ATCurrent = controlP5.addTextlabel("ATuneCurrent","Start",ATLeft+70,ATTop+123);   //
  controlP5.addButton("Send_Auto_Tune",0.0f,ATLeft,ATTop+160,160,20);         //  

  oSField.moveTo("Tab1"); 
  nField.moveTo("Tab1"); 
  lbField.moveTo("Tab1");
  ATButton.moveTo("Tab1");
  ATLabel.moveTo("Tab1");  
  oSLabel.moveTo("Tab1"); 
  nLabel.moveTo("Tab1");
    lbLabel.moveTo("Tab1");
  ATCurrent.moveTo("Tab1"); 
  controlP5.controller("Send_Auto_Tune").moveTo("Tab1"); 
}

public void populateConfigTab()
{
  controlP5.addButton("Reset_Factory_Defaults",0.0f,RsLeft,RsTop,160,20);         //
  controlP5.controller("Reset_Factory_Defaults").moveTo("Tab2");

commconfigLabel1 = controlP5.addTextlabel("spec6","This area will populate when", configLeft,configTop); 
commconfigLabel2 = controlP5.addTextlabel("spec7","connection is established.", configLeft,configTop+15); 
commconfigLabel1.moveTo("Tab2");
commconfigLabel2.moveTo("Tab2");
}

public void populatePrefTab()
{
   //preferences
  for(int i=0;i<prefs.length;i++)
  {
    controlP5.addTextfield(prefs[i],10,30+40*i,60,20);    
    controlP5.controller(prefs[i]).moveTo("Tab3");
  }

  controlP5.addButton("Save_Preferences", 0.0f, 10,30+40*prefs.length,160,20);
  controlP5.controller("Save_Preferences").moveTo("Tab3");

  PopulatePrefVals(); 
}

public void populateProfileTab(){
 LBPref = controlP5.addListBox("Available Profiles",configLeft,configTop+5,160,120);
 controlP5.addTextlabel("spec4","Currently Displaying: ", configLeft+5,configTop+10+15*profs.length);
 ProfButton = controlP5.addButton("Send_Profile",0.0f,configLeft,configTop+25+15*profs.length,160,20);


 int profStatTop = configTop+490;
  ProfCmd = controlP5.addButton("Run_Profile",0.0f,configLeft,profStatTop-40,160,20);
  ProfCmdStop = controlP5.addButton("Stop_Profile",0.0f,configLeft,profStatTop-40,160,20);
  ProfCmdStop.setVisible(false);
 for(int i=0;i<6;i++)
 { 
   controlP5.addTextlabel("profstat"+i,"", configLeft, profStatTop+12*i+5);
   controlP5.controller("profstat"+i).moveTo("Tab4");
 }
 controlP5.addTextlabel("profstatus", "Status", configLeft+9, profStatTop-8);
   controlP5.controller("profstatus").moveTo("Tab4");
 
 for(int i=0;i<profs.length;i++) LBPref.addItem(profs[i].Name, i);
 profSelLabel  = controlP5.addTextlabel("spec5",profs.length==0? "N/A" : profs[0].Name, configLeft+100,configTop+10+15*profs.length); 
 
 LBPref.moveTo("Tab4");
 profSelLabel.moveTo("Tab4");
  ProfButton.moveTo("Tab4");
  ProfCmd.moveTo("Tab4");
  ProfCmdStop.moveTo("Tab4");
 controlP5.controller("spec4").moveTo("Tab4");
}

public void addToRadioButton(RadioButton theRadioButton, String theName, int theValue ) {
  Toggle t = theRadioButton.addItem(theName,theValue);
  t.captionLabel().setColorBackground(color(80));
  t.captionLabel().style().movePadding(2,0,-1,2);
  t.captionLabel().style().moveMargin(-2,0,0,-3);
  t.captionLabel().style().backgroundWidth = 100;
}


  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#F0F0F0", "osPID_FrontEnd" });
  }
}
