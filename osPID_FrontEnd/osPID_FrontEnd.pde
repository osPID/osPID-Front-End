import java.nio.ByteBuffer;
import processing.serial.*;
import controlP5.*;
import java.io.*;

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
float pointWidth= (ioWidth)/float(arrayLength-1);

int vertCount = 10;
int nPoints = 0;
float Input, Setpoint, Output;

boolean madeContact =false;

Serial myPort;

ControlP5 controlP5;
controlP5.Button AMButton, DRButton, ATButton, ConnectButton, DisconnectButton, ProfButton, ProfCmd, ProfCmdStop;
controlP5.Textlabel AMLabel, AMCurrent, InLabel, 
  OutLabel, SPLabel, PLabel, 
  ILabel, DLabel, DRLabel, DRCurrent, ATLabel, 
  oSLabel, nLabel, ATCurrent, Connecting, lbLabel, 
  profSelLabel, commconfigLabel1, commconfigLabel2;
controlP5.Tab runTab;
RadioButton r2, r3;
DropdownList r1;
ListBox LBPref;
String[] CommPorts;
String[] prefs;
float[] prefVals;
controlP5.Textfield SPField, InField, OutField, 
  PField, IField, DField, oSField, nField, T0Field, 
  R0Field, BetaField, lbField, oSecField;
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
boolean settingup = true;

void setup()
{
  size(100, 100);
  surface.setResizable(true);
  frameRate(30);

  // Read in preferences
  prefs = new String[] {
    "Form Width", "Form Height", "Input Scale Minimum", "Input Scale Maximum", "Output Scale Minimum", "Output Scale Maximum", "Time Span (Min)"        };   
  prefVals = new float[] {
    windowWidth, windowHeight, InScaleMin, InScaleMax, OutScaleMin, OutScaleMax, windowSpan / 1000 / 60        };
  try
  {
    reader = createReader("prefs.txt");
    if (reader!=null)
    {
      for (int i=0; i<prefVals.length; i++)
      {

        prefVals[i] = float(reader.readLine());
        println(prefVals[i]);
      }
    }
  }
  catch(FileNotFoundException  ex) {
  }
  catch(IOException ex) {
  }

  PrefsToVals(); //read pref array into global variables

  ReadProfiles(sketchPath("") + File.separator + "profiles");

  controlP5 = new ControlP5(this);                                  // * Initialize the various

  // Initialize UI
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

  settingup=false;
}

void draw()
{
  if (InputCreateReq!="" && InputCard!= InputCreateReq)
  {
    CreateUI(InputCreateReq, "Tab2", configTop);
    InputCreateReq="";
  }
  if (OutputCreateReq!="" && OutputCard!= OutputCreateReq)
  {
    CreateUI(OutputCreateReq, "Tab2", configTop+configH+15);
    OutputCreateReq="";
  }

  ProfileRunTime();

  background(200);

  strokeWeight(1);

  drawButtonArea();
  AdvanceData();
  if (currentTab==5 && curProf>-1)DrawProfile(profs[curProf], ioLeft+4, inputTop, ioWidth-1, inputHeight);
  else drawGraph();
}


// Keeps track of which tab is selected so we know which bounding rectangles to draw
int currentTab=1;
void controlEvent(ControlEvent theControlEvent) {
  if (theControlEvent.isTab()) { 
    currentTab = theControlEvent.getTab().getId();
  } else if (theControlEvent.getName()=="Available Profiles")
  {// a list item was clicked
    curProf=(int)theControlEvent.getValue();
    profSelLabel.setValue(profs[curProf].Name);
  }
}

// Puts preference array into the correct fields
void PopulatePrefVals()
{
  for (int i=0; i<prefs.length; i++)
  {
    println(prefVals[i]+"");
    ((Textfield)controlP5.getController(prefs[i])).setText(prefVals[i]+"");
  }
}

// Translates the preferebce array in the corresponding local variables
// and makes any required UI changes
void PrefsToVals()
{
  windowWidth = int(prefVals[0]);
  windowHeight = int(prefVals[1]);
  InScaleMin = prefVals[2];
  InScaleMax = prefVals[3];
  OutScaleMin = prefVals[4];
  OutScaleMax = prefVals[5];    
  windowSpan = int(prefVals[6] * 1000 * 60);


  inputTop = 25;
  inputHeight = (windowHeight-70)*2/3;
  outputTop = inputHeight+50;
  outputHeight = (windowHeight-70)*1/3;

  println(inputTop);
  println(inputHeight);
  println(outputTop);
  println(outputHeight);

  ioWidth = windowWidth-ioLeft-50;
  ioRight = ioLeft+ioWidth;

  arrayLength = windowSpan / refreshRate+1;
  InputData = (float[])resizeArray(InputData, arrayLength);
  SetpointData = (float[])resizeArray(SetpointData, arrayLength);
  OutputData = (float[])resizeArray(OutputData, arrayLength);   

  pointWidth= (ioWidth)/float(arrayLength-1);
  resizer(windowWidth, windowHeight);
}

private static Object resizeArray (Object oldArray, int newSize) {
  int oldSize = java.lang.reflect.Array.getLength(oldArray);
  Class elementType = oldArray.getClass().getComponentType();
  Object newArray = java.lang.reflect.Array.newInstance(
    elementType, newSize);
  int preserveLength = Math.min(oldSize, newSize);
  if (preserveLength > 0)
    System.arraycopy (oldArray, 0, newArray, 0, preserveLength);
  return newArray;
}

// Resizes the form
void resizer(int w, int h)
{
  surface.setSize(w, h); 
}

void Save_Preferences()
{
  if (settingup)return;
  for (int i=0; i<prefs.length; i++)
  {
    try
    {
      prefVals[i] = float(controlP5.getController(prefs[i]).getValueLabel().getText());
    }
    catch(Exception ex) {
    }
  }
  PrefsToVals();
  PopulatePrefVals(); //in case there was an error we want to put the good values back in

  PrintWriter output;
  try
  {
    output = createWriter("prefs.txt");
    for (int i=0; i<prefVals.length; i++) output.println(prefVals[i]);
    output.flush();
    output.close();
  }
  catch(Exception ex) {
  }
}

// Puts a "---" into all live fields when we're not connected
boolean dashNull=false, tuneNull=false;
void Nullify()
{

  String[] names = {
    "AM", "Setpoint", "Input", "Output", "AMCurrent", "SP", "In", "Out", "Kp (Proportional)", 
    "Ki (Integral)", "Kd (Derivative)", "DR", "P", "I", "D", "DRCurrent", 
    "Noise Band", "ATune", "oStep", "noise", "ATuneCurrent", "Look Back", "lback"  }; //,"  "," ","","Output Step","   ",
  for (int i=0; i<names.length; i++)controlP5.getController(names[i]).setValueLabel("---");
  dashNull=true;
  tuneNull=true;
}

// Draws bounding rectangles based on the selected tab
void drawButtonArea()
{

  stroke(0);
  fill(100);
  rect(0, 0, ioLeft, windowHeight);
  if (currentTab==1) //dash
  {
    rect(commLeft-5, commTop-5, commW+10, commH+10);
    fill(100, 220, 100);
    rect(dashLeft-5, dashTop-5, dashW+10, dashH+10);

    fill(140);
    rect(configLeft-5, configTop+485, configW+10, 82);
    rect(configLeft+5, configTop+479, 35, 12);
  } else if (currentTab==2) //tune
  {
    fill(140);
    rect(tuneLeft-5, tuneTop-5, tuneW+10, tuneH+10);
    fill(120);
    rect(ATLeft-5, ATTop-5, ATW+10, ATH+10);
  } else if (currentTab==3) //config
  {
    fill(140);
    if (madeContact)
    {
      rect(configLeft-5, configTop-5, configW+10, configH+10);
      rect(configLeft-5, configTop+configH+10, configW+10, configH+10);
    } else rect(configLeft-5, configTop-5, configW+10, 2*configH+20);
  } else if (currentTab==5) //profile
  {
    fill(140);
    rect(configLeft-5, configTop+485, configW+10, 82);
    rect(configLeft+5, configTop+479, 35, 12);
  }
}

void Toggle_AM() {
  if (AMButton==null)return;
  if (AMLabel.getValueLabel().getText()=="Manual") 
  {
    AMLabel.setValue("Automatic");
  } else
  {
    AMLabel.setValue("Manual");
  }
}

void Toggle_DR() {
  if (DRButton==null)return;
  if (DRLabel.getValueLabel().getText()=="Direct") 
  {
    DRLabel.setValue("Reverse");
  } else
  {
    DRLabel.setValue("Direct");
  }
}

void ATune_CMD() {
  if (ATButton==null)return;
  if (ATLabel.getValueLabel().getText()=="OFF") 
  {
    ATLabel.setValue("ON");
  } else
  {
    ATLabel.setValue("OFF");
  }
}
