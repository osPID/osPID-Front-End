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
int[] InputData = new int[arrayLength];     //we might not need them this big, but
int[] SetpointData = new int[arrayLength];  // this is worst case
int[] OutputData = new int[arrayLength];

int startTime;

float inputTop = 25;
float inputHeight = (windowHeight-70)*2/3;
float outputTop = inputHeight+50;
float outputHeight = (windowHeight-70)*1/3;

float ioLeft = 140, ioWidth = windowWidth-ioLeft-50;
float ioRight = ioLeft+ioWidth;
float pointWidth= (ioWidth)/PApplet.parseFloat(arrayLength-1);

int vertCount = 10;
int nPoints = 0;
float Input, Setpoint, Output;

boolean madeContact =false;

Serial myPort;

ControlP5 controlP5;
controlP5.Button AMButton, DRButton, ATButton, ConnectButton, DisconnectButton;
controlP5.Textlabel AMLabel, AMCurrent, InLabel, 
OutLabel, SPLabel, PLabel, 
ILabel, DLabel,DRLabel, DRCurrent, ATLabel,
oSLabel,nLabel, ATCurrent, Connecting,lbLabel;
RadioButton r1,r2,r3; 
String[] CommPorts;
String[] prefs;
float[] prefVals;
controlP5.Textfield SPField, InField, OutField, 
PField, IField, DField, oSField, nField,T0Field,
R0Field,BetaField,lbField, oSecField;
String pHold="", iHold="", dHold="";
PrintWriter output;
PFont AxisFont, TitleFont; 

int dashTop = 200, dashLeft = 10, dashW=120, dashH=180; 
int tuneTop = 30, tuneLeft = 10, tuneW=120, tuneH=180;
int ATTop = 230, ATLeft = 10, ATW=120, ATH=180;
int RsTop = 330, RsLeft = 10, RsW=120, RsH=30;
int commTop = 30, commLeft = 10, commW=120, commH=180; 
int configTop = 30, configLeft = 10, configW=120, configH=280; 
BufferedReader reader;



public void setup()
{
  size(100, 100);
  frameRate(30);

  //read in preferences
  prefs = new String[] {
    "Form Width", "Form Height", "Input Scale Minimum","Input Scale Maximum","Output Scale Minimum","Output Scale Maximum", "Time Span (Min)"      };   
  prefVals = new float[] {
    windowWidth, windowHeight, InScaleMin, InScaleMax, OutScaleMin, OutScaleMax, windowSpan / 1000 / 60      };
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


  controlP5 = new ControlP5(this);                                  // * Initialize the various

  //initialize UI
  createTabs();
  populateDashTab();
  populateTuneTab();
  populateConfigTab();
  populatePrefTab();
  
  AxisFont = loadFont("axis.vlw");
  TitleFont = loadFont("Titles.vlw");

  //blank out data fields since we're not connected
  Nullify();
  nextRefresh=millis();
  if (outputFileName!="") output = createWriter(outputFileName);


}

public void draw()
{
  background(200);
  drawGraph();
  drawButtonArea();
}


//keeps track of which tab is selected so we know 
//which bounding rectangles to draw
int currentTab=1;
public void controlEvent(ControlEvent theControlEvent) {
  if (theControlEvent.isTab()) { 
    currentTab = theControlEvent.tab().id();
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

  ioLeft = 140;
  ioWidth = windowWidth-ioLeft-50;
  ioRight = ioLeft+ioWidth;

  arrayLength = windowSpan / refreshRate+1;
  InputData = (int[])resizeArray(InputData,arrayLength);
  SetpointData = (int[])resizeArray(SetpointData,arrayLength);
  OutputData = (int[])resizeArray(OutputData,arrayLength);   

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
public void Nullify()
{

  String[] names = {
    "AM", "Setpoint", "Input", "Output", "AMCurrent", "SP", "In", "Out", "Kp (Proportional)",
    "Ki (Integral)","Kd (Derivative)","DR","P","I","D","DRCurrent","Output Step",
    "Noise Band","ATune","oStep","noise","ATuneCurrent",""," ","  ","   ","Look Back","lback"     };
  for(int i=0;i<names.length;i++)controlP5.controller(names[i]).setValueLabel("---");
}

//draws bounding rectangles based on the selected tab
public void drawButtonArea()
{
  stroke(0);
  fill(100);
  rect(0, 0, ioLeft, windowHeight);
  if(currentTab==1)
  {
    rect(commLeft-5, commTop-5, commW+10, commH+10);
    fill(100,220,100);
    rect(dashLeft-5, dashTop-5, dashW+10, dashH+10);

  }
  else if(currentTab==2)
  {
    fill(140);
    rect(tuneLeft-5, tuneTop-5, tuneW+10, tuneH+10);
    fill(120);
    rect(ATLeft-5, ATTop-5, ATW+10, ATH+10);
  }
  else if(currentTab==3)
  {
    fill(140);
    rect(configLeft-5, configTop-5, configW+10, configH+10);

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

public void Disconnect()
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
  
println(o);
  byte identifier = 5;
  myPort.write(identifier);
  myPort.write(a);
  myPort.write(o);
  myPort.write(floatArrayToByteArray(toSend));
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

//take the string the arduino sends us and parse it
public void serialEvent(Serial myPort)
{
  String read = myPort.readStringUntil(10);
  if(outputFileName!="") output.print(str(millis())+ " "+read);
  String[] s = split(read, " ");
  print(read);
  
  if(s.length==20)
  {
   
    Setpoint = PApplet.parseFloat(s[1]);
    Input = PApplet.parseFloat(s[2]);
    Output = PApplet.parseFloat(s[3]);

    SPLabel.setValue(s[1]);           //   where it's needed
    InLabel.setValue(s[2]);           //
    OutLabel.setValue(s[3]);          //
    AMCurrent.setValue(PApplet.parseInt(s[16]) == 1 ? "Automatic" : "Manual");

    PLabel.setValue(s[4]);
    ILabel.setValue(s[5]);
    DLabel.setValue(s[6]);
    DRCurrent.setValue(PApplet.parseInt(s[17]) == 1 ? "Reverse" : "Direct");

    String oS, n;
    oSLabel.setValue(s[7]);
    nLabel.setValue(s[8]);
    lbLabel.setValue(s[9]);
    ATCurrent.setValue(PApplet.parseInt(s[18])==1? "ATune On" : "ATune Off");

    int ack = PApplet.parseInt(trim(s[19]));
  
    if(!madeContact || ack==1)
    {
      SPField.setText(s[1]);    //   the arduino,  take the
      InField.setText(s[2]);    //   current values and put
      OutField.setText(s[3]);
      AMLabel.setValue(PApplet.parseInt(s[16]) == 1 ? "Automatic" : "Manual");   
    }
    if(!madeContact || ack==2)
    {
      PField.setText(s[4]);    //   the arduino,  take the
      IField.setText(s[5]);    //   current values and put
      DField.setText(s[6]);
      DRLabel.setValue(PApplet.parseInt(s[17]) == 1 ? "Reverse" : "Direct");   
    }
    if(!madeContact || ack==3)
    {
      
      oSField.setText(s[7]);
      nField.setText(s[8]);
      lbField.setValue(s[9]);    
      ATLabel.setValue(PApplet.parseInt(s[18])==1? "ON" : "OFF");
    }
    if(!madeContact || ack==5)
    {
      int v = PApplet.parseInt(s[14]); 
      if(v==0) r2.getItem(0).setState(true);
      else if(v==1) r2.getItem(1).setState(true);
      else r2.getItem(2).setState(true);
      
      v = PApplet.parseInt(s[15]);
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


  // add the latest data to the data Arrays.  the values need
  // to be massaged to get them to graph correctly.  they 
  // need to be scaled to fit where they're going, and 
  // because 0, 0 is the top left, we need to flip the values.
  // this is easier than having the user stand on their head
  // to read the graph.
  if(millis() > nextRefresh && madeContact)
  {
    nextRefresh += refreshRate;

    for(int i=nPoints-1;i>0;i--)
    {
      InputData[i]=InputData[i-1];
      SetpointData[i]=SetpointData[i-1];
      OutputData[i]=OutputData[i-1];
    }
    if (nPoints < arrayLength) nPoints++;

    InputData[0] = PApplet.parseInt(inputHeight)-PApplet.parseInt(inputHeight*(Input-InScaleMin)/(InScaleMax-InScaleMin));
    SetpointData[0] =PApplet.parseInt( inputHeight)-PApplet.parseInt(inputHeight*(Setpoint-InScaleMin)/(InScaleMax-InScaleMin));
    OutputData[0] = PApplet.parseInt(outputHeight)-PApplet.parseInt(outputHeight*(Output-OutScaleMin)/(OutScaleMax-OutScaleMin));
  }
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
    int Y1 = InputData[i];
    int Y2 = InputData[i+1];

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
    Y1 = SetpointData[i];
    Y2 = SetpointData[i+1];

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
    Y1 = OutputData[i];
    Y2 = OutputData[i+1];

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
  controlP5.addButton("Send_Dash",0.0f,dashLeft,dashTop+160,120,20);         //
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
  controlP5.addButton("Send_Tunings",0.0f,tuneLeft,tuneTop+160,120,20);         //  

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
  controlP5.addButton("Send_Auto_Tune",0.0f,ATLeft,ATTop+160,120,20);         //  

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
  controlP5.addButton("Reset_Factory_Defaults",0.0f,RsLeft,RsTop,120,20);         //
  controlP5.controller("Reset_Factory_Defaults").moveTo("Tab2");

  //configuration
  controlP5.addTextlabel("spec0","Specify which input to use: ", configLeft,configTop);                  

  r2 = controlP5.addRadioButton("radioButton2",configLeft,configTop+22);
  r2.setColorForeground(color(120));
  r2.setColorActive(color(255));
  r2.setColorLabel(color(255));
  r2.setItemsPerRow(1);
  r2.setSpacingColumn(75);

  addToRadioButton(r2,"Thermocouple",1);
  addToRadioButton(r2,"Thermistor (10K)",2);
  addToRadioButton(r2,"Thermistor (100K)",3);
  r2.getItem(0).setState(true);

  controlP5.addTextlabel("spec1","Thermistor Coefficients: ", configLeft,configTop+70);
  controlP5.addTextlabel("T","T    =", configLeft+5,configTop+90);
  controlP5.addTextlabel("00","0", configLeft+10,configTop+95);
  controlP5.addTextlabel("R","R    =", configLeft+5,configTop+115);
  controlP5.addTextlabel("01","0", configLeft+12,configTop+120);
  controlP5.addTextlabel("Beta","Beta =", configLeft+5,configTop+140);
  T0Field= controlP5.addTextfield("",configLeft+45,configTop+84,60,20);         //   Buttons, Labels, and
  R0Field = controlP5.addTextfield(" ",configLeft+45,configTop+109,60,20);           //   Text Fields we'll be
  BetaField = controlP5.addTextfield("  ",configLeft+45,configTop+136,60,20);         //   using

  controlP5.addTextlabel("spec3","Specify which output to use: ", configLeft,configTop+166);                  

  r3 = controlP5.addRadioButton("radioButton3",configLeft,configTop+188);
  r3.setColorForeground(color(120));
  r3.setColorActive(color(255));
  r3.setColorLabel(color(255));
  r3.setItemsPerRow(1);
  r3.setSpacingColumn(75);

  addToRadioButton(r3,"Onboard Relay",1);
  addToRadioButton(r3,"Digital Output",2);
  r3.getItem(0).setState(true);

  controlP5.addTextlabel("spec2","Relay Output Window: ", configLeft,configTop+222);  
  controlP5.addTextlabel("sec","Seconds = ", configLeft+5,configTop+242);  
  oSecField = controlP5.addTextfield("   ",configLeft+55,configTop+236,50,20);
  controlP5.addButton("Send_Configuration",0.0f,configLeft,configTop+260,120,20);         //  

  String[] names = {
    "spec0","spec1","spec2","spec3","sec", "T","   ", "00", "R", "01", "Beta", "Send_Configuration"      };
  for(int i=0;i<names.length;i++)controlP5.controller(names[i]).moveTo("Tab2");
  r2.moveTo("Tab2"); 
  r3.moveTo("Tab2"); 
  T0Field.moveTo("Tab2"); 
  R0Field.moveTo("Tab2"); 
  BetaField.moveTo("Tab2");  

}

public void populatePrefTab()
{
   //preferences
  for(int i=0;i<prefs.length;i++)
  {
    controlP5.addTextfield(prefs[i],10,30+40*i,60,20);    
    controlP5.controller(prefs[i]).moveTo("Tab3");
  }

  controlP5.addButton("Save_Preferences", 0.0f, 10,30+40*prefs.length,120,20);
  controlP5.controller("Save_Preferences").moveTo("Tab3");

  PopulatePrefVals(); 
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
